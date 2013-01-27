package com.otfe.caravans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.otfe.caravans.crypto.CryptoUtility;
import com.otfe.caravans.database.FileLog;
import com.otfe.caravans.database.FileLoggerDataSource;

public class FolderObserverService extends Service{
	private static final String TAG = "FolderObserver S";
    
    private List<FolderObserver> folder_observers;
    private final IBinder mBinder = new LocalBinder();
    
    @Override
	public int onStartCommand(Intent intent,int flags, int startId){
    	super.onStartCommand(intent, flags,startId);
    	Log.d(TAG, "Starting FOS");
    	Bundle extras = intent.getExtras();
    	folder_observers = new ArrayList<FolderObserver>();
    	if (extras!=null)
    		addTarget(extras);
    	setupNotification();
    	return Service.START_REDELIVER_INTENT;
    }
    
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG,"Destroying fos");
		cancelNotification();
	}
	
	@Override 
	public boolean onUnbind(Intent intent){
		super.onUnbind(intent);
		Log.d(TAG, "Unbinded");
		return false;
	}
	
	public void stopAll(){
		Log.d(TAG,"Stopping all");
		for (FolderObserver fo: folder_observers)
			fo.stopWatching();
		stopSelf();
	}
	
	public void stopObserving(int id){
		Log.d(TAG, "Stopping id: "+id);
		for (FolderObserver fo: folder_observers){
			if (fo._id == id){
				fo.stopWatching();
				folder_observers.remove(fo);
			}
		}
		if (folder_observers.size()==0)
			stopSelf();
	}
	
	/*
	 * unavailable yet, must have passwords saved in dbase or somewhere
	 * for this to be available
	public void startAll(){
		FolderLoggerDataSource flds = new FolderLoggerDataSource(getApplicationContext());
		for (FolderLog fl : flds.getAllFolderLogs()){
			Bundle extras = new Bundle();
			//extras.putString(Constants.KEY_PASSWORD, fl);
			extras.putString(Constants.KEY_TARG_FILE, fl.getPath());
			extras.putString(Constants.KEY_ALGORITHM, fl.getAlgorithm());
			extras.putInt(Constants.KEY_ROW_ID, (int)fl.getId());
			addTarget(extras);
		}
	}*/
	
	public boolean isObserved(int id){
		for (FolderObserver fo: folder_observers)
			if (fo.getId() == id)
				return true;
		return false;
	}
	
	public void listObservers(){
		Log.d(TAG, "Observers running: "+folder_observers.size());
		for (FolderObserver fo: folder_observers)
			Log.d(TAG,"***\nObserver id: "+fo.getId()+"\nPath: "+fo.getTarget()+"\n***");
	}
	
	/* add new FolderObserver for a specific target directory */
	public boolean addTarget(Bundle extras){
		boolean v = verifyExtras(extras);
		Log.d(TAG,"Adding new target: "+v);
        if (!v) return false;
        
        int _id = extras.getInt(Constants.KEY_ROW_ID,-1);
        String password = extras.getString(Constants.KEY_PASSWORD);
        String algorithm = extras.getString(Constants.KEY_ALGORITHM);
        String path = extras.getString(Constants.KEY_TARG_FILE);
        
        FolderObserver observer = new FolderObserver(_id, path, password, algorithm);
        observer.startWatching();
        Log.d(TAG,"adding observer to list of observers, null?: "+(observer==null));
        folder_observers.add(observer);
        return true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Binded to FOS");
		return mBinder;
	}
	
	public class LocalBinder extends Binder{
		FolderObserverService getService(){
            return FolderObserverService.this;
        }
    }
	
	private class FolderObserver extends FileObserver {
	    private static final String TAG = "FileObserver";
	    private int _id;
	    private String target;
	    private String password;
	    private String algorithm;
	    
	    private Notification noti;
	    private FileLoggerDataSource fld;
	    
	    public FolderObserver(int id, String path, String password,
	            String algorithm) {
	        super(path, Constants.OBSERVER_EVENTS);
	        FolderObserver.this._id = id;
	        FolderObserver.this.target = path;
	        FolderObserver.this.password = password;
	        FolderObserver.this.algorithm = algorithm;
	        FolderObserver.this.fld = new FileLoggerDataSource(getApplicationContext(),
	        		new File(path).getName());
	        
	        Log.d(TAG,"New FileObserver");
	    }
	    
	    public int getId(){
	    	return FolderObserver.this._id;
	    }
	    
	    public String getTarget(){
	    	return FolderObserver.this.target;
	    }
	    
	    @Override
	    public void startWatching(){
	        super.startWatching();
	        Log.d(TAG,"Observing "+target);
	        FolderObserver.this.fld.open();
	    }
	    
	    @Override
	    public void stopWatching(){
	        super.stopWatching();
	        FolderObserver.this.fld.close();
	    }
	    
	    @Override
	    public void onEvent(int event, String path) {
	        if (path==null)return;
	        File f_observerd = new File(FolderObserver.this.target,path);
	        
	        //we do not care about directories :))
	        if (f_observerd.isDirectory()) return;
	        
	        switch(event){
	            case FileObserver.CREATE:    //a new file or subdirectory was created under the monitored directory
	            case FileObserver.MOVED_TO: //a file or subdirectory was moved to the monitored directory
	                Log.d(TAG,"New File detected: "+f_observerd.getPath());
	                
	                Log.d(TAG,"fld null: "+(fld == null));
	                FileLog fileLog = FolderObserver.this.fld.getFileLog(CryptoUtility.getRawFileName(path));
	                Log.d(TAG,"fileLog null: "+(fileLog==null));
	                
	                if (fileLog == null){
	                    /* log the new file to database */
	                	FolderObserver.this.fld.createFileLog(f_observerd);
	                    Log.d(TAG,f_observerd.getPath() +" is a new file, will encrypt it");
	                    //Start a new EncryptorIntentService to encrypt the file
	                    
	                    Intent intent = new Intent(FolderObserverService.this, 
	                        EncryptionIntentService.class);
	                    intent.putExtra(Constants.KEY_PASSWORD, FolderObserver.this.password);
	                    intent.putExtra(Constants.KEY_ALGORITHM, FolderObserver.this.algorithm);
	                    intent.putExtra(Constants.KEY_TARG_FILE, f_observerd.getPath());
	                    intent.putExtra(Constants.EXTRA_SERVICE_TASK, Constants.TASK_ENCRYPT_SINGLE);
	                    startService(intent);
	                }
	                break;
	                
	            /* the monitored file or directory was moved; monitoring continues 
	             * update SQL database of new location
	             * */
	            case FileObserver.MOVE_SELF:
	                Log.d(TAG,f_observerd.getPath() + " was moved...("+path+")");
	                break;
	            /* the target folder was deleted, update SQL database */
	            case FileObserver.DELETE_SELF:
	                Log.d(TAG, f_observerd.getPath() + " was deleted, will update database");
	                break;
	            
	            case FileObserver.DELETE:
	                Log.d(TAG, f_observerd.getPath() + " was deleted, will update database");
	                break;
	            case FileObserver.MOVED_FROM:
	                Log.d(TAG, f_observerd.getPath() + " was moved from target folder, will update database");
	                break;
	        }
	    }
	}

	/* NOTIFICATION FUNCTIONS */
    private void setupNotification(){
    	NotificationManager notiMan = 
    			(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        CharSequence title = "Encryption Service";
        CharSequence text = "Encryption Service is running...";
        int icon = R.drawable.lock;
        long when = System.currentTimeMillis();
        
        Intent noti_intent = new Intent(this, ViewFoldersActivity.class);
        PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, 0);
        
        Notification noti = new Notification(icon, text, when);
        noti.flags = Notification.FLAG_ONGOING_EVENT;
        noti.setLatestEventInfo(this, title, text, content_intent);
        notiMan.notify(Constants.NOTIFICATION_ID, noti);        
    }
    
    private void cancelNotification(){
    	((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
    		.cancel(Constants.NOTIFICATION_ID);      
    }
    
    /* OTHER METHODS */
    private static boolean verifyExtras(Bundle extras){
        if (extras == null) return false;
        int id = extras.getInt(Constants.KEY_ROW_ID,-1);        
        String password = extras.getString(Constants.KEY_PASSWORD);
        String algorithm = extras.getString(Constants.KEY_ALGORITHM);
        File target = new File(extras.getString(Constants.KEY_TARG_FILE));
        if (id==-1 || password.equals("") || password == null ||
                !Constants.ALGORITHMS.contains(algorithm) ||
                !target.isDirectory())
            return false;
        return true;
    }
    
    public static void startObserving(Context context, Bundle extras){
    	boolean v = verifyExtras(extras);
    	if (!v) return;
    	Intent intent = new Intent(context, FolderObserverService.class);
    	intent.putExtras(extras);
    	context.startService(intent);
    }
}

package com.otfe.caravans;

import java.io.File;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;

import com.otfe.caravans.database.FileLog;
import com.otfe.caravans.database.FileLoggerDataSource;

public class FolderObserverIntentService extends IntentService{
	private static final String TAG = "FolderObserver IS";
    private FileLoggerDataSource fld;
    private int _id;
    private String target;
    private String password;
    private String algorithm;
    private FolderObserver folder_observer;
    private NotificationManager notiMan;
    private boolean keepAwake = true;
    
	public FolderObserverIntentService(){
        super(FolderObserverIntentService.class.getName());
    }
	
	private class FolderObserver extends FileObserver {
		private static final String TAG = "FileObserver";
		
		private Notification noti;
	    public String absolutePath;
	    private FileLog fileLog;
	    
	    public FolderObserver(String path) {
	    	super(path, Constants.OBSERVER_EVENTS);
	        absolutePath = path;
	        Log.d(TAG,"New FileObserver");
	    }
	    
	    public void setNotification(Notification noti){
	    	this.noti = noti;
	    }
	    
	    @Override
	    public void startWatching(){
	    	super.startWatching();
	    	setupNotification();
	    	fld.open();
	    }
	    
	    @Override
	    public void stopWatching(){
	    	super.stopWatching();
	    	cancelNotification();
	    	fld.close();
	    }
	    
		@Override
		public void onEvent(int event, String path) {
			if (path==null)return;
			File f_observerd = new File(absolutePath,path);
			
			//we do not care about directories :))
			if (f_observerd.isDirectory()) return;
			
			switch(event){
				case FileObserver.CREATE:	 //a new file or subdirectory was created under the monitored directory
				case FileObserver.MOVED_TO: //a file or subdirectory was moved to the monitored directory
					Log.d(TAG,"New File detected: "+f_observerd.getPath());
					
					Log.d(TAG,"fld null: "+(fld == null));
					fileLog = fld.getFileLog(path);
					Log.d(TAG,"fileLog null"+(fileLog==null));
					
					if (fileLog == null){
		                /* log the new file to database */
		                fld.createFileLog(f_observerd);
		                Log.d(TAG,f_observerd.getPath() +" is a new file, will encrypt it");
						//Start a new EncryptorIntentService to encrypt the file
						
						Intent intent = new Intent(FolderObserverIntentService.this, 
								EncryptionIntentService.class);
		                intent.putExtra(Constants.KEY_PASSWORD, password);
		                intent.putExtra(Constants.KEY_ALGORITHM, algorithm);
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
	
    @Override
    protected void onHandleIntent(Intent intent) {
        int operation = intent.getIntExtra(Constants.EXTRA_SERVICE_TASK, -1);
        Log.d(TAG,"onHandleIntent");
        switch(operation){
            case Constants.TASK_FOLDER_OBSERVE:
            	Log.d(TAG,"Starting to observe..");
                loadExtras(intent.getExtras());
                fld = new FileLoggerDataSource(getApplicationContext(), new File(target).getName());
                //initialize observer
                this.folder_observer = new FolderObserver(this.target);
                this.folder_observer.startWatching();
                setupNotification();
                stayAwake();
                break;
        }
        Log.d(TAG,"OnHandleIntent finished");
    }

    private void loadExtras(Bundle extras){
        this.password = extras.getString(Constants.KEY_PASSWORD);
        this.algorithm = extras.getString(Constants.KEY_ALGORITHM);
        this.target = extras.getString(Constants.KEY_TARG_FILE);
        this._id = extras.getInt(Constants.KEY_ROW_ID,-1);
    }
	
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
    
    public static boolean startObserving(Context context, Bundle extras){
    	boolean v = verifyExtras(extras);
    	Log.d(TAG,"verification: "+v);
    	if (!v) return false;
        Intent intent = new Intent(context, FolderObserverIntentService.class);
        intent.putExtra(Constants.EXTRA_SERVICE_TASK, Constants.TASK_FOLDER_LISTEN);
        intent.putExtras(extras);
        Log.d(TAG,"Starting service...");
        context.startService(intent);
        return true;
    }
    
    public static void stopListening(Context context){
        
    }
    
    private void setupNotification(){
    	notiMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	CharSequence title = "Encryption Service";
		CharSequence text = "Encryption Service is running...";
		int icon = R.drawable.lock;
		long when = System.currentTimeMillis();
		
		Intent noti_intent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, 0);
		
		Notification noti = new Notification(icon, text, when);
		noti.flags = Notification.FLAG_ONGOING_EVENT;
		noti.setLatestEventInfo(this, title, text, content_intent);
		notiMan.notify(Constants.NOTIFICATION_ID, noti);		
    }
    
    private void cancelNotification(){
    	notiMan.cancel(Constants.NOTIFICATION_ID);   	
    }
    
    private void stayAwake(){
    	while(keepAwake){
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.d(TAG,"THREAD INTERRUPTED");
			}
    	}
    }
}
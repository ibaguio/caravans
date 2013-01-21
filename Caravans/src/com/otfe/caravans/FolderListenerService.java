package com.otfe.caravans;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.otfe.caravans.crypto.Utility;
import com.otfe.caravans.database.FileLog;
import com.otfe.caravans.database.FileLoggerDataSource;

public class FolderListenerService extends Service{
	public static final String PASSWORD = "PASS";
	public static final String TARGET = "TARG";
	public static final String ALGORITHM = "ALGO";

	private File target;
	private FileLoggerDataSource fld2;
	private String password;
	private String algorithm;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private String TAG = "Folder LDS";
	
	private NotificationManager mNotificationManager;
	private int NOTIFICATION_ID = 0;
	
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	/* loops every few seconds to check if there is a new file in the folder*/
	  	  	while(true){
	  	  		long endTime = System.currentTimeMillis() + 2000;
	  	  		synchronized (this) {
	  	  			try {
	  	  				//wait for some time
	  	  				wait(endTime - System.currentTimeMillis());
		                if (checkChanges()){
		                	Log.d("Daemon","modification detected");
		                }
	  	  			} catch (Exception e) {
	  	  				Toast.makeText(FolderListenerService.this, "error in waiting", Toast.LENGTH_SHORT).show();
	  	  			}
	  	  		}
	  	  }
	      //stopSelf(msg.arg1);
	    }
	    
	    /* check if a new file is in the folder */
	    private boolean checkChanges(){
	    	File files[] = target.listFiles();
	    	FileLog fileLog;
	    	for (File f: files){
	    		fileLog = fld2.getFileLog( Utility.getRawFileName(f));
	    		if (fileLog == null){
	    			/* log the new file to database */
	    			fld2.createFileLog(f);
	    			
	    			/*Log.d(TAG,"Starting EncryptionService");
	    			Intent intent = new Intent(FolderListenerDaemon.this, EncryptionService.class);
	    			intent.putExtra(EncryptionService.PASSWORD, password);
	    			intent.putExtra(EncryptionService.ALGORITHM, algorithm);
	    			intent.putExtra(EncryptionService.FILEPATH, f.getAbsolutePath());
	    			startService(intent); */
	    			
	    			/* create new encryptor thread and encrypt file */
	    			EncryptorDaemonThread edt = new EncryptorDaemonThread(f,password,algorithm);
	    			new Thread(edt).run();
	    			Log.d(TAG,"encryptor service started");
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		fld2.close();
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
	@Override
	public void onCreate() {		
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand");
		/* get the details of the target folder and encryption */
		password = intent.getStringExtra(PASSWORD);
		algorithm = intent.getStringExtra(ALGORITHM);
		String path = intent.getStringExtra(TARGET);
		target = new File(path);
		
		if (!target.isDirectory()){
			Log.d(TAG,path+" is not a valid directory. will halt service");
			stopSelf();
		}
		String foldr = getFolder(path);
		fld2 = new FileLoggerDataSource(this,foldr);
		fld2.open();
		
		Toast.makeText(getApplicationContext(),"Encrypting files on "+foldr , Toast.LENGTH_SHORT).show();
		
		/* start service proper*/
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		notifyStartService(Utility.pathRemoveMNT(path));
		return START_STICKY;
	}
	/**
	 * returns deepest folder in the path, no error checking
	 * @param path
	 * @return
	 */
	private String getFolder(String path){
		return path.substring(path.lastIndexOf("/")+1);
	}
	
	private void notifyStartService(String folder){
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//mNotificationManager.notify(0, mBuilder.build());
		
		CharSequence title = "Encryption Service";
		CharSequence text = "Encryption service on "+folder;
		int icon = R.drawable.lock;
		long when = System.currentTimeMillis();
		
		Intent noti_intent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, 0);
		Notification noti = new Notification(icon, text, when);
		noti.flags = Notification.FLAG_ONGOING_EVENT;
		noti.setLatestEventInfo(this, title, text, content_intent);
		mNotificationManager.notify(NOTIFICATION_ID, noti);
	}
}
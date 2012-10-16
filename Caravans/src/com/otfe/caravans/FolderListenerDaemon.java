package com.otfe.caravans;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class FolderListenerDaemon extends Service{
	public static final String PASSWORD = "PASS";
	public static final String TARGET = "TARG";
	public static final String ALGORITHM = "ALGO";
	
	private File target;
	private FileLoggerDataSource fld2;
	private String password;
	private String algorithm;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	  	  	for (int i=0;i<50;i++){
	  	  		long endTime = System.currentTimeMillis() + 2000;//wait for some second
		        //while (System.currentTimeMillis() < endTime) {
	  	  		synchronized (this) {
	  	  			try {
	  	  				//Log.d("wait","["+i+"]waiting for 3 seconds");
	  	  				wait(endTime - System.currentTimeMillis());
		                if (checkChanges()){
		                	Log.d("Daemon","modification detected");
		                }
	  	  			} catch (Exception e) {
	  	  				Toast.makeText(FolderListenerDaemon.this, "error in waiting", Toast.LENGTH_SHORT).show();
	  	  			}
	  	  		}
	  	  		//}
	  	  }
	        // Stop the service using the startId, so that we don't stop
	        // the service in the middle of handling another job
	        stopSelf(msg.arg1);
	    }
	    private boolean checkChanges(){
	    	File files[] = target.listFiles();
	    	FileLog fileLog;
	    	for (File f: files){
	    		if (f.isHidden()){
	    			f.delete();
	    			continue;
	    		}
	    		fileLog = fld2.getFileLog( OnTheFlyUtils.getRawFileName(f));
	    		if (fileLog == null){
	    			//Log.d("Folder LDS","New File detected, "+OnTheFlyUtils.getRawFileName(f));
	    			fld2.createFileLog(f);

	    			Log.d("Folder LDS","Starting EDT");
	    			EncryptorDaemonThread edt = new EncryptorDaemonThread(f,password,algorithm);
	    			new Thread(edt).run();
	    			Log.d("Folder LDS","encryptor service started");
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
		Toast.makeText(getApplicationContext(), "Service Done", Toast.LENGTH_SHORT).show(); 
	}
	@Override
	public void onCreate() {		
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		Log.d("Folder LDS","onCreate Finished");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Folder LDS","onStartCommand");
		password = intent.getStringExtra(PASSWORD);
		algorithm = intent.getStringExtra(ALGORITHM);
		String path = intent.getStringExtra(TARGET);
		target = new File(path);
		if (!target.isDirectory()){
			Log.d("Folder LD",path+" is not a valid directory. will halt service");
			stopSelf();
		}
		String foldr = getFolder(path);
		fld2 = new FileLoggerDataSource(this,foldr);
		fld2.open();
		
		Toast.makeText(getApplicationContext(),"Encrypting files on "+foldr , Toast.LENGTH_SHORT).show();
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
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
}
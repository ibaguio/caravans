package com.otfe.caravans;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class FolderListenerDaemon extends Service{
	private File target;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	long fSize =0;
	    	long mod=-1;
	  	  	try{
	  	  		target = new File( Environment.getExternalStorageDirectory().getPath() + "/Download");
	  	  		fSize = target.length();
	  	  		mod = target.lastModified();
	  	  	}catch(Exception er){
	  	  		Toast.makeText(FolderListenerDaemon.this, "error in handling message", Toast.LENGTH_SHORT).show();
	  	  		stopSelf(msg.arg1);
	  	  	}
	  	  	for (int i=0;i<30;i++){
	  	  		long endTime = System.currentTimeMillis() + 3000;//wait for 1 second
		        //while (System.currentTimeMillis() < endTime) {
	  	  		synchronized (this) {
	  	  			try {
	  	  				wait(endTime - System.currentTimeMillis());
		                Log.d("wait","waiting for 3 seconds");
		                if (mod < target.lastModified()){
		                	Log.d("mod","modified");
		                    mod = target.lastModified();
		                    //getChangedFile();
		                }
	  	  			} catch (Exception e) {
	  	  				Toast.makeText(FolderListenerDaemon.this, "error in waiting", Toast.LENGTH_SHORT).show();
	  	  			}
	  	  		}
	  	  		//}
	  	  }
	        // Stop the service using the startId, so that we don't stop
	        // the service in the middle of handling another job
	        //stopSelf(msg.arg1);
	    }
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show(); 
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
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		return START_STICKY;
	}
}
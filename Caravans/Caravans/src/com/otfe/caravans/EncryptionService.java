package com.otfe.caravans;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.otfe.crypto.Encryptor;
import com.otfe.performance_test.PerformanceTester;

public class EncryptionService extends Service{
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	public static final String PASSWORD = "PASS";
	public static final String FILEPATH = "FNAME";
	public static final String ALGORITHM= "ALGO";
		
	private String toEncrypt;
	private String passw;
	private String algo;
	
	private final String TAG = "EncryptionService";
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	Log.d(TAG,"Encrypting "+toEncrypt);
	    	Encryptor otfe = new Encryptor(passw,toEncrypt,algo);
	    	PerformanceTester pt = new PerformanceTester(toEncrypt);
	  	  	boolean ok = otfe.encrypt();
	  	  	pt.endTest();
	  	  	//pt.printResult();
	  	  	Log.d(TAG,"Encrypted: "+ok);
	        stopSelf(msg.arg1);
	    }
	}
	
	@Override
	public void onCreate(){
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		Log.d(TAG,"Handler thread created, starting thread");
		thread.start();
		Log.d(TAG,"Thread started");
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("EDaemon","Onstart command");
		toEncrypt = intent.getStringExtra(FILEPATH);
		passw = intent.getStringExtra(PASSWORD);
		algo = intent.getStringExtra(ALGORITHM);
		
		Log.d("EDaemon","onStartCommand; Password: "+passw+"\nfilename: "+toEncrypt+"\nalgo: "+algo);
		Log.d("EDaemon","onStartCommand finished");
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
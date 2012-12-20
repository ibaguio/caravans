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
import android.widget.Toast;

import com.otfe.crypto.Decryptor;
import com.otfe.performance_test.PerformanceTester;

/**
 * Service that decrypts the target
 * @author Ivan Dominic Baguio
 *
 */
public class DecryptionService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	public static final String PASSWORD = "PASS";
	public static final String SRC_FILEPATH = "SRC";
	public static final String DEST_FILEPATH = "DEST";
	public static final String ALGORITHM = "ALGO";
	public static final String CHECKSUM = "CHECK";
	
	private String src_filepath;
	private String dest_filepath;
	private String algo;
	private String password;
	private String checksum;
	
	/* Service proper */
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	Log.d("DecryptionService","Decrypting "+src_filepath+" to "+dest_filepath+"...");
	    	
	    	/* create instance of OnTheFlyDecryptor */
	    	Decryptor otfd = new Decryptor(password, src_filepath ,dest_filepath,algo);
	    	PerformanceTester pt = new PerformanceTester(src_filepath);	//create instance of Performance Tester
	  	  	otfd.decrypt();			//start decrypting
	  	  	pt.endTest();
	  	  	//pt.printResult();
	  	  	Log.d("DecryptionService","CHECKSUM VERIFICATION: "+otfd.verifyChecksum(checksum));
	  	  	Log.d("DecryptionService","Decrypted!");
	        stopSelf(msg.arg1);	//terminate service
	    }
	}

	@Override
	public void onCreate(){
		/* initialize the threads for the service */
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		Log.d("DecryptionService","Handler thread created, starting thread");
		thread.start();
		Log.d("DecryptionService","Thread started");
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		Log.d("DecryptionService","onCreate Finished");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("DecryptionService","onStartCommand");
		
		/* get the file information from intent */
		src_filepath = intent.getStringExtra(SRC_FILEPATH);		//file path of TARGET FILE
		dest_filepath = intent.getStringExtra(DEST_FILEPATH);	//file path of DESTINATION FOLDER
		algo = intent.getStringExtra(ALGORITHM);				//ALGORITHM to be used for decryption
		password = intent.getStringExtra(PASSWORD);				//PASSWORD to be used for the decryption 
		checksum = intent.getStringExtra(CHECKSUM);
		
		/* notify user that the decryption service has started*/
		Toast.makeText(getApplicationContext(), "Decrypting "+src_filepath, Toast.LENGTH_SHORT).show();
		
		/* start the decryption service*/
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
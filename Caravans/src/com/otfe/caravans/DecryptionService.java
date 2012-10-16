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
	
	public final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	Log.d("DecryptionService","Decrypting "+src_filepath+" to "+dest_filepath+"...");
	    	OnTheFlyDecryptor otfd = new OnTheFlyDecryptor(password, src_filepath ,dest_filepath,algo);
	  	  	otfd.decrypt();
	  	  	Log.d("DecryptionService","CHECKSUM VERIFICATION: "+otfd.verifyChecksum(checksum));
	  	  	Log.d("DecryptionService","Decrypted!");
	        stopSelf(msg.arg1);
	    }
	}

	@Override
	public void onCreate(){
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
		
		src_filepath = intent.getStringExtra(SRC_FILEPATH);
		dest_filepath = intent.getStringExtra(DEST_FILEPATH);
		algo = intent.getStringExtra(ALGORITHM);
		password = intent.getStringExtra(PASSWORD);
		checksum = intent.getStringExtra(CHECKSUM);
		
		Toast.makeText(getApplicationContext(), "service starting", Toast.LENGTH_SHORT).show();
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
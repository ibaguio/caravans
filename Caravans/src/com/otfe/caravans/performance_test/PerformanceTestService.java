package com.otfe.caravans.performance_test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.otfe.caravans.Constants;
import com.otfe.caravans.MainActivity;
import com.otfe.caravans.R;
import com.otfe.caravans.crypto.Decryptor;
import com.otfe.caravans.crypto.Encryptor;
import com.otfe.caravans.crypto.Utility;

/**
 * PerformanceTestService
 * runs performance tests on all algorithms
 * given a specified filesize, dummy target file,
 * and destination folder for the results
 * 
 * PROCESS OF TESTING:
 *  create test dummy file
 *  loop through each algorithm
 *    encrypt test dummy file, loop test_count times
 *      do not save enc file until last iteration
 *    decrypt saved enc file, loop test_count times
 *      do not save decrypted file, i.e delete after
 *  delete test_dummy file
 *  
 * @author Ivan Dominic Baguio
 */
public class PerformanceTestService extends Service{
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private final String  TAG = "PerformanceTest"; 
	private final String password = "testpassword";
	
	private final int OK = 1;
	private final int FAIL = 2;
	
	private File targ_file;
	private File dest_folder;
	private File test_result;
	private int test_count;
	private String fsize;
	
	private BufferedWriter bw;
	private NotificationManager mNotificationManager;
	private Notification noti;
	private int NOTIFICATION_ID = 0;
	
	/* flag if encryption test shall be done */
	private boolean do_encrypt = true;
	
	private String doing_now;
	
	/* flag if decryption test shall be done */
	private boolean do_decrypt = true;

	public final class ServiceHandler extends Handler{
		private int max = test_count * 6;
    	private int progress = 0;
    	private Encryptor otfe;
		private Decryptor otfd;
    	private PerformanceTester pt;
    	
		public ServiceHandler(Looper looper){ 
	        super(looper);
	    }

	    @Override
	    public void handleMessage(Message msg) {
	    	new Thread(){
	    		public void run(){	
	    	try{
	    		test_result.createNewFile();
	    		FileWriter fw = new FileWriter(test_result);
	    		bw = new BufferedWriter(fw);
	    		bw.write(getDeviceInfo()+"\nFILE SIZE: "+
	    			fsize+"\nSTART: "+Utility.getDate("HH:mm:ss"));
	    		
	    		/* ENCRYPTION */
	    		String ok;
	    		for (String algo : Constants.ALGORITHMS){
	    			bw.write("\n"+algo);
	    			long sum = 0;
	    			long t;
	    			/* Encryption */
	    			bw.write("\n  ENCRYPTION");
	    			doing_now = "Encryption/"+algo;
		    		for (int i=0;i<test_count;i++){
		    			updateNotification(max,++progress);
		    			otfe = new Encryptor(password,targ_file.getAbsolutePath(),algo);
		    			otfe.setDestFilePath((new File(dest_folder,"."+algo+".enc")).getAbsolutePath());
		    			otfe.doNotDelete();
		    			pt = new PerformanceTester("");
		    			ok = Boolean.toString(otfe.encrypt()).toUpperCase();
		    			pt.endTest();
		    			t = pt.getResult();
		    			sum += t;
		    			bw.write("\n     "+(i+1)+". "+t+" ms\t\t"+ok);
		    			if (i%3==0)
		    				bw.flush();
		    		}
		    		bw.write("\n  ENC AVE: "+(sum/test_count)+" ms\n");
		    		bw.flush();

		    		/* Decryption part of the same algorithm */
		    		doing_now = "Decryption/"+algo;
		    		sum = 0; //reset sum
		    		File toDecrypt = new File(dest_folder,"."+algo+".enc");
		    		bw.write("  DECRYPTION");
		    		for (int i=0;i<test_count;i++){
		    			updateNotification(max,++progress);
		    			otfd = new Decryptor(password,toDecrypt.getAbsolutePath(),
		    				dest_folder.getAbsolutePath(),algo);
		    			otfd.dontSave();
		    			pt = new PerformanceTester("");
		    			
		    			ok = Boolean.toString(otfd.decrypt()).toUpperCase();
		    			pt.endTest();
		    			t = pt.getResult();
		    			sum += t;
		    			bw.write("\n     "+(i+1)+". "+t+" ms\t\t"+ok);
		    			if (i%3==0)
		    				bw.flush();
		    		}
		    		bw.write("\n  DEC AVE: "+(sum/test_count)+" ms\n");
	    			bw.flush();
		    		/* delete enc file */
		    		Log.d(TAG, "Deleting: "+toDecrypt.getAbsolutePath());
		    		toDecrypt.delete();
	    		}
	    		Log.d(TAG,"Deleting: "+targ_file.getAbsolutePath());
	    		/* delete target file */
	    		targ_file.delete();
	    		endService(OK);
	    	}catch(Exception e){
	    		Toast.makeText(getApplicationContext(), "Failed to create result file. Will terminate test", Toast.LENGTH_LONG).show();
	    		e.printStackTrace();
	    		endService(FAIL);
	    	}
	    	}}.run();
	    }
	}
	
	private void endService(int ok){
		mNotificationManager.cancel(NOTIFICATION_ID);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
		nb.setSmallIcon(R.drawable.lock)
			.setWhen(System.currentTimeMillis());
		
		if (ok == OK){
			//nbuilder.setContentText("Performance Test Completed").setOngoing(false);
			nb.setContentText("Successfully Completed Test!")
				.setContentTitle("Performance Test Completed");
		}else{
			//nbuilder.setContentText("Performance Test Failed").setOngoing(false);
			nb.setContentText("Failed to Complete Test")
				.setContentTitle("Performance Test Failed!");
		}
		mNotificationManager.notify(NOTIFICATION_ID+1,nb.build());
		stopSelf();
	}
	
	private String getDeviceInfo(){
		return "Device Information***\n" +
    			"Board:\t" + android.os.Build.BOARD +"\n"+
    			"Manuf:\t" + android.os.Build.MANUFACTURER +"\n"+
    			"Model:\t" + android.os.Build.MODEL +"\n"+
    			"CPUABI:\t" + android.os.Build.CPU_ABI+"\n";
	}
	@Override
	public void onCreate(){
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		Log.d(TAG,"Handler thread created, starting thread");
		thread.start();
		mServiceLooper = thread.getLooper();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d(TAG,"onStartCommand");
		/* get necessary info for performance test */
		String targ_path = intent.getStringExtra(Constants.KEY_TARG_FILE);
		fsize = intent.getStringExtra(Constants.KEY_FILE_SIZE);
		this.targ_file = new File(targ_path);
		this.dest_folder = targ_file.getParentFile();
		this.test_result = new File(dest_folder,"result_"+Utility.getDate("yy-MM-dd_HH-mm")+".txt");		
		this.test_count = intent.getIntExtra(Constants.KEY_TEST_COUNT, 3);
		
		/* notification setup */
		initNotification();
		
		/* start main service */
		mServiceHandler = new ServiceHandler(mServiceLooper);
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onDestroy(){
		super.onDestroy();
		try{
			bw.write("\nEND: "+Utility.getDate("HH:mm:ss"));
			bw.flush();
			bw.close();
		}catch(Exception e){}
	}
	/**
	 * setups the notification
	 */
	private void initNotification(){
		Log.d(TAG,"Initializing Progress Notification");
		CharSequence title = "Performance Test";
		int icon = R.drawable.lock;
		long when = System.currentTimeMillis();

		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
		contentView.setImageViewResource(R.id.status_icon, icon);
		contentView.setTextViewText(R.id.notification_text,"Testing: 0%");
		contentView.setProgressBar(R.id.notification_progressbar, 100, 0, false);
		
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		/* setup the intent that opens MainMenu when Notification is clicked*/
		Intent noti_intent = new Intent(this, MainActivity.class);
		PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, 0);
		
		noti = new Notification(icon,title,when);
		noti.flags |= Notification.FLAG_ONGOING_EVENT;
		noti.contentView = contentView;
		noti.contentIntent = content_intent;
		
		mNotificationManager.notify(NOTIFICATION_ID,noti);
	}
	
	/**
	 * update notification progress
	 * @param max
	 * @param progress
	 */
	private int updateNotification(int max, int progress){
		Log.d(TAG,"Updating notification: "+progress+"/"+max);
		//nbuilder.setProgress(max, progress, true);
		/*contentView.setProgressBar(R.id.notification_progressbar, max, progress, false);
		if (max!=0)
			contentView.setTextViewText(R.id.notification_text, "Testing: "+((int)Math.ceil(progress/max))/100+"%");
		nbuilder.setContent(contentView);
		mNotificationManager.notify(NOTIFICATION_ID, nbuilder.build());*/
		if (max==0) return 0;
		noti.contentView.setTextViewText(R.id.notification_text,"Testing "+doing_now);
		noti.contentView.setProgressBar(R.id.notification_progressbar, max, progress, false);
		mNotificationManager.notify(NOTIFICATION_ID,noti);
		return 0;
	}
}
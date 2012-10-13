package com.otfe.caravans;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class EncryptorDaemon extends IntentService{
	public static final String PASSWORD = "PASS";
	public static final String FILEPATH = "FNAME";
	private File toEncrypt;
	private String passw;
	
	public EncryptorDaemon(String name) {
		super(name);
	}
	@Override
	public void onDestroy(){
		Log.d("EDaemon","Destroyed");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d("EDaemon","Onstart command");
		Bundle b = intent.getExtras();
		toEncrypt = new File(b.getString(FILEPATH));
		passw = "ivandominic";
		Log.d("EDaemon","onStartCommand; Password: "+passw+"\nfilename: "+toEncrypt.getAbsolutePath());
		if (!toEncrypt.isFile())
			Log.d("EDaemon","toEncrypt not a file");
		Log.d("EDaemon","onStartCommand finished");
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		OnTheFlyEncryptor otfe = new OnTheFlyEncryptor(passw,toEncrypt.getAbsolutePath(),"AES");
		if (!otfe.encrypt())
			Log.d("EDaemon","Failed to encrypt "+toEncrypt.getAbsolutePath());
		else
			Log.d("EDaemon",toEncrypt.getAbsolutePath()+" encrypted successfully");
		stopSelf();
	}
}

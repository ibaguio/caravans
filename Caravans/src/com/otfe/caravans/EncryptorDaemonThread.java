package com.otfe.caravans;

import java.io.File;
import android.util.Log;

public class EncryptorDaemonThread implements Runnable{
	public static final String PASSWORD = "PASS";
	public static final String FILEPATH = "FNAME";
	private File toEncrypt;
	private String passw;
	
	public EncryptorDaemonThread(File f, String pass){
		Log.d("EDT","Created new Encryptor daemon thread");
		this.toEncrypt = f;
		this.passw = pass;
	}
	@Override
	public void run() {
		OnTheFlyEncryptor otfe = new OnTheFlyEncryptor(passw,toEncrypt.getAbsolutePath(),OnTheFlyUtils.TWO_FISH);
		if (!otfe.encrypt())
			Log.d("EDaemon","Failed to encrypt "+toEncrypt.getAbsolutePath());
		else
			Log.d("EDaemon",toEncrypt.getAbsolutePath()+" encrypted successfully");
		Log.d("EDT","Run completed");
	}

}

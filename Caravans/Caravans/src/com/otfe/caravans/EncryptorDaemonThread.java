package com.otfe.caravans;

import java.io.File;

import android.util.Log;

import com.otfe.crypto.Encryptor;
import com.otfe.performance_test.PerformanceTester;

public class EncryptorDaemonThread implements Runnable{
	public static final String PASSWORD = "PASS";
	public static final String FILEPATH = "FNAME";
	private File toEncrypt;
	private String passw;
	private String algorithm;
	
	public EncryptorDaemonThread(File f, String pass, String algo){
		Log.d("EDT","Created new Encryptor daemon thread");
		this.toEncrypt = f;
		this.passw = pass;
		this.algorithm = algo;
	}
	@Override
	public void run() {
		Encryptor otfe = new Encryptor(passw,toEncrypt.getAbsolutePath(),algorithm);
		PerformanceTester pt = new PerformanceTester(toEncrypt.getName());
  	  	boolean ok = otfe.encrypt();
  	  	pt.endTest();
  	  	//pt.printResult();
		
		if (!ok)
			Log.d("EDaemon","Failed to encrypt "+toEncrypt.getAbsolutePath());
		else
			Log.d("EDaemon",toEncrypt.getAbsolutePath()+" encrypted successfully");
		Log.d("EDT","Run completed");
	}
}
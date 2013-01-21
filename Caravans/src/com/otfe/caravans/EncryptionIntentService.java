package com.otfe.caravans;

import com.otfe.caravans.crypto.Encryptor;
import com.otfe.caravans.performance_test.PerformanceTester;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class EncryptionIntentService extends IntentService {
	private static final String TAG = "EncryptionIntentService";
	private String toEncrypt;
	private String passw;
	private String algo;
	
	public EncryptionIntentService(){
		super(EncryptionIntentService.class.getName());
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		int operation = intent.getIntExtra(Constants.EXTRA_SERVICE_TASK, -1);
		switch(operation){
			case Constants.TASK_ENCRYPT_SINGLE:
				loadExtras(intent);
				runMainEncryption();
				break;
		}
	}

	private void loadExtras(Intent intent){
		toEncrypt = intent.getStringExtra(Constants.KEY_TARG_FILE);
		passw = intent.getStringExtra(Constants.KEY_PASSWORD);
		algo = intent.getStringExtra(Constants.KEY_ALGORITHM);
	}
	
	private void runMainEncryption(){
		Log.d(TAG,"Encrypting "+toEncrypt);
    	Encryptor otfe = new Encryptor(passw,toEncrypt,algo);
    	PerformanceTester pt = new PerformanceTester(toEncrypt);
  	  	boolean ok = otfe.encrypt();
  	  	pt.endTest();
	}
}
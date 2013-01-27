package com.otfe.caravans;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.otfe.caravans.crypto.Encryptor;

/**
 * IntentService that encrypts a target file
 * 
 * <p> Required values:
 *	 intent.putExtra(Constants.KEY_PASSWORD, password);
 *	 intent.putExtra(Constants.KEY_ALGORITHM, algorithm);
 *	 intent.putExtra(Constants.KEY_TARG_FILE, target);
 *	 intent.putExtra(Constants.EXTRA_SERVICE_TASK, Constants.TASK_ENCRYPT_SINGLE);
 *   
 * <p> Optional values:
 *   intent.putExtra(Constants.KEY_DEST_FOLDER, dest_folder);
 *   intent.putExtra(Constants.KEY_DELETE_TARG, delete);
 *   
 * @author Ivan Dominic Baguio
 * 
 * @see com.otfe.caravans.crypto.Encyptor.java
 * @since 1.1
 */
public class EncryptionIntentService extends IntentService {
	private static final String TAG = "EncryptionIntentService";
	private String toEncrypt;
	private String passw;
	private String algo;
	private String dest_folder;
	private boolean delete;
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

	/**
	 * Loads the values from the intent
	 * @param intent the intent from the calling activity/service containing
	 * the values to be loaded
	 */
	private void loadExtras(Intent intent){
		toEncrypt = intent.getStringExtra(Constants.KEY_TARG_FILE);
		passw = intent.getStringExtra(Constants.KEY_PASSWORD);
		algo = intent.getStringExtra(Constants.KEY_ALGORITHM);
		
		dest_folder = intent.getStringExtra(Constants.KEY_DEST_FOLDER);
		delete = intent.getBooleanExtra(Constants.KEY_DELETE_TARG, true);
	}

	/**
	 * Creates an Encryptor instance and runs the encryption
	 */
	private void runMainEncryption(){
		Log.d(TAG,"Encrypting "+toEncrypt);
    	Encryptor otfe = new Encryptor(passw,toEncrypt,algo);
    	if (dest_folder!= null && !dest_folder.equals(""))
    		otfe.setDestinationFolder(dest_folder);
    	if (!delete) otfe.doNotDelete();
  	  	otfe.encrypt();
  	  	Log.w(TAG, "finished encrypting: ");
	}
}
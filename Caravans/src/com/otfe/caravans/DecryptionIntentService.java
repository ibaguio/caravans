package com.otfe.caravans;

import com.otfe.caravans.crypto.Decryptor;
import com.otfe.caravans.performance_test.PerformanceTester;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
/**
 * IntentService to run Single Decryption on a separate thread
 * @author baguio
 */
public class DecryptionIntentService extends IntentService {
	private static final String TAG = "Decryption IS";
	private String src_filepath;
	private String dest_filepath;
	private String algo;
	private String password;
	private String checksum;
	
	public DecryptionIntentService(){
		super(DecryptionIntentService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int operation = intent.getIntExtra(Constants.EXTRA_SERVICE_TASK, -1);
		switch(operation){
			case Constants.TASK_DECRYPT_SINGLE:
				loadExtras(intent);
				runMainDecryption();
				break;
		}
	}
	
	/**
	 * load the extras from intent
	 * @param intent
	 */
	private void loadExtras(Intent intent){
		/* get the file information from intent */
		src_filepath = intent.getStringExtra(Constants.KEY_TARG_FILE);	//file path of TARGET FILE
		dest_filepath = intent.getStringExtra(Constants.KEY_DEST_FOLDER);	//file path of DESTINATION FOLDER
		algo = intent.getStringExtra(Constants.KEY_ALGORITHM);				//ALGORITHM to be used for decryption
		password = intent.getStringExtra(Constants.KEY_PASSWORD);			//PASSWORD to be used for the decryption 
		checksum = intent.getStringExtra(Constants.KEY_CHECKSUM);
	}
	
	/**
	 * create new instance of decryptor and start decryption
	 */
	private void runMainDecryption(){		
		Toast.makeText(getApplicationContext(), "Decrypting "+src_filepath, Toast.LENGTH_SHORT).show();
		Log.d("DecryptionService","Decrypting "+src_filepath+" to "+dest_filepath+"...");
    	
    	/* create instance of OnTheFlyDecryptor */
    	Decryptor otfd = new Decryptor(password, src_filepath ,dest_filepath,algo);
    	//PerformanceTester pt = new PerformanceTester(src_filepath);	//create instance of Performance Tester
    	Log.d(TAG,"Starting to decrypt");
  	  	otfd.decrypt();			//start decrypting
  	  	//pt.endTest();
  	  	Log.d("DecryptionService","CHECKSUM VERIFICATION: "+otfd.verifyChecksum(checksum));
  	  	Log.d("DecryptionService","Decrypted!");
        stopSelf();	//terminate service
	}
}

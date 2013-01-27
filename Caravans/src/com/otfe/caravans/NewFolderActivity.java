package com.otfe.caravans;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.Utility.Callback;
import com.otfe.caravans.Utility.PasskeyReturn;
import com.otfe.caravans.crypto.CryptoUtility;
import com.otfe.caravans.crypto.Encryptor;
import com.otfe.caravans.database.FileLoggerDataSource;
import com.otfe.caravans.database.FolderLog;
import com.otfe.caravans.database.FolderLoggerDataSource;

/**
 * Creates an OTF encrypted folder to be monitored
 * by the app, any new file that is added in this
 * folder would be directly encrypted using the set
 * password and algorithm
 * @author Ivan Dominic Baguio
 */
public class NewFolderActivity extends Activity{
	private final int GET_FOLDERPATH = 0;
	private final int MAKE_PATTERN = 1;
	private final String TAG = "NewFolderActivity";
	
	private File target;
	private String passkey;
	private String algorithm;
	private int _id;
	private FolderLoggerDataSource folder_ds;
	private boolean isPattern;
	
	private Callback newEncFolder;
	private PasskeyReturn pwOKCallback;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_encrypted_folder);
		folder_ds = new FolderLoggerDataSource(this);
		((TextView) findViewById(R.id.folder_to_encrypt))
			.setText(CryptoUtility.pathRemoveMNT(
				Constants.SETTINGS_DEFAULT_TARGET_FOLDER_PATH));
		target = new File(Constants.SETTINGS_DEFAULT_TARGET_FOLDER_PATH);
		newEncFolder = new Callback() {
			public void doIt() {
				setupNewEncryptedFolder();
			}
		};
		pwOKCallback = new PasskeyReturn() {
			public void setPasskey(String passkey) {
				NewFolderActivity.this.passkey = passkey;
				isPattern = false;
				Utility.selectedInput(NewFolderActivity.this,
					R.id.password_btn);
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
		    case GET_FOLDERPATH:
		        if (resultCode == RESULT_OK) {
		            @SuppressWarnings("unchecked")
					List<LocalFile> files = (List<LocalFile>) 
		            		data.getSerializableExtra(FileChooserActivity._Results);
		            for (File f : files){
		            	target = f;
		            	TextView tv_folder = (TextView) findViewById(R.id.folder_to_encrypt);
		        		tv_folder.setText(CryptoUtility.pathRemoveMNT(f.getPath()));
		            }
		        }
		        break;
		    case Constants.TASK_MAKE_PATTERN:
		    	if (resultCode == RESULT_OK) {
		    		passkey = data.getStringExtra(LockPatternActivity._Pattern);
		    		isPattern = true;
		    		Log.d(TAG, "Pattern: "+passkey);
		    		Utility.selectedInput(this, R.id.pattern_btn);
		    	}
		    	break;
	    }
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.browse_btn:
				Utility.browseFile(this, Constants.BROWSE_FILE);
				break;
			case R.id.password_btn:
				Utility.showGetPasswordDialog(this, pwOKCallback);
				break;
			case R.id.pattern_btn:
				Utility.showCreateLockPattern(this);
				break;
			case R.id.create_new_folder:
				setupNewEncryptedFolder();
				break;
		}
	}
	
	private void setupNewEncryptedFolder(){
		if (!this.folder_ds.isNewFolder(target.getPath())){
			Toast.makeText(this, "The selected folder is already target for on the fly encryption", 
					Toast.LENGTH_LONG).show();
			target = null;
			((TextView)findViewById(R.id.folder_to_encrypt)).setText("");
			return;
		}else if (!target.exists()){
			Utility.promptCreateFolderDialog(this, target, newEncFolder);
			return;
		}else if (passkey == null || passkey.isEmpty()){
			Toast.makeText(this, "Enter a password or pattern", Toast.LENGTH_SHORT).show();
			return;
		}
		
		/* get the selected Radio Button Id */
		RadioGroup rg = (RadioGroup)this.findViewById(R.id.radioGroup_algorithms);
		int rid = rg.getCheckedRadioButtonId();
		algorithm = ((RadioButton) rg.findViewById(rid)).getText().toString();
			
		Log.d(TAG,"Password: "+passkey+"\nFolder: "+target.getPath()+"\nAlgo: "+algorithm);
		Toast.makeText(this, "Created new Encrypted Folder", Toast.LENGTH_SHORT).show();
		addFolderToDatabase();

		// TODO: Use async task for this, so that no lag occurs
		/* create dialog to prompt user to start encryption service now */
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Run encryption service on target folder now?")
			.setPositiveButton("Yes", startOtfeDialogListener)
			.setNegativeButton("No", startOtfeDialogListener)
			.setTitle("Run Encryption Now?").show();
	}

	/**
	 * setups the SQL database and adds the new folder
	 * @param f
	 * @param algorithm
	 */
	private void addFolderToDatabase(){
		Log.d(TAG,"Setting up folder database, pattern?: "+isPattern);
		FileLoggerDataSource file_ds = new FileLoggerDataSource(this,target.getName());
		/* Adds the newly set up otfe folder to the FolderLog table */
		FolderLog folderlog = this.folder_ds.createFolderLog(target,algorithm,
				Encryptor.generateVerifyHash(passkey,algorithm), isPattern);
		
		/* Creates a new table that lists the files in the folder */
		file_ds.open();
		file_ds.close();
		Log.d(TAG,"Folder Info added to database");
		_id = (int)folderlog.getId();
	}
	
	/**
	 * listener for the dialog that prompts user if 
	 * encryption service should be started now
	 */
	DialogInterface.OnClickListener startOtfeDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	    		/* setup the Bundles used */
	    		Bundle extras = new Bundle();
	    		extras.putString(Constants.KEY_PASSWORD, passkey);
	    		extras.putString(Constants.KEY_TARG_FILE, target.getPath());
	    		extras.putString(Constants.KEY_ALGORITHM, algorithm);
	    		extras.putInt(Constants.KEY_ROW_ID, _id);
	    		/* Start Folder Service Listener */
	        	FolderObserverService.startObserving(getApplicationContext(), extras);
	            break;
	        case DialogInterface.BUTTON_NEGATIVE:
	            break;
	        }
			dialog.dismiss();
			finish();
		}
	};
	
	/* close folder data source when stopped */
	@Override
	protected void onPause(){
		super.onPause();
		this.folder_ds.close();
	}
	/* open folder data source when started */
	@Override
	protected void onResume(){
		super.onResume();
		this.folder_ds.open();
	}
}
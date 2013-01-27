package com.otfe.caravans;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.Utility.Callback;
import com.otfe.caravans.Utility.PasskeyReturn;
import com.otfe.caravans.crypto.CryptoUtility;

public class EncryptSingleActivity extends Activity {
	private static final String TAG = "EncryptSingle";
	private String passkey;
	private File target;
	private File dest_folder;
	
	private Callback encryptCallBack;
	private PasskeyReturn getPasskey;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encrypt_single);
		
		TextView tv = (TextView) findViewById(R.id.destination_folder);
		tv.setText(CryptoUtility.pathRemoveMNT(Constants.SETTINGS_DEFAULT_ENCRYPT_FOLDER_PATH));
		dest_folder = new File(Constants.SETTINGS_DEFAULT_ENCRYPT_FOLDER_PATH);
		encryptCallBack = new Callback(){
			public void doIt(){
				encryptSingle();
			}
		};
		getPasskey = new PasskeyReturn(){
			public void setPasskey(String passkey) {
				EncryptSingleActivity.this.passkey = passkey;
			}
		};
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.single_encrypt_btn:
				encryptSingle();
				break;
			case R.id.browse_target_btn:
				Utility.browseFile(this,Constants.BROWSE_FILE);
				break;
			case R.id.browse_for_dest_btn:
				Utility.browseFile(this,Constants.BROWSE_FOLDER);
				break;
			case R.id.password_btn:
				Utility.showGetPasswordDialog(this, getPasskey);
				break;
			case R.id.pattern_btn:
				Utility.showCreateLockPattern(this);
				break;
		}
	}
	
	private void encryptSingle(){
		String msg = null;
		if (target == null || !target.isFile())
			msg = "Invalid target file";
		else if (dest_folder == null)
			msg = "Invalid Destination folder";
		else if (passkey == null || passkey.equals(""))
			msg = "Enter a password or pattern";
		
		if (msg != null){
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			return;
		}
		
		/* Prompt user to create the destination folder */
		if (!dest_folder.exists()){
			Utility.promptCreateFolderDialog(this, dest_folder, encryptCallBack);
			return;
		}
		
		boolean delete = ((CheckBox) findViewById(R.id.chk_delete)).isChecked();
		RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup_algorithms);
		int rid = rg.getCheckedRadioButtonId();
		String algorithm = ((RadioButton) rg.findViewById(rid)).getText().toString();
		
		Log.d(TAG,"Passkey: "+passkey+"\nAlgo: "+algorithm+"\ndelete: "+delete
				+"\ndest: "+dest_folder.getPath()+"\ntarg: "+target.getPath());
		
		Intent intent = new Intent(this, EncryptionIntentService.class);
		intent.putExtra(Constants.KEY_PASSWORD, passkey);
		intent.putExtra(Constants.KEY_ALGORITHM, algorithm);
		intent.putExtra(Constants.KEY_TARG_FILE, target.getPath());
		intent.putExtra(Constants.KEY_DEST_FOLDER, dest_folder.getPath());
		intent.putExtra(Constants.KEY_DELETE_TARG, delete);
		intent.putExtra(Constants.EXTRA_SERVICE_TASK, Constants.TASK_ENCRYPT_SINGLE);
		
		startService(intent);
		Toast.makeText(getApplicationContext(), "Encrypting "+CryptoUtility.pathRemoveMNT(target.getPath())
				, Toast.LENGTH_SHORT).show();
		finish();
	}
	
	/* result from FileBrowser or LockPattern */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != RESULT_OK) return;
		if ((requestCode == Constants.TASK_GET_TARGET || 
				requestCode == Constants.TASK_GET_DEST_FOLDER)) {
			/* get the files from the file chooser */
			File selected_file=null;
			@SuppressWarnings("unchecked")
			List<LocalFile> files = (List<LocalFile>) 
            		data.getSerializableExtra(FileChooserActivity._Results);
            for (File f : files)
            	selected_file = f;
			
            /* check if returned file is valid*/
            if (selected_file!=null){
				switch(requestCode){
					/* the returned file is the file to be decrypted */
					case Constants.TASK_GET_TARGET:
						target = selected_file;
						/* set the textview text to the files name */
						TextView tv1 = (TextView)findViewById(R.id.file_to_encrypt);
						tv1.setText(CryptoUtility.pathRemoveMNT(target.getPath()));
						break;
					case Constants.TASK_GET_DEST_FOLDER:
						dest_folder = selected_file;
						TextView tv2 = (TextView)findViewById(R.id.destination_folder);
						tv2.setText(CryptoUtility.pathRemoveMNT(dest_folder.getPath()));
						break;
				}
            }
		}else if (requestCode == Constants.TASK_MAKE_PATTERN){
			passkey = data.getStringExtra(LockPatternActivity._Pattern);
			Utility.selectedInput(this,R.id.pattern_btn);
		}
	}
}
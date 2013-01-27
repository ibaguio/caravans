package com.otfe.caravans;

/**
 * DecryptSingle Activity
 * activity for decrypting a single file
 * @author Ivan Dominic Baguio
 */

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.Utility.Callback;
import com.otfe.caravans.crypto.CryptoUtility;
import com.otfe.caravans.crypto.Decryptor;

/* Activity that starts a decryption service */
public class DecryptSingleActivity extends Activity{
	private final String TAG = "DecryptSingle";

	private String passkey; //passkey to be used (may be a string hash of pattern)
	private File dest_folder;
	private File target;
	private SQLiteDatabase db;
	
	private RadioGroup rg1;
	private RadioGroup rg2;
	private int last_group = 0;
	private int algorithm;
	
	private Callback decryptCallBack;
	
	/**
	 * custom class for check changing 
	 * created due to limitations on RadioGroup layout design
	 * @author Ivan Dominic Baguio
	 */
	public class MyCheckChangedListener implements android.widget.RadioGroup.OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(RadioGroup group, int id) {
			algorithm = id;
			if (group.equals(rg1) && last_group == 1){
				rg2.clearCheck();
				last_group = 0;
			}else if (group.equals(rg2) && last_group == 0){
				rg1.clearCheck();
				last_group = 1;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decrypt_single);
		this.rg1 = (RadioGroup)this.findViewById(R.id.radioGroup_algo2);
		this.rg2 = (RadioGroup)this.findViewById(R.id.radioGroup_algo3);
		this.db = this.openOrCreateDatabase(Constants.DATABASE_NAME,
				Constants.DATABASE_VERSION, null);

		/* set auto as the default algorithm */
		this.algorithm = R.id.radio_auto;
		
		/* set the custom check change listener*/
		this.rg1.setOnCheckedChangeListener(new MyCheckChangedListener());
		this.rg2.setOnCheckedChangeListener(new MyCheckChangedListener());
		
		SharedPreferences preferences =
				getSharedPreferences(Constants.SETTINGS_NAME, Constants.SETTINGS_MODE);
		String default_folder_path = preferences.getString(
				getApplicationContext().getString(R.string.preferences_key_decrypted_folder), 
				Constants.SETTINGS_DEFAULT_DECRYPT_FOLDER_PATH);
		dest_folder = new File(default_folder_path);
		((TextView)findViewById(R.id.decrypt_dest)).setText(default_folder_path);
		
		decryptCallBack = new Callback(){
			public void doIt(){
				decryptSingle();
			}
		};
	}

	public void onClick(View view){
		switch(view.getId()){
			case R.id.browse_for_decrypt_btn: /* select the file to be decrypted */
				Utility.browseFile(this,Constants.BROWSE_FILE);
				break;
			case R.id.browse_for_dest_btn: /*selects the destination folder for the decrypted file to be saved */
				Utility.browseFile(this,Constants.BROWSE_FOLDER);
		    	break;
			case R.id.pattern_btn: /* browse pattern*/
				showGetLockPattern();
				break;
			case R.id.password_btn:
				showGetPassword();
				break;
				
		    /* check if the inputs are valid then start a decryption service for the target file
		     * to destination folder */
			case R.id.single_decrypt_btn:
				decryptSingle();
				break;
		}
	}
	
	/**
	 * Checks the prerequisites before starting decryption 
	 *   1. if target file is a valid file
	 *   2. if password is not empty
	 *   3. if dest_folder exist, if not prompt user to create it
	 *  
	 * <p>After checking prereqs, check if the password is correct,
	 * then run decryption
	 */ 
	private void decryptSingle(){
		String msg = null;
		if (target==null || !target.isFile()){
			msg = "Invalid target file";
			((TextView)this.findViewById(R.id.file_to_decrypt)).setText("");
			target = null;
		}else if (passkey == null || passkey.equals("")){
			msg = "Enter a password or pattern";
		}/*else if (!dest_folder.isDirectory()){
			msg = "Invalid destination folder";
			((TextView)this.findViewById(R.id.decrypt_dest)).setText("");
			dest_folder = null;
		}*/
		if (msg !=null){//error has occured, prompt error, and return
			Toast.makeText(getApplicationContext(), msg , Toast.LENGTH_SHORT).show();
			return;
		}else if (!dest_folder.exists()){
			//Show the create Folder dialog and do what needs to be done
			Utility.promptCreateFolderDialog(this, dest_folder, decryptCallBack);
			return;
		}else{ //ok for decryption
			Log.d(TAG, "OK for single Decryption");
			/* get the algorithm from input*/
			String algo = getAlgorithm();
			Toast.makeText(getApplicationContext(),"Will decrypt using "+algo, Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Will be using algo: *"+algo+"*");
			
			/* PASSWORD/ALGORITHM CHECKING*/
			Decryptor otfd = new Decryptor(passkey, 
					target.getAbsolutePath(), dest_folder.getAbsolutePath(), algo);
			if (otfd.correctPassword()){
				/* password/algorithm is correct, continue with decryption service */
				Toast.makeText(getApplicationContext(), "Password and Algorithm is correct", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, DecryptionIntentService.class);
				
				/* pass to intent necessary information to be used in the decryption */
				intent.putExtra(Constants.EXTRA_SERVICE_TASK,Constants.TASK_DECRYPT_SINGLE);
				intent.putExtra(Constants.KEY_TARG_FILE, target.getAbsolutePath());
				intent.putExtra(Constants.KEY_DEST_FOLDER, dest_folder.getAbsolutePath());
				intent.putExtra(Constants.KEY_PASSWORD, passkey);
				intent.putExtra(Constants.KEY_ALGORITHM, algo);
				intent.putExtra(Constants.KEY_CHECKSUM, getStoredChecksum());
				startService(intent);
				db.close();
				finish();
			}else
				Toast.makeText(getApplicationContext(), "Failed: Password and/or Algorithm is invalid",
						Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * Returns a String representation of the algorithm
	 * depending on the selected radio button
	 * @return Algorithm
	 */
	private String getAlgorithm(){
		String algo = "";
		if (algorithm == R.id.radio_aes2)
			algo = Constants.AES;
		else if (algorithm == R.id.radio_two_fish2)
			algo = Constants.TWO_FISH;
		else if (algorithm == R.id.radio_serpent2)
			algo = Constants.SERPENT;
		else if (algorithm == R.id.radio_auto){
			/* auto determine algorithm to be used*/
			algo = getAutoAlgorithm();
			if (algo.equals("")){
				Toast.makeText(getApplicationContext(),"Could not determine algorithm", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(getApplicationContext(),"Error on selected algorithm", Toast.LENGTH_SHORT).show();
		}
		return algo;
	}
	
	/**
	 * result from FileBrowser or LockPattern
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != RESULT_OK) return;
		if (requestCode == Constants.TASK_GET_TARGET || 
				requestCode == Constants.TASK_GET_DEST_FOLDER) {
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
						TextView tv1 = (TextView)findViewById(R.id.file_to_decrypt);
						tv1.setText(CryptoUtility.pathRemoveMNT(target.getPath()));
						break;
					case Constants.TASK_GET_DEST_FOLDER:
						dest_folder = selected_file;
						TextView tv2 = (TextView)findViewById(R.id.decrypt_dest);
						tv2.setText(CryptoUtility.pathRemoveMNT(dest_folder.getPath()));
						break;
				}
            }
		}else if (requestCode == Constants.TASK_MAKE_PATTERN){
			passkey = data.getStringExtra(LockPatternActivity._Pattern);
			Log.d(TAG,"Pattern: "+passkey);
			Utility.selectedInput(this,R.id.pattern_btn);
		}
	}
	/**
	 * looks up the SQL database for the algorithm 
	 * used to encrypt the file
	 * if the file is not in the database, an
	 * empty string is returned
	 * @return
	 */
	private String getAutoAlgorithm(){
		//TODO: add another functionality to determine
		//	algo by brute forcing the headers of the file
		/* initialize db query to get the algorithm from target file */
		String query = "SELECT algorithm FROM folderlogger WHERE path='"+target.getParentFile()+"';";
		String algo="";
		try{
			Cursor c = db.rawQuery(query,null);
			c.moveToFirst();
			Log.d("","got cursor "+c.getCount());
		if (c.getCount()==1)
			algo = c.getString(0);
		}catch(Exception e){
			Log.d(TAG,""+e.getMessage());
		}
		Log.d(TAG,"Algo: "+algo);
		return algo;
	}

	/**
	 * gets the checksum stored in the database
	 * @return checksum
	 */
	private String getStoredChecksum(){
		/* initialize db query*/
		String query = "SELECT checksum FROM filelogger_"+target.getParentFile().getName()
				+" WHERE filename='"+target.getName()+"';";
		String checksum="";
		try{
			Cursor c = db.rawQuery(query,null);
			c.moveToFirst();
		if (c.getCount()==1)
			checksum = c.getString(0);
		}catch(Exception e){
			Log.d(TAG,""+e.getMessage());
		}
		Log.d(TAG,"checksum: "+checksum);
		return checksum;
	}
	
	private void showGetLockPattern(){
		Intent intent = new Intent(this,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.GetPattern);
		startActivityForResult(intent, Constants.TASK_MAKE_PATTERN);
	}
	
	/**
	 * Show a dialog box to prompt for password
	 */
	private void showGetPassword(){
		//TODO: instead of using edit text, create new
		//	layout that has the 'show password' checkbox
		Context context = getApplicationContext();
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View pView = li.inflate(R.layout.prompt_password, null);
		
		final EditText inp = (EditText) pView.findViewById(R.id.input_password);
		
		new AlertDialog.Builder(this)
			.setView(pView)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DecryptSingleActivity.this.passkey = inp.getText().toString();
					Utility.selectedInput(DecryptSingleActivity.this,R.id.password_btn);
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			}).show();
	}
}
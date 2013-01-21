package com.otfe.caravans;

/**
 * DecryptSingle Activity
 * activity for decrypting a single file
 * @author Ivan Dominic Baguio
 */

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.crypto.Decryptor;
import com.otfe.caravans.crypto.Utility;
import com.otfe.caravans.database.FileLogHelper;

/* Activity that starts a decryption service */
public class DecryptSingleActivity extends Activity{
	private final String TAG = "DecryptSingle";

	private String password; //password to be used (may be a string hash of pattern)
	private File dest_folder;
	private File target;
	private SQLiteDatabase db;
	
	private RadioGroup rg1;
	private RadioGroup rg2;
	private int last_group = 0;
	private int algorithm;

	private boolean ready;
	private Drawable default_button;
	
	/* custom class for check changing 
	 * created due to limitations on RadioGroup layout design
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
		
		this.db = this.openOrCreateDatabase(FileLogHelper.DATABASE_NAME,
				FileLogHelper.DATABASE_VERSION, null);

		/* set auto as the default algorithm */
		this.algorithm = R.id.radio_auto;
		
		/* set the custom check change listener*/
		this.rg1.setOnCheckedChangeListener(new MyCheckChangedListener());
		this.rg2.setOnCheckedChangeListener(new MyCheckChangedListener());
		
		Context context = getApplicationContext();
		SharedPreferences preferences =
				getSharedPreferences(Constants.SETTINGS_NAME, Constants.SETTINGS_MODE);
		String default_folder_path = preferences.getString(
				context.getString(R.string.preferences_key_decrypted_folder), 
				Constants.SETTINGS_DEFAULT_DECRYPT_FOLDER_PATH);
		this.dest_folder = new File(default_folder_path);
		TextView tv = (TextView)findViewById(R.id.decrypt_dest);
		tv.setText(default_folder_path);
		default_button = findViewById(R.id.password_btn).getBackground();
	}

	public void onClick(View view){
		Intent intent;
		switch(view.getId()){
			case R.id.browse_for_decrypt_btn: /* select the file to be decrypted */
			case R.id.browse_for_dest_btn: /*selects the destination folder for the decrypted file to be saved */
				browseFolder(view.getId());
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
				this.ready = false;
				/* get the password from input */
				/*check if given inputs are valid */
				checkReadyDecryption();

				if (ready){
					Log.d(TAG, "OK for single Decryption");
					/* get the algorithm from input*/
					String algo = getAlgorithm();
					if (algo.equals("")) break;
					Toast.makeText(getApplicationContext(),"Will decrypt using "+algo, Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Will be using algo: *"+algo+"*");
					
					/* PASSWORD/ALGORITHM CHECKING*/
					Decryptor otfd = new Decryptor(password, 
							target.getAbsolutePath(), dest_folder.getAbsolutePath(), algo);
					
					if (otfd.correctPassword()){
						/* password/algorithm is correct, continue with decryption service */
						Toast.makeText(getApplicationContext(), "Password and Algorithm is correct", Toast.LENGTH_SHORT).show();
						intent = new Intent(this, DecryptionIntentService.class);
						
						/* pass to intent necessary information to be used in the decryption */
						intent.putExtra(Constants.EXTRA_SERVICE_TASK,Constants.TASK_DECRYPT_SINGLE);
						intent.putExtra(Constants.KEY_TARG_FILE, target.getAbsolutePath());
						intent.putExtra(Constants.KEY_DEST_FOLDER, dest_folder.getAbsolutePath());
						intent.putExtra(Constants.KEY_PASSWORD, password);
						intent.putExtra(Constants.KEY_ALGORITHM, algo);
						intent.putExtra(Constants.KEY_CHECKSUM, getStoredChecksum());
						
						Log.d(TAG,"Starting Service");
						startService(intent);
						db.close();
						finish();
					}else
						Toast.makeText(getApplicationContext(), "Failed: Password and/or Algorithm is invalid",
								Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}
	
	/**
	 * Checks the prerequisites before starting decryption
	 *   1. if dest_folder exist, if not prompt user to create it
	 *   2. if target file is a valid file
	 *   3. if password is not empty
	 *   4. if dest_folder is a directory
	 * Doesnt return anything but marks READY flag
	 */ 
	private void checkReadyDecryption(){
		if (!dest_folder.exists()){ //folder does not exist, prompt to create it			
			/* Dialog box to confirm creation of new folder if folder is not there */
			DialogInterface.OnClickListener dialogCreateFolderListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            /* Create button clicked, create the file and
				             * set READY flag to true if successfully created */
				        	try {
								dest_folder.createNewFile();
								if (!dest_folder.isDirectory() || !dest_folder.exists())
									throw new IOException();
								else
									checkReadyDecryption(); //file craeted, call back the function
								
							} catch (IOException e) {
								//e.printStackTrace();
								Toast.makeText(getApplicationContext(), "Failed to create folder", 
									Toast.LENGTH_SHORT).show();
							}
				            break;
				        case DialogInterface.BUTTON_NEGATIVE:
				            break;
			        }
			    }
			};
			
			String msg = "Destination folder does not exist. Create it?";
			AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
			builder.setMessage(msg).setPositiveButton("Create", dialogCreateFolderListener)
			    .setNegativeButton("Cancel", dialogCreateFolderListener).show();
			return;
		}

		Log.d(TAG,"password: *"+password+"*");
		if (target==null || !target.isFile()){
			Toast.makeText(getApplicationContext(), "Invalid target file", Toast.LENGTH_SHORT).show();
			TextView tv = (TextView)this.findViewById(R.id.file_to_decrypt);
			tv.setText("");
			this.target = null;
			Log.d(TAG, "Target file invalid");
			ready = false;
		}else if (password.equals("") || password == null){
			Toast.makeText(getApplicationContext(), "Enter a password or pattern", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Invalid Password");
			ready = false;
		}else if (!dest_folder.isDirectory()){
			Toast.makeText(getApplicationContext(), "Invalid destination folder", Toast.LENGTH_SHORT).show();
			TextView tv = (TextView)this.findViewById(R.id.decrypt_dest);
			tv.setText("");
			this.dest_folder = null;
			Log.d(TAG, "Invalid Dest folder");
			ready = false;
		}else
			ready = true;
	}
	
	/**
	 * Returns a String representation of the algorithm
	 * depending on the selected radio button
	 * @return
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

	private void browseFolder(int id){
		Intent intent = new Intent(this,FileChooserActivity.class);
    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Constants.SDCARD));
    	
		switch (id){
			case R.id.browse_for_decrypt_btn:
				/* browse for files only (target file) */
		    	startActivityForResult(intent, Constants.TASK_GET_DECRYPT_TARGET);
		    	break;
			case R.id.browse_for_dest_btn:
				/* browse for destination folder */
				intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
				startActivityForResult(intent, Constants.TASK_GET_DEST_FOLDER);
				break;
		}
	}
	
	/**
	 * result from FileBrowser or LockPattern
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != RESULT_OK) return;
		if ((requestCode == Constants.TASK_GET_DECRYPT_TARGET || 
				requestCode == Constants.TASK_GET_DEST_FOLDER)) {
			/* get the files from the file chooser */
			File selected_file=null;
			List<LocalFile> files = (List<LocalFile>) 
            		data.getSerializableExtra(FileChooserActivity._Results);
            for (File f : files)
            	selected_file = f;
			
            /* check if returned file is valid*/
            if (selected_file!=null){
				switch(requestCode){
					/* the returned file is the file to be decrypted */
					case Constants.TASK_GET_DECRYPT_TARGET:
						target = selected_file;
						/* set the textview text to the files name */
						TextView tv1 = (TextView)findViewById(R.id.file_to_decrypt);
						tv1.setText(target.getName());
						break;
					case Constants.TASK_GET_DEST_FOLDER:
						dest_folder = selected_file;
						TextView tv2 = (TextView)findViewById(R.id.decrypt_dest);
						tv2.setText(dest_folder.getPath());
						break;
				}
            }
		}else if (requestCode == Constants.TASK_MAKE_PATTERN){
			this.password = data.getStringExtra(LockPatternActivity._Pattern);
			Log.d(TAG,"Pattern: "+password);
			selectedInput(R.id.pattern_btn);
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
	 * @return
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
	
	private void selectedInput(int btn_id){
		Button patt_btn = (Button)findViewById(R.id.pattern_btn);
		Button pass_btn = (Button)findViewById(R.id.password_btn);
		Button selected = (Button)findViewById(btn_id);
		
		patt_btn.setBackgroundDrawable(default_button);
		pass_btn.setBackgroundDrawable(default_button);
		selected.setBackgroundResource(R.drawable.selected_button);
	}
	
	private void showGetLockPattern(){
		Intent intent = new Intent(this,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.GetPattern);
		startActivityForResult(intent, Constants.TASK_MAKE_PATTERN);
	}
	
	private void showGetPassword(){
		Log.d(TAG,"A");
		Context context = getApplicationContext();
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View pView = li.inflate(R.layout.prompt_password, null);
		
		final EditText inp = (EditText) pView.findViewById(R.id.input_password);
		
		new AlertDialog.Builder(this)
			.setView(pView)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DecryptSingleActivity.this.password = inp.getText().toString();
					selectedInput(R.id.password_btn);
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// do nothing
				}
			}).show();
	}
}
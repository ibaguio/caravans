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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.crypto.Utility;
import com.otfe.database.FileLoggerDataSource;
import com.otfe.database.FolderLoggerDataSource;

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
	private final String TAG = "New Folder Activity";
	private String pattern="";
	private FolderLoggerDataSource folder_ds;
	private Intent otfe_intent;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_encrypted_folder);
		folder_ds = new FolderLoggerDataSource(this);
	}
	
	/**
	 * Call intent to show browse folder
	 * @param view
	 */
	public void browseFolder(View view){
    	Intent intent = new Intent(this,FileChooserActivity.class);
    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Utility.SDCARD));
    	intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
    	startActivityForResult(intent, GET_FOLDERPATH);
	}
	
	/**
	 * Call intent to show pattern maker
	 * @param view
	 */
	public void showPattern(View view){
		Intent intent = new Intent(this,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		startActivityForResult(intent, MAKE_PATTERN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
		    case GET_FOLDERPATH:
		        if (resultCode == RESULT_OK) {
		            List<LocalFile> files = (List<LocalFile>) 
		            		data.getSerializableExtra(FileChooserActivity._Results);
		            for (File f : files){
		            	TextView tv_folder = (TextView) findViewById(R.id.text_folder_address);
		        		tv_folder.setText(f.getPath());
		            }
		        }
		        break;
		    case MAKE_PATTERN:
		    	TextView tv = (TextView)this.findViewById(R.id.tv_pattern);
		    	if (resultCode == RESULT_OK) {
		    		pattern = data.getStringExtra(LockPatternActivity._Pattern);
		    		tv.setText("Pattern is SET!");
		    	}else if (resultCode == RESULT_CANCELED && pattern=="")
		    		tv.setText("Pattern is NOT SET!");
		    	break;
	    }
	}
	
	/**
	 * method to be called when button
	 * create new folder is clicked
	 * @param view
	 */
	public int createNewFolder(View view){
		TextView tv_dir = ((TextView) findViewById(R.id.text_folder_address));
		String dir_name = tv_dir.getText().toString();
		
		File dir = new File(dir_name);
		/* check if directory is valid*/
		if (!dir.isDirectory() || !dir.canWrite()){
			Toast.makeText(this, "Not a valid folder", Toast.LENGTH_SHORT).show();
			Log.d(TAG,"Not a valid folder");
			return 0;
		}
		
		/* final password to use */
		String fPass = getFinalPassword();
		Log.d(TAG, "PASSWORD: *"+fPass+"*");
		
		/* check if folder is already in database */
		if (!this.folder_ds.isNewFolder(dir_name)){
			Toast.makeText(this, "The selected folder is already target for on the fly encryption", Toast.LENGTH_LONG).show();
			tv_dir.setText("");
			return 0;
		}
		/* password/pattern verification */
		else if (fPass==null){
			Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
			/* clear the input fields */
			TextView tv = (TextView) findViewById(R.id.edit_password);
			tv.setText("");
			tv = (TextView) findViewById(R.id.edit_password2);
			tv.setText("");
			pattern=""; //reset pattern
			return 0;
		}else if (fPass.isEmpty()){
			Toast.makeText(this, "Please select a password or pattern", Toast.LENGTH_SHORT).show();
			return 0;
		}
		/* get the selected Radio Button Id */
		RadioGroup rg = (RadioGroup)this.findViewById(R.id.radioGroup_algo_nef);
		int algo = rg.getCheckedRadioButtonId();
		
		/* set the final algorithm to be used for enc/dec */
		String algorithm = "";
		if (algo == R.id.radio_aes_nef)
			algorithm = Utility.AES;
		else if (algo ==R.id.radio_two_fish_nef)
			algorithm = Utility.TWO_FISH;
		else if (algo == R.id.radio_serpent_nef)
			algorithm = Utility.SERPENT;
		else{
			Toast.makeText(this, "Please select an Algorithm to use", Toast.LENGTH_SHORT).show();
			return 0;
		}
			
		Log.d(TAG,"Password: "+fPass);
		Log.d(TAG,"Folder: "+dir_name);
		Log.d(TAG,"Algo: "+algorithm);
			
		Toast.makeText(this, "Created new OnTheFly Folder", Toast.LENGTH_SHORT).show();
		setupFolder(dir, algorithm);
		
		/* setup the intent */
		otfe_intent = new Intent(this, FolderListenerService.class);
		otfe_intent.putExtra(FolderListenerService.PASSWORD, fPass);
		otfe_intent.putExtra(FolderListenerService.TARGET, dir_name);
		otfe_intent.putExtra(FolderListenerService.ALGORITHM, algorithm);
		
		/* create dialog to prompt user to start encryption service now */
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Run encryption service on target folder now?")
			.setPositiveButton("Yes", startOtfeDialogListener)
			.setNegativeButton("No", startOtfeDialogListener)
			.setTitle("Run Encryption Now?").show();
		return 0;
	}
	
	/**
	 * returns the final string to be used as password
	 * returns empty string if no password has been set
	 * returns NULL if re-type passwords do not match
	 * @return
	 */
	private String getFinalPassword(){
		String password = ((TextView) findViewById(R.id.edit_password)).getText().toString();
		String password2 = ((TextView) findViewById(R.id.edit_password2)).getText().toString();
		
		Log.d(TAG,"PASS: *"+password+"*");
		Log.d(TAG,"PAS2: *"+password2+"*");
		if (password.isEmpty() && password2.isEmpty()){
			if (!this.pattern.isEmpty())
				return this.pattern;
			return ""; //no password set
		}else if (!password.isEmpty() && password.equals(password2))
			return password;
		
		return null; //passwords do no match
	}
	/**
	 * setups the SQL database and adds the new folder
	 * @param f
	 * @param algorithm
	 */
	private void setupFolder(File f, String algorithm){
		Log.d(TAG,"Setting up folder database");
		FileLoggerDataSource file_ds = new FileLoggerDataSource(this,f.getName());
		/* Adds the newly set up otfe folder to the FolderLog table */
		this.folder_ds.createFolderLog(f,algorithm);
		/* Creates a new table that lists the files in the folder */
		file_ds.open();
		/* close data sources */
		file_ds.close();
		Log.d(TAG,"Folder Info added to database");
	}

	/**
	 * shows the pattern layout or password layout depending on what the user
	 * selects
	 * @param view
	 */
	public void toggleVisibility(View view){
		findViewById(R.id.nef_key_options).setVisibility(View.INVISIBLE);
		switch(view.getId()){
			case R.id.btn_show_password:
				/* set password layout to visible*/
				findViewById(R.id.nef_password).setVisibility(View.VISIBLE);
				break;
			case R.id.btn_show_pattern:
				/* call intent to ask for pattern */
				showPattern(view);
				findViewById(R.id.nef_pattern).setVisibility(View.VISIBLE);
				break;
			case R.id.btn_switch_to_pass:
				/* switch from pattern to password */
				findViewById(R.id.nef_pattern).setVisibility(View.INVISIBLE);
				findViewById(R.id.nef_password).setVisibility(View.VISIBLE);
				this.pattern="";
				break;
		}
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
	        	/* Start Folder Service Listener */ 
	    		startService(otfe_intent);
	            break;
	        case DialogInterface.BUTTON_NEGATIVE:
	            break;
	        }
			dialog.dismiss();
			finish();
		}
	};
	
	/* close folder data source when stopped */
	protected void onDestroy(){
		super.onStop();
		this.folder_ds.close();
	}
	/* open folder data source when started */
	protected void onStart(){
		super.onStart();
		this.folder_ds.open();
	}
}
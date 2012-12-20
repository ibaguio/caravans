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
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.crypto.Decryptor;
import com.otfe.crypto.Utility;
import com.otfe.database.FileLogHelper;

/* Activity that starts a decryption service*/
public class DecryptSingleActivity extends Activity{
	private final String TAG = "DecryptSingle";
	private final int GET_DECRYPT_TARGET = 0;
	private final int GET_DEST_FOLDER = 1;
	private final int MAKE_PATTERN = 2;

	private String pattern;
	private String password;
	private File dest_folder;
	private File target;
	private SQLiteDatabase db;
	
	private RadioGroup rg1;
	private RadioGroup rg2;
	private int last_group = 0;
	private int algorithm;
	
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
	}

	public void onClick(View view){
		Intent intent;
		switch(view.getId()){
			/* select the file to be decrypted */
			case R.id.browse_for_decrypt_btn:
		    	intent = new Intent(this,FileChooserActivity.class);
		    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Utility.SDCARD));
		    	startActivityForResult(intent, GET_DECRYPT_TARGET);
		    	break;
		    
		    /* selects the destination folder for the decrypted file to be saved */
			case R.id.browse_for_dest_btn:
		    	intent = new Intent(this,FileChooserActivity.class);
		    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Utility.SDCARD));
		    	intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
		    	startActivityForResult(intent, GET_DEST_FOLDER);
		    	break;
		    
		    /* check if the inputs are valid then start a decryption service for the target file
		     * to destination folder */
			case R.id.single_decrypt_btn:
				/* get the password from input */
				password = ((TextView)this.findViewById(R.id.decrypt_password)).getText().toString();
				Log.d(TAG,"password: *"+password+"*");
				/*check if given inputs are valid */
				if (target==null || !target.isFile()){
					Toast.makeText(getApplicationContext(), "Invalid target file", Toast.LENGTH_SHORT).show();
					TextView tv = (TextView)this.findViewById(R.id.file_to_decrypt);
					tv.setText("");
					this.target = null;
					Log.d(TAG, "Target file invalid");
					break;
				}/*else if (!target.canRead()){
					Toast.makeText(getApplicationContext(), "Cannot Read file. Please check file permissions", Toast.LENGTH_SHORT).show();
					TextView tv = (TextView)this.findViewById(R.id.file_to_decrypt);
					tv.setText("");
					this.target = null;
					Log.d(TAG, "Cannot read target file");
					break;
				}*/else if (!dest_folder.isDirectory()){
					Toast.makeText(getApplicationContext(), "Invalid destination folder", Toast.LENGTH_SHORT).show();
					TextView tv = (TextView)this.findViewById(R.id.decrypt_dest);
					tv.setText("");
					this.dest_folder = null;
					Log.d(TAG, "Invalid Dest folder");
					break;
				}else if (password.equals("")){
					Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Invalid Password");
					break;
				}else{
					Log.d(TAG, "OK for single Decryption");
					/* get the algorithm from input*/
					String algo = "";		//algorithm to be used passed to the DecryptionService
					
					if (algorithm == R.id.radio_aes2)
						algo = Utility.AES;
					else if (algorithm == R.id.radio_two_fish2)
						algo = Utility.TWO_FISH;
					else if (algorithm == R.id.radio_serpent2)
						algo = Utility.SERPENT;
					else if (algorithm == R.id.radio_auto){
						/* auto determine algorithm to be used*/
						algo = verifyAlgorithm();
						if (algo.equals("")){
							Toast.makeText(getApplicationContext(),"Could not determine algorithm", Toast.LENGTH_SHORT).show();
							break;
						}
					}else{
						Toast.makeText(getApplicationContext(),"Error on selected algorithm", Toast.LENGTH_SHORT).show();
						break;
					}
					
					Toast.makeText(getApplicationContext(),"Algorithm: "+algo, Toast.LENGTH_SHORT).show();
					
					Log.d(TAG, "Will be using algo: *"+algo+"*");
					
					/* PASSWORD/ALGORITHM CHECKING*/
					Decryptor otfd = new Decryptor(password, 
							target.getAbsolutePath(), dest_folder.getAbsolutePath(), algo);
					
					if (otfd.correctPassword()){
						/* password/algorithm is correct, continue with decryption service */
						Toast.makeText(getApplicationContext(), "Password is correct", Toast.LENGTH_SHORT).show();
						intent = new Intent(this, DecryptionService.class);
						
						/* pass to intent necessary information to be used in the decryption */
						intent.putExtra(DecryptionService.SRC_FILEPATH, target.getAbsolutePath());
						intent.putExtra(DecryptionService.DEST_FILEPATH, dest_folder.getAbsolutePath());
						intent.putExtra(DecryptionService.PASSWORD, password);
						intent.putExtra(DecryptionService.ALGORITHM, algo);
						intent.putExtra(DecryptionService.CHECKSUM, getStoredChecksum());
						Log.d(TAG,"Starting Service");
						startService(intent);
						//db.close();
					}else
						Toast.makeText(getApplicationContext(), "Decryption Failed", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	/**
	 * called by the browse buttons
	 * @param view
	 */
	public void browseFolder(View view){
		Intent intent = new Intent(this,FileChooserActivity.class);
    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Utility.SDCARD));
    	
		switch (view.getId()){
			case R.id.browse_for_decrypt_btn:
				/* browse for files only */
		    	startActivityForResult(intent, GET_DECRYPT_TARGET);
		    	break;
			case R.id.browse_for_dest_btn:
				/* browse for destination folder */
				intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
				startActivityForResult(intent, GET_DEST_FOLDER);
				break;
		}
	}

	public void showPattern(View view){
		Intent intent = new Intent(this,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		startActivityForResult(intent, MAKE_PATTERN);
	}
	
	/**
	 * result from FileBrowser or LockPattern
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if ((requestCode == GET_DECRYPT_TARGET || requestCode==GET_DEST_FOLDER) && resultCode == RESULT_OK) {
			File selected_file=null;
			List<LocalFile> files = (List<LocalFile>) 
            		data.getSerializableExtra(FileChooserActivity._Results);
            for (File f : files)
            	selected_file = f;
			
            /* check if returned file is valid*/
            if (selected_file!=null){
				switch(requestCode){
					/* the returned file is the file to be decrypted */
					case GET_DECRYPT_TARGET:
						target = selected_file;
						if (!target.isFile())
							Log.d(TAG,"Target received is not a valid TARGET FILE");
						else
							Log.d(TAG,"Target received OK");
						
						/* set the textview text to the files name */
						TextView tv1 = (TextView)findViewById(R.id.file_to_decrypt);
						tv1.setText(target.getName());
						break;
					case GET_DEST_FOLDER:
						dest_folder = selected_file;
						if (!dest_folder.isDirectory())
							Log.d(TAG,"Dest Folder received is not a valid FOLDER");
						else
							Log.d(TAG,"Dest Folder received OK");
						TextView tv2 = (TextView)findViewById(R.id.decrypt_dest);
						tv2.setText(dest_folder.getPath());
						break;
				}
            }
		}else if (requestCode == MAKE_PATTERN){
	    	if (resultCode == RESULT_OK) {
	    		this.pattern = data.getStringExtra(LockPatternActivity._Pattern);
	    	}
		}
	}
	/**
	 * tries to find the encrypting algorithm
	 * that was used for to encrypt target
	 * @return
	 */
	private String verifyAlgorithm(){
		/* initialize db query to get the algorithm from target file*/
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
		String query = "SELECT checksum FROM folderlogger_"+target.getParentFile().getName()
				+" WHERE filename='"+target+"';";
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
}
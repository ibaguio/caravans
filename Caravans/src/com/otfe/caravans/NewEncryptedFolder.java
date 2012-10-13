package com.otfe.caravans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Creates an OTF encrypted folder to be monitored
 * by the app, any new file that is added in this
 * folder would be directly encrypted using the set
 * password and algorithm
 * @author ibaguio
 */
public class NewEncryptedFolder extends Activity{
	private Spinner list_algo;
	public static final int GET_FOLDERPATH = 0;//code
	private File new_folder;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_encrypted_folder);
		list_algo = (Spinner) findViewById(R.id.list_algorithms);
				
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.algorithms,android.R.layout.simple_spinner_dropdown_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		list_algo.setAdapter(adapter);
	}
	
	public void browseFolder(View view){
		Log.d("NewEncFolder","Browse clicked");
		Intent intent = new Intent(this, FileBrowser.class);
    	intent.putExtra(FileBrowser.FILEPATH, FileBrowser.EXTERNAL_STORAGE);
    	intent.putExtra(FileBrowser.GET_DIRECTORY, true);
    	startActivityForResult(intent, GET_FOLDERPATH);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d("NewEncFolder","Passing back data\nreq code: "+requestCode+" ret code: "+resultCode);
		String filepath = data.getStringExtra(FileBrowser.FILEPATH);
		Log.d("","filepath: "+filepath);
		TextView tv_folder = (TextView) findViewById(R.id.text_folder_address);
		tv_folder.setText(filepath);
		new_folder = new File(filepath);
	}
	
	/**
	 * method to be called when button
	 * create new folder is clicked 
	 * @param view
	 */
	public void createNewFolder(View view){
		String dir_name = ((TextView) findViewById(R.id.text_folder_address)).getText().toString();
		String password = ((TextView) findViewById(R.id.edit_password)).getText().toString();
		String algo = ((Spinner)findViewById(R.id.list_algorithms)).getSelectedItem().toString();
		String algorithm = OnTheFlyUtils.AES;//default is AES
		
		if (algo.equals("AES"))
			algorithm = OnTheFlyUtils.AES;
		else if (algo.equals("Two Fish"))
			algorithm = OnTheFlyUtils.TWO_FISH;
		else if (algo.equals("Serpent"))
			algorithm = OnTheFlyUtils.SERPENT;
		
		File dir = new File(dir_name);
		Log.d("NewEncFolder","Password: "+password);
		Log.d("NewEncFolder","Folder: "+dir_name);
		Log.d("NewEncFolder","Algo: "+algo);
		if (dir.isDirectory() && dir.canWrite()){
			Toast.makeText(this, "Created new OnTheFly Folder", Toast.LENGTH_SHORT).show();
			setupFolder(dir, algorithm);
			
			Intent intent = new Intent(this, FolderListenerDaemon.class);
			intent.putExtra(FolderListenerDaemon.PASSWORD, "ivandominic");
			intent.putExtra(FolderListenerDaemon.TARGET, dir_name);
			intent.putExtra(FolderListenerDaemon.ALGORITHM, algorithm);
			
			startService(intent);
		}else{
			Toast.makeText(this, "Not a valid folder", Toast.LENGTH_SHORT).show();
			Log.d("NewEncFolder","Not a valid folder");
			//request focus for browse here
		}
	}

	private void setupFolder(File f, String algorithm){
		FolderLoggerDataSource fld = new FolderLoggerDataSource(this);
		fld.open();
		fld.createFolderLog(new_folder,algorithm);
		fld.close();
		FileLoggerDataSource fld2 = new FileLoggerDataSource(this,f.getName());
		fld2.open();
		fld2.close();
	}
}
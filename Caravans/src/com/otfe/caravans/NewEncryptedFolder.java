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
	public static final int GET_FOLDERPATH = 0;//code
	public static final String FILEPATH = "FILEPATH";
	private File new_folder;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("NewEncFolder","oncreate");
		Spinner spinner = (Spinner) findViewById(R.id.list_algorithms);
		
		List<String> list = new ArrayList<String>();
		list.add("AES");
		list.add("Two Fish");
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_item, list);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		Log.d("NewEncFolder","Setting spinner");
		//spinner.setAdapter(adapter);
		Log.d("NewEncFolder","Spinner is set");
		setContentView(R.layout.new_encrypted_folder);
	}
	
	public void browseFolder(View view){
		Log.d("","Browse clicked");
		Intent intent = new Intent(this, FileBrowser.class);
		String f = Environment.getExternalStorageDirectory().toString();
    	intent.putExtra(FILEPATH, f);
    	intent.putExtra(FileBrowser.GET_DIRECTORY, true);
    	this.startActivityForResult(intent, GET_FOLDERPATH);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d("NewEncFolder","Passing back data\nreq code: "+requestCode+" ret code: "+resultCode);
		String filepath = data.getStringExtra("FILEPATH");
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
		File dir = new File(dir_name);
		Log.d("NewEncFolder","Password: "+password);
		Log.d("NewEncFolder","Folder: "+dir_name);
		if (dir.isDirectory() && dir.canWrite()){
			Toast.makeText(this, "Setting up new On The Fly Folder", Toast.LENGTH_SHORT).show();
			setupFolder(dir);
			FolderListenerDaemon fld = new FolderListenerDaemon();
		}else{
			Toast.makeText(this, "Not a valid folder", Toast.LENGTH_SHORT).show();
			Log.d("NewEncFolder","Not a valid folder");
			//request focus for browse here
		}
	}

	private void setupFolder(File f){
		FolderLoggerDataSource fld = new FolderLoggerDataSource(this);
		fld.open();
		//((Spinner)this.findViewById(R.id.list_algorithms)).toString()
		fld.createFolderLog(new_folder,"AES");
	}
}
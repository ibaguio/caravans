package com.otfe.caravans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.database.FolderLog;
import com.otfe.caravans.database.FolderLoggerDataSource;

public class ViewFoldersActivity extends ListActivity {
	private static final String TAG_ID = "id";
	private static final String TAG_FOLDER_NAME = "folder_name";
	private static final String TAG_FOLDER_PATH = "folder_path";
	private static final String TAG_LISTENEING = "listening";
	private static final String TAG = "ViewFoldersActivity";
	private FolderLoggerDataSource flds;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.show_folders);
		
		load();
		
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			 public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
				
				Toast.makeText(getApplicationContext(), "Click "+((TextView)view.findViewById(R.id.folder_name))
						.getText().toString(),Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void load(){
		Log.d(TAG,"Loading");
		flds = new FolderLoggerDataSource(getApplicationContext());
		flds.open();
		
		ArrayList<HashMap<String, String>> folderList 
			= new ArrayList<HashMap<String, String>>();
		
		for (FolderLog f : flds.getAllFolderLogs()){
			HashMap<String, String> map = new HashMap<String, String>();
			String listening = "ON";
			
			map.put(TAG_ID, String.valueOf(f.getId()));
			map.put(TAG_FOLDER_NAME, f.getFolderName());
			map.put(TAG_FOLDER_PATH, f.getPath());
			map.put(TAG_LISTENEING, listening);
			
			Log.d(TAG,"adding to map..");
			Log.d(TAG,f.toString());
			folderList.add(map);
		}
		Log.d(TAG,"creating adapter");
		
		ListAdapter adapter = new SimpleAdapter(ViewFoldersActivity.this, folderList,
                R.layout.list_folder_item, new String[] { TAG_ID,
                        TAG_FOLDER_NAME, TAG_FOLDER_PATH, TAG_LISTENEING}, new int[] {
                        R.id.folder_id, R.id.folder_name, R.id.folder_path, R.id.folder_encrypting});
		
		Log.d(TAG,"setting list adapter");
		setListAdapter(adapter);
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		flds.close();
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.btn_start_all:
				
				break;
			case R.id.btn_stop_all:
				
				break;
		}
	}
	
	private void startAll(){
		Toast.makeText(getApplicationContext(), "Initializing listeners to all folders..",
			Toast.LENGTH_SHORT).show();
		new Thread(){
			public void run(){
				for (FolderLog f : flds.getAllFolderLogs()){
					
				}
			}
		}.run();
	}
}
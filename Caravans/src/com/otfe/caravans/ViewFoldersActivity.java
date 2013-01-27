package com.otfe.caravans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.database.FileLog;
import com.otfe.caravans.database.FolderLog;
import com.otfe.caravans.database.FolderLoggerDataSource;

public class ViewFoldersActivity extends ListActivity {
	private static final String TAG = "ViewFoldersActivity";
	private FolderLoggerDataSource flds;
	private FolderObserverService fos;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(TAG, "On CREATE");
		flds = new FolderLoggerDataSource(getApplicationContext());
		flds.open();
		doBindService();
		load();
		
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			 public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
				Log.d(TAG,"Click on lv");
				int _id = Integer.valueOf(((TextView) view.findViewById(R.id.folder_id)).getText().toString());
				FolderLog fLog = flds.getFolderLog(_id);
				Intent intent = new Intent(ViewFoldersActivity.this, EncFolderActivity.class);
				intent.putExtra(Constants.KEY_ROW_ID, _id);
				intent.putExtra(Constants.KEY_FOLDER, fLog.getFolderName());
				intent.putExtra(Constants.KEY_FILE_PATH, fLog.getPath());
				intent.putExtra(Constants.KEY_ALGORITHM, fLog.getAlgorithm());
				Log.d(TAG, "Starting activity");
				startActivity(intent);
			}
		});
	}
	
	private void load(){
		// TODO: refactor to use async task
		Log.d(TAG,"Loading");
		
		List<FolderLog> list_fLog = null;
		try{
			list_fLog = flds.getAllFolderLogs();
		}catch(Exception e){
			flds.open();
			load();
			return;
		}
		ArrayList<HashMap<String, String>> folderList 
			= new ArrayList<HashMap<String, String>>();
		if (list_fLog.size()== 0 || list_fLog == null ){
			setContentView(R.layout.show_no_folders);
			return;
		}
		setContentView(R.layout.show_folders);
		for (FolderLog fLog : list_fLog){
			HashMap<String, String> map = new HashMap<String, String>();
			Log.d(TAG,"checking if ID:"+fLog.getId()+" is being observed");
			String listening = (fos!=null && fos.isObserved((int)fLog.getId()))?  "ON": "OFF";
			
			map.put(Constants.KEY_ROW_ID, String.valueOf(fLog.getId()));
			map.put(Constants.KEY_FOLDER, fLog.getFolderName());
			map.put(Constants.KEY_FILE_PATH, fLog.getPath());
			map.put(Constants.KEY_LISTENING, listening);
			
			Log.d(TAG,"adding to map..");
			Log.d(TAG,fLog.toString());
			folderList.add(map);
		}
		Log.d(TAG,"creating adapter");
		
		ListAdapter adapter = new SimpleAdapter(ViewFoldersActivity.this, folderList,
                R.layout.list_folder_item, new String[] { Constants.KEY_ROW_ID,
					Constants.KEY_FOLDER, Constants.KEY_FILE_PATH, Constants.KEY_LISTENING}, new int[] {
                        R.id.folder_id, R.id.folder_name, R.id.folder_path, R.id.folder_encrypting});
		
		Log.d(TAG,"setting list adapter");
		setListAdapter(adapter);
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.btn_refresh:
				load();
				break;
			case R.id.btn_stop_all:
				if (fos!=null){
					Toast.makeText(this, "Stopping Enncryption Service", Toast.LENGTH_SHORT).show();
					fos.stopAll();
					try{Thread.sleep(750);}catch(Exception e){}
					load();
				}else{
					Toast.makeText(this, "No Encryption Service running", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btn_create_enc_folder:
				Log.d(TAG, "Opening new enc folder");
				Intent intent = new Intent(this, NewFolderActivity.class);
				startActivity(intent);
				break;
		}
	}
	
	private void doBindService(){
		Log.d(TAG,"Binding service..");
		bindService(new Intent(this, FolderObserverService.class), 
				mConnection, Context.BIND_NOT_FOREGROUND);
	}
	
	private ServiceConnection mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder){
			fos = ((FolderObserverService.LocalBinder) binder).getService();
			Log.d(TAG, "Connected to fos");
			load();
		}
		@Override
		public void onServiceDisconnected(ComponentName className){
			Log.d(TAG,"Disconnected to fos");
			fos = null;
		}
	};

	@Override
	public void onPause(){
		super.onPause();
		Log.d(TAG,"PAUSE");
		flds.close();
	}
	
	@Override 
	public void onResume(){
		super.onResume();
		Log.d(TAG,"RESUME");
		flds.open();
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG,"DESTROY");
		unbindService(mConnection);
	}
}
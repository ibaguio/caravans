package com.otfe.caravans;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServiceActivity extends Activity{
	private ListView lv;
	private Activity activity = null;
	private List<FolderLog> enc_dirs;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		try{
		Log.d("ServiceActivity","Creating service activity");
		enc_dirs = getEncryptedDirs();
		List<String> str_dirs = getStringEncDirs();
		
		setContentView(R.layout.file_browser);
		lv = (ListView)findViewById(R.id.list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, str_dirs);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {
				Log.d("ServiceActivity","Click");
				activity.openContextMenu(view);
			}
			
		});
		}catch(Exception e){
			Log.d("ServiceActivity","Error: "+e.toString());
			e.printStackTrace();
		}
	}
	
	private List <FolderLog> getEncryptedDirs(){
		Log.d("ServiceActivity","Getting enc dirs");
		FolderLoggerDataSource fld = new FolderLoggerDataSource(this);
		fld.open();		
		List<FolderLog> folders = fld.getAllFolderLogs();
		return folders;
	}
	
	private List<String> getStringEncDirs(){
		Log.d("ServiceActivity","Getting string dirs");
		
		ListIterator<FolderLog> it = enc_dirs.listIterator();
		List<String> dirs = new ArrayList<String>();
		while(true){
			try{
				FolderLog folder = it.next();
				Log.d("","Loop: "+folder.getFolderName());
				dirs.add(folder.getFolderName());
			}catch(NoSuchElementException e){break;}
		}
		/*String[] d = new String[dirs.size()];
		for(int i=0;i<dirs.size();i++)
			d[i] = dirs.get(i);
		Log.d("","Returning dirs,count: "+dirs.size());*/
		return dirs;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, view, menuInfo);
		Log.d("SeviceActivity","Context menu");
	}
}

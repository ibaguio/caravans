package com.otfe.caravans;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowser extends Activity{
	public static final String GET_DIRECTORY = "GET_DIR";
	
	private ListView lv;
	private String dir;
	private boolean returnDirectory;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("","Opening File browser");
        setContentView(R.layout.file_browser);
        lv = (ListView)findViewById(R.id.list);
        
        Intent intent = getIntent();
        this.dir = intent.getStringExtra(NewEncryptedFolder.FILEPATH);
        this.returnDirectory = intent.getBooleanExtra(GET_DIRECTORY,false);
        
        if (checkExternalMedia()>0){
        	Log.d("Filebrowser","showing sd");
        	showSD();
        }
	}
	
	public void makeDirectory(View view){
		
	}
	private int checkExternalMedia(){
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            return 2;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            return 1;
        } else {
            // Can't read or write
            return 0;
        }   
    }
	
	private void showSD(){
		String dirs[] = getSubFiles(this.dir);
		lv = (ListView)findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, dirs);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        
        lv.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
            	Log.d("","Click");
            	String filename = String.valueOf( ((TextView) view).getText());

                String f = dir +"/"+ filename;
                Log.d("","Filename: "+f);
                
                if (isDirectory(f)){//open directory
                	Intent intent2 = new Intent(FileBrowser.this ,FileBrowser.class);
                    intent2.putExtra(NewEncryptedFolder.FILEPATH, f);
                    intent2.putExtra(GET_DIRECTORY, returnDirectory);
                	startActivity(intent2);	
                }else{
                	if (!returnDirectory){//return nondir filepath
                		Intent data = new Intent();
                    	data.putExtra(NewEncryptedFolder.FILEPATH, f);
                    	returnData(data);	
                	}
                }
            }
        });
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
        	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id){
        		String filename = String.valueOf( ((TextView) view).getText());
        		String f = dir +"/"+ filename;
        		Log.d("","Loooong Click "+filename+"*");
        		
        		File file = new File(f);
        		if (file.isDirectory() && returnDirectory){
        			Intent data = new Intent();
                	data.putExtra("FILEPATH", f);
                	returnData(data);
                	return true;
        		}
        		return false;
        	}
        });
	}
	
	private void returnData(Intent data){
		if (getParent()==null){
    		Log.d("File browser","parent is null");
    		setResult(Activity.RESULT_OK, data);
    	}else{
    		Log.d("File browser","parent is not null");
    		getParent().setResult(Activity.RESULT_OK, data);
    	}
		finish();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.d("File BROWSER","Passing back data\nreq code: "+requestCode+" ret code: "+resultCode);
		returnData(data);
    	
	}
	
    private String[] getSubFiles(String filename){
    	try{
    		File f = new File(filename);
    		Log.d("Subdir","Getting subdir for "+filename+" len: "+f.list().length);
    		
    		if (returnDirectory){//show only directories
    			FilenameFilter filter = new FilenameFilter(){
    				public boolean accept(File file,String name){
    					return file.isDirectory();
    				}
    			};
    			if (f.isDirectory())
    				return f.list(filter);
    		}else if (f.isDirectory())
    			return f.list();
    	}catch(Exception e){
    	}
    	Log.d("Err subdir","Returned null");
    	return new String[0];
    }
    
    private boolean isDirectory(String filename){
    	try{
    		return (new File(filename)).isDirectory();
    	}catch(Exception e){
    		Log.d("File Error", e.getMessage());
    	}
    	return false;
    }
}

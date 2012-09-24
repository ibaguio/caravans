package com.example.thismustwork;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListOfFiles extends Activity{
    
    private String dir = "";
    private String dirs[];
    private ListView lv;
    private String choices[] = {"Open","Delete","Encrypt","Decrypt","Cancel"};
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_files);
        Intent intent = getIntent();
        String filename = intent.getStringExtra("FILENAME");
        this.dir = filename;
        dirs = getDirs(filename);
        //setListAdapter(new ArrayAdapter<String>(this, R.layout.view_card, dirs));
        
        lv = (ListView)findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, dirs);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        
        lv.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
            	Log.d("","Click");
            	String filename = String.valueOf( ((TextView) view).getText());
                
                Intent intent2 = new Intent(ListOfFiles.this ,ListOfFiles.class);
                String f = dir +"/"+ filename;
                Log.d("","Filename: "+f);
                if (isDirectory(f)){
                	boolean ok = true;
                	try{
                		if (new File(f).listFiles().length < 1)
                			ok = false;
                	}catch(Exception e){
                		ok = false;
                		Log.d("File Err",e.getMessage());
                	}
                	if (ok){
                		intent2.putExtra("FILENAME", f);
                		startActivity(intent2);
                	}else{
                		Log.d("Not OK","NOT OK");
                	}
                }else{
                	String msg = filename+ ": Not a directory";
                	Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        /*lv.setOnItemLongClickListener(new OnItemLongClickListener(){
        	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id){
        		String filename = String.valueOf( ((TextView) view).getText());
        		Log.d("","Loooong Click "+filename+"*");
        		Toast.makeText(getApplicationContext(), "Long Click",Toast.LENGTH_SHORT).show();
        		subdirs = getSubDirs(dir);
        		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ListOfFiles.this, R.layout.list_item, subdirs);
        		lv.setAdapter(adapter);
        		ListOfFiles.this.registerForContextMenu(lv);
        		Log.d("","Registered for Context Menu");
        		return true;
        	}
        });*/
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
    	Log.d("","Creating Menu");
    	try{
	    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	        menu.setHeaderTitle(dirs[info.position]);
	        for (int i = 0; i<choices.length; i++) {
	          menu.add(Menu.NONE, i, i, choices[i]);
	        }
    	}catch(Exception e){
    		Log.d("Err CM","err "+ e.getMessage());
    		Log.d("Err CM","err "+e.toString());
    	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      int mindex = item.getItemId();
      String filez = dirs[info.position];
      if (mindex == 4)
    	  return false;
      else if (mindex == 0){
    	  Intent intent = new Intent(ListOfFiles.this, ListOfFiles.class);
    	  intent.putExtra("FILENAME", dir+"/"+filez);
    	  startActivity(intent);
      }else if(mindex == 1){
    	  //delete
    	  File del = new File(dir+"/"+filez);
    	  if (del.isFile()){
    		  del.delete();
    	  }
      }else if(mindex == 2){
    	  //encrypt
    	  Log.d("","Encrypting "+filez);
    	  TestEncryptor te = new TestEncryptor(0,new File(dir+"/"+filez),"ivandominic");//hardcoded password
    	  te.doIt();
      }else if(mindex == 3){
    	  //decrypt
    	  Log.d("","Decrypting "+filez);
    	  TestEncryptor te = new TestEncryptor(1,new File(dir+"/"+filez),"ivandominic");//hardcoded password
    	  te.doIt();
      }
      Log.d("","DONE IT");
      return true;
    }
    
    private boolean isDirectory(String filename){
    	try{
    		File f = new File(filename);
    		return f.isDirectory();
    	}catch(Exception e){
    		Log.d("File Error", e.getMessage());
    	}
    	return false;
    }
    
    public String[] getSubDirs(String filename){
    	Log.d("Subdir","Getting subdir for "+filename);
    	try{
    		File f = new File(filename);
    		if (f.isDirectory())
    			return f.list();
    	}catch(Exception e){
    		Log.d("Err subdir","err in subdir");
    	}
    	Log.d("Err subdir","Returned null");
    	return null;
    }
    
    public String[] getDirs(String filename){
        String r[]=null;
        try{
        	File f = new File(filename);
        	if (f.isDirectory())
        		r = f.list();
        	Log.d("", "Getting dirs bitches: " + filename);
        }catch(Exception e){
        }
        return r;
    }
}

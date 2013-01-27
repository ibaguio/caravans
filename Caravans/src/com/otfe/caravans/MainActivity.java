package com.otfe.caravans;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.otfe.caravans.performance_test.PerformanceTestService;

/* Main activity for Caravans*/
public class MainActivity extends Activity {
	private static final String TAG = "Main";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    /** No Menu Yet
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }*/
    
    public void onClick(View view){
    	switch (view.getId()) {
	        case R.id.new_enc_folder_btn:
	        	newFolder();
	        	break;
	        case R.id.encrypt_btn:
	        	encryptSingle();
	        	break;
	        case R.id.decrypt_btn:
	        	decryptSingle();
    			break;
	        /*case R.id.help_btn:
	        	showHelp();
    			break;*/
	        case R.id.settings_btn:
	        	showSettings();
	        	break;
	        case R.id.folders_btn:
	        	showFolders();
	        	break;
        }
    }
    
    private void showFolders(){
    	Log.d(TAG, "Showing folders");
    	Intent intent = new Intent(this, ViewFoldersActivity.class);
    	startActivity(intent);
    }
    
    private void encryptSingle(){
    	Log.d(TAG,"Encrypting Single file");
    	Intent intent = new Intent(this, EncryptSingleActivity.class);
    	startActivity(intent);
    }
    private void decryptSingle(){
    	Log.d(TAG,"Decrypting Single file");
    	Intent intent = new Intent(this,DecryptSingleActivity.class);
    	startActivity(intent);
    }
    private void showHelp(){
    	Log.d(TAG,"Performance testing");
    	Intent intent = new Intent(this, PerformanceTestService.class);
    	startService(intent);
    }
    
    private void showSettings(){
    	Log.d(TAG,"Show settings");
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    }
    
    private void startService(){
    	
    }
    
    private void newFolder(){
    	Log.d(TAG,"New Folder");
    	Intent intent = new Intent(MainActivity.this,NewFolderActivity.class);
    	startActivity(intent);
    }
}
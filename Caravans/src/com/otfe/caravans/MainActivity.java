package com.otfe.caravans;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
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
	        case R.id.help_btn:
	        	showHelp();
    			break;
        }
    }
    
    private void encryptSingle(){
    	Log.d("Main","Encrypting Single file");
    }
    private void decryptSingle(){
    	Log.d("Main","Decrypting Single file");
    }
    private void showHelp(){
    	Log.d("Main","Show Help");
    }
    
    private void newFolder(){
    	Log.d("Main","New Folder");
    	Intent intent = new Intent(MainActivity.this,NewEncryptedFolder.class);
    	startActivity(intent);
    }
}

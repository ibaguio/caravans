package com.example.thismustwork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.thismustwork.MESSAGE";
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
    
    public void getFiles(View view){
    	Intent intent = new Intent(this, ViewSDCard.class);
    	startActivity(intent);
    }
    
    public void showFiles(View view){
    	Intent intent = new Intent(this, ListOfFiles.class);
    	String root = Environment.getExternalStorageDirectory().toString();
    	intent.putExtra("FILENAME", root);
    	startActivity(intent);
    }
}

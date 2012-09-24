package com.example.thismustwork;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

public class ViewSDCard extends Activity{
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		tv.setTextSize(12);
		tv.setText(this.getSDFiles());
		
		setContentView(tv);
	}
	
	private String getSDFiles(){
		String ret = "root: "+Environment.getExternalStorageDirectory().toString();
		File f = Environment.getExternalStorageDirectory();
        if (f.isDirectory())
            ret += "\n"+getSubFiles(f,"");
        return ret;
	}
	/* returns a list of sub files*/
	private String getSubFiles(File f,String indent){
		String ret="";
		if (f.isDirectory()){
			//	ret += indent + f.getName();
			for (File ff: f.listFiles()){
				ret += "\n" +indent + ff.getName();
				if (ff.isDirectory()){
					try{
						if (ff.listFiles().length > 0)
							ret += getSubFiles(ff,indent+"   ");
					}catch(Exception e){
					}
				}
            }
		}
		return ret;
	}
}

package com.otfe.caravans.performance_test;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.Constants;
import com.otfe.caravans.R;
import com.otfe.caravans.crypto.CryptoUtility;

public class PerformanceTestActivity extends Activity{	
	private static final String TAG = "Performance Activity";
	
	private File dest_folder;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(TAG,"Starting");
		setContentView(R.layout.performance_tester);
	}

	public int runPerfTest(View view){
		EditText et_fSize = (EditText)this.findViewById(R.id.file_size);
		TextView et_tCount = (EditText)this.findViewById(R.id.test_count);
		Spinner size = (Spinner)this.findViewById(R.id.size_spinner);
		
		/* check if given destination folder is valid */
		if (!dest_folder.isDirectory()){
			Toast.makeText(getApplicationContext(), "Invalid destination folder", Toast.LENGTH_SHORT).show();
			return 0;
		}
		
		/* calculate size of file to be encrypted/decrypted */
		int fSize = Integer.parseInt(et_fSize.getText().toString());
		int tcount = Integer.parseInt(et_tCount.getText().toString());
		
		if (fSize < 1){
			Toast.makeText(getApplicationContext(), "Invalid file size", Toast.LENGTH_SHORT).show();
			return 0;
		}else if (tcount < 1){
			Toast.makeText(getApplicationContext(), "Invalid test counts", Toast.LENGTH_SHORT).show();
			return 0;
		}
		
		/* calculate file size */
		String b = size.getSelectedItem().toString();
		double multiplier = 1024;
		if (b.equals("MB")) multiplier = Math.pow(multiplier, 2);
		long final_size = (long)(fSize * multiplier);
		
		/* create hidden test file */
		String fname = ".test_"+CryptoUtility.getDate("yy-MM-dd_HH-mm")+".txt";
		File targ_file = new File(dest_folder,fname);
		Log.d(TAG,"Creating test file");
		try{
			targ_file.createNewFile(); //creates the file
			new RandomAccessFile(targ_file,"rw").setLength(final_size); //adds random bytes to the file
		}catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"Error in creating test file");
			Toast.makeText(getApplicationContext(), "Error in creating test file", Toast.LENGTH_SHORT).show();
			return 0;
		}
		/* run performance test service */
		String toast_msg = "Size: "+fSize+" "+b+"("+final_size+")\nTests: "+tcount;
		Log.d(TAG, "File created, size: "+final_size);
		Toast.makeText(getApplicationContext(), toast_msg , Toast.LENGTH_LONG).show();
		Intent perf_intent = new Intent(this,PerformanceTestService.class);
		perf_intent.putExtra(Constants.KEY_TARG_FILE, targ_file.getAbsolutePath());
		perf_intent.putExtra(Constants.KEY_FILE_SIZE, fSize+" " +b);
		perf_intent.putExtra(Constants.KEY_TEST_COUNT, tcount);
		Log.d(TAG,"starting service");
		startService(perf_intent);
		finish();
		return 0;
	}
	
	/**
	 * browse target folder
	 * @param view
	 */
	public void browseFolder(View view){
		Intent intent = new Intent(this,FileChooserActivity.class);
    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Constants.SDCARD));
    	
		switch (view.getId()){
			case R.id.browse_for_dest_btn:
				/* browse for destination folder */
				intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
				startActivityForResult(intent, Constants.TASK_GET_DEST_FOLDER);
				break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_OK && requestCode==Constants.TASK_GET_DEST_FOLDER) {
			File selected_file=null;
			List<LocalFile> files = (List<LocalFile>) 
            		data.getSerializableExtra(FileChooserActivity._Results);
            for (File f : files)
            	selected_file = f;

            if (selected_file!=null){
            	dest_folder = selected_file;//new File(OnTheFlyUtils.pathRemoveMNT(selected_file.getAbsolutePath()));
				TextView tv2 = (TextView)findViewById(R.id.performance_test_dest);
				tv2.setText(dest_folder.getPath());
            }
		}
	}
}
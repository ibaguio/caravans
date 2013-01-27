package com.otfe.caravans;

import group.pals.android.lib.ui.lockpattern.LockPatternActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.otfe.caravans.database.FolderLog;
import com.otfe.caravans.database.FolderLoggerDataSource;

public class EncFolderActivity extends Activity{
	private static final String TAG = "EncFolderActivity";
	private static final int STOP = 0;
	private static final int START = 1;
	
	private int _id;
	private String folderName;
	private String path;
	private String algorithm;
	private String passkey="";
	private boolean isStop;
	
	private FolderObserverService fos;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(TAG,"On create");
		doBindService();
		setContentView(R.layout.encrypted_folder);
		
		setTo(START);
		Bundle extras = getIntent().getExtras();
		_id = extras.getInt(Constants.KEY_ROW_ID,-1);
        algorithm = extras.getString(Constants.KEY_ALGORITHM);
        folderName = extras.getString(Constants.KEY_FOLDER);
        path = extras.getString(Constants.KEY_FILE_PATH);
		
        TextView tv_id = (TextView) findViewById(R.id.folder_id);
        TextView tv_name = (TextView) findViewById(R.id.folder_name);
        TextView tv_path = (TextView) findViewById(R.id.folder_path);
        TextView tv_algo = (TextView) findViewById(R.id.folder_algorithm);
        
        /* disply values */
        tv_id.setText(""+_id);
        tv_name.setText(folderName);
        tv_path.setText(path);
        tv_algo.setText(algorithm);
        
        Log.d(TAG, "ID: "+tv_id.getText().toString());
        Log.d(TAG, "NAME: "+tv_name.getText().toString());
        Log.d(TAG, "PATH: "+tv_path.getText().toString());
        Log.d(TAG, "ALGO: "+tv_algo.getText().toString());
	}
	
	public void onClick(View view){
		switch(view.getId()){
			case R.id.observe_toggle:
				startObserving();
				break;
		}
	}
	
	private void startObserving(){
		if (!isStop){
			if (passkey.equals("")){
                Log.d(TAG,"Passkey none, will prompt user");
                FolderLoggerDataSource flds = new FolderLoggerDataSource(getApplicationContext());
                flds.open();
                FolderLog fl = flds.getFolderLog(_id);
                flds.close();
                if (fl.isPattern())
                    showGetLockPattern();
                else
                    showGetPassword();
                return;
            }
            
            Log.d(TAG, "Creating bundle for obersrver service");
            Bundle extras = new Bundle();
            extras.putString(Constants.KEY_PASSWORD, passkey);
            extras.putString(Constants.KEY_TARG_FILE, path);
            extras.putString(Constants.KEY_ALGORITHM, algorithm);
            extras.putInt(Constants.KEY_ROW_ID, _id);

            if (fos == null){ //No service is active, start new service
                Log.d(TAG,"No running service yet, will create new");
                FolderObserverService.startObserving(getApplicationContext(), extras);
            }else{ //a service is active, add target folder to list of observed paths
                Log.d(TAG,"Observer Instance already running, will add target");
                fos.addTarget(extras);
            }
			setTo(START);
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(EncFolderActivity.this);
            builder.setMessage("Confirm Encryption Stopage")
                .setPositiveButton("Yes", stopServiceDialogListener)
                .setNegativeButton("Cancel", stopServiceDialogListener)
                .setTitle("Stop Encryption for "+folderName+"?").show();
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
			setTo(STOP);
		}
		@Override
		public void onServiceDisconnected(ComponentName className){
			Log.d(TAG,"Disconnected to fos");
			setTo(START);
			fos = null;
		}
	};
	
	/* dialog listener for stopping service */
	DialogInterface.OnClickListener stopServiceDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	        	Toast t; 
	        	if (fos!=null){
	        		fos.stopObserving(_id);
	        		t = Toast.makeText(getApplicationContext(), "Stopping Encryption Service for "+folderName, 
		        			Toast.LENGTH_SHORT);
	        		setTo(STOP);
	        	}else
	        		t = Toast.makeText(getApplicationContext(), "No Service to stop", 
		        			Toast.LENGTH_SHORT);
	        	t.show();
	            break;
	        case DialogInterface.BUTTON_NEGATIVE:
	            break;
	        }
			dialog.dismiss();
		}
	};
	/* INPUT METHODS FOR STARTING SERVICE */
	private void showGetPassword(){
		Context context = getApplicationContext();
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View pView = li.inflate(R.layout.prompt_password, null);
		
		final EditText inp = (EditText) pView.findViewById(R.id.input_password);
		
		new AlertDialog.Builder(this)
			.setView(pView)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					passkey = inp.getText().toString();
					startObserving();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			}).show();
	}
	
	private void showGetLockPattern(){
		Intent intent = new Intent(this,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.GetPattern);
		startActivityForResult(intent, Constants.TASK_MAKE_PATTERN);
	}

	/**
	 * result from FileBrowser or LockPattern
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != RESULT_OK) return;
		if (requestCode == Constants.TASK_MAKE_PATTERN){
			passkey = data.getStringExtra(LockPatternActivity._Pattern);
			Log.d(TAG,"Pattern: "+passkey);
			startObserving();
		}
	}
		
	/* Manual toggle of button*/
	private void setTo(int b){
		isStop = (b==STOP);
		Button btn = (Button) findViewById(R.id.observe_toggle);
		switch(b){
			case STOP:
				btn.setText("Stop");
				btn.setBackgroundResource(R.drawable.stop_button);
				break;
			case START:
				btn.setText("Start");
				btn.setBackgroundResource(R.drawable.start_button);
				break;
		}
	}
}
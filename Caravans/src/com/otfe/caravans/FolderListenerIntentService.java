package com.otfe.caravans;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.otfe.caravans.crypto.Utility;
import com.otfe.caravans.database.FileLog;
import com.otfe.caravans.database.FileLoggerDataSource;

public class FolderListenerIntentService extends IntentService{
	private static final String TAG = "FolderListener IS";
	
	private static SharedPreferences preferences;
	private static int preference_interval;
	private static boolean preference_power_saving;
	
	private File target;
	private FileLoggerDataSource fld;
	private String password;
	private String algorithm;
	
	public FolderListenerIntentService(){
		super(FolderListenerIntentService.class.getName());
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		int operation = intent.getIntExtra(Constants.EXTRA_SERVICE_TASK, -1);
		switch(operation){
			case Constants.TASK_FOLDER_LISTEN:
				loadExtras(intent.getExtras());
				checkChanges();
				break;
		}
	}
	
	private static void loadPreferences(Context context){
		preferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Constants.SETTINGS_MODE);
		preference_interval = Integer.parseInt(
				preferences.getString(context.getString(R.string.preferences_key_encrypt_interval), 
				Constants.SETTINGS_DEFAULT_ENCRYPT_INTERVAL));
		preference_power_saving = preferences.getBoolean(
				context.getString(R.string.preferences_key_power_saving), 
				Constants.SETTINGS_DEFAULT_POWER_SAVING);
	}
	
	private void loadExtras(Bundle extras){
		this.password = extras.getString(Constants.KEY_PASSWORD);
		this.algorithm = extras.getString(Constants.KEY_ALGORITHM);
		this.target = new File(extras.getString(Constants.KEY_TARG_FILE));
	}
	
	private static boolean verifyExtras(Bundle extras){
		if (extras == null) return false;
		String password = extras.getString(Constants.KEY_PASSWORD);
		String algorithm = extras.getString(Constants.KEY_ALGORITHM);
		File target = new File(extras.getString(Constants.KEY_TARG_FILE));
		if (password.equals("") || password == null ||
				!Constants.ALGORITHMS.contains(algorithm) ||
				!target.isFile())
			return false;
		return true;
	}
	
	public static void startListening(Context context, Bundle extras){
		scheduleFolderListener(context, extras, false);
	}
	
	public static void stopListening(Context context){
		scheduleFolderListener(context, null, true);
	}
	
	private static boolean scheduleFolderListener(Context context, Bundle extras, boolean stop){
		if (!verifyExtras(extras)) return false;
		final Intent intent = new Intent(context, FolderListenerIntentService.class);
		intent.putExtra(Constants.EXTRA_SERVICE_TASK, Constants.TASK_FOLDER_LISTEN);
		intent.putExtras(extras);
		
		final PendingIntent pending = PendingIntent.getService(context, 1, intent, 0);
		final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		
		final Calendar c = new GregorianCalendar();
		c.add(Calendar.SECOND, preference_interval);
		
		if (preference_power_saving){
			// set alarm to go only when phone is awake
			alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), preference_interval , pending);	
		}else{
			/* wakeup phone when asleep, force encryption
			  use only this when downloading, or another app is writing to target folder */
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					c.getTimeInMillis(),preference_interval, pending);
		}
		return true;
	}
	
	private void checkChanges(){
    	File files[] = target.listFiles();
    	FileLog fileLog;
    	for (File f: files){
    		fileLog = fld.getFileLog(Utility.getRawFileName(f));
    		if (fileLog == null){
    			/* log the new file to database */
    			fld.createFileLog(f);
    			
    			Log.d(TAG,"Starting EncryptionIntentService");
    			Intent intent = new Intent(this, EncryptionIntentService.class);
    			intent.putExtra(Constants.KEY_PASSWORD, password);
    			intent.putExtra(Constants.KEY_ALGORITHM, algorithm);
    			intent.putExtra(Constants.KEY_TARG_FILE, f.getAbsolutePath());
    			startService(intent);
    		}
    	}
    }
}
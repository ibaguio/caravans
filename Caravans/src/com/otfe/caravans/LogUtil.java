package com.otfe.caravans;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LogUtil {
	private static SharedPreferences preferences;
	private static boolean preferences_debug_mode;
	private static boolean preferences_sdcard_logging;
	private static boolean loaded = false;
	private static final String err = "\nError: ";
	private static File sdlog;
	
	public static void initialize(Context context){
		if (loaded) return;
		preferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Constants.SETTINGS_MODE);
		preferences_debug_mode = preferences.getBoolean(
				context.getString(R.string.preferences_key_debugging_mode), Constants.SETTINGS_DEBUG_MODE);
		preferences_sdcard_logging = preferences.getBoolean(
				context.getString(R.string.preferences_key_sdcard_logging), Constants.SETTINGS_SDCARD_LOGGING);
		if (preferences_sdcard_logging){
			sdlog = new File(Constants.SETTINGS_DEFAULT_LOG_FILE);
			if (!sdlog.exists())
				try{
					sdlog.createNewFile();
				}catch(IOException e){
					e("LogUtil","Failed to create log file",e);
				}
		}
		loaded = true;
	}
	
	public static void d(String tag, String msg) {
		if (!loaded)
			Log.d(tag, msg);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (!loaded)
			Log.d(tag, msg, tr);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg+err+tr.getMessage());
		else if (preferences_debug_mode)
			Log.d(tag, msg, tr);
	}

	public static void e(String tag, String msg) {
		if (!loaded)
			Log.e(tag, msg);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (!loaded)
			Log.e(tag, msg, tr);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.e(tag, msg, tr);
	}

	public static void i(String tag, String msg) {
		if (!loaded)
			Log.i(tag, msg);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (!loaded)
			Log.i(tag, msg, tr);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.i(tag, msg, tr);
	}

	public static void v(String tag, String msg) {
		if (!loaded)
			Log.v(tag, msg);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (!loaded)
			Log.v(tag, msg, tr);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.v(tag, msg, tr);
	}

	public static void w(String tag, String msg) {
		if (!loaded)
			Log.w(tag, msg);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg);
		else if (preferences_debug_mode)
			Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (!loaded)
			Log.w(tag, msg, tr);
		else if (preferences_sdcard_logging)
			writeToSDLog(tag,msg+err+tr.getMessage());
		else if (preferences_debug_mode)
			Log.w(tag, msg, tr);
	}

	private static void writeToSDLog(String tag, String msg){
		
	}
}
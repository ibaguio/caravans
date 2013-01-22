package com.otfe.caravans;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Environment;
import android.os.FileObserver;

/**
 * 
 * @author Ivan Dominic Baguio
 *
 */
public final class Constants {
	public static final String TAG = "Caravans";
	public static final String AES = "AES";
    public static final String TWO_FISH = "TWOFISH";
    public static final String SERPENT = "SERPENT";
    public static final String BLOCK_CIPHER_MODE = "CBC";
    public static final String VERIFY_STRING = "TRUE";
    public static final String PROVIDER = "SC";
    public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static final List<String> ALGORITHMS = Arrays.asList(new String[] {AES,TWO_FISH,SERPENT});
    
	public static final String SETTINGS_NAME = "Caravans_Settings";
	public static final int SETTINGS_MODE = Activity.MODE_MULTI_PROCESS;
	public static final boolean SETTINGS_DEFAULT_POWER_SAVING = true;
	public static final boolean SETTINGS_DEBUG_MODE = true;
	public static final boolean SETTINGS_SDCARD_LOGGING = false;
	
	/* FILE PATHS */
	public static final String CARAVANS_PATH = SDCARD + File.separator + "Caravans";
	public static final String SETTINGS_DEFAULT_LOG_FILE = CARAVANS_PATH + File.separator + "logs";
	public static final String SETTINGS_DEFAULT_DECRYPT_FOLDER_PATH = CARAVANS_PATH + File.separator + "Decrypted/";
	
	/* TASKS */
	public static final String EXTRA_SERVICE_TASK = "service_task";
	public static final int TASK_DECRYPT_SINGLE = 0;
	public static final int TASK_ENCRYPT_SINGLE = 1;
	public static final int TASK_GET_DECRYPT_TARGET = 2;
	public static final int TASK_GET_DEST_FOLDER = 3;
	public static final int TASK_MAKE_PATTERN = 4;
	public static final int TASK_FOLDER_LISTEN = 5;
	public static final int TASK_FOLDER_OBSERVE = 5;
	
	/* KEYS */
	public static final String KEY_PASSWORD = "pass";
	public static final String KEY_TARG_FILE = "targ_file";
	public static final String KEY_DEST_FOLDER = "dest_folder";
	public static final String KEY_ALGORITHM = "algo";
	public static final String KEY_CHECKSUM = "chechsum";
	public static final String KEY_TEST_COUNT = "test_count";
	public static final String KEY_FILE_SIZE = "file_size";
	public static final String KEY_ROW_ID = "row_id";
	
	public static int OBSERVER_EVENTS = FileObserver.CREATE +FileObserver.DELETE_SELF +
			FileObserver.MOVE_SELF + FileObserver.MOVED_TO + FileObserver.MOVED_FROM;
	
	/* DATABASE */
	public static final int DATABASE_VERSION = 1;
	
	/* OTHER */
	public static final int NOTIFICATION_ID = 0;
	
}

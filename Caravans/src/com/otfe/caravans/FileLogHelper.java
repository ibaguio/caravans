package com.otfe.caravans;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * FileLog database helper
 * creates the SQLite database for the list of
 * files(not directories) that are otf encrypted
 * @author baguio
 */
public class FileLogHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 1;
	public static final String COLUMN_ID = "_id";
	public static String TARGET_NAME = "";
	private String TABLE_NAME;
	public String TABLE_CREATE;
	public static final String BASE_NAME = "filelogger_";
	public static final String DATABASE_NAME = "otfelogger.db";
	public static final String COLUMN_FILENAME = "filename";
	public static final String COLUMN_CHECKSUM = "checksum";
	public static final String COLUMN_FILESIZE = "filesize";
	public static final String COLUMN_LASTMOD = "lastmodified";
	public static final String COLUMN_FILETYPE = "filetype";
	public static final String TABLE_CREATE_1 = "CREATE TABLE ";
	public static final String TABLE_CREATE_2 = "("+COLUMN_ID+" integer primary key autoincrement, " +
					COLUMN_FILENAME + " TEXT not NULL, " +
					COLUMN_CHECKSUM + " TEXT not NULL, " +
					COLUMN_FILESIZE + " INTEGER not NULL, " +
					COLUMN_LASTMOD  + " INTEGER not NULL, " +
					COLUMN_FILETYPE + " TEXT)";
	
	public FileLogHelper(Context context, String foldername){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d("File LH","TARGET: "+foldername);
		TARGET_NAME = foldername.toLowerCase();
		TABLE_NAME = BASE_NAME + TARGET_NAME;
		TABLE_CREATE = TABLE_CREATE_1 + TABLE_NAME + TABLE_CREATE_2;
		
		SQLiteDatabase db;
		try {
			Log.d("","Opening/creating database: "+DATABASE_NAME);
            db = context.openOrCreateDatabase(DATABASE_NAME, DATABASE_VERSION, null);
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (!tableExist(c)){
            	Log.d("File LH",TABLE_NAME+ " does not yet exist, creating it");
            	this.onCreate(db);
            }else
            	Log.d("File LH",TABLE_NAME+" exits. will continue");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private boolean tableExist(Cursor c){
		Log.d("File LH","checking if "+TABLE_NAME+" exists");
		if (c.moveToFirst()){
	        while ( !c.isAfterLast() ){
	        	String x = c.getString( c.getColumnIndex("name"));
	        	Log.d("","**"+x);
	        	if (x.equals(TABLE_NAME))
	        		return true;
	        	c.moveToNext();
	        }
	    }
		return false;
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		if (TARGET_NAME.equals(""))
			Log.e("File LH","Target is empty");
		Log.d("File LH","craeted new table, name: "+TABLE_NAME);
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
		Log.d(FileLogHelper.class.getName(),"Upgrading database from version "+
				old_version+" to "+new_version+", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		onCreate(db);
	}
	
	public static String[] getAllColumns(){
		String[] ret = {COLUMN_ID,COLUMN_FILENAME,COLUMN_CHECKSUM,COLUMN_FILESIZE,COLUMN_LASTMOD,COLUMN_FILETYPE};
		return ret;
	}
}
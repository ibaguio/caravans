package com.otfe.caravans;

import android.content.Context;
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
	public static final String DATABASE_NAME = "otfelogger.db";
	public static final String TABLE_NAME = "filelogger.";
	public static final String COLUMN_FILENAME = "filename";
	public static final String COLUMN_CHECKSUM = "checksum";
	public static final String COLUMN_FILESIZE = "filesize";
	public static final String COLUMN_LASTMOD = "lastmodified";
	public static final String COLUMN_FILETYPE = "filetype";
	public static final String TABLE_CREATE = "CREATE TABLE " 
			+ DATABASE_NAME + "("+COLUMN_ID+" integer primary key autoincrement" +
					COLUMN_FILENAME + " TEXT not NULL, " +
					COLUMN_CHECKSUM + " TEXT not NULL, " +
					COLUMN_FILESIZE + " INTEGER not NULL, " +
					COLUMN_LASTMOD  + " INTEGER not NULL, " +
					COLUMN_FILETYPE + " TEXT)";
	
	public FileLogHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		Log.d("File LH","craeted new database, name: "+DATABASE_NAME+"; table: "+TABLE_NAME);
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
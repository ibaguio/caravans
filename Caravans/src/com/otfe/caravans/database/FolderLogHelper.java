package com.otfe.caravans.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.otfe.caravans.Constants;
/**
 * SQLite Helper for FolderLogs
 * @author Ivan Dominic Baguio
 */
public class FolderLogHelper extends SQLiteOpenHelper {
	private static final String TAG = "Folder LH";
	public static final String COLUMN_ID = "_id";
	public static final String TABLE_NAME = "folderlogger";
	public static final String COLUMN_FOLDERNAME = "foldername";
	public static final String COLUMN_ALGORITHM = "algorithm";
	public static final String COLUMN_AUTH_TYPE = "type";//password type (0-pass,1-pattern)
	public static final String COLUMN_HASH = "hash";
	public static final String COLUMN_PATH = "path";
	public static final String TABLE_CREATE = "CREATE TABLE " 
			+ TABLE_NAME + "("+COLUMN_ID+" integer primary key autoincrement," +
					COLUMN_FOLDERNAME + " TEXT not NULL, " +
					COLUMN_PATH + " TEXT not NULL, " +
					COLUMN_ALGORITHM + " TEXT not NULL," +
					COLUMN_HASH + " BLOB not NULL, " +
					COLUMN_AUTH_TYPE + " INT not NULL, " +
					"UNIQUE("+COLUMN_PATH+"));";
	
	public FolderLogHelper(Context context){
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
		SQLiteDatabase db;
		try {
			Log.d(TAG,"Opening/creating database: "+Constants.DATABASE_NAME);
            db = context.openOrCreateDatabase(Constants.DATABASE_NAME, Constants.DATABASE_VERSION, null);
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (!tableExist(c)){
            	Log.d(TAG,TABLE_NAME+ " does not yet exist, creating it");
            	onCreate(db);
            }else
            	Log.d(TAG,TABLE_NAME+" exits. will continue");
            db.close();
        } catch (SQLiteException e) {
        	//e.printStackTrace();
        	Log.e(TAG,e.getMessage());
        }
	}
	
	private boolean tableExist(Cursor c){
		Log.d(TAG,"checking if "+TABLE_NAME+" exists");
		if (c.moveToFirst()){
	        while ( !c.isAfterLast() ){
	        	String x = c.getString( c.getColumnIndex("name"));
	        	if (x.equals(TABLE_NAME))
	        		return true;
	        	c.moveToNext();
	        }
	    }
		return false;
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		Log.d(TAG,"Creating "+TABLE_NAME);
		try{
			db.execSQL(TABLE_CREATE);
			Log.d(TAG,"Created new table "+TABLE_NAME);
		}catch(SQLiteException e){
			//e.printStackTrace();
			Log.e(TAG,e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
		Log.d(FileLogHelper.class.getName(),"Upgrading database from version "+
				old_version+" to "+new_version+", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		Log.d("Database","FolderLogHelper upgrade");
		onCreate(db);
	}
	
	public static String[] getAllColumns(){
		String[] ret = {COLUMN_ID,COLUMN_FOLDERNAME,COLUMN_PATH,
				COLUMN_ALGORITHM,COLUMN_HASH,COLUMN_AUTH_TYPE};
		return ret;
	}
}
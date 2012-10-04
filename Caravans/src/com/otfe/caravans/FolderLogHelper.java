package com.otfe.caravans;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FolderLogHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String COLUMN_ID = "_id";
	public static final String DATABASE_NAME = "otfelogger";
	public static final String TABLE_NAME = "folderlogger";
	public static final String COLUMN_FOLDERNAME = "foldername";
	public static final String COLUMN_LASTMOD = "lastmodified";
	public static final String COLUMN_ALGORITHM = "algorithm";
	public static final String TABLE_CREATE = "CREATE TABLE " 
			+ TABLE_NAME + "("+COLUMN_ID+" integer primary key autoincrement," +
					COLUMN_FOLDERNAME + " TEXT not NULL, " +
					COLUMN_ALGORITHM + " TEXT not NULL," +
					COLUMN_LASTMOD  + " INTEGER not NULL);";
	
	
	public FolderLogHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		SQLiteDatabase db;
		try {
            db = context.openOrCreateDatabase(DATABASE_NAME, DATABASE_VERSION, null);
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (!tableExist(c)){
            	Log.d("Folder LH",TABLE_NAME+ " does not yet exist, creating it");
            	this.onCreate(db);
            }else
            	Log.d("Folder LH",TABLE_NAME+" exits. will continue");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private boolean tableExist(Cursor c){
		Log.d("Folder LH","checking if "+TABLE_NAME+" exists");
		if (c.moveToFirst()){
	        while ( !c.isAfterLast() ){
	        	if (c.getString( c.getColumnIndex("name")).equals(TABLE_NAME))
	        		return true;
	        	c.moveToNext();
	        }
	    }
		return false;
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		Log.d("Database","Creating "+TABLE_NAME);
		db.execSQL(TABLE_CREATE);
		Log.d("Database","Craeted new table "+TABLE_NAME);
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
		String[] ret = {COLUMN_ID,COLUMN_FOLDERNAME,COLUMN_ALGORITHM,COLUMN_LASTMOD};
		return ret;
	}
}


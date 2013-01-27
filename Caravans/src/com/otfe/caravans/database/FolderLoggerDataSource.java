package com.otfe.caravans.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Class that is responsible for inserting rows
 * to the folderlogger table
 * @author Ivan Dominic Baguio
 */
public class FolderLoggerDataSource {
	private final String TAG = "Folder LDS";
	private SQLiteDatabase database;
	private FolderLogHelper dbHelper;
	private final String[] allColumns = FolderLogHelper.getAllColumns();
	
	public FolderLoggerDataSource(Context context){
		Log.d(TAG,"New Instance created");
		dbHelper = new FolderLogHelper(context);
	}
	
	public void open(){
		database = dbHelper.getWritableDatabase();
		Log.d(TAG,"Opened "+database.getPath());
	}
	
	public void close(){
		Log.d(TAG,"Closing "+database.getPath());
		dbHelper.close();
		//database.close();
	}
	
	/**
	 * adds a new otfe folder to the database 
	 * @param f
	 * @param algorithm
	 * @return a FolderLog representation of newly added folder
	 */
	public FolderLog createFolderLog(File f, String algorithm, byte[] hash, 
			boolean isPattern){
		ContentValues values = new ContentValues();
		values.put(FolderLogHelper.COLUMN_FOLDERNAME, f.getName());
		values.put(FolderLogHelper.COLUMN_PATH, f.getAbsolutePath());
		values.put(FolderLogHelper.COLUMN_HASH, hash);
		values.put(FolderLogHelper.COLUMN_ALGORITHM, algorithm);
		values.put(FolderLogHelper.COLUMN_AUTH_TYPE, isPattern? 1:0);
		
		Log.d(TAG, "NEW. isPattern: "+(isPattern?1:0));
		try{
			/* inserts the information to the database, throws exception
			   when folder is already in database */
			long insertId = database.insertOrThrow(FolderLogHelper.TABLE_NAME,null,values);
			Log.d(TAG,"Inseted new data to database");
			Cursor cursor = database.query(FolderLogHelper.TABLE_NAME, allColumns, 
					FolderLogHelper.COLUMN_ID + " = " + insertId,null,null,null,null);
			Log.d(TAG,"query completed");
			cursor.moveToFirst();
			Log.d(TAG,"moved to first");
			FolderLog newFolderLog = cursorToFolderLog(cursor);
			Log.d(TAG,"created new folder log");
			cursor.close();
			return newFolderLog;
		}catch(SQLiteException e){
			Log.d(TAG, "Folder already in list");
		}
		return null;
	}
	
	public void deleteFolderLog(FolderLog folderLog){
		long id = folderLog.getId();
		System.out.println("FolderLog delete with id: "+id);
		database.delete(FolderLogHelper.TABLE_NAME, FolderLogHelper.COLUMN_ID + " = " +id,null);
	}

	/**
	 * returns a list of all Folders are in database
	 * @return
	 */
	public List<FolderLog> getAllFolderLogs(){
		try{
			Log.d(TAG,"Getting all folderLog");
			List<FolderLog> folderLogs = new ArrayList<FolderLog>();
			Cursor cursor = database.query(FolderLogHelper.TABLE_NAME,allColumns,null,null,null,null,null);
			Log.d(TAG,"Folderlog count: "+cursor.getCount());
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				FolderLog folderLog = cursorToFolderLog(cursor);
				folderLogs.add(folderLog);
				cursor.moveToNext();
			}
			Log.d(TAG,"returning folderLogs, count: "+folderLogs.size());
			return folderLogs;
		}catch(SQLiteException e){}
		return null;
	}

	public FolderLog getFolderLog(int row_id){
		try{
			String query = "SELECT * FROM "+FolderLogHelper.TABLE_NAME+" WHERE "+FolderLogHelper.COLUMN_ID+" =?";
			Cursor c = database.rawQuery(query, new String[]{""+row_id});
			c.moveToFirst();
			if ( c.getCount() == 1)
				return cursorToFolderLog(c);
		}catch(SQLiteException e){}
		return null;
	}
	
	/**
	 * Converts a cursor(db row) to a FolderLog
	 * @param cursor
	 * @return
	 */
	private FolderLog cursorToFolderLog(Cursor cursor){
		Log.d(TAG,"Converting cursor to folderLog");
		FolderLog folderLog = new FolderLog();
		folderLog.setId(cursor.getLong(0));
		folderLog.setFolderName(cursor.getString(1));
		folderLog.setPath(cursor.getString(2));
		folderLog.setAlgorithm(cursor.getString(3));
		folderLog.setVerifyHash(cursor.getBlob(4));
		folderLog.setIsPattern(cursor.getInt(5)==1);
		Log.d(TAG,"ID: "+cursor.getLong(0)+"\nFolderName: "+cursor.getString(1)+"\nPath: "+cursor.getString(2)
				+"\nAlgo: "+cursor.getString(3)+"\nPattern?: "+folderLog.isPattern());
		return folderLog;
	}

	/**
	 * checks if the given absolute path for a directory is not yet 
	 * in the database, thus not net registered to be monitored by otfe 
	 * @param path
	 * @return
	 */
	public boolean isNewFolder(String path){
		try{
			String sql = "SELECT "+FolderLogHelper.COLUMN_ID+" FROM "+FolderLogHelper.TABLE_NAME+
					" WHERE "+FolderLogHelper.COLUMN_PATH+"=?";
			Cursor c = database.rawQuery(sql, new String[] {path});
			return c.getCount() == 0;
		}catch(SQLiteException e){}
		return true;
	}
}
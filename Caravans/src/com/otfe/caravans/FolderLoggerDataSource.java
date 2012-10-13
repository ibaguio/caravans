package com.otfe.caravans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FolderLoggerDataSource {
	private SQLiteDatabase database;
	private FolderLogHelper dbHelper;
	private final String[] allColumns = FolderLogHelper.getAllColumns();
	
	public FolderLoggerDataSource(Context context){
		Log.d("Folder LDS","New Instance created");
		dbHelper = new FolderLogHelper(context);
	}
	
	public void open() throws SQLException{
		Log.d("Folder LDS","Opening");
		database = dbHelper.getWritableDatabase();
		Log.d("Folder LDS","Opened");
	}
	
	public void close(){
		Log.d("Folder LDS","Close");
		dbHelper.close();
	}
	
	public FolderLog createFolderLog(File f, String algorithm){
		ContentValues values = new ContentValues();
		values.put(FolderLogHelper.COLUMN_FOLDERNAME, f.getName());
		values.put(FolderLogHelper.COLUMN_PATH, f.getAbsolutePath());	
		values.put(FolderLogHelper.COLUMN_LASTMOD, f.lastModified());
		values.put(FolderLogHelper.COLUMN_ALGORITHM, algorithm);
		Log.d("Folder LDS","Creating new folder log\nFilename: "+f.getName()+"\nPath: "+f.getAbsolutePath()+"\nAlgo: " +
				algorithm + "\nLastMod: "+f.lastModified());
		
		long insertId = database.insert(FolderLogHelper.TABLE_NAME,null,values);
		Log.d("Folder LDS","Inseted new data to database");
		Cursor cursor = database.query(FolderLogHelper.TABLE_NAME, allColumns, 
				FolderLogHelper.COLUMN_ID + " = " + insertId,null,null,null,null);
		Log.d("Folder LDS","query completed");
		cursor.moveToFirst();
		Log.d("Folder LDS","moved to first");
		FolderLog newFileLog = cursorToFolderLog(cursor);
		Log.d("Folder LDS","created new file log");
		cursor.close();
		return newFileLog;
	}
	
	public void deleteFolderLog(FolderLog fileLog){
		long id = fileLog.getId();
		System.out.println("FolderLog delete with id: "+id);
		database.delete(FolderLogHelper.TABLE_NAME, FolderLogHelper.COLUMN_ID + " = " +id,null);
	}

	public List<FolderLog> getAllFolderLogs(){
		Log.d("Folder LDS","Getting all folderLog");
		List<FolderLog> folderLogs = new ArrayList<FolderLog>();
		Cursor cursor = database.query(FolderLogHelper.TABLE_NAME,allColumns,null,null,null,null,null);
		Log.d("Folder LDS","Folderlog count: "+cursor.getCount());
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			FolderLog folderLog = cursorToFolderLog(cursor);
			folderLogs.add(folderLog);
			cursor.moveToNext();
		}
		Log.d("Folder LDS","returning folderLogs, count: "+folderLogs.size());
		return folderLogs;
	}
	private FolderLog cursorToFolderLog(Cursor cursor){
		Log.d("Folder LDS","Converting cursor to folderLog");
		FolderLog folderLog = new FolderLog();
		folderLog.setId(cursor.getLong(0));
		folderLog.setFolderName(cursor.getString(1));
		folderLog.setPath(cursor.getString(2));
		folderLog.setAlgorithm(cursor.getString(3));
		folderLog.setLastModified(cursor.getLong(4));
		Log.d("Folder LDS","ID: "+cursor.getLong(0)+"\nFolderName: "+cursor.getString(1)+"\nPath: "+cursor.getString(2)
				+"\nAlgo: "+cursor.getString(3)+"\nLastmod: "+cursor.getLong(4));
		return folderLog;
	}
}
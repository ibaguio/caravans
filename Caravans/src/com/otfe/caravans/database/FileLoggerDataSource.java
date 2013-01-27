package com.otfe.caravans.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.otfe.caravans.crypto.CryptoUtility;

public class FileLoggerDataSource {
	private static final String TAG = "File LDS";
	private String TABLE_NAME;
	private SQLiteDatabase database;
	private FileLogHelper dbHelper;
	private String[] allColumns = FileLogHelper.getAllColumns();
	
	public FileLoggerDataSource(Context context,String foldername){
		Log.d(TAG,"New Instance created; foldername: "+foldername);
		TABLE_NAME = FileLogHelper.BASE_NAME + foldername.toLowerCase();
		dbHelper = new FileLogHelper(context,foldername);
	}
	
	public void open(){
		database = dbHelper.getWritableDatabase();
		Log.d(TAG,"Opened "+database.getPath());
	}
	
	public void close(){
		Log.d(TAG,"Closed");
		dbHelper.close();
		//database.close();
	}
	
	public FileLog createFileLog(File f){
		Log.d(TAG,"Creating new log");
		ContentValues values = new ContentValues();
		String checksum="";
		try{
			checksum = CryptoUtility.byteToString(CryptoUtility.md5Sum(f));
		}catch(Exception e){
			e.printStackTrace();
		}
		String filetype="none";
		try{
			filetype = CryptoUtility.getFileType(f);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.d(TAG,"Filename "+f.getName()+"\nchecksum: "+checksum
				+"\nfiletype: "+filetype+ "\nfilesize: "+f.length());
		
		values.put(FileLogHelper.COLUMN_FILENAME, CryptoUtility.getRawFileName(f));
		values.put(FileLogHelper.COLUMN_CHECKSUM, checksum);
		values.put(FileLogHelper.COLUMN_FILESIZE, f.length());
		values.put(FileLogHelper.COLUMN_LASTMOD, f.lastModified());
		if (filetype!=null)
			values.put(FileLogHelper.COLUMN_FILETYPE, filetype);
		
		long insertId = database.insert(TABLE_NAME,null,values);
		Cursor cursor = database.query(TABLE_NAME, allColumns, 
				FileLogHelper.COLUMN_ID + " = " + insertId,null,null,null,null);
		cursor.moveToFirst();
		FileLog newFileLog = cursorToFileLog(cursor);
		cursor.close();
		return newFileLog;
	}
	
	public void deleteFileLog(FileLog fileLog){
		long id = fileLog.getId();
		Log.d(TAG,"Deleting fileLog with id: "+id);
		Log.d(TAG,fileLog.toString());
		database.delete(TABLE_NAME, FileLogHelper.COLUMN_ID + " = " +id,null);
	}
	
	public List<FileLog> getAllFileLogs(){
		List<FileLog> fileLogs = new ArrayList<FileLog>();
		
		Cursor cursor = database.query(TABLE_NAME,allColumns,null,null,null,null,null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			FileLog fileLog = cursorToFileLog(cursor);
			fileLogs.add(fileLog);
			cursor.moveToNext();
		}
		return fileLogs;
	}
	
	public FileLog getFileLog(String filename){
		String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+FileLogHelper.COLUMN_FILENAME + " =?";
		Log.d(TAG, "Getting file Log\nquery: "+query);
		Cursor c = database.rawQuery(query, new String[]{filename});
		c.moveToFirst();
		if (c.getCount()==0){
			Log.d(TAG,filename+" is not found in table"+TABLE_NAME);
			return null;
		}
		return cursorToFileLog(c);
	}
	
	private FileLog cursorToFileLog(Cursor cursor){
		FileLog fileLog = new FileLog();
		fileLog.setId(cursor.getLong(0));
		fileLog.setFileName(cursor.getString(1));
		fileLog.setChecksum(cursor.getString(2));
		fileLog.setFileSize(cursor.getLong(3));
		fileLog.setLastModified(cursor.getLong(4));
		fileLog.setFileType(cursor.getString(5));
		return fileLog;
	}
}
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

public class FileLoggerDataSource {
	private String TABLE_NAME;
	private SQLiteDatabase database;
	private FileLogHelper dbHelper;
	private String[] allColumns = FileLogHelper.getAllColumns();
	
	public FileLoggerDataSource(Context context,String foldername){
		Log.d("File LDS","New Instance created; foldername: "+foldername);
		TABLE_NAME = FileLogHelper.BASE_NAME + foldername.toLowerCase();
		dbHelper = new FileLogHelper(context,foldername);
	}
	
	public void open() throws SQLException{
		Log.d("File LDS","Opened");
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		Log.d("File LDS","Closed");
		dbHelper.close();
	}
	
	public FileLog createFileLog(File f){
		Log.d("File LDS","Creating new log");
		ContentValues values = new ContentValues();
		String checksum="";
		try{
			checksum = OnTheFlyUtils.byteToString(OnTheFlyUtils.md5Sum(f));
		}catch(Exception e){
			e.printStackTrace();
		}
		String filetype="none";
		try{
			filetype = OnTheFlyUtils.getFileType(f);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.d("File LDS","Filename "+f.getName()+"\nchecksum: "+checksum
				+"\nfiletype: "+filetype+ "\nfilesize: "+f.length());
		
		values.put(FileLogHelper.COLUMN_FILENAME, OnTheFlyUtils.getRawFileName(f));
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
		Log.d("File LDS","Deleting fileLog with id: "+id);
		Log.d("File LDS",fileLog.toString());
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
		Cursor c = database.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+FileLogHelper.COLUMN_FILENAME + " = '"+filename+"'", null);
		c.moveToFirst();
		if (c.getCount()==0){
			Log.d("File LDS",filename+" is not found in table"+TABLE_NAME);
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
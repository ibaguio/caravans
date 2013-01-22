package com.otfe.caravans.database;

/* Object instance of a FileLog in the database */
public class FileLog {
	private long id;
	private String filename;
	private String checksum;
	private String filetype;
	private long lastmodified;
	private long filesize;
	
	public long getId(){
		return this.id;
	}
	public void setId(long id){
		this.id = id;
	}
	public String getFileName(){
		return this.filename;
	}
	public void setFileName(String filename){
		this.filename = filename;
	}
	public String getChecksum(){
		return this.checksum;
	}
	public void setChecksum(String checksum){
		this.checksum = checksum;
	}
	public long getLastModified(){
		return this.lastmodified;
	}
	public void setLastModified(long lastmodified){
		this.lastmodified = lastmodified;
	}
	public String getFileType(){
		return this.filetype;
	}
	public void setFileType(String filetype){
		this.filetype = filetype;
	}
	public long getFileSize(){
		return this.filesize;
	}
	public void setFileSize(long filesize){
		this.filesize = filesize;
	}
	
	@Override
	public String toString(){
		return "Filename: "+this.filename+
				"\nChecksum: " +this.checksum+
				"\nFilesize: " +this.filesize+
				"\nLastMod: " +this.lastmodified+
				"\nFiletype: "+this.filetype;
	}
}
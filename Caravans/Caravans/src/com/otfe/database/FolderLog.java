package com.otfe.database;

/**
 * FolderLog - Custom data type that contains
 * the columns/data of a single row in the 
 * table 'folderlogger'  
 * @author Ivan Dominic Baguio
 */
public class FolderLog {
	private long id;
	private String foldername;
	private String algorithm;
	private long lastmodified;
	private String path;
	public long getId(){
		return this.id;
	}
	public void setId(long id){
		this.id = id;
	}
	public String getFolderName(){
		return this.foldername;
	}
	public void setFolderName(String foldername){
		this.foldername = foldername;
	}
	public String getPath(){
		return this.path;
	}
	public void setPath(String path){
		this.path = path;
	}
	public String getAlgorithm(){
		return this.algorithm;
	}
	public void setAlgorithm(String algorithm){
		this.algorithm = algorithm;
	}	
	
	public long getLastModified(){
		return this.lastmodified;
	}
	public void setLastModified(long lastmodified){
		this.lastmodified = lastmodified;
	}
	
	@Override
	public String toString(){
		return "Filename: "+this.foldername+
				"LastMod: " +this.lastmodified;
	}
}
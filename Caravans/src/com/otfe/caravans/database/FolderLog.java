package com.otfe.caravans.database;

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
	private byte[] hash;
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
	public void setVerifyHash(byte[] hash){
		this.hash = hash;
	}
	
	public byte[] getVerifyHash(){
		return hash;
	}
	
	@Override
	public String toString(){
		return "Filename: "+this.foldername+
				"\nAlgorithm: "+this.algorithm+
				"\nPath: "+this.path;
	}
}
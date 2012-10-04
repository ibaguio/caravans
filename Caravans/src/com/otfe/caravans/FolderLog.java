package com.otfe.caravans;

public class FolderLog {
	private long id;
	private String foldername;
	private String algorithm;
	private long lastmodified;
	
	public long getId(){
		return this.id;
	}
	public void setId(long id){
		this.id = id;
	}
	public String getFileName(){
		return this.foldername;
	}
	public void setFileName(String filename){
		this.foldername = filename;
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
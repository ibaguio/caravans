package com.otfe.caravans;
import android.os.FileObserver;

public class FolderObserver extends FileObserver {
    public String absolutePath;
    
    private static int events = FileObserver.CREATE +FileObserver.DELETE_SELF +
			FileObserver.MOVE_SELF + FileObserver.MOVED_TO + FileObserver.MOVED_FROM;
    
    public FolderObserver(String path) {
    	super(path, events);
        absolutePath = path;
    }
    
    @Override
    public void onEvent(int event, String path) {
        if (path == null)
            return;
        
        //a new file or subdirectory was created under the monitored directory
        if ((FileObserver.CREATE & event)!=0) {
            
        }
        
        //a file or directory was opened
        if ((FileObserver.OPEN & event)!=0) {
            
        }
        
        //data was read from a file
        if ((FileObserver.ACCESS & event)!=0) {
            
        }
        //data was written to a file
        if ((FileObserver.MODIFY & event)!=0) {
            
        }
        //someone has a file or directory open read-only, and closed it
        if ((FileObserver.CLOSE_NOWRITE & event)!=0) {
            
        }
        //someone has a file or directory open for writing, and closed it 
        if ((FileObserver.CLOSE_WRITE & event)!=0) {
            
        }
        //[todo: consider combine this one with one below]
        //a file was deleted from the monitored directory
        if ((FileObserver.DELETE & event)!=0) {
            
        }
        //the monitored file or directory was deleted, monitoring effectively stops
        if ((FileObserver.DELETE_SELF & event)!=0) {
            
        }
        //a file or subdirectory was moved from the monitored directory
        if ((FileObserver.MOVED_FROM & event)!=0) {
            
        }
        //a file or subdirectory was moved to the monitored directory
        if ((FileObserver.MOVED_TO & event)!=0) {
            
        }
        //the monitored file or directory was moved; monitoring continues
        if ((FileObserver.MOVE_SELF & event)!=0) {
            
        }
        //Metadata (permissions, owner, timestamp) was changed explicitly
        if ((FileObserver.ATTRIB & event)!=0) {
            
        }
    }
}
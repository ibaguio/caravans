package com.otfe.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

/**
 * Decryptor class
 * Decrypts a target encrypted file using the specified algorithm and password
 * First checks the headers to ensure that password and algorithm
 * specified is correct for the file
 * 
 * Defaults:
 *    target file will not be deleted
 * 	  destination file will be saved (not saved in testing)
 * Uses Spongy Castle Library 
 * @author Ivan Dominic Baguio
 */

public class Decryptor{
	private static final String TAG = "Decryptor";
    private File toDecrypt;
    private File dest_file;
    private File dest_folder;
    
    private SecretKey secretKey;
    private String algorithm;
    private byte[] iv;
    private String[] info;
    
    /* file extension of the destination file */
    private String extension;
    
    private CipherInputStream cin;
    private long fSize;
    
    /* check marker to determine if Decryption is ready*/
    private boolean checked = false;
    
    /* flag that determines if all
       info needed is set for decryption */
    private boolean ready = false;
    
    /* flag to save the destination file */
    private boolean save = true; 
    
    /* flat to delete the target encrypted file */
    private boolean deleteTarget = false;
    
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    public Decryptor(String password, String src_path, String destFolder, String algo){
        try{
            toDecrypt = new File(src_path);
            secretKey = Utility.getKey(password.toCharArray());
            algorithm = algo;
            dest_folder = new File(destFolder);
            
            //Get the first 16 bytes as IV
            FileInputStream fis = new FileInputStream(toDecrypt);
            iv = Utility.read(fis,16,0);
            fis.close();
            if (!toDecrypt.isFile()){
                ready = false;
                Log.d(TAG,"Not ready toDecrypt");
            }
            Log.d(TAG,"New Decryptor\nTarget: "+toDecrypt.getAbsolutePath()+"\nDestination: "+dest_folder.getAbsolutePath()
            		+"\nAlgo:"+algorithm);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @return boolean value of ready variable
     */
    public boolean isReady(){
        return ready;
    }
    
    /**
     * Set the save flag as false
     * doesnt save the decrypted file, used primarily in performance testing
     * (actually deletes the decrypted file)
     */
    public void dontSave(){
    	this.save = false;
    }

    /**
     * Decryption proper
     * @return true if decryption process is successful
     */
    public boolean decrypt(){
        Log.d(TAG,"Decrypting...");
        boolean verified=false;
        try{
        	/*check if the password is correct*/
            if (!checked)
                ready=check();
            Log.d(TAG,"Check: "+ready);
            if (ready){
                setExtension();
                String d = dest_folder+ File.separator + Utility.getRawFileName(toDecrypt) + extension;
                Log.d(TAG,"FINAL DEST: *"+d+"*");
                dest_file = new File(d);
                dest_file.createNewFile();
                Log.d(TAG,"Password Correct");
                writeOut(cin);
                if (!save)
                	Log.d(TAG,"not saving decrypted file: "+dest_file.delete());
                verified = verifySize();
                Log.d(TAG,"File Verified: "+verified);
            }else
                Log.d(TAG,"NOT ready");
        }catch(Exception e){
            e.printStackTrace();
        }
        return verified;
    }

    private void writeOut(CipherInputStream cin) throws Exception{
        FileOutputStream fos = new FileOutputStream(dest_file);
        int i;
        long size=0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        fSize = size;
        Log.d(TAG,"Wrote "+size+ " bytes");
    }

    /* returns a CipherInputStream of decoded data  */
    private CipherInputStream setDecrypt(long skip) throws Exception{
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",Utility.PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(toDecrypt);
        long skp = fis.skip(skip);
        Log.d(TAG,"Skipped "+skp+" bytes");
        return new CipherInputStream(fis,cipher);
    }
    
    /**
     * Calls for check function to check if the password and/or algorithm is correct
     * @return the ready state of the decryptor
     */
    public boolean correctPassword(){
    	ready = check();
    	Log.d(TAG,"correct pass: "+ready);
    	return ready;
    }
    
    /**
     * Checks if the password and/or algorithm is correct
     * @return true if supplied info are valid (password, algorithm)
     */
    private boolean check(){
        checked = true;
        Log.d(TAG,"Checking if password/algorithm is correct for "+toDecrypt.getAbsolutePath());
        byte[] header = new byte[16];
        try{
        	if (info != null)
        		return info[0].equals("TRUE");
            cin = setDecrypt(16);
            cin.read(header);
            info = extractHeaders(new String(header,"UTF-8"));
            return info[0].equals("TRUE");
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Extracts a header string and returns a string array of header info
     * @param header
     * @return String[] of header
     * 0 - IV
     * 1 - "TRUE"
     * 2 - file size and extension
     */
    private String[] extractHeaders(String header){
    	Log.d(TAG,"header string:"+header+"*");
        String header_info[] = new String[3];
        header_info[0] = header.substring(0,4);
        header_info[1] = header.substring(4,8);
        header_info[2] = header.substring(8);
        for (int i=0;i<header_info.length;i++)
        	Log.d(TAG,"header:*"+header_info[i]+"*");
        return header_info;
    }
    
    /**
     * sets the extension of the destination file
     */
    private void setExtension(){
        if (!info[1].equals("none"))
            extension = "."+info[1].trim();
        else
            extension = "";
    }

    /**
     * Verify the size of the decrypted file
     * @return true if the size of decrypted file is equal to the size supplied in the headers
     */
    private boolean verifySize(){
        long originalSize = Long.parseLong(info[2]);
        return originalSize == fSize;
    }
    
    /**
     * verifies the checksum of the decrypted file
     * @param checksum
     * @return true if file integrity is intact
     */
    public boolean verifyChecksum(String checksum){
    	try{
    		byte[] bsum = Utility.md5Sum(dest_file);
    		return checksum.equals(Utility.byteToString(bsum));
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
}
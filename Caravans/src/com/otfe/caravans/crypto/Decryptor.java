package com.otfe.caravans.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

import com.otfe.caravans.Constants;

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
 * 
 * @author Ivan Dominic Baguio
 * @see com.otfe.caravans.crypto.Encryptor
 * @see com.otfe.caravans.crypto.Utility
 * @since 1.0
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
                if (!dest_file.exists())
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
        Log.d(TAG,"Verified: "+verified);
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
        Cipher cipher = Cipher.getInstance(algorithm+"/"+
        		Constants.BLOCK_CIPHER_MODE+"/PKCS5Padding",Constants.PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(toDecrypt);
        long skp = fis.skip(skip);
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
        boolean ok = false;
        try{
        	if (info == null){
	            cin = setDecrypt(16);
	            cin.read(header);
	            info = extractHeaders(new String(header,"UTF-8"));
        	}
            ok = info[0].equals(Constants.VERIFY_STRING);
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"Password/Algorithm correct: "+ok);
        return ok;
    }

    /**
     * Extracts a header string and returns a string array of header info
     * @param header
     * @return String[] of header
     * 0 - IV
     * 1 - VERIFY_STRING
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
    
    /**
     * Checks if the <code>hash</code> is correct given the <code>algorithm</code>
     * and <code>password</code>. The initialization vector <code>iv</code> will
     * be extracted from the <code>hash</code> itself. 
     * 
     * @param password the password to be used in decryption check
     * @param algorithm a valid algorithm to be used in decryption check
     * @param hash
     * @see com.otfe.caravas.crypto.Encryptor#generateVerifyHash(String,String)
     * @return
     */
    public static boolean checkVerifyHash(String password, String algorithm, byte[] hash){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] enc_xtrue = new byte[16];
            byte[] iv = new byte[16];
            byte[] xtrue = new byte[4];

            //extract encrypted bytes and IV from the hash
            System.arraycopy(hash, 0, enc_xtrue, 0, 16);
            System.arraycopy(hash, 16, iv, 0, 16);

            //create and initialize the cipher to be used on decrypting
            Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",Constants.PROVIDER);
            SecretKey secretK = Utility.getKey(password.toCharArray());
            cipher.init(Cipher.DECRYPT_MODE, secretK, new IvParameterSpec(iv));
            CipherOutputStream cos = new CipherOutputStream(baos,cipher);
            
            //write the encrypted bytes to output steam
            cos.write(enc_xtrue);
            cos.flush();
            cos.close();
            
            //get the decrypted bytes from stream
            xtrue = baos.toByteArray();
            baos.close();
            
            return new String(xtrue,"UTF-8").equals("TRUE");
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
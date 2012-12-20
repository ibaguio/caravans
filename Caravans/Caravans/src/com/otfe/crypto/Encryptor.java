package com.otfe.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.SequenceInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

/**
 * Encryptor class
 * Given a target file, encrypts it using 
 * the specified algorithm.
 * 
 * Defaults:
 *    Saves the encrypted file as <filename>.enc
 *      unless specified by calling setDestFilePath function 
 * Uses Spongy Castle Library
 * @author Ivan Dominic Baguio
 */
public class Encryptor{
	private final String TAG = "Encryptor";
	private final int blockSize = 16;
	
    private File toEncrypt;
    private SecretKey secretKey;
    private String algorithm;
    private byte[] iv;
    private byte[] headers;
    
    /* marker to show if Encryptor is ready to encrypt
     * and all prerequisites are fulfilled */
    private boolean ready = true;
    
    /* flag to delete the target file*/
    private boolean delete = true;
    
    /* destination file path of the encrypted file */
    private String dest_file_path = "";
    
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    /**
     * Constructor for Encryptor
     * @param password - password to be used to encrypt the file
     * @param filepath - path of the file to be encrypted
     * @param algo - algorithm to be used in encryption
     */
    public Encryptor(String password, String filepath, String algo){
        try{
            toEncrypt = new File(filepath);
            secretKey = Utility.getKey(password.toCharArray());
            algorithm = algo;
            iv = Utility.generateIV();
            if (!toEncrypt.isFile()){
            	ready = false;
            	Log.d(TAG, "File to be encrypted is not valid");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"New Encryptor\nTarget: "+filepath+"\nAlgo: "+algo);
    }
    
    /**
     * Constructor for Encryptor
     * @param password - password to be used to encrypt the file
     * @param target - the file to be encrypted
     * @param algo - algorithm to be used in encryption
     */
    public Encryptor(String password, File target, String algo){
    	try{
            toEncrypt = target;
            secretKey = Utility.getKey(password.toCharArray());
            algorithm = algo;
            iv = Utility.generateIV();
            if (!toEncrypt.isFile()){
            	ready = false;
            	Log.d(TAG, "File to be encrypted is not valid");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"New Encryptor\nTarget: "+toEncrypt.getAbsolutePath()+"\nAlgo: "+algo);
    }
    
    /**
     * Sets the destination of the encrypted file manually
     * @param path - file path where the encrypted file will be saved
     */
    public void setDestFilePath(String path){
    	Log.d(TAG, "New Enc Dest File: "+path);
    	this.dest_file_path = path;
    }
    
    /**
     * Sets the delete flag to false
     * so that the target file will not be removed after encryption
     */
    public void doNotDelete(){
    	Log.d(TAG,"Will not be deleting target file");
    	delete = false;
    }

    /**
     * Checks if Encryptor is ready for encryption
     * @return ready
     */
    public boolean isReady(){
    	if (!ready){
    		Log.e(TAG, "NOT READY FOR ENCRYPTION");
    		Log.e(TAG, toEncrypt.getAbsolutePath()+" not valid file");
    	}
        return ready;
    }

    /**
     * Encrypt the target file
     * @return true if the encryption process was a success, otherwise false
     */
    public boolean encrypt(){
        if (!isReady())
            return false;
        try{
        	Log.d(TAG,"Starting encryption");
            newEncryptedFile();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * @throws Exception
     */
    private void newEncryptedFile() throws Exception{
        /* checks if destination is already set, if not set default destination*/
        if (this.dest_file_path.equals(""))
        	dest_file_path = Utility.getNewFilePath(this.toEncrypt,this.toEncrypt.getParent(),".enc");
        
        /* String representation of the file info, to be used as file headers */
        String fileInfo = Utility.getFileType(this.toEncrypt) + Utility.getFileSize(this.toEncrypt);
        
        Log.d(TAG,"Fileinfo "+fileInfo);

        /* Convert "TRUE" and file info into bytes */
        byte[] xtrue = "TRUE".getBytes();
        byte[] xinfo = fileInfo.getBytes();

        /* encrypted file info to be written at start of cipher File */
        headers = new byte[blockSize];
        FileOutputStream encFos = new FileOutputStream(this.dest_file_path);

        encFos.write(iv);
        System.arraycopy(xtrue,0,headers,0,xtrue.length);//append encrypted "TRUE"
        System.arraycopy(xinfo,0,headers,xtrue.length,xinfo.length);//append encrypted file info
        Log.d(TAG,"HEADERS:"+Utility.byteToString(headers));
        
        CipherInputStream cin = setEncrypt();
        int i, size = 0;
        while ((i=cin.read())!=-1){
            encFos.write(i);
            size+=1;
        }
        encFos.close();
        Log.d(TAG,"WROTE "+size+ " bytes of ENC data");
        Log.d(TAG,"TOTAL: "+(headers.length+size)+" bytes");
        if (delete)
        	Log.d(TAG,"Deleting "+this.toEncrypt.getName() +this.toEncrypt.delete());
    }

    /**
     * 
     * @return CipherInputStream of encrypted bytes
     * @throws FileNotFoundException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    private CipherInputStream setEncrypt() throws FileNotFoundException, NoSuchProviderException,
    	NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
    	InvalidKeyException{
       
    	Log.d(TAG,"Setting main cipher stream");
        if (headers == null) return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(headers);
        SequenceInputStream sis = new SequenceInputStream(bais,new FileInputStream(toEncrypt));
        
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",Utility.PROVIDER);
        IvParameterSpec ips = null;
        if (iv.length==16)
            ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,ips);
        CipherInputStream cin = new CipherInputStream(sis,cipher);
        return cin;
    }
}
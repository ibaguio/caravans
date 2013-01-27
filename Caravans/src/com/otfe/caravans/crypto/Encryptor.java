package com.otfe.caravans.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

import com.otfe.caravans.Constants;

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
 * @see com.otfe.caravans.crypto.Decryptor
 * @see com.otfe.caravans.crypto.CryptoUtility
 * 
 * @since 1.0
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
    	this(password, new File(filepath), algo);
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
            secretKey = CryptoUtility.getKey(password.toCharArray());
            algorithm = algo;
            iv = CryptoUtility.generateIV(blockSize);
            if (!toEncrypt.isFile()){
            	ready = false;
            	Log.d(TAG, "File "+toEncrypt.getPath()+" is not valid");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"New Encryptor\nTarget: "+toEncrypt.getAbsolutePath()+"\nAlgo: "+algo);
    }
    
    /**
     * Sets the destination folder of the encrypted file manually
     * @param path - file path where the encrypted file will be saved
     */
    public void setDestinationFolder(String parent){
    	Log.d(TAG, "Encryption Dest folder: "+parent);
    	this.dest_file_path = CryptoUtility.getNewFilePath(toEncrypt, parent , 
    			Constants.ENCRYPTED_FILE_EXTENSION);
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
        	dest_file_path = CryptoUtility.getNewFilePath(this.toEncrypt,this.toEncrypt.getParent(),
        			Constants.ENCRYPTED_FILE_EXTENSION);
        
        /* String representation of the file info, to be used as file headers */
        String fileInfo = CryptoUtility.getFileType(this.toEncrypt) + CryptoUtility.getFileSize(this.toEncrypt);
        
        Log.d(TAG,"Fileinfo "+fileInfo);

        /* Convert VERIFY_STRING and file info into bytes */
        byte[] xtrue = Constants.VERIFY_STRING.getBytes();
        byte[] xinfo = fileInfo.getBytes();

        /* encrypted file info to be written at start of cipher File */
        headers = new byte[blockSize];
        FileOutputStream encFos = new FileOutputStream(this.dest_file_path);

        encFos.write(iv);
        System.arraycopy(xtrue,0,headers,0,xtrue.length);//append VERIFY_STRING
        System.arraycopy(xinfo,0,headers,xtrue.length,xinfo.length);//append file info
        Log.d(TAG,"HEADERS:"+CryptoUtility.byteToString(headers));
        
        CipherInputStream cin = setEncrypt();
        int i, size = 0;
        while ((i=cin.read())!=-1){
            encFos.write(i);
            size+=1;
        }
        encFos.close();
        Log.d(TAG,"WROTE "+size+ " bytes of ENC data");
        if (delete){
        	boolean d = toEncrypt.delete();
        	Log.d(TAG,"Deleting "+toEncrypt.getName() +d);
        }
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
        
        Cipher cipher = Cipher.getInstance(algorithm+"/"+
        		Constants.BLOCK_CIPHER_MODE+"/PKCS5Padding",Constants.PROVIDER);
        IvParameterSpec ips = null;
        if (iv.length==16)
            ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,ips);
        CipherInputStream cin = new CipherInputStream(sis,cipher);
        return cin;
    }
    
    /**
     * Generates a verification hash(semi-encrypted) from the string
     * VERIFY_STRING. 
     * 
     * <p>The hash is encrypted using the <code>password</code>
     * and <code>algorithm</code> provided.
     * 
     * <p>The initialization vector <code>iv</code> used will be randomly
     * generated, and will be appended to the end of encrypted string.
     * 
     * @param password the password to be used in the encryption
     * @param algorithm a valid algorithm to be used
     * @return a byte array in this composition: <encrypted("TRUE") + IV>
     */
    public static byte[] generateVerifyHash(String password, String algorithm){
        try{
            byte[] final_hash;
            //generate a random IV
            byte iv[] = CryptoUtility.generateIV(16);
            byte[] xtrue = Constants.VERIFY_STRING.getBytes();
            
            //the output stream of bytes where the encrypted bytes are written
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //cipher to be used in encryption
            Cipher cipher = Cipher.getInstance(algorithm+"/"+
        		Constants.BLOCK_CIPHER_MODE+"/PKCS5Padding",Constants.PROVIDER);
            IvParameterSpec ips = new IvParameterSpec(iv);
            SecretKey secretK = CryptoUtility.getKey(password.toCharArray());

            //initialize the cipher to encrypt, using the IV and secret key
            cipher.init(Cipher.ENCRYPT_MODE,secretK,ips);
            CipherOutputStream cos = new CipherOutputStream(baos,cipher);

            //write decrypted data to outputstream and close
            cos.write(xtrue);
            cos.flush();
            cos.close();

            //append raw IV to outpustream
            baos.write(iv);
            
            //get the final bytes from the output stream
            final_hash = baos.toByteArray();

            baos.close();
            return final_hash;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
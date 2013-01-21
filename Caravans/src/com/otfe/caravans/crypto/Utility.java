package com.otfe.caravans.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.otfe.caravans.Constants;

/**
 * Utility
 * static utility functions used mostly by Encryptor 
 * and Decryptor classes
 * @author Ivan Dominic Baguio
 *
 */
public class Utility{
    private static final String TAG = "Utility";

    /**
     * Returns a string of the given file's type or extension
     * does not verify if it is really the type of file
     * not very good error checking here
     * @param f
     * @return
     */
    public static String getFileType(File f){
        String filename = f.getName();
        String ext = "none";
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1)
            ext = filename.substring(i+1).toLowerCase();
        for (i=0;i<4-ext.length();i++)
            ext+=" ";
        Log.d(TAG,"Filetype "+filename+" "+ext);
        return ext;
    }

    /* returns the filename without the extension
     * ex. Filename = "test.txt" would return test */
    public static String getRawFileName(File f){
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if (i<0)
            return filename;
        return filename.substring(0,i);
    }

    /**
     * Returns a string number whose length is 8,
     * which represents the file size of given file
     * used in storing file size as string header
     * @param f - file whose size to be taken
     * @return string representation of file size of given file
     */
    public static String getFileSize(File f){
        String size = String.valueOf(f.length());
        int s =size.length();
        for (int i = 0; i<8-s; i++)
            size = '0'+size;
        Log.d(TAG,"File size " + f.getName() +" "+size);
        return size;
    }

    /**
     * Retuns a secret key to be used for
     * encryption and decryption
     * @param password - password for the secret key
     * @return SecretKey
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     */
    public static SecretKey getKey(char[] password)throws NoSuchAlgorithmException,
    	InvalidKeySpecException, UnsupportedEncodingException{
        /* for now salt is hardcoded */
        byte[] salt = "saltsalt".getBytes("UTF-8");//hardcoded salt
        /* Derive the key, given password and salt. */
        /* uses PBKDF2- password based key derivation function 2*/
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        /* Use AES as key spec */
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), Constants.AES);
        return secret;
    }

    /**
     * returns a random array of bytes used 
     * as Initialization Vector for encryptor and
     * decryptor classes
     * @param len - length of the IV
     * @return byte of IV
     */
    public static byte[] generateIV(int len){
        SecureRandom rand = new SecureRandom();
        byte iv[] = new byte[len];
        rand.nextBytes(iv);
        return iv;
    }
    
    /**
     * Reads 'bytes' number of bytes with offset bytes as offsets 
     * @param in - FileInputStream to be read
     * @param bytes - number of bytes to be read
     * @param offset - offset of bytes to be skipped before reading the first byte
     * @return
     * @throws Exception
     */
    public static byte[] read(FileInputStream in,int bytes, int offset) throws Exception{
        in.skip(offset);
        Log.d(TAG,"Reading "+bytes+" bytes; offset "+offset+" available "+in.available());
        byte[] content = new byte[bytes];
        in.read(content,0,bytes);
        in.close();
        return content;
    }
   /**
    * converts a byte to human readable string
    * @param b - byte to be converted
    * @return human readable string representation of the given byte
    */
   public static String byteToString(byte[] b) {
       String result = "";
       for (int i=0; i < b.length; i++) 
           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
       return result;
       
   }

   /**
    * 
    * @param target
    * @param parent
    * @param extension
    * @return
    */
   public static String getNewFilePath(File target, String parent, String extension){
      if (parent == null)
        return getRawFileName(target)+extension;
      return parent+"/"+getRawFileName(target)+extension;
   }
   
   /**
    * calculates the md5 hash of a given file
    * src: http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java 
    * @param f - file whose md5 hash to be calculated
    * @return byte array of md5 hash, can be converted to human readable
    * string by using Utility.byteToString function 
    * @throws Exception
    */
   public static byte[] md5Sum(File f) throws Exception {
      InputStream fis =  new FileInputStream(f);

      byte[] buffer = new byte[1024];
      MessageDigest md = MessageDigest.getInstance("MD5");
      int numRead;
      do {
          numRead = fis.read(buffer);
          if (numRead > 0) 
              md.update(buffer, 0, numRead);
      } while (numRead != -1);
      fis.close();
      return md.digest();
  }
   
   /**
    * removes /mnt from path
    * @param path
    * @return file path without '/mnt' on it
    */
   public static String pathRemoveMNT(String path){
		if (path.indexOf("/mnt")>=0)
			return path.substring(4);
		return path;
	}
   
   /**
    * Returns a string representation of the current date
    * following a format specified by arg format
    * @param format - format to be used for the date time string
    * @return formated date time string
    */
   public static String getDate(String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		return dateFormat.format(date);
	}
}
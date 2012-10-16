package com.otfe.caravans;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class OnTheFlyUtils{
    public static final String AES = "AES";
    public static final String EBC = "EBC";
    public static final String TWO_FISH = "TWOFISH";
    public static final String SERPENT = "SERPENT";
    public static final String SPONGY = "SC";
    public static final String SunJCE = "SunJCE";
    
    private static final String TAG = "OnTheFlyUtils";

    /* returns true if file exists and is not directory*/
    public static boolean validFile(File f){
        return !f.isDirectory() && f.isFile();
    }

    public static String getProvider(String algo){
        if (algo == "AES")
          return SunJCE;
        return SPONGY;
    }
    
    public static String getFileType(File f) throws Exception{
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
     * ex. Filename = "test.txt" would return test
     * */
    public static String getRawFileName(File f){
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if (i<0)
            return filename;
        Log.d(TAG,"Filename: "+f.getName()+" RAW: "+filename.substring(0,i));
        return filename.substring(0,i);
    }

    public static String[] extractFileInfo(String info){
        String infos[] = new String[2];
        infos[0] = info.substring(0,4);
        infos[1] = info.substring(5);
        return infos;
    }

    public static String getFileSize(File f) throws Exception{
        String size = String.valueOf(f.length());
        int s =size.length();
        for (int i = 0; i<8-s; i++)
            size = '0'+size;
        Log.d(TAG,"File size " + f.getName() +" "+size);
        return size;
    }

    /*retuns a secret key to be used by encryption and decryption*/
    public static SecretKey getKey(char[] password) throws Exception{
        /* for now salt is hardcoded*/
        byte[] salt = "saltsalt".getBytes("UTF-8");//hardcoded salt
        /* Derive the key, given password and salt. */
        /* uses PBKDF2- password based key derivation function 2*/
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), AES);
        return secret;
    }

    /* returns a random array of bytes*/
    public static byte[] generateIV() throws Exception{
        SecureRandom rand = new SecureRandom();
        byte iv[] = new byte[16];
        rand.nextBytes(iv);
        return iv;
    }

    public static byte[] read(FileInputStream in,int bytes, int offset) throws Exception{
        in.skip(offset);
        Log.d(TAG,"Reading "+bytes+" bytes; offset "+offset+" available "+in.available());
        byte[] content = new byte[bytes];
        //Thread.sleep(500);
        in.read(content,0,bytes);
        in.close();
        return content;
    }

    //http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
    public static byte[] createChecksum(File f) throws Exception {
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

   /* converts a byte to human readable string */
   public static String byteToString(byte[] b) throws Exception {
       String result = "";
       for (int i=0; i < b.length; i++) 
           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
       return result;
   }

   
   public static String getNewFilePath(File target, String parent, String extension){
      if (parent == null)
        return getRawFileName(target)+extension;
      return parent+"/"+getRawFileName(target)+extension;
   }
   
   //http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
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
   
   public static String getParentFolder(File target){
	    String path = target.getAbsolutePath();
	    String ret = path.substring(0,path.lastIndexOf("/"));
	    System.out.println("RET:"+ret+"\nPath:"+path);

	    return ret;
   }
}
package thesis;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import java.util.*;
import thesis.OnTheFlyUtils;

public class OnTheFlyEncryptor{
    private File toEncrypt;
    private File dest_file;
    private SecretKey secretKey;
    private String algorithm;
    private byte[] iv;

    private final int blockSize = 16;

    private boolean ok=true;
    public OnTheFlyEncryptor(String password, String filepath, String algo){
        try{
            toEncrypt = new File(filepath);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            iv = OnTheFlyUtils.generateIV();
            if (!OnTheFlyUtils.validFile(toEncrypt))
                ok=false;

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isReady(){
        return ok;
    }

    public boolean encrypt(){
        if (!ok)
            return false;
        try{
            newEncryptedFile();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void newEncryptedFile() throws Exception{
        System.out.println("Creating new encrypted file");
        String enc_file_path = OnTheFlyUtils.getNewFilePath(toEncrypt,toEncrypt.getParent(),".enc");

        FileOutputStream encFos = new FileOutputStream(enc_file_path);
        
        String fileInfo = OnTheFlyUtils.getFileType(toEncrypt) + OnTheFlyUtils.getFileSize(toEncrypt);
        System.out.println("Fileinfo "+fileInfo);

        byte[] enc_true = encryptEBC(secretKey, "TRUE".getBytes(), algorithm);
        byte[] enc_info = encryptEBC(secretKey, fileInfo.getBytes(),algorithm);

        /* encrypted file info to be written at start of cipher File */
        byte[] toWrite = new byte[blockSize*3];
        
        System.out.println("Wrote "+enc_true.length+" bytes (enc_true)");
        System.out.println("Wrote "+enc_info.length+" bytes (enc_info)");
        System.out.println("Wrote "+iv.length+" bytes (enc_iv)");
        
        System.arraycopy(iv,0,toWrite,0,blockSize);//append encrypted "TRUE"
        System.arraycopy(enc_true,0,toWrite,blockSize,blockSize);//append encrypted file info
        System.arraycopy(enc_info,0,toWrite,2*blockSize,blockSize);//append IV
        encFos.write(toWrite);
        
        CipherInputStream cin = setEncrypt(algorithm+"/CBC/PKCS5Padding");
        int i, size = 0;
        while ((i=cin.read())!=-1){
            encFos.write(i);
            size+=1;
        }
        encFos.close();
        System.out.println("Wrote "+(size)+ " bytes (enc_data)");
        System.out.println("TOTAL: "+(toWrite.length+size)+" bytes");
        System.out.println("Deleting toEncrypt: "+toEncrypt.delete());
    }
    
    /*returns encrypted value of plain text using EBC mode*/
    private static byte[] encryptEBC(SecretKey key, byte[] ptxt, String algorithm) throws Exception{
        /* Encrypt the message. */
        System.out.println("Encrypting EBC");
        Cipher cipher = Cipher.getInstance(algorithm,"BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(ptxt);
        System.out.println("EBC input size "+ptxt.length+ " output size "+cipherText.length);
        return cipherText;
    }

    //AES/CBC/PKCS5Padding
    private CipherInputStream setEncrypt(String algorithm) throws Exception{
        System.out.println("Setting main encryption");
        Cipher cipher = Cipher.getInstance(algorithm,"BC");
        IvParameterSpec ips = null;
        if (iv.length==16)
            ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,ips);
        CipherInputStream cin = new CipherInputStream(new FileInputStream(toEncrypt),cipher);
        return cin;
    }
}
package com.otfe.caravans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.SequenceInputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

public class OnTheFlyEncryptor{
    private File toEncrypt;
    private SecretKey secretKey;
    private String algorithm;
    private String provider;
    private byte[] iv;
    private byte[] headers;
    private final int blockSize = 16;
    private boolean ok = true;
    
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    public OnTheFlyEncryptor(String password, String filepath, String algo){
        try{
            toEncrypt = new File(filepath);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            provider = OnTheFlyUtils.getProvider(algo);
            iv = OnTheFlyUtils.generateIV();
            if (!OnTheFlyUtils.validFile(toEncrypt))
                ok = false;
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d("OTF Encryptor","New OTFE algo: "+algo+"\nprovider: "+provider);
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
        Log.d("OTF Encryptor","Creating new encrypted file");
        String enc_file_path = OnTheFlyUtils.getNewFilePath(toEncrypt,toEncrypt.getParent(),".enc");
        String fileInfo = OnTheFlyUtils.getFileType(toEncrypt) + OnTheFlyUtils.getFileSize(toEncrypt);
        
        Log.d("OTF Encryptor","Fileinfo "+fileInfo);

        byte[] enc_true = encryptEBC(secretKey, "TRUE".getBytes(), algorithm);
        byte[] enc_info = encryptEBC(secretKey, fileInfo.getBytes(),algorithm);

        /* encrypted file info to be written at start of cipher File */
        byte[] toWrite = new byte[blockSize*3];
        
        Log.d("OTF Encryptor","Wrote "+enc_true.length+" bytes (enc_true)");
        Log.d("OTF Encryptor","Wrote "+enc_info.length+" bytes (enc_info)");
        Log.d("OTF Encryptor","Wrote "+iv.length+" bytes (enc_iv)");
        
        System.arraycopy(iv,0,toWrite,0,blockSize);//append encrypted "TRUE"
        System.arraycopy(enc_true,0,toWrite,blockSize,blockSize);//append encrypted file info
        System.arraycopy(enc_info,0,toWrite,2*blockSize,blockSize);//append IV
        headers = toWrite;
        
        FileOutputStream encFos = new FileOutputStream(enc_file_path);
        CipherInputStream cin = setEncrypt();
        int i, size = 0;
        while ((i=cin.read())!=-1){
            encFos.write(i);
            size+=1;
        }
        encFos.close();
        Log.d("OTF Encryptor","Wrote "+(size)+ " bytes (enc_data)");
        Log.d("OTF Encryptor","TOTAL: "+(toWrite.length+size)+" bytes");
        Log.d("OTF Encryptor","Deleting "+toEncrypt.getName() +toEncrypt.delete());
    }
    
    /*returns encrypted value of plain text using EBC mode*/
    private byte[] encryptEBC(SecretKey key, byte[] ptxt, String algorithm) throws Exception{
        /* Encrypt the message. */
        Log.d("OTF Encryptor","Encrypting EBC");
        Cipher cipher = Cipher.getInstance(algorithm,provider);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(ptxt);
        Log.d("OTF Encryptor","EBC input size "+ptxt.length+ " output size "+cipherText.length);
        return cipherText;
    }

    private CipherInputStream setEncrypt() throws Exception{
        Log.d("OTF Encryptor","Setting main encryption");
        if (headers == null)
        	return null;
        ByteArrayInputStream bias = new ByteArrayInputStream(headers);
        SequenceInputStream sis = new SequenceInputStream(bias,new FileInputStream(toEncrypt));
        
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",provider);
        IvParameterSpec ips = null;
        if (iv.length==16)
            ips = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,ips);
        CipherInputStream cin = new CipherInputStream(sis,cipher);
        return cin;
    }
}
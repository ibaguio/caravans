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
    private final String TAG = "OTF Encryptor";
    
    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    public OnTheFlyEncryptor(String password, String filepath, String algo){
        try{
        	Log.d(TAG,"PASSWORD:"+password);
            toEncrypt = new File(filepath);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            provider = OnTheFlyUtils.getProvider(algo);
            iv = OnTheFlyUtils.generateIV();
            Log.d(TAG,"IV:"+OnTheFlyUtils.byteToString(iv));
            if (!OnTheFlyUtils.validFile(toEncrypt))
                ok = false;
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG,"New OTFE algo: "+algo+"\nprovider: "+provider);
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
        Log.d(TAG,"Creating new encrypted file");
        String enc_file_path = OnTheFlyUtils.getNewFilePath(toEncrypt,toEncrypt.getParent(),".enc");
        String fileInfo = OnTheFlyUtils.getFileType(toEncrypt) + OnTheFlyUtils.getFileSize(toEncrypt);
        
        Log.d(TAG,"NEW ENC FILE DEST: "+enc_file_path);
        Log.d(TAG,"Fileinfo "+fileInfo);

        byte[] xtrue = "TRUE".getBytes();
        byte[] xinfo = fileInfo.getBytes();

        Log.d(TAG,"true.len: "+xtrue.length+" xinfo.len: "+xinfo.length+" total: "+(xinfo.length+xtrue.length));
        /* encrypted file info to be written at start of cipher File */
        byte[] toWrite = new byte[blockSize];
        FileOutputStream encFos = new FileOutputStream(enc_file_path);

        encFos.write(iv);
        //System.arraycopy(iv,0,toWrite,0,blockSize);//append encrypted "TRUE"
        System.arraycopy(xtrue,0,toWrite,0,xtrue.length);//append encrypted file info
        System.arraycopy(xinfo,0,toWrite,xtrue.length,xinfo.length);//append IV
        headers = toWrite;
        Log.d(TAG,"HEADERS:"+OnTheFlyUtils.byteToString(headers));
        CipherInputStream cin = setEncrypt();
        int i, size = 0;
        while ((i=cin.read())!=-1){
            encFos.write(i);
            size+=1;
        }
        encFos.close();
        Log.d(TAG,"Wrote "+(size)+ " bytes (enc_data)");
        Log.d(TAG,"TOTAL: "+(toWrite.length+size)+" bytes");
        Log.d(TAG,"Deleting "+toEncrypt.getName() +toEncrypt.delete());
    }

    private CipherInputStream setEncrypt() throws Exception{
        Log.d(TAG,"Setting main encryption");
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
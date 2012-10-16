package com.otfe.caravans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Log;

public class OnTheFlyDecryptor{
    private File toDecrypt;
    private File dest_file;
    private File dest_folder;
    
    private SecretKey secretKey;
    private String algorithm;
    private String provider;
    private String extension;
    private String[] info;
    
    private CipherInputStream cin;
    private byte[] iv;
    private long fSize;
    private boolean checked=false;
    private final String TAG = "OTF Decryptor";
    
    /* boolean flag that determines if all
       info needed is set for decryption */
    private boolean ok = true;

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    public OnTheFlyDecryptor(String password, String src_path, String destFolder, String algo){
        try{
            Log.d(TAG,"TARGET: "+src_path+"\nDEST: "+destFolder);
            toDecrypt = new File(src_path);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            provider = OnTheFlyUtils.getProvider(algo);
            dest_folder = new File(destFolder);
            //get the first 16 bytes w/c is the IV
            FileInputStream fis = new FileInputStream(toDecrypt);
            iv = OnTheFlyUtils.read(fis,16,0);
            fis.close();
            Log.d(TAG,"IV:"+OnTheFlyUtils.byteToString(iv));
            if (!toDecrypt.isFile()){
                ok = false;
                Log.d(TAG,"Not OK toDecrypt");
            }
            Log.d(TAG,"Algo: "+algorithm);
        }catch(Exception e){
            ok=false;
            e.printStackTrace();
        }
    }

    public boolean isReady(){
        return ok;
    }

    public boolean decrypt(){
        Log.d(TAG,"decrypting");
        try{
            if (!checked)
                ok=check();
            Log.d(TAG,"Check: "+ok);
            if (ok){
                setExtension();
                String d = dest_folder+ File.separator + OnTheFlyUtils.getRawFileName(toDecrypt) + extension;
                Log.d(TAG,"FINAL DEST: *"+d+"*");
                dest_file = new File(d);
                dest_file.createNewFile();
                Log.d(TAG,"Password Correct");
                writeOut(cin);
                Log.d(TAG,"File Verified: "+verifySize());
            }else
                Log.d(TAG,"NOT OK");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
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
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",provider);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(toDecrypt);
        long skp = fis.skip(skip);
        Log.d(TAG,"Skipped "+skp+" bytes");
        return new CipherInputStream(fis,cipher);
    }
    
    public boolean correctPassword(){
    	ok = check();
    	Log.d(TAG,"correct pass: "+ok);
    	return ok;
    }
    
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
            return false;
        }
    }
    
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
    
    private void setExtension(){
        if (!info[1].equals("none"))
            extension = "."+info[1].trim();
        else
            extension = "";
    }

    private boolean verifySize(){
        long originalSize = Long.parseLong(info[2]);
        return originalSize == fSize;
    }
    
    public boolean verifyChecksum(String checksum){
    	try{
    		byte[] bsum = OnTheFlyUtils.md5Sum(dest_file);
    		return checksum.equals(OnTheFlyUtils.byteToString(bsum));
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
}
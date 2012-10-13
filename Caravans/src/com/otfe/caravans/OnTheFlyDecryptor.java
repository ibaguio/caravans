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
    private SecretKey secretKey;
    private String algorithm;
    private String provider;
    private String extension;
    private String header;
    private File dest_folder;
    private byte[] iv;

    /* boolean flag that determines if all
       info needed is set for decryption */
    private boolean ok = true;

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }
    
    public OnTheFlyDecryptor(String password, String src_path, String dest_folder, String algo){
        try{
            Log.d("OTF Decryptor","TARGET: "+src_path+"\nDEST: "+dest_folder);
            toDecrypt = new File(src_path);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            provider = OnTheFlyUtils.getProvider(algo);
            this.dest_folder = new File(dest_folder);
            iv = OnTheFlyUtils.read(new FileInputStream(toDecrypt),16,0);//get the first 16 bytes w/c is the IV
            if (!toDecrypt.isFile()){
                ok = false;
                Log.d("OTF Decryptor","Not OK toDecrypt");
            }
            Log.d("OTF Decryptor","Algo: "+algorithm);
        }catch(Exception e){
            ok=false;
            e.printStackTrace();
        }
    }

    public boolean isReady(){
        return ok;
    }

    public boolean decrypt(){
        Log.d("OTF Decryptor","decrypting");
        try{
            String info[];
            if ((info = check())!= null){
            	extension = info[0];
            	String d = dest_folder+"/"+OnTheFlyUtils.getRawFileName(toDecrypt)+"."+extension;
            	Log.d("OTF Decryptor","extension:"+extension+"\nFINAL DEST FILE: "+d);
            	dest_file = new File(dest_folder,OnTheFlyUtils.getRawFileName(toDecrypt)+"."+extension);
                Log.d("OTF Decryptor","Password Correct");//continue decrypting
                CipherInputStream cin = setDecrypt();
                writeOut(cin);
            }else
                Log.d("OTF Decryptor","NOT OK");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void writeOut(CipherInputStream cin) throws Exception{
    	dest_file.createNewFile();
        FileOutputStream fos = new FileOutputStream(dest_file);
        int i;
        int size=0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        Log.d("OTF Decryptor","Wrote "+size+ " bytes");
    }

    public static String createNewFileName(String filename, String ftype){
        if (filename.indexOf('.')<0)
            return filename;
        Log.d("OTF Decryptor","Getting filename of "+filename+ " ftype " +ftype);
        String fname = filename.substring(0, filename.indexOf(".enc"));
        Log.d("OTF Decryptor","FILENAME: "+fname);
        return fname + ftype;
    }
    /* returns a CipherInputStream of decoded data  */
    private CipherInputStream setDecrypt(long skip) throws Exception{
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding",provider);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(toDecrypt);
        long skp = fis.skip(skip);
        Log.d("OTF Decryptor","Skipped "+skp+" bytes");
        return new CipherInputStream(fis,cipher);
    }
    
    /* decrypts the first 16 bytes if it is equal to "TRUE"
        to verify if password or algorithm is correct
        returns info if valid*/
    private String[] check() throws Exception{
        /* read 16 bytes from cipherFile */
        FileInputStream fis = new FileInputStream(toDecrypt);
        byte[] enc_headers = OnTheFlyUtils.read(fis,0,16*3);
        try{
            header = decrpytHeaders(enc_headers);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (ptrue.equals("TRUE")){
           String info = decrpytHeaders(OnTheFlyUtils.read(fis,16,0));
           String info2[] = OnTheFlyUtils.extractFileInfo(info);
           Log.d("OTF Decryptor","INFO "+info);
           Log.d("OTF Decryptor",info2[0]+"*"+info2[1]);
           return info2;
        }
        return null;
    }
    /* returns a UTF-8 decrypted string of ctxt */
    private boolean decrpytHeaders(byte[] ctxt) throws Exception{
    	CipherInputStream cin = setDecrypt(0);
    	extractIV(cin);
    	boolean check = extractCheck(cin);
    	if (!check)
    		return false;
    	String info[] = extractInfo(cin);
    	
        Log.d("OTF Decryptor","SecretKey encoding length "+secretKey.getEncoded().length);
        Log.d("OTF Decryptor","Ctxt "+new String(ctxt,"UTF-8"));
        Cipher cipher = Cipher.getInstance(algorithm,provider);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        Log.d("OTF Decryptor","len** "+ctxt.length);
        byte[] out = cipher.doFinal(ctxt);
        String ptxt = new String(out,"UTF-8");
        return ptxt;
    }
    
    private boolean extractIV(CipherInputStream cin){
    	try{
    		iv = new byte[16];
    		cin.read(iv);
    	}catch(Exception e){
    		return false;
    	}
    	return true;
    }
    
    private boolean extractCheck(CipherInputStream cin){
    	try{
    		byte check[] = new byte[16];
    		cin.read(check);
    		return OnTheFlyUtils.byteToString(check).equals("TRUE");
    	}catch(Exception e){
    		return false;
    	}
    }
    
    private String[] extractInfo(CipherInputStream cin) {
    	try{
	    	byte[] info_byte = new byte[16];
	    	cin.read(info_byte);
	    	return OnTheFlyUtils.extractFileInfo(OnTheFlyUtils.byteToString(info_byte));
    	}catch(Exception e){
    		return null;
    	}
    }
    
    public boolean correctPassword(){
    	Log.d("OTF Decryptor","Checking if password is correct for "+toDecrypt.getAbsolutePath());
    	try{
    		if (check()!= null)
    			return true;
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }
}
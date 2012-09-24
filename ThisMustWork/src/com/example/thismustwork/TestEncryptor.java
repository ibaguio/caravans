package com.example.thismustwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class TestEncryptor {
	public final int ENCRYPT = 0;
	public final int DECRYPT = 1;

	private int action;
	private File f;
	private String dir;
	private String password;
	public TestEncryptor(int action,File f,String password){
		if (f.isDirectory()){
			Log.d("ERROR","File to be enc/dec is a directory");
		}
		this.action = action;
		this.f = f; //file to manipulate
		this.password = password;
		this.dir = f.getParent();
	}
	
	public boolean doIt(){
		if (this.action == this.ENCRYPT){
			try{
				this.encrypt();
			}catch(Exception e){
				Log.d("Enc ERR","Error on encrypting file");
				Log.d("Enc Err",""+e.toString());
			}
			return true;
		}else if (this.action == this.DECRYPT){
			try{
				this.decrypt();
			}catch(Exception e){
				Log.d("Dec ERR","Error on decrypting file");
				Log.d("Dec Err",""+e.toString());
			}
			return true;
		}
		return false;
	}
	
	private void encrypt() throws Exception{
		SecretKey key = this.getKey(this.password.toCharArray());
		String filename = f.getName();
		byte[] iv = this.generateIV(this.password);
		
		Log.d("enc","Encrypting "+dir+"/"+filename);
		Log.d("enc","Password: "+this.password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,key,new IvParameterSpec(iv));
        CipherInputStream cin = new CipherInputStream(new FileInputStream(dir+"/"+filename),cipher);
        FileOutputStream fos = new FileOutputStream(new File(dir+"/encxx_"+filename));//openFileOutput(dir+"/encxx_"+filename, MODE_PRIVATE);//
        int i;
        int size = 0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        Log.d("","Wrote "+size+" bytes to encxx_"+filename);
	}
	
	private void decrypt() throws Exception{
		SecretKey key = this.getKey(this.password.toCharArray());
		String filename = f.getName();
		byte[] iv = this.generateIV(this.password);
		
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        FileInputStream fis =null;
        try{
            fis = new FileInputStream(dir+"/"+filename);
        }catch(FileNotFoundException e){
            Log.d("Err Dec","File not found "+dir+"/"+filename);
        }
        CipherInputStream cin = new CipherInputStream(fis,cipher);
        FileOutputStream fos = new FileOutputStream(new File(dir+"/decxx_"+filename));
        int i;
        int size=0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        Log.d("","Wrote "+size+" bytes");
	}
	
	/* hash the password and get the first 16 chars*/
    private byte[] generateIV(String password) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes("UTF-8"));
        byte[] hash = md.digest();
        System.out.println("MD5 Hash: "+new String(hash,"UTF-8"));
        return Arrays.copyOfRange(hash, 0,16);
    }
    /* generate secret key to be used by cipher*/
    private SecretKey getKey(char[] password) throws Exception{
        byte[] salt = "saltsalt".getBytes("UTF-8");//hardcoded salt
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }
}

package thesis;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import java.util.*;
import thesis.OnTheFlyUtils;

public class OnTheFlyDecryptor{
    private File toDecrypt;
    private File dest_file;
    private SecretKey secretKey;
    private String algorithm;
    private byte[] iv;

    /* boolean flag that determines if all
       info needed is set for decryption */
    private boolean ok = true;

    public OnTheFlyDecryptor(String src_path, String src_dest, String password, String algo){
        try{
            System.out.println("TARGET: "+src_path+"\nDEST: "+src_dest);
            toDecrypt = new File(src_path);
            dest_file = new File(src_dest);
            secretKey = OnTheFlyUtils.getKey(password.toCharArray());
            algorithm = algo;
            iv = OnTheFlyUtils.read(new FileInputStream(toDecrypt),16,0);//get the first 16 bytes w/c is the IV
            if (!toDecrypt.isFile()){
                ok = false;
                System.out.println("Not OK toDecrypt");
            }
            System.out.println("Algo: "+algorithm);
        }catch(Exception e){
            ok=false;
            e.printStackTrace();
        }
    }

    public boolean isReady(){
        return ok;
    }

    public boolean decrypt(){
        System.out.println("decrypting");
        try{
            String info[];
            if ((info = check())!= null){
                System.out.println("Password Correct");//continue decrypting
                CipherInputStream cin = setDecrypt();
                writeOut(cin);
            }else
                System.out.println("NOT OK");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void writeOut(CipherInputStream cin) throws Exception{
        FileOutputStream fos = new FileOutputStream(dest_file);
        int i;
        int size=0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        System.out.println("Wrote "+size+ " bytes");
    }


    public static String getFileName(String filename, String ftype){
        if (filename.indexOf('.')<0)
            return filename;
        System.out.println("Getting filename of "+filename+ " ftype " +ftype);
        String fname = filename.substring(0, filename.indexOf(".enc"));
        System.out.println("FILENAME: "+fname);
        return fname + ftype;
    }

    /**
     *  returns a CipherInputStream of decoded data  
     */
    private CipherInputStream setDecrypt()throws Exception{
        Cipher cipher = Cipher.getInstance(algorithm+"/CBC/PKCS5Padding","BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(toDecrypt);
        long skp=fis.skip(3*16);
        System.out.println("Skipped "+skp+" bytes");
        return new CipherInputStream(fis,cipher);
    }
    /* decrypts the first 16 bytes if it is equal to "TRUE"
        to verify if password or algorithm is correct
        returns info if valid*/

    private String[] check() throws Exception{
        /* read 16 bytes from cipherFile */
        FileInputStream fis = new FileInputStream(toDecrypt);
        byte[] enc_true = OnTheFlyUtils.read(fis,16,16);
        System.out.println("LEN: "+enc_true.length);
        String ptrue;
        try{
            ptrue = decrpytEBC(enc_true);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (ptrue.equals("TRUE")){
           String info = decrpytEBC(OnTheFlyUtils.read(fis,16,0));
           String info2[] = OnTheFlyUtils.extractFileInfo(info);
           System.out.println("INFO "+info);
           System.out.println(info2[0]+"*"+info2[1]);
           return info2;
        }
        return null;
    }
    /* returns a UTF-8 decrypted string of ctxt */
    private String decrpytEBC(byte[] ctxt) throws Exception{
        System.out.println("SecretKey encoding length "+secretKey.getEncoded().length);
        System.out.println("Ctxt "+new String(ctxt,"UTF-8"));
        Cipher cipher = Cipher.getInstance(algorithm,"BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        System.out.println("len** "+ctxt.length);
        byte[] out= cipher.doFinal(ctxt);
        String ptxt = new String(out,"UTF-8");
        return ptxt;
    }
}
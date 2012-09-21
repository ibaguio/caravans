import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import java.util.*;

public class CipherStreamEncrypt{
    public static void main(String args[]){
        try{
            String cmd = null;
            String password = null;
            String filename = null;
            if (args.length==3){
                cmd = args[0];
                filename = args[1];
                password = args[2];
            }else if(args.length == 0){
                cmd = getUserInput("Encrypt or Decrypt? (Enc or Dec): ");
                filename = getUserInput("Input Filename: ");
                password = getUserInput("Input Password: ");
            }else{
                System.out.println("Usage: java CipherStreamEncrypt <[enc|dec]> <filename> <password>");
                System.exit(0);
            }
            if (cmd.equals("enc")){
                System.out.println("Encrypting "+filename);
                System.out.println("Password "+password);
                System.out.println("Size: "+new File(filename).length()+" bytes");
                byte[] iv = generateIV(password);
                encrypt(getKey(password.toCharArray()),filename,iv);
            }else{
                System.out.println("Decrypting "+filename);
                System.out.println("Password "+password);
                System.out.println("Size: "+new File(filename).length()+" bytes");
                byte[] iv = generateIV(password);
                decrypt(getKey(password.toCharArray()),filename,iv);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /* hash the password and get the first 16 chars*/
    private static byte[] generateIV(String password) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes("UTF-8"));
        byte[] hash = md.digest();
        System.out.println("MD5 Hash: "+new String(hash,"UTF-8"));
        return Arrays.copyOfRange(hash, 0,16);
    }

    private static SecretKey getKey(char[] password) throws Exception{
        byte[] salt = "saltsalt".getBytes("UTF-8");//hardcoded salt
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    private static void encrypt(SecretKey key, String filename,byte[] iv) throws Exception{        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,key,new IvParameterSpec(iv));
        System.out.println("IV: "+new String(iv,"UTF-8")+"len: "+iv.length);
        CipherInputStream cin = new CipherInputStream(new FileInputStream(filename),cipher);
        FileOutputStream fos = new FileOutputStream(new File("encxx_"+filename));
        int i;
        int size = 0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        System.out.println("Wrote "+size+" bytes to encxx_"+filename);
    }

    private static void printToFile(String filename,byte[] data) throws Exception{
        FileOutputStream fos = new FileOutputStream(new File(filename));
        DataOutputStream dos = new DataOutputStream(fos);
        dos.write(data);
        dos.close();
        System.out.println("Wrote "+data.length+" bytes to "+filename);
    }

    private static void decrypt(SecretKey key, String filename,byte[] iv) throws Exception{
        System.out.println("IV: "+new String(iv,"UTF-8")+"*");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        FileInputStream fis =null;
        try{
            fis = new FileInputStream(filename);
        }catch(FileNotFoundException e){
            fis = new FileInputStream("encxx_"+filename);
        }
        CipherInputStream cin = new CipherInputStream(fis,cipher);
        FileOutputStream fos = new FileOutputStream(new File("decxx_"+filename));
        int i;
        int size=0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        System.out.println("Wrote "+size+" bytes");
    }

    private static String getUserInput(String prompt){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(prompt);
        String input = "";
        try{
            input = in.readLine();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static byte[] getFileBytes(String filename, int size){
        byte[] out = new byte[size];//64 bytes
        try{
            FileInputStream fis = new FileInputStream(filename);
            DataInputStream dis = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            int c;
            for (int i=0; (c = br.read()) != -1 ;i++) 
                 out[i] = (byte)c;
            System.out.println("Read "+out.length+" bytes");
        }catch(Exception e){
            System.out.println("Error fetching file");
            return null;
        }
        return out;
    }
}
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;

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
                encrypt(getKey(password.toCharArray()),filename);
            }else{
                System.out.println("Decrypting "+filename);
                System.out.println("Password "+password);
                System.out.println("Size: "+new File(filename).length()+" bytes");
                decrypt(getKey(password.toCharArray()),filename);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
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

    private static void encrypt(SecretKey key, String filename) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        CipherInputStream cin = new CipherInputStream(new FileInputStream(filename),cipher);
        FileOutputStream fos = new FileOutputStream(new File("encxx_"+filename));
        int i;
        int size = 0;
        while ((i=cin.read())!=-1){
            fos.write(i);
            size+=1;
        }
        System.out.println("Wrote "+size+" bytes"); 
    }

    private static void decrypt(SecretKey key, String filename) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
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
}
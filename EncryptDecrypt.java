import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;

public class EncryptDecrypt{
    public static void test(String args[]){
    /*    try{
            String cmd = null;
            String pass = null;
            String fname = null;
            if (args.length==3){
                cmd = args[0];
                fname = args[1];
                pass = args[2];
            }else if(args.length == 0){
                cmd = getUserInput("Encrypt or Decrypt? (Enc or Dec): ");
                fname = getUserInput("Input Filename: ");
                pass = getUserInput("Input Password: ");
            }else{
                System.out.println("Error in params");
                System.exit(0);
            }
            if (cmd.equals("enc")){
                System.out.println("Encrypting");
                System.out.println("Pass "+pass);
                byte[] fileContent = getFile(fname).getBytes();
                printToFile(encryptAES(fileContent,pass.toCharArray()),"enc_"+fname);
            }else{
                System.out.println("Decrypting");
                System.out.println("Pass "+pass);
                byte[] fileContent = getFile(fname).getBytes();
                byte[] iv = getFileBytes("iv");
                System.out.println("IV length: "+iv.length);
                printToFile(decryptAES(iv,fileContent,pass.toCharArray()).getBytes(),"enc_"+fname);
            }
        }catch(Exception e){
            e.printStackTrace();
        }*/
    }

    public static void main(String args[]){
        try{
            String fname = null;
            String password = "ivandominicbaguio";
            if (args.length==1){
                fname = args[0];
            }else if(args.length == 0){
                fname = getUserInput("Input Filename: ");
            }else{
                System.out.println("Error in params");
                System.exit(0);
            }
            byte[] f = getFile(fname);
            ec(password.toCharArray(),f,fname);
        }catch(Exception e){
            System.out.println("error");
            e.printStackTrace();
        }

    }

    private static void ec(char[] password,byte[] msg,String filename) throws Exception{
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[8];
        sr.nextBytes(salt);

        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        System.out.println("Provider: "+cipher.getProvider()+" Block size: "+cipher.getBlockSize());
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        System.out.println("Output size: "+cipher.getOutputSize(msg.length));
        byte[] ciphertext = cipher.doFinal(msg);
        printToFile(ciphertext,"encoded_"+filename);

        /* Decrypt the message, given derived key and initialization vector. */
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        System.out.println("Output size: "+cipher.getOutputSize(msg.length));
        System.out.println("IV: "+cipher.getIV() );
        String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
        printToFile(plaintext.getBytes("UTF-8"),"decoded_"+filename);
    }

    private static void printToFile(byte[] bytes,String filename) throws Exception{
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(filename));
        fos.write(bytes);
        fos.close();
        System.out.println("Wrote "+bytes.length+ " bytes to "+filename);
    }

    private static byte[] getFile(String filename){
        File file = new File(filename);
        System.out.println("File name: "+filename+" size: " + file.length()+ " bytes");
        byte[] result = new byte[(int)file.length()];
        int totalBytesRead = 0;
        try{
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            while(totalBytesRead < result.length){
                int bytesRemaining = result.length - totalBytesRead;
                int bytesRead = is.read(result, totalBytesRead, bytesRemaining); 
                if (bytesRead > 0)
                    totalBytesRead = totalBytesRead + bytesRead;
            }
            return result;
        }catch(Exception e){
            System.out.println("Error fetching file");
            return null;
        }
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
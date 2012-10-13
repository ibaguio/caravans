package thesis;

import java.io.*;
import thesis.OnTheFlyEncryptor;
import thesis.OnTheFlyDecryptor;

public class TestDriver{
    public static void main(String args[]){
        try{
            String cmd = null;
            String password = null;
            String filename = null;
            if (args.length==3){
                cmd = args[0];
                filename = args[1];
                password = args[2];
                System.out.println();
                for (int i=0;i<args.length;i++)
                    System.out.print(args[i] +"  ");
                System.out.println("Filetype: "+getFileExtention(filename));
            }else if(args.length == 0){
                cmd = getUserInput("Encrypt or Decrypt? (Enc or Dec): ");
                filename = getUserInput("Input Filename: ");
                password = getUserInput("Input Password: ");
            }else{
                System.out.println("Usage: java TestDriver <[enc|dec]> <filename> <password>");
                System.exit(0);
            }
            if (cmd.equals("enc")){
                
                System.out.println("Encrypting "+filename);
                System.out.println("Password "+password);
                System.out.println("Size: "+new File(filename).length()+" bytes");
                OnTheFlyEncryptor otfe = new OnTheFlyEncryptor(password,filename,OnTheFlyUtils.SERPENT);
                if (otfe.isReady()) otfe.encrypt();
            }else{
                System.out.println("Decrypting "+filename);
                System.out.println("Password "+password);
                String dest = OnTheFlyDecryptor.getFileName(filename,".txt");
                OnTheFlyDecryptor otfd = new OnTheFlyDecryptor(filename,dest,password,OnTheFlyUtils.SERPENT);
                if (otfd.isReady()) otfd.decrypt();
            }
        }catch(Exception e){
            e.printStackTrace();
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

        public static String getFileExtention(String filename) throws java.io.IOException{
        String ext = null;
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) ext = filename.substring(i+1).toLowerCase();
        if(ext == null) return "";
        return ext;
    }
}
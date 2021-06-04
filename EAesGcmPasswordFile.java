package com.crypto;

import com.crypto.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;



public class EAesGcmPasswordFile { //The original class name is EncryptorAesGcmPasswordFile

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static byte[] encrypt(byte[] pText, String password) throws Exception {

        // 16 bytes salt
        byte[] salt = CryptoUtils.getRandomNonce(SALT_LENGTH_BYTE);

        // GCM recommended 12 bytes iv?
        byte[] iv = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);

        // secret key from password
        SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] cipherText = cipher.doFinal(pText);

        // prefix IV and Salt to cipher text
        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                .put(iv)
                .put(salt)
                .put(cipherText)
                .array();

        return cipherTextWithIvSalt;

    }

    // we need the same password, salt and iv to decrypt it
    private static byte[] decrypt(byte[] cText, String password) throws Exception {

        // get back the iv and salt that was prefixed in the cipher text
        ByteBuffer bb = ByteBuffer.wrap(cText);

        byte[] iv = new byte[12];
        bb.get(iv);

        byte[] salt = new byte[16];
        bb.get(salt);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        // get back the aes key from the same password and salt
        SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] plainText = cipher.doFinal(cipherText);
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("Decrypted text:"+plainText );
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        return plainText;

    }

    //public static void encryptFile(String fromFile, String toFile, String password) throws Exception {
public static void encryptFile(File fromFile, File toFile, String password) throws Exception {
        // read a normal txt file
     //  byte[] fileContent = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fromFile).toURI()));
	   
	   	byte[] fileContent= readContentIntoByteArray(fromFile);

        // encrypt with a password
        byte[] encryptedText = EAesGcmPasswordFile.encrypt(fileContent, password);

        // save a file
        Path path = Paths.get(toFile.toString());

        Files.write(path, encryptedText);

    }

    public static byte[] decryptFile(File fromEncryptedFile, String password) throws Exception {

        // read a file
        //byte[] fileContent = Files.readAllBytes(Paths.get(fromEncryptedFile));
	byte[] fileContent= readContentIntoByteArray(fromEncryptedFile);
        return EAesGcmPasswordFile.decrypt(fileContent, password);

    }

    public static void main(String[] args) throws Exception {

        String password = "password123";
       // String fromFile = "readme.txt"; // from resources folder
					 //createWindow();
			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfc.setCurrentDirectory(new File("."));
				jfc.setDialogTitle("Specify a file to open"); 
			int returnValue = jfc.showOpenDialog(null);
			// int returnValue = jfc.showSaveDialog(null);
			File fromFile=null;
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				System.out.println("file selectedFile:"+selectedFile);
					 fromFile=selectedFile;
			}

	
		
       // String toFile = "readme.encrypted.txt";
	   
				JFileChooser jfc2 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jfc2.setCurrentDirectory(new File("."));
				jfc2.setDialogTitle("Specify a file to save"); 
			int returnValue2 = jfc2.showSaveDialog(null);
			// int returnValue = jfc.showSaveDialog(null);
			File toFile=null;
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile2 = jfc2.getSelectedFile();
				System.out.println("file selectedFile:"+selectedFile2);   
				 toFile=selectedFile2;
			}



        // encrypt file
        EAesGcmPasswordFile.encryptFile(fromFile, toFile, password);

        // decrypt file
        byte[] decryptedText = EAesGcmPasswordFile.decryptFile(toFile, password);
        String pText = new String(decryptedText, UTF_8);
        System.out.println(pText);

    }
	
	public static byte[] readContentIntoByteArray(File file)
	   {
		  FileInputStream fileInputStream = null;
		  byte[] bFile = new byte[(int) file.length()];
		  try
		  {
			 //convert file into array of bytes
			 fileInputStream = new FileInputStream(file);
			 fileInputStream.read(bFile);
			 fileInputStream.close();
			 for (int i = 0; i < bFile.length; i++)
			 {
				System.out.print((char) bFile[i]);
			 }
			 System.out.println();
		  }
		  catch (Exception e)
		  {
			 e.printStackTrace();
		  }
		  return bFile;
	   }


}

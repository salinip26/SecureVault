package com.example.securevault.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Util {

    private static SecretKey secretKey;
    private static Cipher cipher;

    static {
        try {
            secretKey = KeyGenerator.getInstance("AES").generateKey();
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static File encrypt(String inputFilePath, String encryptedFileName) {
        try (FileInputStream fileIn = new FileInputStream(inputFilePath);
             FileOutputStream fileOut = new FileOutputStream(encryptedFileName)) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            fileOut.write(iv);

            try (CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
                byte[] inputBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileIn.read(inputBuffer)) != -1) {
                    cipherOut.write(inputBuffer, 0, bytesRead);
                }
            }
            return new File(encryptedFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File decrypt(File encryptedFile, String decryptedFileName) throws FileNotFoundException {
        try{
        try (FileInputStream fileIn = new FileInputStream(encryptedFile);
             FileOutputStream fileOut = new FileOutputStream(decryptedFileName)) {
            byte[] iv = new byte[16];
            fileIn.read(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            try (CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher)) {
                byte[] inputBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = cipherIn.read(inputBuffer)) != -1) {
                    fileOut.write(inputBuffer, 0, bytesRead);
                }
            }
            return new File(decryptedFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }}catch (Exception e){
            return null;
        }
    }

//    public static void main(String[] args) {
//        // Example usage
//        File inputFile = new File("input.txt");
//        File encryptedFile = encrypt(inputFile, "encrypted.enc");
//        System.out.println("File encrypted: " + encryptedFile);
//
//        File decryptedFile = decrypt(encryptedFile, "decrypted.txt");
//        System.out.println("File decrypted: " + decryptedFile);
//    }
}

package licenta.medicaldata.crypto;

import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class AesEncryption {

    private static String keyFile = "keyFile";
    private static String ivFile = "ivFile";

    public IvParameterSpec generateIV() {
        SecureRandom srandom = new SecureRandom();
        byte[] iv = new byte[128 / 8];
        srandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        try (FileOutputStream out = new FileOutputStream(ivFile)) {
            out.write(iv);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ivspec;
    }

    public SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecretKey skey = kgen.generateKey();
        saveKey(skey);
        return skey;
    }

    public void saveKey(SecretKey skey){
        try (FileOutputStream out = new FileOutputStream(keyFile)) {
            byte[] keyb = skey.getEncoded();
            out.write(keyb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SecretKey loadKey(){
        byte[] keyb = new byte[0];
        try {
            keyb = Files.readAllBytes(Paths.get(keyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SecretKeySpec skey = new SecretKeySpec(keyb, "AES");
        return skey;
    }

    public IvParameterSpec loadIvSpec() throws IOException {
        byte[] iv = Files.readAllBytes(Paths.get(ivFile));
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        return ivspec;
    }

    public byte[] encrypt(SecretKey skey, IvParameterSpec ivspec, byte[] file) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher ci = null;
        try {
            ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
        byte[] encoded=ci.doFinal(file);
        return encoded;
    }

    public byte[] encryptCall (byte[] file){
        try {
            SecretKey skey = loadKey();
            IvParameterSpec ivParameterSpec =loadIvSpec();
            return encrypt(skey, ivParameterSpec, file);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt (SecretKey skey, IvParameterSpec ivspec, byte[] file) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher ci = null;
        try {
            ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ci.init(Cipher.DECRYPT_MODE, skey, ivspec);
        byte[] decoded=ci.doFinal(file);
        return decoded;
    }

    public byte[] decryptCall (byte[] file){
        try {
            SecretKey skey = loadKey();
            IvParameterSpec ivParameterSpec = loadIvSpec();
            return decrypt(skey, ivParameterSpec, file);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void encryptFile (InputStream in, OutputStream out) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey skey = loadKey();
        IvParameterSpec ivParameterSpec = loadIvSpec();
        Cipher ci = null;
        try {
            ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ci.init(Cipher.ENCRYPT_MODE, skey, ivParameterSpec);
        processFile(ci, in, out);
    }

    public byte[] decryptFile (InputStream in, OutputStream out) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey skey = loadKey();
        IvParameterSpec ivParameterSpec = loadIvSpec();
        Cipher ci = null;
        try {
            ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        ci.init(Cipher.DECRYPT_MODE, skey, ivParameterSpec);
        return processFile(ci, in, out);
    }
    static public byte[] processFile(Cipher ci, InputStream in, OutputStream out) throws javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException, IOException {
        byte[] ibuf = new byte[1024];
        int len;
        byte[] all = new byte[0];
        while ((len = in.read(ibuf)) != -1) {
            byte[] obuf = ci.update(ibuf, 0, len);
            all = (byte[]) ArrayUtils.addAll(all, obuf);
            if (obuf != null) out.write(obuf);
        }
        byte[] obuf = ci.doFinal();
        all = (byte[]) ArrayUtils.addAll(all, obuf);

        if (obuf != null) {
            out.write(obuf);
        }
        in.close();
        out.flush();
        out.close();
        System.out.println("decrypt done function");
        return all;
    }
}


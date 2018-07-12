package licenta.medicaldata.crypto;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignatureRSA {

    private KeyPair keypair;


    public Signature generateSignature(){
        try {
            Signature signature = Signature.getInstance("SHA256WithDSA");
            return signature;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public KeyPair getKeypair() {
        return keypair;
    }

    public void setKeypair(KeyPair keypair) {
        try {
            this.keypair = generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey(byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PublicKey pk = getPublicKey(publicKey);
        Signature signature2 = Signature.getInstance("SHA256WithRSA");
        signature2.initVerify(pk);

        signature2.update(data);

        boolean verified = signature2.verify(signature);
        return verified;
    }

    public void storePrivateKey(PrivateKey key, String fileName) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec priv = fact.getKeySpec(key,
                RSAPrivateKeySpec.class);
        saveToFile(fileName,
                priv.getModulus(), priv.getPrivateExponent());

    }

    private static void saveToFile(String fileName,
                                   BigInteger mod, BigInteger exp) throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)));
        oout.writeObject(mod);
        oout.writeObject(exp);
        oout.close();
    }

    public static PrivateKey readPrivateKey(String filename) throws IOException {
        InputStream in = new FileInputStream(filename);
        ObjectInputStream oin =
                new ObjectInputStream(new BufferedInputStream(in));
        try {
            BigInteger m = (BigInteger) oin.readObject();
            BigInteger e = (BigInteger) oin.readObject();
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = (PrivateKey) fact.generatePrivate(keySpec);
            oin.close();
            return privateKey;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] sign(PrivateKey privateKey , byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        SecureRandom secureRandom = new SecureRandom();
        signature.initSign(privateKey, secureRandom);
        signature.update(data);
        return signature.sign();
    }

    public void storeSignature(byte[] signature, String filename){
        try (FileOutputStream out = new FileOutputStream("signature/"+filename)) {
            out.write(signature);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getSignature(String filename) throws IOException {
        byte[] signature;
        signature = Files.readAllBytes(Paths.get("signature/"+filename));
        return signature;
    }
}

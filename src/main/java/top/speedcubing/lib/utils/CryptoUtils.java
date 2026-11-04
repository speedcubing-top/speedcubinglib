package top.speedcubing.lib.utils;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import top.speedcubing.lib.utils.bytes.DataConversion;

public class CryptoUtils {
    public static String toSHA256(byte[] data) throws NoSuchAlgorithmException {
        return DataConversion.byteArrayToHexString(MessageDigest.getInstance("SHA-256").digest(data));
    }

    public static String toSHA1(byte[] data) throws NoSuchAlgorithmException {
        return DataConversion.byteArrayToHexString(MessageDigest.getInstance("SHA-1").digest(data));
    }

    public static String toMD5(byte[] data) throws NoSuchAlgorithmException {
        return DataConversion.byteArrayToHexString(MessageDigest.getInstance("MD5").digest(data));
    }

    public static byte[] decryptRsa(KeyPair keyPair, byte[] bytes) throws GeneralSecurityException {
        return decryptRsa(keyPair.getPrivate(), bytes);
    }

    public static byte[] decryptRsa(byte[] key, byte[] bytes) throws GeneralSecurityException {
        return decryptRsa(toPrivateRSA(key), bytes);
    }

    public static byte[] decryptRsa(PrivateKey privateKey, byte[] bytes) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(bytes);
    }

    public static byte[] encryptRSA(KeyPair keyPair, byte[] bytes) throws GeneralSecurityException {
        return encryptRSA(keyPair.getPublic(), bytes);
    }

    public static byte[] encryptRSA(byte[] key, byte[] bytes) throws GeneralSecurityException {
        return encryptRSA(toPublicKeyRSA(key), bytes);
    }

    public static byte[] encryptRSA(PublicKey publicKey, byte[] bytes) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(bytes);
    }

    public static PublicKey toPublicKeyRSA(byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey toPrivateRSA(byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}

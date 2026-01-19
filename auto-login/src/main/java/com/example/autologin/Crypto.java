package com.example.autologin;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class Crypto {

    public static class Result {
        public String enc;
        public String salt;
        public String iv;
    }

    public static Result encrypt(String plain, char[] master) throws Exception {
        byte[] salt = random(16);
        byte[] iv = random(12);

        byte[] key = kdf(master, salt);

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
               new GCMParameterSpec(128, iv));

        byte[] enc = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        Result r = new Result();
        r.enc = b64(enc);
        r.salt = b64(salt);
        r.iv = b64(iv);
        return r;
    }

    public static String decrypt(AutoLoginConfig.Credential cred, char[] master)
            throws Exception {

        byte[] salt = b64(cred.salt);
        byte[] iv = b64(cred.iv);
        byte[] enc = b64(cred.enc);

        byte[] key = kdf(master, salt);

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
               new GCMParameterSpec(128, iv));

        byte[] dec = c.doFinal(enc);
        return new String(dec, StandardCharsets.UTF_8);
    }

    private static byte[] kdf(char[] master, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(master, salt, 65536, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return f.generateSecret(spec).getEncoded();
    }

    private static byte[] random(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static byte[] b64(String s) {
        return Base64.getDecoder().decode(s);
    }

    private static String b64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }
}

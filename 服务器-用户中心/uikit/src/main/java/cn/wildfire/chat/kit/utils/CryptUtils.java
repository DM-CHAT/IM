package cn.wildfire.chat.kit.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.util.Base64.NO_WRAP;

public class CryptUtils {
    public static byte[] sha256(byte[] data){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            data = messageDigest.digest();
        } catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
    public static String aesEncrypt(byte[] data, String key){
        try {
            byte[] keyHash = sha256(key.getBytes());
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyHash, "AES"), new IvParameterSpec(iv));
            byte[] encData = cipher.doFinal(data);
            return Base64.encodeToString(encData,NO_WRAP);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

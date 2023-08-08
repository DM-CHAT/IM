package cn.wildfire.chat.app.utils;

import static android.util.Base64.NO_WRAP;

import android.util.Base64;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class OsnUtils {
    private static final SimpleDateFormat mFormater= new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");

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
    public static String sha256s(byte[] data){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            data = messageDigest.digest();
            return b64Encode(data);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] genRipeMD160(byte[] input) {
        //byte[] sha256 = sha256(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(input, 0, input.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static String b64Encode(byte[] data){
        return Base64.encodeToString(data, NO_WRAP);
    }
    public static byte[] b64Decode(String data){
        return Base64.decode(data, 0);
    }
    public static String aesEncrypt(byte[] data, byte[] key){
        try {
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] encData = cipher.doFinal(data);
            return b64Encode(encData);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String aesEncrypt(String data, String key){
        byte[] pwdHash = sha256(key.getBytes());
        return aesEncrypt(data.getBytes(), pwdHash);
    }
    public static byte[] aesDecrypt(byte[] data, byte[] key){
        try {
            byte[] iv = new byte[16];
            Arrays.fill(iv,(byte)0);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(data);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String aesDecrypt(String data, String key){
        byte[] pwdHash = sha256(key.getBytes());
        byte[] decData = b64Decode(data);
        if(decData == null)
            return null;
        decData = aesDecrypt(decData,pwdHash);
        return new String(decData);
    }
    public static byte[] getAesKey(){
        byte[] key = new byte[32];
        Random random = new Random();
        for(int i = 0; i < 32; ++i)
            key[i] = (byte)random.nextInt(256);
        return key;
    }


}

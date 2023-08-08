package com.mhhy.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class Aes128Util {

    /**
     * 加密 模式:AES/CBC/PKCS7Padding
     * @param content 明文
     * @return 密文
     */
    public static String AESEncode(String content, byte[] keyByte) {

        if (keyByte.length != 32){
            return null;
        }

        byte[] salt = new byte[keyByte.length / 2];
        byte[] encryptKey = new byte[keyByte.length / 2];
        // keybyte 分成2个128字节的byte[]
        // 前一个作为salt
        // 后一个作为key
        System.arraycopy(keyByte,0, salt, 0, keyByte.length / 2);
        System.arraycopy(keyByte,keyByte.length / 2, encryptKey, 0, keyByte.length / 2);

        // 初始化
        Security.addProvider(new BouncyCastleProvider());
        // 转化成JAVA的密钥格式
        Key key = new SecretKeySpec(encryptKey, "AES");
        try {
            // 初始化cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(salt));
            byte[] encryptedText = cipher.doFinal(content.getBytes());

            return Base64.getEncoder().encodeToString(encryptedText);

            //return new String(Hex.encodeHex(encryptedText)).replaceAll("\r\n", "");
        } catch (Exception e) {
            log.info("AESEncode error", e);
        }
        return null;
    }

    /**
     * 解密
     * @param content 密文
     * @return 铭文
     */
    public static String AESDecode(String content, byte[] keyByte) {

        if (keyByte.length != 32){
            return null;
        }

        byte[] salt = new byte[keyByte.length / 2];
        byte[] encryptKey = new byte[keyByte.length / 2];
        // keybyte 分成2个128字节的byte[]
        // 前一个作为salt
        // 后一个作为key
        System.arraycopy(keyByte,0, salt, 0, keyByte.length / 2);
        System.arraycopy(keyByte,keyByte.length / 2, encryptKey, 0, keyByte.length / 2);
        try {
            //byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            SecretKeySpec skeySpec = new SecretKeySpec(encryptKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            IvParameterSpec ivParameter = new IvParameterSpec(salt);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameter);
            try {
                //byte[] encrypted1 = Hex.decodeHex(content);// 先用base64解密
                byte[] encrypted1 = Base64.getDecoder().decode(content);
                byte[] original = cipher.doFinal(encrypted1);
                return new String(original);
            } catch (Exception e) {
                log.info(e.toString());
                return null;
            }
        } catch (Exception ex) {
            log.info(ex.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        //sha256加密
        byte[] sha256Str = PasswordUtil.getSHA256Str("123456");
        //AES加密
        String encrypt = Aes128Util.AESEncode("9121184", sha256Str);

        System.out.println(encrypt);

        String s = Aes128Util.AESDecode(encrypt, sha256Str);
        System.out.println(s);
    }

}


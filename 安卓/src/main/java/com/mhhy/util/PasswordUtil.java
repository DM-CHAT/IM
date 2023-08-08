package com.mhhy.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {
    /**平台加密算法
     *         //前端获取服务端随机数
     *         //前端使用 用户密码+sha256加密 对随机数进行加密
     *         //后端生成密文流程：用户登录密码+sha256+base64--入库
     *         //后端解析前端密文：拿数据库数据进行base64反解密--对前端传递过来的密钥解密--结果是一个服务端生产的随机数。
     *         //随机数去比对缓存里面的随机数
     *         //随机数设置过期时间 5分钟
     */

    //sha256加密
    public static byte[] getSHA256Str(String str){
        MessageDigest messageDigest;
        String encdeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            return hash;
            //encdeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //base64编码
    public static String encoderBase64(byte[] sha256){
        return java.util.Base64.getEncoder().encodeToString(sha256);
    }

    //base64 解码
    public static byte[] decodeBase64(String encoderBase64){
        return java.util.Base64.getDecoder().decode(encoderBase64);
    }

    public static void main(String[] args) {
        //sha256加密
        byte[] sha256Str = PasswordUtil.getSHA256Str("111111");
        //AES加密
        String encrypt = Aes128Util.AESEncode("10908325", sha256Str);

        System.out.println(encrypt);
        String str = "bHCrPMT2MFqhbXdjh_fCRwNQkGruSOU2nk4ZHmRB02H4DLOxzIcpbVOJSZmMPpMMa5HLTPq7PGweInFL9p1YE5sSmlgpUWWl5Kfb_Cstay0Gb4U7HkcS5rNxpiFj45C1SsGxQhG7qEJ7qSxId3Ho_Qgs-T7XTfYM8b_9MM26Yz9-g5EXVoItwXWUlfGEf2dbruFH1jFn4_6wGYawb4SsehIPqmSoTx8oaVRABZcq9Xo=";
        String s = Aes128Util.AESDecode(str, sha256Str);
        String[] mhhy2022s = s.split("MHHY2022");
        System.out.println("token:" + mhhy2022s[0]);
        System.out.println("会话id：" + mhhy2022s[1]);

        String data = "{\"password\": \"111111\"}";
        String result = Aes128Util.AESEncode(data, PasswordUtil.getSHA256Str(mhhy2022s[1]));
        System.out.println(result);
    }

}

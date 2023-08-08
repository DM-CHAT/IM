package com.ospn.osnsdk.utils;

import static com.ospn.osnsdk.utils.OsnUtils.b64Decode;
import static com.ospn.osnsdk.utils.OsnUtils.b64Encode;
import static com.ospn.osnsdk.utils.OsnUtils.genRipeMD160;
import static com.ospn.osnsdk.utils.OsnUtils.sha256;

public class ECUtils {
    private static native byte[] ecSignSSL(byte[] priKey, byte[] data);
    private static native boolean ecVerifySSL(byte[] pubKey, byte[] data, byte[] sign);
    private static native byte[] ecIESEncryptSSL(byte[] pubKey, byte[] data);
    private static native byte[] ecIESDecryptSSL(byte[] priKey, byte[] data);
    private static native byte[] createECKey();
    private static native byte[] getECPublicKey(byte[] priKey);
    public static native String b58Encode(byte[] data);
    public static native byte[] b58Decode(String data);
    private static native byte[] fromPrivate(byte[] data);

    static {
        System.loadLibrary("ecSSL");
    }

    /*private static byte[] toPublicKey(String osnID){
        try {
            if (!osnID.startsWith("OSN") || osnID.length() <= 4)
                return null;
            String pubKey = osnID.substring(4);
            byte[] pKey = b58Decode(pubKey);
            if(pKey == null || pKey.length <= 2)
                return null;
            byte[] rKey = new byte[pKey.length - 2];
            System.arraycopy(pKey, 2, rKey, 0, pKey.length - 2);
            return rKey;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }*/

    public static byte[] toPublicKey(String osnID){
        if (osnID == null) {
            return null;
        }

        if(!osnID.startsWith("OSN"))
            return null;

        if (osnID.length() <= 4)
            return null;

        String pubKey = osnID.substring(4);
        byte[] pKey = b58Decode(pubKey);
        byte[] rKey = new byte[pKey.length-2];
        System.arraycopy(pKey, 2, rKey, 0, pKey.length-2);

        if (rKey.length > 31+20){
            byte[] rKey2 = new byte[rKey.length-20];
            System.arraycopy(rKey, 0, rKey2, 0, rKey2.length);
            return rKey2;
        }

        return rKey;
    }

    private static byte[] toPrivateKey(String osnID){
        if(osnID.length() <= 3)
            return null;
        String priKey = osnID.substring(3);
        return b64Decode(priKey);
    }

    public static String osnHash(byte[] data){
        byte[] hash = sha256(data);
        return b64Encode(hash);
    }
    public static String osnSign(String priKey, byte[] data){
        try {
            byte[] pKey = toPrivateKey(priKey);
            if(pKey == null)
                return null;
            byte[] sign = ecSignSSL(pKey, data);
            return sign == null ? null : b64Encode(sign);
        }
        catch (Exception e){
            OsnUtils.logInfo(e.toString());
        }
        return null;
    }
    public static boolean osnVerify(String osnID, byte[] data, String sign){
        try {
            byte[] signData = b64Decode(sign);
            byte[] pKey = toPublicKey(osnID);
            if(signData == null || pKey == null)
                return false;
            return pKey != null && ecVerifySSL(pKey, data, signData);
        }
        catch (Exception e){
            OsnUtils.logInfo(e.toString());
        }
        return false;
    }
    public static byte[] ecIESEncrypt(String osnID, byte[] data){
        byte[] pKey = toPublicKey(osnID);
        return pKey == null ? null : ecIESEncryptSSL(pKey, data);
    }
    public static byte[] ecIESDecrypt(String priKey, byte[] data){
        byte[] pKey = toPrivateKey(priKey);
        return pKey == null ? null : ecIESDecryptSSL(pKey, data);
    }

    public static String toHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        for (byte value : b) {
            String hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1)
                hex = '0' + hex;
            hs.append(hex);
        }
        return hs.toString();
    }

    public static String[] createOsnIdFromMnemonic(byte[] seed, String password) {

        try {

            String hexStr = toHexString(seed);
            System.out.println("@@@ [seed] : "+hexStr);

            byte[] aesKey = sha256(password.getBytes());

            byte[] hash1 = sha256(seed);
            byte[] hash2 = sha256(hash1);
            System.out.println("@@@ [hash1] : "+toHexString(hash1));
            System.out.println("@@@ [hash2] : "+toHexString(hash2));
            byte[] priKey1 = fromPrivate(hash1);
            byte[] priKey2 = fromPrivate(hash2);
            System.out.println("@@@ [priKey1] : "+toHexString(priKey1));
            System.out.println("@@@ [priKey2] : "+toHexString(priKey2));

            byte[] pubKey1 = getECPublicKey(priKey1);
            byte[] pubKey2 = getECPublicKey(priKey2);


            System.out.println("@@@ [RipeMD160] createOsnIdFromMnemonic befor : " + toHexString(pubKey2));
            byte[] RipeMD160 = genRipeMD160(pubKey2);
            System.out.println("@@@ [RipeMD160] createOsnIdFromMnemonic end : " + toHexString(RipeMD160));
            /*System.out.println("@@@ [RipeMD160] createOsnIdFromMnemonic : " + b64Encode(RipeMD160));
            System.out.println("@@@ [RipeMD160] createOsnIdFromMnemonic size : " + RipeMD160.length);
            System.out.println("@@@ [RipeMD160] createOsnIdFromMnemonic pubKey2 : " + b64Encode(pubKey2));*/

            byte[] address = new byte[2 + pubKey1.length + RipeMD160.length];
            address[0] = 2;
            address[1] = 0;
            String osnType = "OSNU";

            System.arraycopy(pubKey1, 0, address, 2, pubKey1.length);
            System.arraycopy(RipeMD160, 0, address, 2+pubKey1.length, RipeMD160.length);

            System.out.println("@@@ [address] : "+toHexString(address));

            String addrString = osnType + b58Encode(address);
            System.out.println("@@@ [osnid] : "+addrString);
            String priKeys = "VK0" + b64Encode(priKey1);
            System.out.println("@@@ [priKeys] : "+priKeys);
            String priKeys2 = OsnUtils.aesEncrypt(priKey2, aesKey);
            return new String[]{addrString, priKeys, priKeys2};


        } catch (Exception e) {

        }

        return null;
    }




    public static String[] createOsnID(String type){
        try {
            byte[] priKey = createECKey();
            byte[] pubKey = getECPublicKey(priKey);
            if(priKey == null || pubKey == null)
                return null;

            byte[] address = new byte[1 + 1 + pubKey.length]; //version(1)|flag(1)|pubkey(33)
            address[0] = 1;
            address[1] = 0;
            String osnType = "OSNU";
            if (type.equalsIgnoreCase("group")) {
                address[1] = 1;
                osnType = "OSNG";
            }
            else if (type.equalsIgnoreCase("service")) {
                address[1] = 2;
                osnType = "OSNS";
            }
            System.arraycopy(pubKey, 0, address, 2, pubKey.length);
            String addrString = osnType + b58Encode(address);
            String priKeys = "VK0"+b64Encode(priKey);

            return new String[]{addrString, priKeys};
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //    public static byte[] ecDecrypt(String priKey, String data){
//        try {
//            byte[] rawData = b58Decode(data);
//            short keyLength = (short)((rawData[0]&0xff)|((rawData[1]&0xff)<<8));
//            byte[] ecData = new byte[keyLength];
//            System.arraycopy(rawData,2,ecData,0,keyLength);
//            ecData = ecIESDecrypt(priKey, ecData);
//
//            byte[] aesKey = new byte[16];
//            byte[] aesIV = new byte[16];
//            byte[] aesData = new byte[rawData.length-keyLength-2];
//            System.arraycopy(ecData,0,aesKey,0,16);
//            System.arraycopy(ecData,16,aesIV,0,16);
//            System.arraycopy(rawData,keyLength+2,aesData,0,rawData.length-keyLength-2);
//
//            IvParameterSpec iv = new IvParameterSpec(aesIV);
//            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, key, iv);
//            return cipher.doFinal(aesData);
//        }catch (Exception e){
//            OsnUtils.logInfo(e.toString());
//        }
//        return null;
//    }
//    public static String ecEncrypt(String pubKey, byte[] data){
//        byte[] aesKey = new byte[16];
//        byte[] aesIV = new byte[16];
//        Random random = new Random();
//        for(int i = 0; i < 16; ++i){
//            aesKey[i] = (byte)random.nextInt(256);
//            aesIV[i] = 0;
//        }
//        try {
//            IvParameterSpec iv = new IvParameterSpec(aesIV);
//            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
//            byte[] encData = cipher.doFinal(data);
//
//            byte[] encKey = new byte[32];
//            System.arraycopy(aesKey,0,encKey,0,16);
//            System.arraycopy(aesIV,0,encKey,16,16);
//            byte[] encECKey = ecIESEncrypt(pubKey, encKey);
//
//            byte[] eData = new byte[encECKey.length+encData.length+2];
//            eData[0] = (byte)(encECKey.length&0xff);
//            eData[1] = (byte)((encECKey.length)>>8&0xff);
//            System.arraycopy(encECKey,0,eData,2,encECKey.length);
//            System.arraycopy(encData,0,eData,encECKey.length+2,encData.length);
//            return b58Encode(eData);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
    public static String ecEncrypt2(String osnID, byte[] data){
        byte[] encData = ecIESEncrypt(osnID, data);
        return encData == null ? null : b64Encode(encData);
    }
    public static byte[] ecDecrypt2(String priKey, String data){
        byte[] decData = b64Decode(data);
        return decData == null ? null : ecIESDecrypt(priKey, decData);
    }
    public static String pri2pub(byte[] pri) {
        byte[] pubKey = getECPublicKey(pri);
        if (pubKey == null) {
            return null;
        }
        System.out.println("@@@ [RipeMD160] pri2pub pubKey2 : " + b64Encode(pubKey));
        byte[] RipeMD160 = genRipeMD160(pubKey);
        System.out.println("@@@ [RipeMD160] pri2pub genRipeMD160: " + b64Encode(RipeMD160));

        byte[] address = new byte[1 + 1 + pubKey.length];
        address[0] = 1;
        address[1] = 0;
        System.arraycopy(pubKey, 0, address, 2, pubKey.length);



        String pub = "OSNU" + b58Encode(address);
        return pub;
    }

    public static String toOsnPrivacy(byte[] pri) {
        String priKey = "VK0"+b64Encode(pri);
        return priKey;
    }


}

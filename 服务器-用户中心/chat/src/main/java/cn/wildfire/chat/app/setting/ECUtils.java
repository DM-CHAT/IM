package cn.wildfire.chat.app.setting;


import static cn.wildfire.chat.app.setting.OsnUtils.b64Decode;
import static cn.wildfire.chat.app.setting.OsnUtils.b64Encode;
import static cn.wildfire.chat.app.setting.OsnUtils.sha256;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

public class ECUtils {
    private static native byte[] ecSignSSL(byte[] priKey, byte[] data);

    private static native boolean ecVerifySSL(byte[] pubKey, byte[] data, byte[] sign);

    private static native byte[] ecIESEncryptSSL(byte[] pubKey, byte[] data);

    private static native byte[] ecIESDecryptSSL(byte[] priKey, byte[] data);

    private static native byte[] createECKey();

    private static native byte[] getECPublicKey(byte[] priKey);

    private static native byte[] fromPrivate(byte[] data);

    public static native String b58Encode(byte[] data);

    public static native byte[] b58Decode(String data);

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

    public static byte[] toPublicKey(String osnID) {
        if (osnID == null) {
            return null;
        }

        if (!osnID.startsWith("OSN"))
            return null;

        if (osnID.length() <= 4)
            return null;

        String pubKey = osnID.substring(4);
        byte[] pKey = b58Decode(pubKey);
        byte[] rKey = new byte[pKey.length - 2];
        System.arraycopy(pKey, 2, rKey, 0, pKey.length - 2);

        if (rKey.length > 31 + 20) {
            byte[] rKey2 = new byte[rKey.length - 20];
            System.arraycopy(rKey, 0, rKey2, 0, rKey2.length);
            return rKey2;
        }

        return rKey;
    }

    private static byte[] toPrivateKey(String osnID) {
        if (osnID.length() <= 3)
            return null;
        String priKey = osnID.substring(3);
        return b64Decode(priKey);
    }

    public static String osnHash(byte[] data) {
        byte[] hash = OsnUtils.sha256(data);
        return b64Encode(hash);
    }

    public static String osnSign(String priKey, byte[] data) {
        try {
            byte[] pKey = toPrivateKey(priKey);
            if (pKey == null)
                return null;
            byte[] sign = ecSignSSL(pKey, data);
            return sign == null ? null : b64Encode(sign);
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean osnVerify(String osnID, byte[] data, String sign) {
        try {
            byte[] signData = b64Decode(sign);
            byte[] pKey = toPublicKey(osnID);
            if (signData == null || pKey == null)
                return false;
            return pKey != null && ecVerifySSL(pKey, data, signData);
        } catch (Exception e) {
        }
        return false;
    }

    public static byte[] ecIESEncrypt(String osnID, byte[] data) {
        byte[] pKey = toPublicKey(osnID);
        return pKey == null ? null : ecIESEncryptSSL(pKey, data);
    }

    public static byte[] ecIESDecrypt(String priKey, byte[] data) {
        byte[] pKey = toPrivateKey(priKey);
        return pKey == null ? null : ecIESDecryptSSL(pKey, data);
    }
    public static String private2ECKey(byte[] rawKey) {
        byte[] priKey = fromPrivate(rawKey);
        return priKey == null ? null : ("VK0" + b64Encode(priKey));
    }
    public static String[] createOsnIdFromMnemonic(byte[] seed, String password) {

        try {

            byte[] aesKey = sha256(password.getBytes());

            byte[] hash1 = sha256(seed);
            byte[] hash2 = sha256(hash1);
            byte[] priKey1 = fromPrivate(hash1);
            byte[] priKey2 = fromPrivate(hash2);

            byte[] pubKey1 = getECPublicKey(priKey1);
            byte[] pubKey2 = getECPublicKey(priKey2);
            byte[] RipeMD160 = genRipeMD160(pubKey2);

            byte[] address = new byte[2 + pubKey1.length + 20];
            address[0] = 2;
            address[1] = 0;
            String osnType = "OSNU";

            System.arraycopy(pubKey1, 0, address, 2, pubKey1.length);
            System.arraycopy(RipeMD160, 0, address, address.length - 20, RipeMD160.length);
            String addrString = osnType + b58Encode(address);
            String priKeys = "VK0" + b64Encode(priKey1);
            String priKeys2 = "VK1" + OsnUtils.aesEncrypt(priKey2, aesKey);
            return new String[]{addrString, priKeys, priKeys2};


        } catch (Exception e) {

        }

        return null;
    }


    public static byte[] genRipeMD160(byte[] input) {
        byte[] sha256 = sha256(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static String[] createOsnID2(String type, byte[] aesKey) {
        try {
            byte[] priKey = createECKey();
            byte[] pubKey = getECPublicKey(priKey);
            byte[] priKey2 = createECKey();
            byte[] pubKey2 = getECPublicKey(priKey2);
            byte[] RipeMD160 = genRipeMD160(pubKey2);
            byte[] address = new byte[2 + pubKey.length + 20];
            address[0] = 2;
            address[1] = 0;
            String osnType = "OSNU";
            if (type.equalsIgnoreCase("group")) {
                address[1] = 1;
                osnType = "OSNG";
            } else if (type.equalsIgnoreCase("service")) {
                address[1] = 2;
                osnType = "OSNS";
            }

            System.arraycopy(pubKey, 0, address, 2, pubKey.length);
            System.arraycopy(RipeMD160, 0, address, address.length - 20, RipeMD160.length);
            String addrString = osnType + b58Encode(address);
            String priKeys = "VK0" + b64Encode(priKey);
            String priKeys2 = "VK1" + OsnUtils.aesEncrypt(priKey2, aesKey);
            return new String[]{addrString, priKeys, priKeys2};
        } catch (Exception var12) {
            //log.error("", var12);
            return null;
        }
    }

    public static String[] createOsnID(String type) {
        try {
            byte[] priKey = createECKey();
            byte[] pubKey = getECPublicKey(priKey);
            if (priKey == null || pubKey == null)
                return null;

            byte[] address = new byte[1 + 1 + pubKey.length]; //version(1)|flag(1)|pubkey(33)
            address[0] = 1;
            address[1] = 0;
            String osnType = "OSNU";
            if (type.equalsIgnoreCase("group")) {
                address[1] = 1;
                osnType = "OSNG";
            } else if (type.equalsIgnoreCase("service")) {
                address[1] = 2;
                osnType = "OSNS";
            }
            System.arraycopy(pubKey, 0, address, 2, pubKey.length);
            String addrString = osnType + b58Encode(address);
            String priKeys = "VK0" + b64Encode(priKey);

            return new String[]{addrString, priKeys};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String ecEncrypt2(String osnID, byte[] data) {
        byte[] encData = ecIESEncrypt(osnID, data);
        return encData == null ? null : b64Encode(encData);
    }

    public static byte[] ecDecrypt2(String priKey, String data) {
        byte[] decData = b64Decode(data);
        return decData == null ? null : ecIESDecrypt(priKey, decData);
    }

    public static String pri2pub(byte[] pri) {
        byte[] pubKey = getECPublicKey(pri);
        if (pubKey == null) {
            return null;
        }

        byte[] address = new byte[1 + 1 + pubKey.length];
        address[0] = 1;
        address[1] = 0;
        System.arraycopy(pubKey, 0, address, 2, pubKey.length);

        String pub = "OSNU" + b58Encode(address);
        return pub;
    }

    public static String toOsnPrivacy(byte[] pri) {
        String priKey = "VK0" + b64Encode(pri);
        return priKey;
    }


}

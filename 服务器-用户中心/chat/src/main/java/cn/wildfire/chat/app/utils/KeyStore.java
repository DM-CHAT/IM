package cn.wildfire.chat.app.utils;

import static cn.wildfire.chat.app.utils.OsnUtils.b64Decode;
import static cn.wildfire.chat.app.utils.OsnUtils.sha256;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class KeyStore {

    final static String KeystorePathDir = "/Keystore";



    public static String[] listAccount(Context context) {
        String baseDir = getBaseDir(context);
        File baseDirFile = new File(baseDir);
        return baseDirFile.list();
    }

    public static boolean isKeyExist(Context context, String account) {
        String baseDir = getBaseDir(context);
        String filePath = baseDir + "/" +account;
        File newKey = new File(filePath);
        return newKey.exists();
    }

    public static boolean createKey(Context context, String account, JSONObject data) {
        String baseDir = getBaseDir(context);
        String filePath = baseDir + "/" +account;
        if (isKeyExist(context, account)) {
            return false;
        }

        FileOutputStream fileOutputStream = null;
        try {
            File newKey = new File(filePath);
            //newKey.createNewFile();

            fileOutputStream = new FileOutputStream(newKey, true); // true 代表写完一条接着写，反之清空之前的数据再写入
            if (null != fileOutputStream) {
                fileOutputStream.write(data.toString().getBytes());
            }

            if (null != fileOutputStream) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }

            if (!newKey.exists()) {
                return false;
            }

            return true;

        } catch (Exception e) {

        } finally {
            try {
                if (null != fileOutputStream) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

        return false;
    }

    //登录时候用
    public static JSONObject getKeyInfo(Context context, String account) {
        String baseDir = getBaseDir(context);

        String accountPath = baseDir +"/" + account;
        File keyFile = new File(accountPath);
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();

        if (keyFile.exists()) {
            try {
                //inputStream = context.openFileInput(KeystorePathDir + "/" + account);
                bufferedReader = new BufferedReader(new BufferedReader(new FileReader(accountPath)));
                String line = "";
                while (null != (line = bufferedReader.readLine())) {
                    stringBuilder.append(line);
                }

                String data = stringBuilder.toString();

                return JSONObject.parseObject(data);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    public static String getBaseDir(Context context) {
        File baseDir = context.getFilesDir();
        String basePath = baseDir.getPath() + KeystorePathDir;
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return basePath;
    }

    public static boolean updatePassword(Context context, String account, String oldPassword, String newPassword) {
        JSONObject info = getKeyInfo(context, account);
        if (info == null) {
            return false;
        }
        try {
            String osnPassword = info.getString("osnPassword");
            String[] arr = osnPassword.split("-");

            byte[] endDataHex = b64Decode(arr[2]);
            byte[] pwdHash = sha256(oldPassword.getBytes());

            byte[] privateKey = OsnUtils.aesDecrypt(endDataHex, pwdHash);
            if (privateKey == null) {
                return false;
            }
            byte[] pwdHash2 = sha256(newPassword.getBytes());
            String newPrivateKey = OsnUtils.aesEncrypt(privateKey, pwdHash2);
            //arr[2] = newPrivateKey;
            String newOsnPassword = arr[0] + "-" + arr[1] + "-" + newPrivateKey;

            info.remove("osnPassword");
            info.put("osnPassword", newOsnPassword);


            String mnemonic = OsnUtils.aesDecrypt(info.getString("mnemonic"), oldPassword);
            info.remove("mnemonic");
            String newMnemonic = OsnUtils.aesEncrypt(mnemonic.getBytes(), pwdHash2);
            info.put("mnemonic", newMnemonic);




            if (!delKey(context, account)) {
                return false;
            }

            return createKey(context, account, info);

        } catch (Exception e) {
            System.out.println("@@@             e");
        }

        return false;

    }

    private static boolean delKey(Context context, String account) {
        String baseDir = getBaseDir(context);
        String filePath = baseDir + "/" +account;
        File file = new File(filePath);
        return file.delete();
    }

    public static String getMnemonic(Context context, String account, String password) {

        JSONObject info = getKeyInfo(context, account);
        if (info == null) {
            return null;
        }

        try {

            String mnemonicEnc = info.getString("mnemonic");

            return OsnUtils.aesDecrypt(mnemonicEnc, password);


        } catch (Exception e) {

        }

        return null;
    }

}

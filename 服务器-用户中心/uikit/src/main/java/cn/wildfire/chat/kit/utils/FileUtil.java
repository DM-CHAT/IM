package cn.wildfire.chat.kit.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class FileUtil {
    private String SDPATH;

    public FileUtil() {

    }

    public String getSDPATH() {
        return SDPATH;
    }

    public FileUtil(String SDPATH){
        //得到外部存储设备的目录（/SDCARD）
        SDPATH = Environment.getExternalStorageDirectory() + "/" ;
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     * @param dirName 目录名字
     * @return 文件目录
     */
    public File createDir(String dirName){
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }
    public static void renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return;
        }

        if (TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        file.renameTo(new File(newPath));
    }
    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /** 删除单个文件
     * @param filePathName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePathName) {
        File file = new File(filePathName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public File write2SDFromInput(String path,String fileName,InputStream input){
        File file = null;
        OutputStream output = null;

        try {
            createDir(path);
            file =createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte [] buffer = new byte[4 * 1024];
            while(input.read(buffer) != -1){
                output.write(buffer);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 删除单个文件
     * @param   path    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean delAllFile(String path) {

        boolean flag = false;

        File file = new File(path);

        if (!file.exists()) {

            return flag;

        }

        if (!file.isDirectory()) {

            return flag;

        }

        String[] tempList = file.list();

        File temp = null;

        for (int i = 0; i < tempList.length; i++) {

            if (path.endsWith(File.separator)) {

                temp = new File(path + tempList[i]);

            } else {

                temp = new File(path + File.separator + tempList[i]);

            }

            if (temp.isFile()) {

                temp.delete();

            }

            if (temp.isDirectory()) {

                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件

                //     delFolder(path + "/" + tempList[i]);//再删除空文件夹

                flag = true;

            }

        }

        return flag;

    }

    /**
     * 获取指定文件大小
     * @return
     * @throws Exception 　　
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            //file.createNewFile();
            //System.out.println("@@@        获取文件大小  文件不存在");
        }
        return size;
    }

    public static long getFileSize(String filePath) {
        long size = 0;

        try {
            File file = new File(filePath);
            size = getFileSize(file);
        } catch (Exception e) {

        }
        return size;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean copy(String src, String target) {

        try {
            Path pathSrc = Paths.get(src);
            Path pathTgt = Paths.get(target);
            Files.copy(pathSrc, pathTgt, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {

            System.out.println("[FileUtil] copy failed." + e.getMessage());
            e.printStackTrace();
            //System.out.println(e.getMessage());
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String copy(Context context, String src) {

        Random random = new Random();
        int number = random.nextInt(899999);
        number = number + 100000;

        String target = getTempDir(context) + "/" + System.currentTimeMillis() + number + getFileType(src);
        if (copy(src, target)) {
            System.out.println("[FileUtil] copy success.");
            return target;
        } else {
            System.out.println("[FileUtil] copy failed.");
            return src;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String copy(Context context, String src, String fileName) {

        Random random = new Random();
        int number = random.nextInt(899999);
        number = number + 100000;

        String target = getTempDir(context) + "/" + System.currentTimeMillis() + number + getFileTypeFromName(fileName);
        if (copy(src, target)) {
            System.out.println("[FileUtil] copy success.");
            return target;
        } else {
            System.out.println("[FileUtil] copy failed.");
            return src;
        }
    }

    public static String getFileName(String path) {
        int pos = path.lastIndexOf('/');
        String name = path.substring(pos + 1);
        return name;
    }

    public static String TempDir = "/temp";
    public static String getTempDir(Context context) {
        File baseDir = context.getFilesDir();
        String basePath = baseDir.getPath() + TempDir;
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return basePath;
    }

    public static String getTempDir(Context context, String childDir) {
        File baseDir = context.getFilesDir();
        String basePath = baseDir.getPath() + "/" + childDir;
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return basePath;
    }

    public static String getFileType(String path){

        int pos1 = path.lastIndexOf('/');
        int pos2 = path.lastIndexOf('.');
        if (pos2 < pos1) {
            return "";
        }
        if (pos2 == -1){
            return "";
        }

        String fileType = path.substring(pos2);
        return fileType;
    }

    public static String getFileTypeFromName(String name){

        int pos = name.lastIndexOf('.');
        if (pos == -1){
            return "";
        }
        String fileType = name.substring(pos);
        return fileType;
    }

}
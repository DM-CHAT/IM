package cn.wildfirechat.client;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public static void renameFile(String oldPath, String newPath) {
        if(TextUtils.isEmpty(oldPath)) {
            return;
        }

        if(TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        file.renameTo(new File(newPath));
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     * @throws IOException
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

    /**
     * 判断文件是否存在
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName){
        File file = new File(SDPATH + fileName);
        return file.exists();
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
}
package cn.wildfire.chat.kit.utils;


import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class HttpUtils {

    String result;
    String boundary;
    HttpURLConnection httpURLConnection;
    private static String SDPATH = "";

    public static String doGet(String sUrl,String path){
        String result = null;
        try{
            URL url = new URL(sUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();
            InputStream inputStream;
            if(httpURLConnection.getResponseCode() == 200){
                inputStream = httpURLConnection.getInputStream();
                SDPATH = path;
                writeStreamToSDCard(path,"ce.txt",inputStream);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null)
                    stringBuffer.append(line);
                result = stringBuffer.toString();
            }
            else{
             //   logInfo("response code: "+httpURLConnection.getResponseCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将一个InputStream中的数据写入至SD卡中
     */
    public static File writeStreamToSDCard(String dirpath, String filename, InputStream input) {
        File file = null;
        OutputStream output=null;
        try {
            //创建目录；
            createDIR(dirpath);
            //在创建 的目录上创建文件；
            file = createFile(dirpath+filename);
            output=new FileOutputStream(file);
            byte[]bt=new byte[4*1024];
            while (input.read(bt)!=-1) {
                output.write(bt);
            }
            //刷新缓存，
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{

            try{
                output.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;

    }

    /*
     * 在SD卡上创建目录；
     */
    public static File createDIR(String dirpath) {
        File dir=new File(SDPATH+dirpath);
        dir.mkdir();
        return dir;
    }
    /*
     * 在SD卡上创建文件；
     */
    public static File createFile(String filepath) throws IOException{
        File file=new File(SDPATH+filepath);
        file.createNewFile();
        return file;
    }



    public static String doPost(String sUrl, String data){
        String result = null;
        try{
            URL url = new URL(sUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.flush();

            int read;
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = httpURLConnection.getInputStream();
            while((read=inputStream.read(buffer)) > 0)
                byteArrayOutputStream.write(buffer,0,read);
            result = byteArrayOutputStream.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static JSONObject postFormdata(String url, Map<String,String> textMap, Map<String,String> fileMap){
        try{
            URL urls = new URL(url);
            String boundary = "----------"+System.currentTimeMillis();
            HttpURLConnection httpURLConnection = (HttpURLConnection)urls.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            StringBuilder builder = new StringBuilder();
            if(textMap != null){
                for(String key : textMap.keySet()){
                    builder.append("\r\n--").append(boundary).append("\r\n");
                    builder.append("Content-Disposition: form-data;name=\"").append(key).append("\"").append("\"\r\n\r\n");
                    builder.append(textMap.get(key));
                }
                outputStream.write(builder.toString().getBytes());
            }
            if(fileMap != null){
                for(String key : fileMap.keySet()){
                    String fileName = fileMap.get(key);
                    if(fileName == null){
                        continue;
                    }
                    File file = new File(fileName);
                    if(!file.exists()){
                   //     logInfo("file no exist: "+file.getAbsolutePath());
                    }
                    String contentType;
                    if (fileName.endsWith(".png")) {
                        contentType = "image/png";
                    }else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpe")) {
                        contentType = "image/jpeg";
                    }else if (fileName.endsWith(".gif")) {
                        contentType = "image/gif";
                    }else if (fileName.endsWith(".ico")) {
                        contentType = "image/image/x-icon";
                    }else{
                        contentType = "application/octet-stream";
                    }
                    builder.setLength(0);
                    builder.append("\r\n--").append(boundary).append("\r\n");
                    builder.append("Content-Disposition: form-data;name=\"").
                            append(key).append("\"").append("\"\r\n\r\n");
                    builder.append("\r\n--").append(boundary).append("\r\n");
                    builder.append("Content-Disposition: form-data;name=\"").append(key).
                            append("\";filename=\"").append(fileName).append("\"\r\n");
                    builder.append("Content-Type:").append(contentType).append("\r\n\r\n");
                    outputStream.write(builder.toString().getBytes());
                    DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                    int bytes;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = dataInputStream.read(bufferOut)) != -1) {
                        outputStream.write(bufferOut, 0, bytes);
                    }
                    dataInputStream.close();
                }
            }
            builder.setLength(0);
            builder.append("\r\n--").append(boundary).append("--\r\n");
            outputStream.write(builder.toString().getBytes());
            outputStream.flush();

            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while((line=bufferedReader.readLine()) != null)
                stringBuffer.append(line).append("\r\n");
            bufferedReader.close();
            outputStream.close();
            httpURLConnection.disconnect();
            return JSON.parseObject(stringBuffer.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

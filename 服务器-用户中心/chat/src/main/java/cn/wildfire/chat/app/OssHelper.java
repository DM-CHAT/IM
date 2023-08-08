package cn.wildfire.chat.app;

import static cn.wildfire.chat.kit.utils.DownloadManager.isExistDir;

import android.content.Context;
import android.util.Log;


import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import cn.wildfire.chat.kit.litapp.FileUploadSetup;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.SPUtils;

public class OssHelper {

    private static String AccessKeyId = null;
    private static String AccessKeySecret = null;
    private static String ENDPOINT = null;
    private static String BUCKETNAME = null;
    public static String FileBUCKETNAME = null;
    public static String UserPortraitDirectory = null;
    public static String GroupPortraitDirectory = null;
    public static String TempDirectory = null;
    public static String RemoteTempFilePath = null;


    private static OSS oss;

    public OSS ossClient;

    public volatile static OssHelper ossHelper;

    public OssHelper(Context context){
        if (AccessKeyId == null) {
            AccessKeyId = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"AccessKeyId","");
            System.out.println("@@@@ AccessKeyId : " + AccessKeyId);
        }
        if (AccessKeySecret == null) {
            AccessKeySecret = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"AccessKeySecret","");
            System.out.println("@@@@ AccessKeySecret : " + AccessKeySecret);
        }


        if (ENDPOINT == null) {
            ENDPOINT = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"ENDPOINT","");
            System.out.println("@@@@ ENDPOINT : " + ENDPOINT);
        }
        if (BUCKETNAME == null) {
            BUCKETNAME = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"BUCKETNAME","");
            System.out.println("@@@@ BUCKETNAME : " + BUCKETNAME);
        }
        if (FileBUCKETNAME == null) {
            FileBUCKETNAME = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"FileBUCKETNAME","");
            System.out.println("@@@@ FileBUCKETNAME : " + FileBUCKETNAME);
        }
        if (UserPortraitDirectory == null) {
            UserPortraitDirectory = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"UserPortraitDirectory","");
            System.out.println("@@@@ UserPortraitDirectory : " + UserPortraitDirectory);
        }
        if (GroupPortraitDirectory == null) {
            GroupPortraitDirectory = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"GroupPortraitDirectory","");
            System.out.println("@@@@ GroupPortraitDirectory : " + GroupPortraitDirectory);
        }

        if (TempDirectory == null) {
            TempDirectory = (String) cn.wildfire.chat.kit.utils.SPUtils.get(context,"TempDirectory","");
            System.out.println("@@@@ TempDirectory : " + TempDirectory);
        }
        if (RemoteTempFilePath == null) {
            RemoteTempFilePath = (String) SPUtils.get(context,"RemoteTempFilePath","");
            System.out.println("@@@@ RemoteTempFilePath : " + RemoteTempFilePath);
        }



        System.out.println("@@@@ OssHelper begin");
        OSSCredentialProvider credentialProvider1 = new OSSPlainTextAKSKCredentialProvider(AccessKeyId,AccessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(30 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(3); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(true);
        oss = new OSSClient(context.getApplicationContext(), ENDPOINT, credentialProvider1,conf);
    }

    public OssHelper(Context context, String AccessKeyId, String AccessKeySecret, String ENDPOINT){
        System.out.println("@@@@ OssHelper 2 begin.");
        OSSCredentialProvider credentialProvider1 = new OSSPlainTextAKSKCredentialProvider(AccessKeyId,AccessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(30 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(3); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(true);
        ossClient = new OSSClient(context.getApplicationContext(), ENDPOINT, credentialProvider1,conf);
    }


    public static OssHelper getInstance(Context context){
        if (ossHelper == null){
            synchronized (OssHelper.class){
                if (ossHelper == null){
                    ossHelper = new OssHelper(context);
                }
            }
        }
        return ossHelper;
    }
    /**
     * 上传方法
     * @param path      需上传文件的路径
     * @return 外网访问的路径
     */
    public String uploadImage(String path,String pathName,String filePath) {
        //     String objectKey = FileUtils.getFileNameFromPath(path);
        //int math = (int) ((Math.random() * 10));
        Random random = new Random();
        int number = random.nextInt(899999);
        number = number + 100000;

        String fileType = getFileType(path);

        String objectKey = String.valueOf(System.currentTimeMillis()) + number + fileType;

        // 构造上传请求
        //"userPortrait/"
        PutObjectRequest put = new PutObjectRequest(BUCKETNAME, filePath + objectKey, path);
        try {
            PutObjectResult putResult = oss.putObject(put);
            Log.d("PutObject", "UploadSuccess");
            Log.d("ETag", putResult.getETag());
            Log.d("RequestId", putResult.getRequestId());
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            Log.e("RawMessage", e.getRawMessage());
        }
        return oss.presignPublicObjectURL(BUCKETNAME, filePath + objectKey);
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

    public String getImageRemoteUrl(String fileName, String childDir){

        String remoteUrl = "";
        remoteUrl = oss.presignPublicObjectURL(BUCKETNAME, childDir + fileName);
        if (remoteUrl == null) {
            remoteUrl = RemoteTempFilePath + childDir + fileName;
        }
        return remoteUrl;
    }

    public String getMediaRemoteUrl(String fileName, String childDir){
        //     String fileName = FileUtils.getFileNameFromPath(path);
        String remoteUrl = "";
        remoteUrl = oss.presignPublicObjectURL(FileBUCKETNAME, childDir + fileName);
        if (remoteUrl == null) {
            remoteUrl = RemoteTempFilePath + childDir + fileName;
        }
        return remoteUrl;
    }

    public void uploadFile(String path, String childDir, CallBack callBack) {
        //String objectKey = FileUtils.getFileNameFromPath(path);
        Random random = new Random();
        int number = random.nextInt(899999);
        number = number + 100000;
        //int math = (int) ((Math.random() * 10));
        String fileType = getFileType(path);
        String objectKey = String.valueOf(System.currentTimeMillis()) + number + fileType;
        // 构造上传请求
        //"userPortrait/"

        PutObjectRequest put = new PutObjectRequest(BUCKETNAME, childDir + objectKey, path);
        try {

            PutObjectResult putResult = oss.putObject(put);
            Log.d("PutObject", "UploadSuccess");
            Log.d("ETag", putResult.getETag());
            Log.d("RequestId", putResult.getRequestId());

            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                @Override
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    System.out.println("@@@     上传文件进度：currentSize:"+currentSize +"  totalSize："+totalSize);
                }
            });

            System.out.println("@@@ putResult:" + oss.presignPublicObjectURL(BUCKETNAME, childDir + objectKey));


            String remoteUrl = oss.presignPublicObjectURL(BUCKETNAME, childDir + objectKey);
            if (remoteUrl == null) {
                remoteUrl = RemoteTempFilePath + childDir + objectKey;
            }

            callBack.success(remoteUrl, objectKey);
        } catch (ClientException e) {
            e.printStackTrace();
            callBack.fail();
        } catch (ServiceException e) {
            Log.e("RawMessage", e.getRawMessage());
            callBack.fail();
        }

    }

    public void uploadFile(OSS oss, String path, String childDir, String BUCKETNAME, CallBack callBack) {
        //String objectKey = FileUtils.getFileNameFromPath(path);
        Random random = new Random();
        int number = random.nextInt(899999);
        number = number + 100000;
        //int math = (int) ((Math.random() * 10));
        String fileType = getFileType(path);
        String objectKey = String.valueOf(System.currentTimeMillis()) + number + fileType;
        // 构造上传请求
        //"userPortrait/"
        PutObjectRequest put = new PutObjectRequest(BUCKETNAME, childDir + objectKey, path);
        try {
            PutObjectResult putResult = oss.putObject(put);
            Log.d("PutObject", "UploadSuccess");
            Log.d("ETag", putResult.getETag());
            Log.d("RequestId", putResult.getRequestId());

            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                @Override
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    System.out.println("@@@     上传文件进度：currentSize:"+currentSize +"  totalSize："+totalSize);
                }
            });

            String remoteUrl = oss.presignPublicObjectURL(BUCKETNAME, childDir + objectKey);

            callBack.success(remoteUrl, objectKey);
        } catch (ClientException e) {
            e.printStackTrace();
            callBack.fail();
        } catch (ServiceException e) {
            Log.e("RawMessage", e.getRawMessage());
            callBack.fail();
        }
    }

    public static void upload(Context context, FileUploadSetup fileUploadSetup, String filePath, CallBack callBack){
        OssHelper ossHelper = new OssHelper(context,
                fileUploadSetup.AccessKeyId,
                fileUploadSetup.AccessKeySecret,
                fileUploadSetup.ENDPOINT);
        ossHelper.uploadFile(ossHelper.ossClient,
                filePath,
                fileUploadSetup.childDir,
                fileUploadSetup.BUCKETNAME,
                callBack);
    }

    /**
     * 判断本地是否有该图片,有 -- 直接设置; 没有 -- 先下载再显示;
     * @param filename  下载文件名称
     */
    public void setImageBackground(String filename, CallBack callBack) {
//        String path = UIUtils.getContext().getCacheDir().getAbsolutePath() + "/" + filename;
//        File file = new File(path);
//        if(file.exists()){
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            imageView.setImageBitmap(bitmap);
//            return;
//        }
        GetObjectRequest get = new GetObjectRequest(BUCKETNAME, UserPortraitDirectory  + filename);
        oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                long length = result.getContentLength();
                if (length > 0) {
                    byte[] buffer = new byte[(int) length];
                    int readCount = 0;
                    while (readCount < length) {
                        try{
                            readCount += result.getObjectContent().read(buffer, readCount, (int) length - readCount);
                        }catch (Exception e){
                            OSSLog.logInfo(e.toString());
                        }
                    }
                    // 下载保存位置
                    String path = UIUtils.getContext().getCacheDir().getAbsolutePath()+ "/" + filename;

                    try {
                        FileOutputStream fout = new FileOutputStream(path);
                        fout.write(buffer);
                        fout.close();

                    } catch (Exception e) {
                        OSSLog.logInfo(e.toString());
                    }
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientException,
                                  ServiceException serviceException)  {

            }
        });
    }


    public void ossDownload(String filename,String path,CallBack callBack) {
        GetObjectRequest get = new GetObjectRequest(FileBUCKETNAME, filename);
        oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                long length = result.getContentLength();
                if (length > 0) {
                    byte[] buffer = new byte[(int) length];
                    int readCount = 0;
                    while (readCount < length) {
                        try{
                            readCount += result.getObjectContent().read(buffer, readCount, (int) length - readCount);
                        }catch (Exception e){
                            OSSLog.logInfo(e.toString());
                        }
                    }
                    // 下载保存位置
//                    String path = UIUtils.getContext().getCacheDir().getAbsolutePath()+ "/" + filename;
                    try {
                        String savePath = isExistDir(path);
                        File file = new File(savePath, filename);
                        FileOutputStream fout = new FileOutputStream(file);
                        fout.write(buffer);
                        fout.close();
                        callBack.success();
                    } catch (Exception e) {
                        OSSLog.logInfo(e.toString());
                        callBack.fail();
                    }
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientException,
                                  ServiceException serviceException)  {
                callBack.fail();
            }
        });
    }

    public interface CallBack{
        void success();
        void success(String remoteUrl, String fileName);
        void fail();
    }
}

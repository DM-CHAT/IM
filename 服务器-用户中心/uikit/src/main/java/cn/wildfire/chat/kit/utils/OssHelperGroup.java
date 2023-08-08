package cn.wildfire.chat.kit.utils;

import static cn.wildfire.chat.kit.utils.OssHelper.getFileType;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
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

import cn.wildfire.chat.kit.third.utils.FileUtils;
import cn.wildfire.chat.kit.third.utils.UIUtils;

public class OssHelperGroup {

    /*private static final String AccessKeyId = "LTAI5tQ2JnUwihLqmPSvHDkB";
    private static final String AccessKeySecret = "DZB9JYIrlt6gJeJVtIwP9xlqZkWgT3";
    private static final String ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
    private static final String BUCKETNAME = "zolo-image1";
    private static final String SECONDFILENAME = "groupPortrait/";*/


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

    public volatile static OssHelperGroup ossHelper;

    public OssHelperGroup(Context context){
        if (AccessKeyId == null) {
            AccessKeyId = (String) SPUtils.get(context,"AccessKeyId","");
        }
        if (AccessKeySecret == null) {
            AccessKeySecret = (String) SPUtils.get(context,"AccessKeySecret","");
        }


        if (ENDPOINT == null) {
            ENDPOINT = (String) SPUtils.get(context,"ENDPOINT","");
        }
        if (BUCKETNAME == null) {
            BUCKETNAME = (String) SPUtils.get(context,"BUCKETNAME","");
        }
        if (FileBUCKETNAME == null) {
            FileBUCKETNAME = (String) SPUtils.get(context,"FileBUCKETNAME","");
        }
        if (UserPortraitDirectory == null) {
            UserPortraitDirectory = (String) SPUtils.get(context,"UserPortraitDirectory","");
        }
        if (GroupPortraitDirectory == null) {
            GroupPortraitDirectory = (String) SPUtils.get(context,"GroupPortraitDirectory","");
        }

        if (TempDirectory == null) {
            TempDirectory = (String) SPUtils.get(context,"TempDirectory","");
        }
        if (RemoteTempFilePath == null) {
            RemoteTempFilePath = (String) SPUtils.get(context,"RemoteTempFilePath","");
        }


        OSSCredentialProvider credentialProvider1 = new OSSPlainTextAKSKCredentialProvider(AccessKeyId,AccessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(30 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(3); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(true);
        oss = new OSSClient(context.getApplicationContext(), ENDPOINT, credentialProvider1,conf);
    }

    public static OssHelperGroup getInstance(Context context){
        if (ossHelper == null){
            synchronized (OssHelperGroup.class){
                if (ossHelper == null){
                    ossHelper = new OssHelperGroup(context);
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
    public void upload(String path, String childDir, CallBack callBack) {
    //    String objectKey = FileUtils.getFileNameFromPath(path);

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
            String remoteUrl = oss.presignPublicObjectURL(BUCKETNAME, childDir + objectKey);
            //String remoteUrl = RemoteTempFilePath + childDir + objectKey;
            callBack.success(remoteUrl);
        } catch (ClientException e) {
            e.printStackTrace();
            callBack.fail();
        } catch (ServiceException e) {
            Log.e("RawMessage", e.getRawMessage());
            callBack.fail();
        }
    }

    /**
     * 判断本地是否有该图片,有 -- 直接设置; 没有 -- 先下载再显示;
     * @param context   上下文
     * @param filename  下载文件名称
     * @param imageView 显示位置
     */
    public void setImageBackground(Context context,String filename, ImageView imageView) {
        String path = UIUtils.getContext().getCacheDir().getAbsolutePath() + "/" + filename;
        File file = new File(path);
        if(file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(bitmap);
            return;
        }
        GetObjectRequest get = new GetObjectRequest(BUCKETNAME, UserPortraitDirectory + filename);
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

                        if(!((Activity)context).isFinishing()){
                            ((Activity)context).runOnUiThread(() -> {
                                Bitmap bitmap = BitmapFactory.decodeFile(path);
                                imageView.setImageBitmap(bitmap);
                            });
                        }
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

    public interface CallBack{
        void success();
        void success(String remoteUrl, String fileName);
        void success(String remoteUrl);
        void fail();
    }
}

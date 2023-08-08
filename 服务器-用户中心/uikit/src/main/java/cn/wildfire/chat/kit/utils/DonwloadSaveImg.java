package cn.wildfire.chat.kit.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.mm.MMPreviewActivity;
import cn.wildfirechat.message.Message;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DonwloadSaveImg {
    private static Context context;
    private static String filePath;
    private static Bitmap mBitmap;
    private static String mSaveMessage = "失败";
    private final static String TAG = "PictureActivity";
    private static ProgressDialog mSaveDialog = null;

    public static void donwloadImg(Context contexts, String filePaths) {
        context = contexts;
        filePath = filePaths;
        mSaveDialog = ProgressDialog.show(context, contexts.getString(R.string.save_as), contexts.getString(R.string.waiting), true);
        new Thread(saveFileRunnable).start();
    }

    private static Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(filePath)) { //网络图片
                    //1.创建OkHttpClient对象
                    OkHttpClient okHttpClient = new OkHttpClient();
                    //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
                    Request request = new Request.Builder().
                            url(filePath).
                            get().
                            build();
                    //3.创建一个call对象,参数就是Request请求对象
                    Call call = okHttpClient.newCall(request);
                    //4.请求加入调度,重写回调方法
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("okhttp3下载文件", "onFailure: "+call.toString() );
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            //拿到字节流
                            InputStream inputStream = response.body().byteStream();
                            //对网上资源进行下载转换位图图片
                            mBitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                            saveFile(mBitmap);
                            mSaveMessage = context.getString(R.string.picture_save_success);
                        }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };

    /*private static Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!TextUtils.isEmpty(filePath)) { //网络图片
                    // 对资源链接
                    URL url = new URL(filePath);
                    //打开输入流
                    InputStream inputStream = url.openStream();
                    //对网上资源进行下载转换位图图片
                    mBitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                }
                saveFile(mBitmap);
                mSaveMessage = "图片保存成功！";
            } catch (IOException e) {
                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };*/

    private static Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            mSaveDialog.dismiss();
            Log.d(TAG, mSaveMessage);
            Toast.makeText(context, mSaveMessage, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 保存图片
     * @param bitmap
     * @throws IOException
     */
    public static void saveFile(Bitmap bitmap) {
        try {
            //获取要保存的图片的位图
            //创建一个保存的Uri
            ContentValues values = new ContentValues();
            //设置图片名称
            values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + "code.png");
            //设置图片格式
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            //设置图片路径
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri saveUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (TextUtils.isEmpty(saveUri.toString())) {
                Toast.makeText(context, context.getString(R.string.picture_save_failure), Toast.LENGTH_SHORT).show();
                mSaveDialog.dismiss();
                return;
            }
            OutputStream outputStream = context.getContentResolver().openOutputStream(saveUri);
            //将位图写出到指定的位置
            //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
            //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
            //第三个参数：具体的输出流
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                Looper.prepare();
                Toast.makeText(context, context.getString(R.string.picture_save_success), Toast.LENGTH_SHORT).show();
                mSaveDialog.dismiss();
                Looper.loop();
            } else {
                Toast.makeText(context, context.getString(R.string.picture_save_failure), Toast.LENGTH_SHORT).show();
                mSaveDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mSaveDialog.dismiss();
        }
    }
}

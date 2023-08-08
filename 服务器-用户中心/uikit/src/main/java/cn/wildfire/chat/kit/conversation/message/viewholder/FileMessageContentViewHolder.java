/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.DownloadManager;
import cn.wildfire.chat.kit.utils.DownloadUtil;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.FileUtils;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.utils.HttpUtils;
import cn.wildfire.chat.kit.utils.MapTable;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.MessageContent;

@MessageContentType(FileMessageContent.class)
@EnableContextMenu
public class FileMessageContentViewHolder extends MediaMessageContentViewHolder {

    @BindView(R2.id.fileNameTextView)
    TextView nameTextView;
    @BindView(R2.id.fileSizeTextView)
    TextView sizeTextView;

    private FileMessageContent fileMessageContent;

    public FileMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        super.onBind(message);
        fileMessageContent = (FileMessageContent) message.message.content;
        nameTextView.setText(fileMessageContent.getName());
        sizeTextView.setText(FileUtils.getReadableFileSize(fileMessageContent.getSize()));
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R2.id.imageView)
    public void onClick(View view) {

        if (message.isDownloading) {
            return;
        }


        File file = messageViewModel.mediaMessageContentFile(message.message, fragment.getContext());
        if (file == null) {
            return;
        }

        //progressBar.

        if (file.exists()) {
            MessageContent content = message.message.content;
            String path = ((MediaMessageContent) content).localPath;
            openFile(path);
//            Intent intent = FileUtils.getViewIntent(fragment.getContext(), file);
//            if(intent == null){
//                openFileManager(file);
//                return;
//            }
//            ComponentName cn = intent.resolveActivity(fragment.getContext().getPackageManager());
//            if (cn == null) {
//                //Toast.makeText(fragment.getContext(), "找不到能打开此文件的应用", Toast.LENGTH_SHORT).show();
//                openFileManager(file);
//                return;
//            }
//            fragment.startActivity(intent);
        } else {
            // 文件下载地址：
            String remoteUrl = ((MediaMessageContent) message.message.content).remoteUrl;
            String localSavePath = ((MediaMessageContent) message.message.content).localPath;

            downloadFileAndDecrypt(remoteUrl, localSavePath, message.message.content.decKey);

            /*String paths = fragment.getActivity().getFilesDir().toString();
       //     String paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))+"/";
            // Android 4.0 之后不能在主线程中请求HTTP请求
            new Thread(new Runnable(){
                @Override
                public void run() {
                    DownloadManager.download(remoteUrl, localSavePath, new DownloadManager.OnDownloadListener() {
                        @Override
                        public void onSuccess(File file) {
                            message.isDownloading = false;
                            message.progress = 100;
                            openFile(file.getAbsolutePath());
                        }
                        @Override
                        public void onProgress(int percent) {
                            message.progress = percent;
                        }
                        @Override
                        public void onFail() {
                            message.isDownloading = false;
                            message.progress = 0;
                            Log.e(AudioMessageContentViewHolder.class.getSimpleName(), "download failed: " + message.message.messageId);
                        }
                    });
                }
            }).start();*/




        }
        //  messageViewModel.downloadMedia(fragment.getActivity(),message, new File(remoteUrl));
    }

    private void downloadFileAndDecrypt(String remoteUrl, String localSavePath, String decKey) {
        String localSavePathTemp = localSavePath + ".tmp";
        // String paths = fragment.getActivity().getFilesDir().toString();
        //     String paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))+"/";
        // Android 4.0 之后不能在主线程中请求HTTP请求
        new Thread(new Runnable(){
            @Override
            public void run() {

                DownloadManager.download(remoteUrl, localSavePathTemp, new DownloadManager.OnDownloadListener() {
                    @Override
                    public void onSuccess(File file) {

                        try {
                            if (decKey != null) {

                                File[] files = CompressUtil.unzip(localSavePathTemp, decKey);
                                if (files.length > 0) {
                                    String unZipPath = files[0].getPath();
                                    FileUtil.renameFile(unZipPath, localSavePath);
                                } else {
                                    Toast.makeText(fragment.getContext(), "下载文件失败", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                FileUtil.renameFile(localSavePathTemp, localSavePath);
                            }

                            message.isDownloading = false;
                            message.progress = 100;
                            openFile(localSavePath);

                        } catch (Exception e) {

                        }
                        message.isDownloading = false;
                        FileUtil.deleteSingleFile(localSavePathTemp);

                    }
                    @Override
                    public void onProgress(int percent) {
                        message.progress = percent;
                    }
                    @Override
                    public void onFail() {
                        FileUtil.deleteSingleFile(localSavePathTemp);
                        message.isDownloading = false;
                        message.progress = 0;
                        Log.e(AudioMessageContentViewHolder.class.getSimpleName(), "download failed: " + message.message.messageId);
                    }
                });
            }
        }).start();
    }

    private void httpDown(final String path,String filePath) {

        new Thread() {

            @Override

            public void run() {

                URL url;

                HttpURLConnection connection;

                try {

                    //统一资源

                    url = new URL(path);

                    //打开链接

                    connection = (HttpURLConnection) url.openConnection();

                    //设置链接超时

                    connection.setConnectTimeout(4000);

                    //设置允许得到服务器的输入流,默认为true可以不用设置

                    connection.setDoInput(true);

                    //设置允许向服务器写入数据，一般get方法不会设置，大多用在post方法，默认为false

                    connection.setDoOutput(true);//此处只是为了方法说明

                    //设置请求方法

                    connection.setRequestMethod("GET");

                    //设置请求的字符编码

                    connection.setRequestProperty("Charset", "utf-8");

                    //设置connection打开链接资源

                    connection.connect();

                    //得到链接地址中的file路径

                    String urlFilePath = connection.getURL().getFile();

                    //得到url地址总文件名 file的separatorChar参数表示文件分离符

                    String fileName = urlFilePath.substring(urlFilePath.lastIndexOf(File.separatorChar) + 1);

                    //创建一个文件对象用于存储下载的文件 此次的getFilesDir()方法只有在继承至Context类的类中

                    // 可以直接调用其他类中必须通过Context对象才能调用，得到的是内部存储中此应用包名下的文件路径

                    //如果使用外部存储的话需要添加文件读写权限，5.0以上的系统需要动态获取权限 此处不在不做过多说明。

                    File file = new File(filePath, fileName);

                    //创建一个文件输出流

                    FileOutputStream outputStream = new FileOutputStream(file);

                    //得到链接的响应码 200为成功

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        //得到服务器响应的输入流

                        InputStream inputStream = connection.getInputStream();

                        //获取请求的内容总长度

                        int contentLength = connection.getContentLength();

                        //设置progressBar的Max

                        //          mPb.setMax(contentLength);

                        //创建缓冲输入流对象，相对于inputStream效率要高一些

                        BufferedInputStream bfi = new BufferedInputStream(inputStream);

                        //此处的len表示每次循环读取的内容长度

                        int len;

                        //已经读取的总长度

                        int totle = 0;

                        //bytes是用于存储每次读取出来的内容

                        byte[] bytes = new byte[1024];

                        while ((len = bfi.read(bytes)) != -1) {

                            //每次读取完了都将len累加在totle里

                            totle += len;

                            //每次读取的都更新一次progressBar

                            //            mPb.setProgress(totle);

                            //通过文件输出流写入从服务器中读取的数据

                            outputStream.write(bytes, 0, len);

                        }

                        //关闭打开的流对象

                        outputStream.close();

                        inputStream.close();

                        bfi.close();


                    }

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }.start();

    }

    public void openFile(String file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setDataAndType(Uri.fromFile(new File(file)), MapTable.getMIMEType(file));
            intent.setDataAndType(FileProvider.getUriForFile(fragment.getActivity(), fragment.getActivity().getPackageName() + ".fileProvider", new File(file)), MapTable.getMIMEType(file));
            fragment.startActivity(intent);
            Intent.createChooser(intent, "请选择对应的软件打开该附件！");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(fragment.getActivity(), "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }

    public void openFileManager(File file) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("*/*");
        //intent.setDataAndType(Uri.fromFile(file),"*/*");
        fragment.startActivity(intent);
    }
}

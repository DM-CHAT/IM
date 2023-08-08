/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.mm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImagePreviewActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.channel.ChannelInfoActivity;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.ConversationMessageAdapter;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.litapp.LitappInfoActivity;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.DownloadManager;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.UserInfo;
import me.kareluo.imaging.IMGEditActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author imndx
 */
public class MMPreviewActivity extends WfcBaseActivity {
    private SparseArray<View> views;
    private View currentVideoView;
    private ViewPager viewPager;
    private MMPagerAdapter adapter;
    private PhotoView photoView;
    private ImageView editImage;
    private RelativeLayout layout;
    private Button forwardButton;

    private static int currentPosition = -1;
    private static UiMessage message;
    private static List<MediaEntry> entries;
    private boolean pendingPreviewInitialMedia;

    private class MMPagerAdapter extends PagerAdapter {
        private List<MediaEntry> entries;

        public MMPagerAdapter(List<MediaEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            MediaEntry entry = entries.get(position);
            if (entry.getType() == MediaEntry.TYPE_IMAGE) {
                view = LayoutInflater.from(MMPreviewActivity.this).inflate(R.layout.preview_photo, null);
            } else {
                view = LayoutInflater.from(MMPreviewActivity.this).inflate(R.layout.preview_video, null);
            }

            container.addView(view);
            views.put(position % 1, view);
            if (pendingPreviewInitialMedia) {
                preview(view, entry);
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            // do nothing ?
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return entries == null ? 0 : entries.size();
        }

        public MediaEntry getEntry(int position) {
            return entries.get(position);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // TODO 可以在此控制透明度
        }

        @Override
        public void onPageSelected(int position) {
            View view = views.get(position % 3);
            if (view == null) {
                // pending layout
                return;
            }
            if (currentVideoView != null) {
                resetVideoView(currentVideoView);
                currentVideoView = null;
            }
            MediaEntry entry = adapter.getEntry(position);
            preview(view, entry);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void preview(View view, MediaEntry message) {
        if (message.getType() == MediaEntry.TYPE_IMAGE) {
            previewImage(view, message);
        } else {
            previewVideo(view, message);
        }
    }

    private void resetVideoView(View view) {
        PhotoView photoView = view.findViewById(R.id.photoView);
        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        ImageView playButton = view.findViewById(R.id.btnVideo);
        VideoView videoView = view.findViewById(R.id.videoView);

        photoView.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
        videoView.stopPlayback();
        videoView.setVisibility(View.INVISIBLE);

    }

    private void previewVideo(View view, MediaEntry entry) {

        PhotoView photoView = view.findViewById(R.id.photoView);
        ImageView saveImageView = view.findViewById(R.id.saveImageView);
        saveImageView.setVisibility(View.GONE);
        if (entry.getThumbnail() != null) {
            GlideApp.with(photoView).load(entry.getThumbnail()).into(photoView);
        } else {
            GlideApp.with(photoView).load(entry.getThumbnailUrl()).into(photoView);
        }

        VideoView videoView = view.findViewById(R.id.videoView);
        if(videoView != null){
            videoView.setVisibility(View.INVISIBLE);

            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }


        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        if(loadingProgressBar != null){
            loadingProgressBar.setVisibility(View.GONE);
        }

        ImageView btn = view.findViewById(R.id.btnVideo);
        if(btn == null){
            return;
        }
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(entry.getMediaUrl())) {
                    return;
                }
                btn.setVisibility(View.GONE);
                if (TextUtils.isEmpty(entry.getMediaLocalPath())) {
//                    String name = DownloadManager.md5(entry.getMediaUrl());
                    String name = entry.getMediaUrl();
                    File videoFile = new File(Config.getVideoSaveDir(MMPreviewActivity.this), name);
                    if (!videoFile.exists()) {
                        view.setTag(name);
                        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        final WeakReference<View> viewWeakReference = new WeakReference<>(view);

                        DownloadManager.download(entry.getMediaUrl(), Config.getVideoSaveDir(MMPreviewActivity.this), name, new DownloadManager.OnDownloadListener() {
                            @Override
                            public void onSuccess(File file) {
                                UIUtils.postTaskSafely(() -> {
                                    View targetView = viewWeakReference.get();
                                    if (targetView != null && name.equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        playVideo(targetView, file.getAbsolutePath());
                                    }
                                });
                            }

                            @Override
                            public void onProgress(int progress) {
                                // TODO update progress
                                Log.e(MMPreviewActivity.class.getSimpleName(), "video downloading progress: " + progress);
                            }

                            @Override
                            public void onFail() {
                                View targetView = viewWeakReference.get();
                                UIUtils.postTaskSafely(() -> {
                                    if (targetView != null && name.equals(targetView.getTag())) {
                                        targetView.findViewById(R.id.loading).setVisibility(View.GONE);
                                        targetView.findViewById(R.id.btnVideo).setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
                    } else {
                        playVideo(view, videoFile.getAbsolutePath());
                    }
                } else {
                    playVideo(view, entry.getMediaLocalPath());
                }
            }
        });
    }
    private void playVideo(View view, String videoUrl) {
        VideoView videoView = view.findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

        PhotoView photoView = view.findViewById(R.id.photoView);
        photoView.setVisibility(View.GONE);

        ImageView btn = view.findViewById(R.id.btnVideo);
        btn.setVisibility(View.GONE);

        ProgressBar loadingProgressBar = view.findViewById(R.id.loading);
        loadingProgressBar.setVisibility(View.GONE);
        view.findViewById(R.id.loading).setVisibility(View.GONE);
        currentVideoView = view;

        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoUrl);

        //视频进度条
        videoView.setMediaController(new MediaController(MMPreviewActivity.this));
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(MMPreviewActivity.this, "play error", Toast.LENGTH_SHORT).show();
            resetVideoView(view);
            return true;
        });
        videoView.setOnCompletionListener(mp -> resetVideoView(view));
        videoView.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void previewImage(View view, MediaEntry entry) {
        photoView = view.findViewById(R.id.photoView);
        ImageView saveImageView = view.findViewById(R.id.saveImageView);

        String mediaUrl = entry.getMediaUrl();
        if (TextUtils.isEmpty(entry.getMediaLocalPath()) && !TextUtils.isEmpty(mediaUrl)) {
            String imageFileName = DownloadManager.md5(mediaUrl) + mediaUrl.substring(mediaUrl.lastIndexOf('.'));
            File file = new File(Config.getPhotoSaveDir(this), imageFileName);
            if (file.exists()) {
                saveImageView.setVisibility(View.GONE);
            } else {
                saveImageView.setVisibility(View.VISIBLE);
                saveImageView.setOnClickListener(v -> {
                    saveImageView.setVisibility(View.GONE);
                    DownloadManager.download(entry.getMediaUrl(), Config.getPhotoSaveDir(this), imageFileName, new DownloadManager.SimpleOnDownloadListener() {
                        @Override
                        public void onUiSuccess(File file1) {
                            if (isFinishing()) {
                                return;
                            }
                            Toast.makeText(MMPreviewActivity.this, "图片保存成功", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        } else {
            saveImageView.setVisibility(View.GONE);
        }


        // 本地文件存在，显示本地文件
        // 本地文件不存在
        // 是dec文件，显示加密图标
        // 不是dec文件，显示远程图

        if (FileUtil.isFileExist(entry.getMediaLocalPath())){
            // 本地文件存在，显示本地文件
            GlideApp.with(MMPreviewActivity.this)
                    .load(entry.getMediaLocalPath())
                    .placeholder(new BitmapDrawable(getResources(), entry.getThumbnail()))
                    .into(photoView);
        } else {
            // 本地文件不存在

            // 是dec文件，显示加密图标
            String remoteUrl = entry.getMediaUrl();
            if (remoteUrl == null) {
                // 显示破损图
                GlideApp.with(MMPreviewActivity.this)
                        .load(R.mipmap.ic_image_defauil)
                        .placeholder(R.mipmap.ic_icon_defuil)
                        .into(photoView);

            } else {
                String fileType = remoteUrl.substring(remoteUrl.length()-4);
                if (fileType.equalsIgnoreCase(".zip")) {
                    // 显示加密文件
                    GlideApp.with(MMPreviewActivity.this)
                            .load(R.mipmap.ic_icon_defuil)
                            .placeholder(R.mipmap.ic_image_defauil)
                            .into(photoView);
                } else {
                    // 显示远程图片
                    GlideApp.with(MMPreviewActivity.this).load(remoteUrl)
                            .placeholder(new BitmapDrawable(getResources(), entry.getThumbnailUrl()))
                            .into(photoView);
                }

            }
        }


        /*if (entry.getThumbnail() != null) {
            GlideApp.with(MMPreviewActivity.this)
                    .load(entry.getMediaLocalPath())
                    .placeholder(new BitmapDrawable(getResources(), entry.getThumbnail()))
                    .into(photoView);
        } else {
            String mediaUrl1 = entry.getMediaUrl();
            String isZip = mediaUrl1.substring(mediaUrl1.lastIndexOf("."));
            if(isZip.equals(".zip")){
                // zip 显示加密图
                GlideApp.with(MMPreviewActivity.this)
                        .load(entry.getMediaLocalPath())
                        .placeholder(new BitmapDrawable(getResources(), entry.getThumbnailUrl()))
                        .into(photoView);
            }else{
                GlideApp.with(MMPreviewActivity.this).load(mediaUrl1)
                        .placeholder(new BitmapDrawable(getResources(), entry.getThumbnailUrl()))
                        .into(photoView);
            }
        }*/
        /*System.out.println("@@@             文件是否存在："+FileUtil.isFileExist(entry.getMediaLocalPath()));

        if (!FileUtil.isFileExist(entry.getMediaLocalPath())) {

            if (message.message.content.decKey != null) {
                String tmpFilePath = entry.getMediaLocalPath() + ".zip";
                *//*new Thread(() -> {
                    FileUtil.download(mediaUrl, tmpFilePath);
                }).start();*//*
                FileUtil.deleteSingleFile(tmpFilePath);
                //        FileUtil.okdownload(mediaUrl,tmpFilePath, message.message.content.decKey, entry.getMediaLocalPath());
                //1.创建OkHttpClient对象
                OkHttpClient okHttpClient = new OkHttpClient();
                //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
                Request request = new Request.Builder().
                        url(mediaUrl).
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
                        InputStream is = response.body().byteStream();
                        int len = 0;
                        //设置下载图片存储路径和名称
                        File file = new File(tmpFilePath);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buf = new byte[128];
                        while((len = is.read(buf))!= -1){
                            fos.write(buf,0,len);
                            Log.e("okhttp3下载文件", "onResponse: "+len );
                        }
                        fos.flush();
                        fos.close();
                        is.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (FileUtil.isFileExist(tmpFilePath)) {
                                    // 解压缩
                                    File[] unzipFiles = new File[0];
                                    try {
                                        unzipFiles = CompressUtil.unzip(tmpFilePath, message.message.content.decKey);
                                        if (unzipFiles != null) {
                                            if (unzipFiles.length > 0) {
                                                String unZipPath = unzipFiles[0].getPath();

                                                GlideApp.with(MMPreviewActivity.this).load(unZipPath)
                                                        .placeholder(R.mipmap.ic_icon_defuil)
                                                        .into(photoView);
                                                FileUtil.renameFile(unZipPath, entry.getMediaLocalPath());
                                                // 删除
                                                if (!unZipPath.equalsIgnoreCase(entry.getMediaLocalPath())){
                                                    FileUtil.deleteSingleFile(unZipPath);
                                                }
                                                FileUtil.deleteSingleFile(tmpFilePath);
                                            }
                                        }

                                    } catch (Exception e) {
                                        System.out.println("@@@     图片解密显示失败："+e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                });


                *//*if (FileUtil.isFileExist(tmpFilePath)) {
                    // 解压缩
                    File[] unzipFiles = new File[0];
                    try {
                        unzipFiles = CompressUtil.unzip(tmpFilePath, message.message.content.decKey);
                        if (unzipFiles != null) {
                            if (unzipFiles.length > 0) {
                                String unZipPath = unzipFiles[0].getPath();
                                FileUtil.renameFile(unZipPath, entry.getMediaLocalPath());
                                // 删除
                                if (!unZipPath.equalsIgnoreCase(entry.getMediaLocalPath())){
                                    FileUtil.deleteSingleFile(unZipPath);
                                }
                                FileUtil.deleteSingleFile(tmpFilePath);
                            }
                        }

                    } catch (Exception e) {

                    }
                }*//*
            }
            else {
                // 传统下载

                String imageFileName = DownloadManager.md5(mediaUrl) + mediaUrl.substring(mediaUrl.lastIndexOf('.'));
                File file = new File(Config.PHOTO_SAVE_DIR, imageFileName);
                if (file.exists()) {
                    saveImageView.setVisibility(View.GONE);
                } else {
                    saveImageView.setVisibility(View.VISIBLE);
                    saveImageView.setOnClickListener(v -> {
                        saveImageView.setVisibility(View.GONE);
                        DownloadManager.download(entry.getMediaUrl(), Config.PHOTO_SAVE_DIR, imageFileName, new DownloadManager.SimpleOnDownloadListener() {
                            @Override
                            public void onUiSuccess(File file1) {
                                if (isFinishing()) {
                                    return;
                                }
                                Toast.makeText(MMPreviewActivity.this, "图片保存成功", Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                }
            }

        }

        if (entry.getThumbnail() != null) {
            GlideApp.with(MMPreviewActivity.this).load(entry.getMediaLocalPath())
                    .placeholder(R.mipmap.ic_icon_defuil)
                    .into(photoView);
        } else {
            FileUtil.deleteSingleFile(entry.getMediaLocalPath());
            //         Toast.makeText(MMPreviewActivity.this,"图片下载失败，请重新点击下载",Toast.LENGTH_SHORT).show();
            GlideApp.with(MMPreviewActivity.this).load(entry.getMediaUrl())
                    .placeholder(R.mipmap.ic_icon_defuil)
                    .into(photoView);
        }*/
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                List<String> titles = new ArrayList<>();
     //           titles.add("编辑");
                titles.add("转发");
                titles.add("识别二维码");

                new MaterialDialog.Builder(MMPreviewActivity.this).items(titles).itemsCallback((dialog, v, position, text) -> {
                    switch (position){
                        /*case 0:
                            Intent intent = new Intent(MMPreviewActivity.this, IMGEditActivity.class);
                            try {
                                String path = ((ImageMessageContent) message.message.content).localPath;
                                Uri uri = Uri.fromFile(new File(path));
                                intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri);
                                startActivityForResult(intent, 1234);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;*/
                        case 0:
                            /*Intent intent2 = new Intent(MMPreviewActivity.this, ForwardActivity.class);
                            intent2.putExtra("message", message.message);
                            startActivity(intent2);*/
                            Intent intent2 = new Intent(MMPreviewActivity.this, ContactListActivity.class);
                            intent2.putExtra("message", message.message);
                            intent2.putExtra("forward",true);
                            startActivity(intent2);
                            break;
                        case 1:
                            /*Bitmap bitmap = ((BitmapDrawable)photoView.getDrawable()).getBitmap();
                            Result ret = parsePic(bitmap);
                            if (null == ret) {
                                Toast.makeText(MMPreviewActivity.this, "解析结果：null",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MMPreviewActivity.this,
                                        "解析结果：" + ret.toString(), Toast.LENGTH_LONG).show();
                            }*/
                            Bitmap bitmap = ((BitmapDrawable)photoView.getDrawable()).getBitmap();
                            if(null == bitmap){
                                Toast.makeText(MMPreviewActivity.this, "qrcode: null",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            Result ret = parsePic(bitmap);
                            if (null == ret) {
                                Toast.makeText(MMPreviewActivity.this, "qrcode: null",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                onScanPcQrCode(ret.toString());
                            }
                            break;
                    }
                }).show();
                return false;
            }
        });
    }

    private void onScanPcQrCode(String qrcode) {
        System.out.println("@@@@ onScanPcQrCode :" +qrcode);
        String prefix = qrcode.substring(0, qrcode.lastIndexOf('/') + 1);
        String value = qrcode.substring(qrcode.lastIndexOf("/") + 1);
        switch (prefix) {
//            case WfcScheme.QR_CODE_PREFIX_PC_SESSION:
//                pcLogin(value);
//                break;
            case WfcScheme.QR_CODE_PREFIX_USER:
                showUser(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_GROUP:
                joinGroup(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_CHANNEL:
                subscribeChannel(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_LITAPP:
                showLitapp(value);
                break;
            default:
                Toast.makeText(this, "qrcode: " + qrcode, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showUser(String uid) {
        /*UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
         userInfo = userViewModel.getUserInfo(uid, true);
        if (userInfo == null) {
            return;
        }*/
        UserInfo userInfo = new UserInfo(uid);
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    private void joinGroup(String groupId) {
        Intent intent = new Intent(this, GroupInfoActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void showLitapp(String litappId) {
        Intent intent = new Intent(this, LitappInfoActivity.class);
        intent.putExtra("litappId", litappId);
        startActivity(intent);
    }

    private void subscribeChannel(String channelId) {
        Intent intent = new Intent(this, ChannelInfoActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == 1234){
                String path = data.getStringExtra("path");
                //随机文件名
                Random random = new Random();
                int number = random.nextInt(899999);
                number = number + 100000;
                String fileType = OssHelper.getFileType(path);
                String fileName = String.valueOf(System.currentTimeMillis()) + number + fileType;

                layout.setVisibility(View.VISIBLE);
                editImage.setImageURI(Uri.fromFile(new File(path)));
                forwardButton.setOnClickListener(v -> {
                    String paths = FileUtil.getTempDir(this);
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                    }*/
                    String pwd = CompressUtil.getPsw(8);
                    message.message.conversation.decKey = pwd;
                    String zipPath = CompressUtil.zip(path,paths+"/", pwd);

                    System.out.println("@@@    压缩zipPath：   "+zipPath);
                    System.out.println("@@@    压缩path：   " + path);

                    String remotePath = OssHelper.getInstance(this).uploadImage(MMPreviewActivity.this,zipPath,fileName, OssHelper.TempDirectory);
                    FileUtil.deleteSingleFile(zipPath);

                    if (remotePath == null) {
                        Toast.makeText(MMPreviewActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
                    } else {
                        ImageMessageContent imageMessageContent = (ImageMessageContent) message.message.content;
                        imageMessageContent.localPath = zipPath;
                        imageMessageContent.remoteUrl = OssHelper.getInstance(this).getImageRemoteUrl(fileName, OssHelper.TempDirectory);

                        Message msg = new Message();
                        msg.conversation = message.message.conversation;
                        msg.content = imageMessageContent;

                        Intent intent2 = new Intent(MMPreviewActivity.this, ForwardActivity.class);
                        intent2.putExtra("message", msg);
                        startActivity(intent2);
                    }

                });
            }
        }
    }


    public Result parsePic(Bitmap bitmap) {
        // 解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        // 新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        int lWidth = bitmap.getWidth();
        int lHeight = bitmap.getHeight();
        int[] lPixels = new int[lWidth * lHeight];
        bitmap.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(lWidth,
                lHeight, lPixels);
        // 将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                rgbLuminanceSource));
        // 初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        // 开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mm_preview);
        editImage = findViewById(R.id.editImage);
        layout = findViewById(R.id.image_container);
        forwardButton = findViewById(R.id.btn_ok);
        views = new SparseArray<>(3);
        viewPager = findViewById(R.id.viewPager);
        adapter = new MMPagerAdapter(entries);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(pageChangeListener);
        if (currentPosition == 0) {
            viewPager.post(() -> {
                pageChangeListener.onPageSelected(0);
            });
        } else {
            viewPager.setCurrentItem(currentPosition);
            pendingPreviewInitialMedia = true;
        }
//        ZXingLibrary.initDisplayOpinion(this);
    }*/

    @Override
    protected void afterViews() {
        super.afterViews();
        editImage = findViewById(R.id.editImage);
        layout = findViewById(R.id.image_container);
        forwardButton = findViewById(R.id.btn_ok);
        views = new SparseArray<>(3);
        viewPager = findViewById(R.id.viewPager);
        adapter = new MMPagerAdapter(entries);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(pageChangeListener);
        /*if (currentPosition == 0) {
            viewPager.post(() -> {
                pageChangeListener.onPageSelected(0);
            });
        } else */{
            viewPager.setCurrentItem(currentPosition);
            pendingPreviewInitialMedia = true;
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_mm_preview;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        entries = null;
    }

    public static void previewMedia(Context context, List<MediaEntry> entries, int current, UiMessage msg) {
        if (entries == null || entries.isEmpty()) {
            Log.w(MMPreviewActivity.class.getSimpleName(), "message is null or empty");
            return;
        }
        MMPreviewActivity.entries = entries;
        MMPreviewActivity.currentPosition = current;
        MMPreviewActivity.message = msg;
        Intent intent = new Intent(context, MMPreviewActivity.class);
        context.startActivity(intent);
    }

    public static void previewImage(Context context, ImageMessageContent imageMessageContent) {
        List<MediaEntry> entries = new ArrayList<>();

        MediaEntry entry = new MediaEntry();
        entry.setType(MediaEntry.TYPE_IMAGE);
        entry.setThumbnail(imageMessageContent.getThumbnail());
        entry.setMediaUrl(imageMessageContent.remoteUrl);
        entry.setMediaLocalPath(imageMessageContent.localPath);
        entries.add(entry);
        previewMedia(context, entries, 0,null);
    }

    public static void previewImage(Context context, String imageUrl) {
        List<MediaEntry> entries = new ArrayList<>();

        MediaEntry entry = new MediaEntry();
        entry.setType(MediaEntry.TYPE_IMAGE);
        entry.setMediaUrl(imageUrl);
        entries.add(entry);
        previewMedia(context, entries, 0,null);
    }

    public static void previewVideo(Context context, VideoMessageContent videoMessageContent) {
        List<MediaEntry> entries = new ArrayList<>();

        MediaEntry entry = new MediaEntry();
        entry.setType(MediaEntry.TYPE_VIDEO);
        entry.setThumbnail(videoMessageContent.getThumbnail());
        entry.setMediaUrl(videoMessageContent.remoteUrl);
        entry.setMediaLocalPath(videoMessageContent.localPath);
        entries.add(entry);
        previewMedia(context, entries, 0,null);
    }


    public static void previewVideo(Context context, String videoUrl) {
        List<MediaEntry> entries = new ArrayList<>();

        MediaEntry entry = new MediaEntry();
        entry.setType(MediaEntry.TYPE_VIDEO);
//        entry.setThumbnail(videoMessageContent.getThumbnail());
        entry.setMediaUrl(videoUrl);
//        entry.setMediaLocalPath(videoMessageContent.localPath);
        entries.add(entry);
        previewMedia(context, entries, 0,null);
    }
}

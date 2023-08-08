/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.GlideRequest;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.widget.BubbleImageView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.client.CompressUtil;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

@MessageContentType(ImageMessageContent.class)
@EnableContextMenu
public class ImageMessageContentViewHolder extends MediaMessageContentViewHolder {

    @BindView(R2.id.imageView)
    BubbleImageView imageView;

    public ImageMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        ImageMessageContent imageMessage = (ImageMessageContent) message.message.content;
        Bitmap thumbnail = imageMessage.getThumbnail();
        int width = thumbnail != null ? thumbnail.getWidth() : 400;
        int height = thumbnail != null ? thumbnail.getHeight() : 400;
//        imageView.getLayoutParams().width = UIUtils.dip2Px(width > 200 ? 200 : width);
//        imageView.getLayoutParams().height = UIUtils.dip2Px(height > 200 ? 200 : height);

        //会话页面图片直接显示大图
        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = height;

        if(imageMessage.remoteUrl == null){
            GlideApp.with(fragment)
                    .load(R.mipmap.ic_image_defauil)
                    .placeholder(R.mipmap.ic_icon_defuil)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        /*String name = imageMessage.remoteUrl.substring(imageMessage.remoteUrl.lastIndexOf("."));
        String unZipPath = null;
        System.out.println("@@@    name = "+name);*/



        if (FileUtil.isFileExist(imageMessage.localPath)){
            GlideApp.with(fragment)
                    .load(imageMessage.localPath)
                    .placeholder(R.mipmap.ic_image_defauil)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        if (imageMessage.decKey == null) {
            // 显示远程
            GlideApp.with(fragment)
                    .load(((ImageMessageContent) message.message.content).remoteUrl)
                    .placeholder(R.mipmap.ic_image_defauil)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        // 文件不存在，又有decKey标记
        // 显示加密图标
        GlideApp.with(fragment)
                .load(R.mipmap.ic_icon_defuil)
                .placeholder(R.mipmap.ic_icon_defuil)
                .centerCrop()
                .into(imageView);



        /*{
            if (thumbnail != null) {
                // 本地文件不存在，显示默认图
                BitmapDrawable drawable = new BitmapDrawable(thumbnail);
                GlideApp.with(fragment)
                        .load(drawable)
                        .placeholder(R.mipmap.ic_icon_defuil)
                        .centerCrop()
                        .into(imageView);
            }else{
                GlideApp.with(fragment)
                        .load(((ImageMessageContent) message.message.content).remoteUrl)
                        .placeholder(R.mipmap.ic_icon_defuil)
                        .centerCrop()
                        .into(imageView);
            }
        }*/
        /*if (!TextUtils.isEmpty(imageMessage.localPath)) {
            if(name.equalsIgnoreCase(".zip")){
                File[] unzipFiles = new File[0];
                try {
                    unzipFiles = CompressUtil.unzip(imageMessage.remoteUrl, imageMessage.decKey);
                    unZipPath = unzipFiles[0].getPath();
                } catch (Exception e) {

                }
                GlideApp.with(fragment)
                        .load(unZipPath)
                        .placeholder(R.mipmap.avatar_def)
                        .centerCrop()
                        .into(imageView);
            }else{
                GlideApp.with(fragment)
                        .load(imageMessage.remoteUrl)
                        .placeholder(R.mipmap.avatar_def)
                        .centerCrop()
                        .into(imageView);
            }

        } else {
            GlideRequest<Drawable> request = GlideApp.with(fragment)
                    .load(imageMessage.remoteUrl);
            if (thumbnail != null) {
                request = request.placeholder(new BitmapDrawable(fragment.getResources(), imageMessage.getThumbnail()));
            } else {
                request = request.placeholder(R.mipmap.img_error);
            }
            request.centerCrop()
                    .into(imageView);
        }*/
        /*if(imageMessage.getThumbnail().equals("") || imageMessage.getThumbnail() == null){
            if (!TextUtils.isEmpty(imageMessage.localPath)) {
                if(name.equalsIgnoreCase(".zip")){
                    File[] unzipFiles = new File[0];
                    try {
                        unzipFiles = CompressUtil.unzip(imageMessage.remoteUrl, imageMessage.decKey);
                        unZipPath = unzipFiles[0].getPath();
                    } catch (Exception e) {

                    }
                    GlideApp.with(fragment)
                            .load(unZipPath)
                            .placeholder(R.mipmap.avatar_def)
                            .centerCrop()
                            .into(imageView);
                }else{
                    GlideApp.with(fragment)
                            .load(imageMessage.remoteUrl)
                            .placeholder(R.mipmap.avatar_def)
                            .centerCrop()
                            .into(imageView);
                }

            } else {
                GlideRequest<Drawable> request = GlideApp.with(fragment)
                        .load(imageMessage.remoteUrl);
                if (thumbnail != null) {
                    request = request.placeholder(new BitmapDrawable(fragment.getResources(), imageMessage.getThumbnail()));
                } else {
                    request = request.placeholder(R.mipmap.img_error);
                }
                request.centerCrop()
                        .into(imageView);
            }
        }
        else{
            BitmapDrawable drawable = new BitmapDrawable(imageMessage.getThumbnail());
            if (!TextUtils.isEmpty(imageMessage.localPath)) {
                GlideApp.with(fragment)
                        .load(((ImageMessageContent) message.message.content).remoteUrl)
                        .placeholder(drawable)
                        .centerCrop()
                        .into(imageView);
            } else {
                GlideRequest<Drawable> request = GlideApp.with(fragment)
                        .load(imageMessage.remoteUrl);
                if (thumbnail != null) {
                    request = request.placeholder(new BitmapDrawable(fragment.getResources(), imageMessage.getThumbnail()));
                } else {
                    request = request.placeholder(R.mipmap.img_error);
                }
                request.centerCrop()
                        .into(imageView);
            }
        }*/

    }
    @OnClick(R2.id.imageView)
    void preview() {
        previewMM();
    }

    @Override
    protected void setSendStatus(Message item) {
        super.setSendStatus(item);
        MessageContent msgContent = item.content;
        if (msgContent instanceof ImageMessageContent) {
            boolean isSend = item.direction == MessageDirection.Send;
            if (isSend) {
                MessageStatus sentStatus = item.status;
                if (sentStatus == MessageStatus.Sending) {
                    imageView.setPercent(message.progress);
                    imageView.setProgressVisible(true);
                    imageView.showShadow(true);
                } else if (sentStatus == MessageStatus.Send_Failure) {
                    imageView.setProgressVisible(false);
                    imageView.showShadow(false);
                } else if (sentStatus == MessageStatus.Sent) {
                    imageView.setProgressVisible(false);
                    imageView.showShadow(false);
                }
            } else {
                imageView.setProgressVisible(false);
                imageView.showShadow(false);
            }
        }
    }

}

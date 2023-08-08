/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.ConversationMessageAdapter;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.mm.MMPreviewActivity;
import cn.wildfire.chat.kit.mm.MediaEntry;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class MediaMessageContentViewHolder extends NormalMessageContentViewHolder {

    private boolean isDowning = false;

    public MediaMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBind(UiMessage message) {
        if (message.isDownloading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    protected void previewMM() {
        List<UiMessage> messages = ((ConversationMessageAdapter) adapter).getMessages();
        List<MediaEntry> entries = new ArrayList<>();
        UiMessage msg;

        int current = 0;
        int index = 0;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);
            if (msg.message.content.getMessageContentType() != MessageContentType.ContentType_Image
                && msg.message.content.getMessageContentType() != MessageContentType.ContentType_Video) {
                continue;
            }
            MediaEntry entry = new MediaEntry();
            if (msg.message.content.getMessageContentType() == MessageContentType.ContentType_Image) {
                entry.setType(MediaEntry.TYPE_IMAGE);
                entry.setThumbnail(((ImageMessageContent) msg.message.content).getThumbnail());

            } else {
                entry.setType(MediaEntry.TYPE_VIDEO);
                entry.setThumbnail(((VideoMessageContent) msg.message.content).getThumbnail());
            }
            entry.setMediaUrl(((MediaMessageContent) msg.message.content).remoteUrl);
            entry.setMediaLocalPath(((MediaMessageContent) msg.message.content).localPath);
            entries.add(entry);

            if (message.message.messageId == msg.message.messageId) {
                current = index;
            }
            index++;
        }
        if (entries.isEmpty()) {
            return;
        }
        if (isNeedDecrypt(message.message.content)) {
            // 下载 解密
            if (!isDowning) {
                if (message.message.content.decKey != null) {
                    downloadAndDecrypt(((MediaMessageContent) message.message.content).remoteUrl,
                            ((MediaMessageContent) message.message.content).localPath,
                            message.message.content.decKey,
                            message.message.messageId,
                            message.message.content.getMessageContentType()
                    );
                } else {
                    downloadFile(((MediaMessageContent) message.message.content).remoteUrl,
                            ((MediaMessageContent) message.message.content).localPath,
                            message.message.messageId,
                            message.message.content.getMessageContentType()
                    );
                }
            }
        } else {
            MMPreviewActivity.previewMedia(fragment.getContext(), entries, current, message);
        }
    }

    private boolean downloadAndDecrypt(String remote,
                                       String local,
                                       String decKey,
                                       long messageId,
                                       int type

    ) {
        // 下载remote
        // 解密压缩
        // 更名称local

        progressBar.setVisibility(View.VISIBLE);
        isDowning = true;
        String tmpFilePath = local + ".zip";
        FileUtil.deleteSingleFile(tmpFilePath);
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
        Log.e("unzip file", "remote: " + remote);
        if(remote == null){
            progressBar.setVisibility(View.GONE);
            return false;
        }
        Request request = new Request.Builder().url(remote).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("okhttp3下载文件", "onFailure: " + call.toString());
                Toast.makeText(fragment.getContext(), "下载失败", Toast.LENGTH_SHORT).show();
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
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
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                //    Log.e("okhttp3下载文件", "onResponse: " + len);
                }
                fos.flush();
                fos.close();
                is.close();

                if (FileUtil.isFileExist(tmpFilePath)) {
                    // 解压缩
                    File[] unzipFiles = new File[0];
                    try {
                        Log.e("unzip file", "path: " + tmpFilePath);
                        Log.e("unzip file", "pwd: " + decKey);
                        try {
                            unzipFiles = CompressUtil.unzip(tmpFilePath, decKey);
                        } catch (Exception e) {
                            System.out.println("[downloadAndDecrypt] Exception :" + e.getMessage());
                        }
                        FileUtil.deleteSingleFile(tmpFilePath);
                        if (unzipFiles.length > 0) {
                            String unZipPath = unzipFiles[0].getPath();
                            Log.e("unzip file", "local: " + local);
                            FileUtil.renameFile(unZipPath, local);
                            isDowning = false;

                        } else {
                            Toast.makeText(fragment.getContext(), "下载图片失败", Toast.LENGTH_SHORT).show();
                        }
                        fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                if (type != MessageContentType.ContentType_File) {
                                    if (adapter instanceof ConversationMessageAdapter) {
                                        ((ConversationMessageAdapter) adapter).notifyItemChanged(((ConversationMessageAdapter) adapter).getMessagePosition(messageId));
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        try {
                            fragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }catch (Exception e1){
                            return;
                        }

                    }
                }
            }
        });

        return isDowning;
    }

    private boolean downloadFile(String remote,
                                 String local,
                                 long messageId,
                                 int type
    ) {
        // 下载remote
        // 解密压缩
        // 更名称local

        progressBar.setVisibility(View.VISIBLE);
        isDowning = true;
        String tmpFilePath = local + ".tmp";
        FileUtil.deleteSingleFile(tmpFilePath);
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
        Log.e("unzip file", "remote: " + remote);
        if(remote == null){
            progressBar.setVisibility(View.GONE);
            return false;
        }
        Request request = new Request.Builder().url(remote).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("okhttp3下载文件", "onFailure: " + call.toString());
                Toast.makeText(fragment.getContext(), "下载失败", Toast.LENGTH_SHORT).show();
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //拿到字节流
                InputStream is = response.body().byteStream();
                int len = 0;
                //设置下载存储路径和名称
                File file = new File(tmpFilePath);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[128];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
        //            Log.e("okhttp3下载文件", "onResponse: " + len);
                }
                fos.flush();
                fos.close();
                is.close();

                FileUtil.renameFile(tmpFilePath, local);

                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (type != MessageContentType.ContentType_File) {
                            if (adapter instanceof ConversationMessageAdapter) {
                                ((ConversationMessageAdapter) adapter).notifyItemChanged(((ConversationMessageAdapter) adapter).getMessagePosition(messageId));
                            }
                        }
                    }
                });

            }

        });

        return isDowning;
    }

    private boolean isNeedDecrypt(MessageContent content) {
        if (content.getMessageContentType() == MessageContentType.ContentType_Image) {
            if (!TextUtils.isEmpty(content.decKey)) {
                if (!FileUtil.isFileExist(((MediaMessageContent) content).localPath)) {
                    return true;
                }
            }
        } else if (content.getMessageContentType() == MessageContentType.ContentType_Video) {
            if (!FileUtil.isFileExist(((MediaMessageContent) content).localPath)) {
                return true;
            }
        } else if (content.getMessageContentType() == MessageContentType.ContentType_File) {
            if (!FileUtil.isFileExist(((MediaMessageContent) content).localPath)) {
                return true;
            }
        }
        return false;
    }
}

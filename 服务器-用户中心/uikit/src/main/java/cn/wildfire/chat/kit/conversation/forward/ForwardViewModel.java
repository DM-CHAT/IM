/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.forward;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;

public class ForwardViewModel extends ViewModel {
    public MutableLiveData<OperateResult<Integer>> forward(Conversation targetConversation, Message... messages) {
        MutableLiveData<OperateResult<Integer>> resultMutableLiveData = new MutableLiveData<>();
        AtomicInteger count = new AtomicInteger(0);

        for (Message message : messages) {
            if (message != null) {
                count.addAndGet(1);
            }
        }

        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            message.conversation = targetConversation;
            // 做特殊处理，如果是图片，需要上传oss以后，再转发
            message.decKey = message.content.decKey;

            Message messageSend = message;

            ChatManager.Instance().sendMessage(messageSend, new SendMessageCallback() {
                @Override
                public void onSuccess(long messageUid, long timestamp) {
                    if (count.decrementAndGet() == 0) {
                        resultMutableLiveData.postValue(new OperateResult<>(0));
                    }
                }

                @Override
                public void onFail(int errorCode) {
                    if (count.decrementAndGet() == 0) {
                        resultMutableLiveData.postValue(new OperateResult<>(errorCode));
                    }
                }

                @Override
                public void onPrepare(long messageId, long savedTime) {

                }
            });
        }
        return resultMutableLiveData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public MutableLiveData<OperateResult<Integer>> forward(Context context, Conversation targetConversation, Message... messages) {
        MutableLiveData<OperateResult<Integer>> resultMutableLiveData = new MutableLiveData<>();
        AtomicInteger count = new AtomicInteger(0);

        ChatManager.Instance().getWorkHandler().post(() -> {

            for (Message message : messages) {
                if (message != null) {
                    count.addAndGet(1);
                }
            }

            for (Message message : messages) {
                if (message == null) {
                    continue;
                }
                // 做特殊处理，如果是图片，需要上传oss以后，再转发
                Message messageSend = handleForwardMessage(context, message);

                messageSend.conversation = targetConversation;
                messageSend.decKey = messageSend.content.decKey;

                ChatManager.Instance().sendMessage(messageSend, new SendMessageCallback() {
                    @Override
                    public void onSuccess(long messageUid, long timestamp) {
                        if (count.decrementAndGet() == 0) {
                            resultMutableLiveData.postValue(new OperateResult<>(0));
                        }
                    }

                    @Override
                    public void onFail(int errorCode) {
                        if (count.decrementAndGet() == 0) {
                            resultMutableLiveData.postValue(new OperateResult<>(errorCode));
                        }
                    }

                    @Override
                    public void onPrepare(long messageId, long savedTime) {

                    }
                });
            }
        });
        return resultMutableLiveData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Message handleForwardMessage(Context context, Message message) {

       /* if (message.content instanceof ImageMessageContent){
            Message msgSend = new Message();
            msgSend.conversation = null;
            msgSend.content = createImageMessageContent(context, (ImageMessageContent)message.content);
            msgSend.decKey = msgSend.content.decKey;
            return msgSend;
        }

        if (message.content instanceof VideoMessageContent) {
            Message msgSend = new Message();
            msgSend.conversation = null;
            msgSend.content = createVideoMessageContent(context, (VideoMessageContent)message.content);
            msgSend.decKey = msgSend.content.decKey;
            return msgSend;
        }

        return message;*/


        if (message.content instanceof ImageMessageContent){
            Message msgSend = new Message();
            msgSend.conversation = null;
            msgSend.content = createImageMessageContent(context, (ImageMessageContent)message.content);
            msgSend.decKey = msgSend.content.decKey;
            return msgSend;
        }

        if (message.content instanceof VideoMessageContent) {
            Message msgSend = new Message();
            msgSend.conversation = null;
            msgSend.content = createVideoMessageContent(context, (VideoMessageContent)message.content);
            msgSend.decKey = msgSend.content.decKey;
            return msgSend;
        }

        if (message.content instanceof FileMessageContent) {
            Message msgSend = new Message();
            msgSend.conversation = null;
            msgSend.content = createFileMessageContent(context, (FileMessageContent)message.content);
            msgSend.decKey = msgSend.content.decKey;
            return msgSend;
        }

        return message;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private MessageContent createImageMessageContent(Context context, ImageMessageContent content) {

        try {
            if (content.decKey != null) {
                // copy file
                String pathSrc = FileUtil.copy(context, content.localPath);
                // zip
                String tempDir = FileUtil.getTempDir(context);
                String pwd = CompressUtil.getPsw(8);
                String zipPath = CompressUtil.zip(pathSrc,tempDir+"/", pwd);
                if (zipPath == null){
                    System.out.println("[ForwardViewModel] createImageMessageContent zip failed.");
                    return content;
                }

                // up oss
                String remotePath = OssHelper.getInstance(context).uploadImage(context, zipPath, null,OssHelper.TempDirectory);
                // delete temp file
                FileUtil.deleteSingleFile(zipPath);

                if (remotePath != null) {
                    //
                    ImageMessageContent imageMessageContent = new ImageMessageContent();
                    imageMessageContent.decKey = pwd;
                    imageMessageContent.remoteUrl = remotePath;
                    imageMessageContent.localPath = pathSrc;
                    imageMessageContent.imageHeight = content.imageHeight;
                    imageMessageContent.imageWidth = content.imageWidth;
                    System.out.println("[ForwardViewModel] createImageMessageContent success.");
                    return imageMessageContent;
                }

                System.out.println("[ForwardViewModel] up oss failed.");

            } else {

                System.out.println("[ForwardViewModel] remote:" + content.remoteUrl);
                Object lock = new Object();
                final File[] file = new File[1];

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            file[0] = GlideApp.with(context)
                                    .load(content.remoteUrl)
                                    .downloadOnly(100, 100)
                                    .get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }).start();


                try {
                    synchronized (lock) {
                        lock.wait(16000);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

                if (!file[0].exists()) {
                    System.out.println("[ForwardViewModel] createImageMessageContent file not exist.");
                    return content;
                }

                String tempDir = FileUtil.getTempDir(context);
                String pwd = CompressUtil.getPsw(8);
                String zipPath = CompressUtil.zip(file[0].getAbsolutePath(),tempDir+"/", pwd);
                if (zipPath == null){
                    System.out.println("[ForwardViewModel] createImageMessageContent zip failed.");
                    return content;
                }

                System.out.println("[ForwardViewModel] createImageMessageContent compress success.");

                // up oss
                String remotePath = OssHelper.getInstance(context).uploadImage(context, zipPath, null,OssHelper.TempDirectory);
                // delete temp file
                FileUtil.deleteSingleFile(zipPath);

                if (remotePath != null) {
                    //
                    ImageMessageContent imageMessageContent = new ImageMessageContent();
                    imageMessageContent.decKey = pwd;
                    imageMessageContent.remoteUrl = remotePath;
                    imageMessageContent.localPath = file[0].getAbsolutePath();
                    imageMessageContent.imageHeight = content.imageHeight;
                    imageMessageContent.imageWidth = content.imageWidth;
                    System.out.println("[ForwardViewModel] createImageMessageContent success.");
                    return imageMessageContent;
                }

                System.out.println("[ForwardViewModel] up oss failed.");


            }
        } catch (Exception e) {
            System.out.println("[ForwardViewModel] Exception." + e.getMessage());
        }

        return content;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private MessageContent createVideoMessageContent(Context context, VideoMessageContent content) {

        try {

            if (FileUtil.isFileExist(content.localPath)) {

                // zip
                String tempDir = FileUtil.getTempDir(context);
                String pwd = CompressUtil.getPsw(8);
                String zipPath = CompressUtil.zip(content.localPath, tempDir + "/", pwd);
                if (zipPath == null) {
                    System.out.println("[ForwardViewModel] createVideoMessageContent zip failed.");
                    return content;
                }
                // up oss
                String remotePath = OssHelper.getInstance(context).uploadImage(context, zipPath, null, OssHelper.TempDirectory);
                // delete temp file
                FileUtil.deleteSingleFile(zipPath);

                if (remotePath != null) {
                    //
                    VideoMessageContent videoMessageContent = new VideoMessageContent();
                    videoMessageContent.decKey = pwd;
                    videoMessageContent.remoteUrl = remotePath;
                    videoMessageContent.localPath = content.localPath;
                    System.out.println("[ForwardViewModel] createVideoMessageContent success.");
                    return videoMessageContent;
                }
                System.out.println("[ForwardViewModel] up oss failed.");
            }
        } catch (Exception e) {
            System.out.println("[ForwardViewModel] Exception." + e.getMessage());
        }

        return content;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private MessageContent createFileMessageContent(Context context, FileMessageContent content) {

        try {
            if (FileUtil.isFileExist(content.localPath)) {
                // zip
                String tempDir = FileUtil.getTempDir(context);
                String pwd = CompressUtil.getPsw(8);
                String zipPath = CompressUtil.zip(content.localPath, tempDir + "/", pwd);
                if (zipPath == null) {
                    System.out.println("[ForwardViewModel] createFileMessageContent zip failed.");
                    return content;
                }
                // up oss
                String remotePath = OssHelper.getInstance(context).uploadImage(context, zipPath, null, OssHelper.TempDirectory);
                // delete temp file
                FileUtil.deleteSingleFile(zipPath);

                if (remotePath != null) {
                    //
                    FileMessageContent fileMessageContent = new FileMessageContent();
                    fileMessageContent.decKey = pwd;
                    fileMessageContent.remoteUrl = remotePath;
                    fileMessageContent.localPath = content.localPath;
                    fileMessageContent.setName(content.getName());
                    System.out.println("[ForwardViewModel] createFileMessageContent success.");
                    return fileMessageContent;
                }
                System.out.println("[ForwardViewModel] up oss failed.");
            }
        } catch (Exception e) {
            System.out.println("[ForwardViewModel] Exception." + e.getMessage());
        }

        return content;
    }


}

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */
package cn.wildfire.chat.kit.viewmodel;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.audio.AudioPlayManager;
import cn.wildfire.chat.kit.audio.IAudioPlayListener;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.conversation.message.viewholder.AudioMessageContentViewHolder;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.third.location.data.LocationData;
import cn.wildfire.chat.kit.third.utils.FileUtils;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.CompressUtil;
import cn.wildfire.chat.kit.utils.DownloadManager;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfirechat.client.SqliteUtils;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.LocationMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.FriendGreetingMessageContent;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ReadEntry;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.OnClearMessageListener;
import cn.wildfirechat.remote.OnDeleteMessageListener;
import cn.wildfirechat.remote.OnMessageDeliverListener;
import cn.wildfirechat.remote.OnMessageReadListener;
import cn.wildfirechat.remote.OnMessageUpdateListener;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;
import cn.wildfirechat.remote.OnSendMessageListener;
import cn.wildfirechat.remote.SendMessageCallback;

import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_Share;
public class MessageViewModel extends ViewModel implements OnReceiveMessageListener,
    OnSendMessageListener,
    OnDeleteMessageListener,
    OnRecallMessageListener,
    OnMessageUpdateListener,
    OnMessageDeliverListener,
    OnMessageReadListener,
    OnClearMessageListener {
    private MutableLiveData<UiMessage> messageLiveData;
    private MutableLiveData<UiMessage> messageUpdateLiveData;
    private MutableLiveData<UiMessage> messageRemovedLiveData;
    private MutableLiveData<String> messageReportLiveData;
    private MutableLiveData<Map<String, String>> mediaUploadedLiveData;
    private MutableLiveData<Object> clearMessageLiveData;
    private MutableLiveData<Map<String, Long>> messageDeliverLiveData;
    private MutableLiveData<List<ReadEntry>> messageReadLiveData;
    private Message toPlayAudioMessage;
    private boolean callStatue = false;
    public MessageViewModel() {
        ChatManager.Instance().addOnReceiveMessageListener(this);
        ChatManager.Instance().addRecallMessageListener(this);
        ChatManager.Instance().addSendMessageListener(this);
        ChatManager.Instance().addOnMessageUpdateListener(this);
        ChatManager.Instance().addClearMessageListener(this);
        ChatManager.Instance().addMessageDeliverListener(this);
        ChatManager.Instance().addMessageReadListener(this);
    }
    @Override
    protected void onCleared() {
        ChatManager.Instance().removeOnReceiveMessageListener(this);
        ChatManager.Instance().removeRecallMessageListener(this);
        ChatManager.Instance().removeSendMessageListener(this);
        ChatManager.Instance().removeOnMessageUpdateListener(this);
        ChatManager.Instance().removeClearMessageListener(this);
        ChatManager.Instance().removeMessageDeliverListener(this);
        ChatManager.Instance().removeMessageReadListener(this);
    }
    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        if (messageLiveData != null && messages != null) {
            for (Message msg : messages) {
                postNewMessage(new UiMessage(msg));
            }
        }
    }
    public void onReportMessage(String url){
        if (messageReportLiveData != null && url != null) {
            UIUtils.postTaskSafely(() -> messageReportLiveData.setValue(url));
        }
    }
    public MutableLiveData<UiMessage> messageLiveData() {
        if (messageLiveData == null) {
            messageLiveData = new MutableLiveData<>();
        }
        return messageLiveData;
    }
    public MutableLiveData<UiMessage> messageUpdateLiveData() {
        if (messageUpdateLiveData == null) {
            messageUpdateLiveData = new MutableLiveData<>();
        }
        return messageUpdateLiveData;
    }
    public MutableLiveData<UiMessage> messageRemovedLiveData() {
        if (messageRemovedLiveData == null) {
            messageRemovedLiveData = new MutableLiveData<>();
        }
        return messageRemovedLiveData;
    }
    public MutableLiveData<String> messageReportLiveData() {
        if (messageReportLiveData == null) {
            messageReportLiveData = new MutableLiveData<>();
        }
        return messageReportLiveData;
    }
    public MutableLiveData<Map<String, String>> mediaUpdateLiveData() {
        if (mediaUploadedLiveData == null) {
            mediaUploadedLiveData = new MutableLiveData<>();
        }
        return mediaUploadedLiveData;
    }
    public MutableLiveData<Object> clearMessageLiveData() {
        if (clearMessageLiveData == null) {
            clearMessageLiveData = new MutableLiveData<>();
        }
        return clearMessageLiveData;
    }
    public MutableLiveData<Map<String, Long>> messageDeliverLiveData() {
        if (messageDeliverLiveData == null) {
            messageDeliverLiveData = new MutableLiveData<>();
        }
        return messageDeliverLiveData;
    }
    public MutableLiveData<List<ReadEntry>> messageReadLiveData() {
        if (messageReadLiveData == null) {
            messageReadLiveData = new MutableLiveData<>();
        }
        return messageReadLiveData;
    }
    @Override
    public void onRecallMessage(Message message) {
        if (message != null) {
            UiMessage uiMessage = new UiMessage(message);
            postMessageUpdate(uiMessage);
        }
    }
    public void resendMessage(Message message) {
        deleteMessage(message);
        sendMessage(message.conversation, message.content);
    }
    /*public void recallMessage(Message message) {
        ChatManager.Instance().recallMessage(message, new GeneralCallback() {
            @Override
            public void onSuccess() {
                Message msg = ChatManager.Instance().getMessage(message.messageId);
                postMessageUpdate(new UiMessage(msg));
            }
            @Override
            public void onFail(int errorCode) {
                Log.e(MessageViewModel.class.getSimpleName(), WfcUIKit.getString(R.string.recall_failed) + errorCode);
                // TODO 撤回失败
            }
        });
    }*/
    public void recallMessage(Message message) {

        System.out.println("@@@   model: "+message.messageHash);
        ChatManager.Instance().recallMessage(message, new GeneralCallback() {
            @Override
            public void onSuccess() {
                //点击确定事件
                LoadingDialog.hideLoading();

                Message msg = ChatManager.Instance().getMessage(message.messageId);
                postMessageUpdate(new UiMessage(msg));
                /*if (messageRemovedLiveData != null) {
                    messageRemovedLiveData.setValue(new UiMessage(message));
                }*/
            }
            @Override
            public void onFail(int errorCode) {
                Log.e(MessageViewModel.class.getSimpleName(), WfcUIKit.getString(R.string.recall_failed) + errorCode);
                LoadingDialog.hideLoading();
                Message msg = ChatManager.Instance().getMessage(message.messageId);
                postMessageUpdate(new UiMessage(msg));
                // TODO 撤回失败
            }
        });
    }
    public void deleteMessageTo(Message message) {
        if (messageRemovedLiveData != null) {
            messageRemovedLiveData.setValue(new UiMessage(message));
        }
        ChatManager.Instance().deleteMessageTo(message, new GeneralCallback() {
            @Override
            public void onSuccess() {
                //点击确定事件
                LoadingDialog.hideLoading();

                Message msg = ChatManager.Instance().getMessage(message.messageId);
                postMessageUpdate(new UiMessage(msg));
                if (messageRemovedLiveData != null) {
                    messageRemovedLiveData.setValue(new UiMessage(message));
                }
            }
            @Override
            public void onFail(int errorCode) {
                Log.e(MessageViewModel.class.getSimpleName(), WfcUIKit.getString(R.string.recall_failed) + errorCode);
                // TODO 撤回失败
            }
        });
    }
    @Override
    public void onDeleteMessage(Message message) {
        if (messageRemovedLiveData != null) {
            messageRemovedLiveData.setValue(new UiMessage(message));
        }
    }
    public void deleteMessage(Message message) {
        if (messageRemovedLiveData != null) {
            messageRemovedLiveData.setValue(new UiMessage(message));
        }
        ChatManager.Instance().deleteMessage(message);
    }
    public void playAudioMessage(UiMessage message, Context context) {
        if (message == null || !(message.message.content instanceof SoundMessageContent)) {
            return;
        }
        if (toPlayAudioMessage != null && toPlayAudioMessage.equals(message.message)) {
            AudioPlayManager.getInstance().stopPlay();
            toPlayAudioMessage = null;
            return;
        }
        toPlayAudioMessage = message.message;
        if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
            message.message.status = MessageStatus.Played;
            ChatManager.Instance().setMediaMessagePlayed(message.message.messageId);
        }
        File file = mediaMessageContentFile(message.message, context);
        if (file == null) {
            return;
        }
        if (file.exists()) {
            playAudio(message, file);
        } else {
            Log.e("ConversationViewHolder", "audio not exist");
        }
    }
    public void openRedPacket(UiMessage message){
        postMessageUpdate(message);
    }
    public void stopPlayAudio() {
        AudioPlayManager.getInstance().stopPlay();
    }
    public void saveCallMessage(Conversation conversation, MessageContent content, boolean isSend){
        Message msg = new Message();
        msg.conversation = conversation;
        msg.content = content;
        if(isSend){
            msg.direction = MessageDirection.Send;
            msg.sender = ChatManager.Instance().getUserId();
            msg.status = MessageStatus.Sent;
        }else{
            msg.direction = MessageDirection.Receive;
            msg.sender = conversation.target;
            msg.status = MessageStatus.Readed;
        }
        msg.serverTime = System.currentTimeMillis();
        ChatManager.Instance().saveCallMessage(msg);
    }
    public void sendMessage(Conversation conversation, MessageContent content, SendMessageCallback callback) {
        Message message = new Message();
        message.conversation = conversation;
        message.content = content;
        message.sender = ChatManager.Instance().getUserId();
        ChatManager.Instance().sendMessage(message, callback);
    }
    public void sendMessage(Conversation conversation, MessageContent content) {
        Message msg = new Message();
        msg.conversation = conversation;
        msg.content = content;
        msg.decKey = conversation.decKey;
        sendMessage(msg);
    }
    public void sendCallMessage(String target, CallMessageContent call){
        Message msg = new Message();
        msg.conversation = new Conversation(Conversation.ConversationType.Call, target);
        msg.content = call;
        msg.sender = ChatManager.Instance().getUserId();
        ChatManager.Instance().sendCallMessage(msg, null);
    }
    public MutableLiveData<OperateResult<Boolean>> sendMessageEx(Message message){
        // TODO
        return null;
    }
    public void sendMessage(Message message) {
        // the call back would be called on the ui thread
        message.sender = ChatManager.Instance().getUserId();
        ChatManager.Instance().sendMessage(message, null);
    }
    public void sendTextMsg(Conversation conversation, TextMessageContent txtContent) {
        sendMessage(conversation, txtContent);
        ChatManager.Instance().setConversationDraft(conversation, null);
    }
    public void sendShareMsg(String osnID, String name, String content, String theme, String portrait, String target, String url){
        Conversation conversation = new Conversation(osnID.startsWith("OSNU") ? Conversation.ConversationType.Single
                : Conversation.ConversationType.Group, osnID, 0);
        CardMessageContent cardMessageContent = new CardMessageContent(CardType_Share, target, name, content, portrait, theme, url);
        sendMessage(conversation, cardMessageContent);
    }
    public void sendRedPacketMsg(Conversation conversation, RedPacketMessageContent redPacketMessageContent) {
        sendMessage(conversation, redPacketMessageContent);
    }
    public void saveDraft(Conversation conversation, String draftString) {
        ChatManager.Instance().setConversationDraft(conversation, draftString);
    }
    public void setConversationSilent(Conversation conversation, boolean silent) {
        ChatManager.Instance().setConversationSilent(conversation, silent);
    }
    public void sendImgMsg(Conversation conversation, Uri imageFileThumbUri, Uri imageFileSourceUri,String remoteUrl) {
        ImageMessageContent imgContent = new ImageMessageContent(imageFileSourceUri.getEncodedPath());
        String thumbParam = ChatManager.Instance().getImageThumbPara();
        if (!TextUtils.isEmpty(thumbParam)) {
            imgContent.setThumbPara(ChatManager.Instance().getImageThumbPara());
        }
        ((MediaMessageContent)imgContent).remoteUrl = remoteUrl;
        imgContent.decKey = conversation.decKey;
        sendMessage(conversation, imgContent);
    }
    public void sendImgMsg(Conversation conversation, File imageFileThumb, File imageFileSource,String remoteUrl) {
        // Uri.fromFile()遇到中文檔名會轉 ASCII，這個 ASCII 的 path 將導致後面 ChatManager.sendMessage()
        // 在 new File()時找不到 File 而 return
        Uri imageFileThumbUri = Uri.parse(Uri.decode(imageFileThumb.getAbsolutePath()));
//        Uri imageFileThumbUri = Uri.fromFile(imageFileThumb);
        Uri imageFileSourceUri = Uri.parse(Uri.decode(imageFileSource.getAbsolutePath()));
//        Uri imageFileSourceUri = Uri.fromFile(imageFileSource);
        sendImgMsg(conversation, imageFileThumbUri, imageFileSourceUri,remoteUrl);
    }
    public void sendVideoMsg(Conversation conversation, File file,String remoteUrl) {
        VideoMessageContent videoMessageContent = new VideoMessageContent(file.getPath());
        videoMessageContent.remoteUrl = remoteUrl;
        videoMessageContent.decKey = conversation.decKey;
        sendMessage(conversation, videoMessageContent);
    }
    public void sendStickerMsg(Conversation conversation, String localPath, String remoteUrl) {
        StickerMessageContent stickerMessageContent = new StickerMessageContent(localPath);
        stickerMessageContent.remoteUrl = remoteUrl;
        sendMessage(conversation, stickerMessageContent);
    }
    public void sendFileMsg(Conversation conversation, File file,String remoteUlr,String fileName) {
        FileMessageContent fileMessageContent = new FileMessageContent(file.getPath());
        ((MediaMessageContent)fileMessageContent).remoteUrl = remoteUlr;
        fileMessageContent.setName(fileName);
        sendMessage(conversation, fileMessageContent);
    }
    public void sendLocationMessage(Conversation conversation, LocationData locationData) {
        LocationMessageContent locCont = new LocationMessageContent();
        locCont.setTitle(locationData.getPoi());
        locCont.getLocation().setLatitude(locationData.getLat());
        locCont.getLocation().setLongitude(locationData.getLng());
        locCont.setThumbnail(locationData.getThumbnail());
        sendMessage(conversation, locCont);
    }
    public void sendAudioFile(Conversation conversation, Uri audioPath, int duration,String remoteUrl) {
        if (audioPath != null) {
            File file = new File(audioPath.getPath());
            if (!file.exists() || file.length() == 0L) {
                Log.e("ConversationViewModel", "send audio file fail");
                return;
            }
            SoundMessageContent soundContent = new SoundMessageContent(file.getAbsolutePath());
            soundContent.setDuration(duration);
            soundContent.remoteUrl = remoteUrl;
            soundContent.decKey = conversation.decKey;
            sendMessage(conversation, soundContent);
        }
    }
    private void playAudio(UiMessage message, File file) {
        Uri uri = Uri.fromFile(file);
        /**
         * 如果名称中有%号
         * copy文件，重新生成uri
         * **/




        AudioPlayManager.getInstance().startPlay(WfcUIKit.getWfcUIKit().getApplication(), uri, new IAudioPlayListener() {
            @Override
            public void onStart(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = true;
                    postMessageUpdate(message);
                }
            }
            @Override
            public void onStop(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = false;
                    toPlayAudioMessage = null;
                    postMessageUpdate(message);
                }
            }
            @Override
            public void onComplete(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = false;
                    toPlayAudioMessage = null;
                    postMessageUpdate(message);
                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public File mediaMessageContentFile(Message message, Context context) {
        String dir = null;
        String name = null;
        MessageContent content = message.content;
        if (!(content instanceof MediaMessageContent)) {
            return null;
        }
        if (!TextUtils.isEmpty(((MediaMessageContent) content).localPath)) {
            return new File(((MediaMessageContent) content).localPath);
        }
        switch (((MediaMessageContent) content).mediaType) {
            case VOICE:
                name = message.messageUid + ".mp3";
                dir = Config.getAudioSaveDir(context);
                break;
            case FILE:
                name = message.messageUid + "-" + ((FileMessageContent) message.content).getName();
                dir = Config.getFileSaveDir(context);
                break;
            case VIDEO:
                name = message.messageUid + ".mp4";
                dir = Config.getVideoSaveDir(context);
                break;
            default:
        }
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(name)) {
            return null;
        }
        return new File(dir, name);
    }


    public void downloadMedia(Context context, UiMessage message, File targetFile) {
        MessageContent content = message.message.content;
        if (!(content instanceof MediaMessageContent)) {
            return;
        }
        if (message.isDownloading) {
            return;
        }
        message.isDownloading = true;
        postMessageUpdate(message);


        DownloadManager.download(((MediaMessageContent) content).remoteUrl,
                targetFile.getParent(),
                targetFile.getName() + ".tmp",
                new DownloadManager.OnDownloadListener() {
            @Override
            public void onSuccess(File file) {

                String decKey = message.message.content.decKey;


                if (decKey != null) {

                    try {
                        File[] files = CompressUtil.unzip(file.getPath(), decKey);
                        if (files.length > 0) {
                            String unZipPath = files[0].getPath();
                            FileUtil.renameFile(unZipPath, targetFile.getPath());
                        } else {
                            System.out.println("[MessageViewModel] download file unzip failed..");
                            Toast.makeText(context, "下载文件失败", Toast.LENGTH_SHORT).show();
                        }
                        message.progress = 100;
                    } catch (Exception e) {
                        System.out.println("[MessageViewModel] download file unzip Exception.");
                        message.progress = 0;
                    } finally {
                        file.delete();
                    }



                } else {
                    file.renameTo(targetFile);
                    message.progress = 100;
                }


                message.isDownloading = false;

                postMessageUpdate(message);
            }
            @Override
            public void onProgress(int percent) {
                message.progress = percent;
                postMessageUpdate(message);
            }
            @Override
            public void onFail() {
                message.isDownloading = false;
                message.progress = 0;
                postMessageUpdate(message);
                Log.e(AudioMessageContentViewHolder.class.getSimpleName(), "download failed: " + message.message.messageId);
            }
        });
    }
    private void postMessageUpdate(UiMessage message) {
        if (message == null || message.message == null) {
            return;
        }
        if (messageUpdateLiveData != null) {
            UIUtils.postTaskSafely(() -> messageUpdateLiveData.setValue(message));
        }
    }
    private void postNewMessage(UiMessage message) {
        if (message == null || message.message == null) {
            return;
        }
        if (messageLiveData != null) {
            UIUtils.postTaskSafely(() -> messageLiveData.setValue(message));
        }
    }
    @Override
    public void onSendSuccess(Message message) {
        postMessageUpdate(new UiMessage(message));
    }
    @Override
    public void onSendFail(Message message, int errorCode) {
        postMessageUpdate(new UiMessage(message));
    }
    @Override
    public void onSendPrepare(Message message, long savedTime) {
        postNewMessage(new UiMessage(message));
    }
    @Override
    public void onProgress(Message message, long uploaded, long total) {
        UiMessage uiMessage = new UiMessage(message);
        uiMessage.progress = (int) (uploaded * 100 / total);
        postMessageUpdate(uiMessage);
    }
    @Override
    public void onMediaUpload(Message message, String remoteUrl) {
        if (mediaUploadedLiveData != null) {
            Map<String, String> map = new HashMap<>();
            map.put(((MediaMessageContent) message.content).localPath, remoteUrl);
            UIUtils.postTaskSafely(() -> mediaUploadedLiveData.setValue(map));
        }
    }
    @Override
    public void onMessageUpdate(Message message) {
        postNewMessage(new UiMessage(message));
    }
    @Override
    public void onClearMessage(Conversation conversation) {
        if (clearMessageLiveData != null) {
            clearMessageLiveData.postValue(new Object());
        }
    }
    @Override
    public void onMessageDelivered(Map<String, Long> deliveries) {
        if (messageDeliverLiveData != null) {
            messageDeliverLiveData.postValue(deliveries);
        }
    }
    @Override
    public void onMessageRead(List<ReadEntry> readEntries) {
        if (messageReadLiveData != null) {
            messageReadLiveData.postValue(readEntries);
        }
    }
    public boolean isCalling(){
        return callStatue;
    }
    public void setCallStatue(boolean status){
        callStatue = status;
    }
}

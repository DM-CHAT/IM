/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import static android.os.FileUtils.copy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.Objects.BasicParams;
import com.z.fileselectorlib.Objects.FileInfo;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import cn.wildfire.chat.kit.AppServiceProvider;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.MessageContextMenuItem;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.ConversationMessageAdapter;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.favorite.FavoriteItem;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.DonwloadSaveImg;
import cn.wildfire.chat.kit.utils.DownloadManager;
import cn.wildfire.chat.kit.utils.FileUtil;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MessageContentMediaType;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetGroupInfoCallback;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.UserSettingScope;

/**
 * 普通消息
 */
public abstract class NormalMessageContentViewHolder extends MessageContentViewHolder {
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int REQUEST_EXTERNAL_STORAGE = 200;
    @BindView(R2.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.frameLayout)
    FrameLayout frameLayout;
    @BindView(R2.id.errorLinearLayout)
    LinearLayout errorLinearLayout;
    @BindView(R2.id.nameTextView)
    TextView nameTextView;
    @BindView(R2.id.progressBar)
    ProgressBar progressBar;
    @BindView(R2.id.checkbox)
    CheckBox checkBox;

    @BindView(R2.id.singleReceiptImageView)
    @Nullable
    ImageView singleReceiptImageView;

    @BindView(R2.id.groupReceiptFrameLayout)
    @Nullable
    FrameLayout groupReceiptFrameLayout;

    @BindView(R2.id.deliveryProgressBar)
    @Nullable
    ProgressBar deliveryProgressBar;
    @BindView(R2.id.readProgressBar)
    @Nullable
    ProgressBar readProgressBar;


    private static boolean delete = false;

    public NormalMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message, int position) {
        super.onBind(message, position);
        this.message = message;
        this.position = position;

        setSenderAvatar(message.message);
        setSenderName(message.message);
        setSendStatus(message.message);
        try {
            onBind(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (message.isFocus) {
            highlightItem(itemView, message);
        }
    }

    protected abstract void onBind(UiMessage message);

    /**
     * when animation finish, do not forget to set {@link UiMessage#isFocus} to {@code true}
     *
     * @param itemView the item view
     * @param message  the message to highlight
     */
    protected void highlightItem(View itemView, UiMessage message) {
        Animation animation = new AlphaAnimation((float) 0.4, (float) 0.2);
        itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorPrimary));
        animation.setRepeatCount(2);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                itemView.setBackground(null);
                message.isFocus = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        itemView.startAnimation(animation);
    }

    // TODO 也用注解来做？
    public boolean checkable(UiMessage message) {
        return true;
    }

    @Optional
    @OnClick(R2.id.errorLinearLayout)
    public void onRetryClick(View itemView) {
        new MaterialDialog.Builder(fragment.getContext())
                .content(WfcUIKit.getString(R.string.is_resend))
                .negativeText(WfcUIKit.getString(R.string.cancel))
                .positiveText(WfcUIKit.getString(R.string.resend))
                .onPositive((dialog, which) -> messageViewModel.resendMessage(message.message))
                .build()
                .show();
    }

    @Optional
    @OnClick(R2.id.groupReceiptFrameLayout)
    public void OnGroupMessageReceiptClick(View itemView) {
        ((ConversationMessageAdapter) adapter).onGroupMessageReceiptClick(message.message);
    }

    /*@MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_RECALL, priority = 10)
    public void recall(View itemView, UiMessage message) {
        messageViewModel.recallMessage(message.message);
    }*/

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_CALLBACK, confirm = true, priority = 11)
    public void callBack(View itemView, UiMessage message) {
        //撤回
        messageViewModel.recallMessage(message.message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_DELETETO, confirm = true, priority = 11)
    public void deleteTo(View itemView, UiMessage message) {
        //删除对方消息
        messageViewModel.deleteMessageTo(message.message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_DELETE, confirm = true, priority = 11)
    public void removeMessage(View itemView, UiMessage message) {
        messageViewModel.deleteMessage(message.message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_TOPMESSAGE, priority = 12)
    public void topMessage(View itemView, UiMessage message) {
        //置顶消息
        long uid = message.message.messageId;
        Message message1 = ChatManager.Instance().getMessage(uid);
        GroupViewModel groupViewModel1 = ViewModelProviders.of(fragment).get(GroupViewModel.class);
        GroupInfo groupInfo1 = groupViewModel1.getGroupInfo(message1.conversation.target, false);

        String serverTime = String.valueOf(message1.serverTime);
        String topTime = "top_"+serverTime;
        TextMessageContent textMessageContent = (TextMessageContent)message1.content;
        String content = textMessageContent.content;
        JSONObject json = new JSONObject();
        json.put("fromUser",message1.sender);
        json.put("data",content);
        json.put("key",topTime);
        json.put("type","text");
        json.put("hasho",message1.messageHash0);

        String value = json.toJSONString();
        ChatManager.Instance().topMessage(topTime, value, groupInfo1.target, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_FORWARD, priority = 11)
    public void forwardMessage(View itemView, UiMessage message) {
        Intent intent = new Intent(fragment.getContext(), ForwardActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);

        /*Intent intent = new Intent(fragment.getContext(), ContactListActivity.class);
        intent.putExtra("message", message.message);
        intent.putExtra("forward", true);
        fragment.startActivity(intent);*/

    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_MULTI_CHECK, priority = 13)
    public void checkMessage(View itemView, UiMessage message) {
        fragment.toggleMultiMessageMode(message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_CHANEL_PRIVATE_CHAT, priority = 12)
    public void startChanelPrivateChat(View itemView, UiMessage message) {
        Intent intent = ConversationActivity.buildConversationIntent(fragment.getContext(), Conversation.ConversationType.Channel, message.message.conversation.target, message.message.conversation.line, message.message.sender);
        fragment.startActivity(intent);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_QUOTE, priority = 14)
    public void quoteMessage(View itemView, UiMessage message) {
        if (message.message.messageHash0 == null) {
            Message msgTemp = ChatManager.Instance().getMessage(message.message.messageId);
            if (msgTemp != null) {
                fragment.getConversationInputPanel().quoteMessage(msgTemp);
                return;
            } else {
                System.out.println("[QuoteMessage] message temp is null");
            }
        }
        fragment.getConversationInputPanel().quoteMessage(message.message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_FAV, confirm = false, priority = 12)
    public void fav(View itemView, UiMessage message) {
        AppServiceProvider appServiceProvider = WfcUIKit.getWfcUIKit().getAppServiceProvider();
        FavoriteItem favoriteItem = FavoriteItem.fromMessage(message.message);

        appServiceProvider.addFavoriteItem(favoriteItem, new SimpleCallback<Void>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(Void aVoid) {
                Toast.makeText(fragment.getContext(), "fav ok", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(fragment.getContext(), "fav error: " + code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_SAVE, priority = 12)
    public void saveMessage(View itemView, UiMessage message) {
        String path = message.message.content.encode().localMediaPath;
        /*MessageContent messageContent = message.message.content;
        ImageMessageContent imageMessageContent = (ImageMessageContent) messageContent;
        if(imageMessageContent.getThumbnail() != null){
            saveBitmap(imageMessageContent.getThumbnail());
        }else{
            DonwloadSaveImg.donwloadImg(fragment.getActivity(),imageMessageContent.remoteUrl);
        }*/

        String paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

        String file_name;
        if (message.message.content.getMessageContentType() == MessageContentType.ContentType_File){
            file_name = ((FileMessageContent)message.message.content).getName();
        } else {
            file_name = getFileNameWithSuffix(path);
        }
        if (FileUtil.copy(path, paths + "/" + file_name)){
            Toast.makeText(fragment.getActivity(), "已保存到Documents目录！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(fragment.getActivity(), "请点击文件下载在保存", Toast.LENGTH_SHORT).show();
        }

        /*if(copyFile(path, paths + "/" + file_name)){
            Toast.makeText(fragment.getActivity(), "已保存到Documents目录！", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(fragment.getActivity(), "请点击文件下载在保存", Toast.LENGTH_SHORT).show();
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_DETAILS, priority = 12)
    public void showDetails(View itemView, UiMessage message) {
        String path = message.message.content.encode().localMediaPath;
        String paths = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        String file_name = getFileNameWithSuffix(path);
        String time = getFileLastModifiedTime(new File(path));
        String size = "";
        try {
            size = String.valueOf(formatFileSize(getFileSize(new File(path))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragment.getActivity());
        View view = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.dialog_details, null);
        alertDialog.setView(view);
        alertDialog.setCancelable(true);
        alertDialog.create();
        final AlertDialog dialog = alertDialog.show();
        dialog.getWindow().getDecorView().setBackground(null);
        TextView tv_path = view.findViewById(R.id.tv_paht);
        TextView tv_size = view.findViewById(R.id.tv_size);
        TextView tv_time = view.findViewById(R.id.tv_time);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);

        tv_path.setText(path);
        tv_size.setText(size);
        tv_time.setText(time);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //    copyFile(path,paths+"/"+file_name);
    }

    private static final String mformatType = "yyyy/MM/dd HH:mm:ss";

    public static String getFileLastModifiedTime(File file) {
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat(mformatType);
        cal.setTimeInMillis(time);

        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.getTime());
    }

    /**
     * 转换文件大
     *
     * @param fileSize 文件大小 字节
     * @return
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileSize == 0) {
            return wrongSize;
        }
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "KB";
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取指定文件大小
     *
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        String title = WfcUIKit.getString(R.string.no_set);
        switch (tag) {
            /*case MessageContextMenuItemTags.TAG_RECALL:
                title = WfcUIKit.getString(R.string.recall);
                break;*/
            case MessageContextMenuItemTags.TAG_CALLBACK:
                title = WfcUIKit.getString(R.string.callback);
                break;
            case MessageContextMenuItemTags.TAG_DELETE:
                title = WfcUIKit.getString(R.string.delete);
                break;
            case MessageContextMenuItemTags.TAG_DELETETO:
                title = WfcUIKit.getString(R.string.deleteto);
                break;
            case MessageContextMenuItemTags.TAG_FORWARD:
                title = WfcUIKit.getString(R.string.forward);
                break;
            case MessageContextMenuItemTags.TAG_QUOTE:
                title = WfcUIKit.getString(R.string.quota);
                break;
            case MessageContextMenuItemTags.TAG_MULTI_CHECK:
                title = WfcUIKit.getString(R.string.multi_check);
                break;
            case MessageContextMenuItemTags.TAG_CHANEL_PRIVATE_CHAT:
                title = WfcUIKit.getString(R.string.private_cheat);
                break;
            case MessageContextMenuItemTags.TAG_FAV:
                title = WfcUIKit.getString(R.string.fav);
                break;
            case MessageContextMenuItemTags.TAG_SAVE:
                title = WfcUIKit.getString(R.string.save_as);
                break;
            case MessageContextMenuItemTags.TAG_DETAILS:
                title = WfcUIKit.getString(R.string.details);
                break;
            case MessageContextMenuItemTags.TAG_TOPMESSAGE:
                title = WfcUIKit.getString(R.string.top_message);
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public String contextConfirmPrompt(Context context, String tag) {
        String title = WfcUIKit.getString(R.string.no_set);
        switch (tag) {
            case MessageContextMenuItemTags.TAG_DELETE:
                title = WfcUIKit.getString(R.string.confirm_delete);
                break;
            case MessageContextMenuItemTags.TAG_CALLBACK:
                title = WfcUIKit.getString(R.string.confirm_callback);
                break;
            case MessageContextMenuItemTags.TAG_DELETETO:
                title = WfcUIKit.getString(R.string.confirm_delete);
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public boolean contextMenuItemFilter(UiMessage uiMessage, String tag) {
        Message message = uiMessage.message;
        if (message == null)
            return false;

        GroupViewModel groupViewModel1 = ViewModelProviders.of(fragment).get(GroupViewModel.class);
        GroupInfo groupInfo1 = groupViewModel1.getGroupInfo(message.conversation.target, false);
        System.out.println("@@@      groupInfo1.Attribute  ="+groupInfo1.attribute);

        //转发
        if(MessageContextMenuItemTags.TAG_FORWARD.equals(tag)){
            if(groupInfo1.isGroupForward()){
                if(uiMessage.message.content.getMessageContentType() == 12 || uiMessage.message.content.messageType == null){
                    return true;
                }else{
                    return false;
                }
            }else{
                return true;
            }

        }

        //复制
        if(MessageContextMenuItemTags.TAG_CLIP.equals(tag)){
            if(groupInfo1.isGroupCopy()){
                return false;
            }else{
                return true;
            }
        }

        //置顶消息
        if(MessageContextMenuItemTags.TAG_TOPMESSAGE.equals(tag)){

            /*GroupMember groupMember1 = ChatManager.Instance().getGroupMember(groupInfo1.target, ChatManager.Instance().getUserId());
            if(groupMember1.type == GroupMember.GroupMemberType.Normal){
                return true;
            }else{
                return false;
            }*/

            if(message.conversation.type == Conversation.ConversationType.Single){
                return true;
            }else{
                String myself = ChatManager.Instance().getUserId();
                List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupInfo1.target);
                GroupMember groupMember = ChatManager.Instance().getGroupMember(groupInfo1.target, myself);
                if (groupMember.type == GroupMember.GroupMemberType.Manager
                        || groupMember.type == GroupMember.GroupMemberType.Owner
                        || groupMember.memberId.equalsIgnoreCase(groupInfo1.owner)
                ) {
                    return false;
                }else{
                    for (GroupMember member : managers) {
                        if(member.memberId.equals(ChatManager.Instance().getUserId())){
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        //多选 暂时隐藏
        if(MessageContextMenuItemTags.TAG_MULTI_CHECK.equals(tag)){

            if(message.content.messageType == null){
                return true;
            }else{
                return false;
            }

        }

        if (MessageContextMenuItemTags.TAG_RECALL.equals(tag)) {
            /**----------------- 开始 ------------------*/
            //暂时隐藏撤回
            String userId = ChatManager.Instance().getUserId();
            if (message.conversation.type == Conversation.ConversationType.Group) {
                GroupViewModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
                GroupInfo groupInfo = groupViewModel.getGroupInfo(message.conversation.target, false);
                if (groupInfo != null && userId.equals(groupInfo.owner)) {
                    return false;
                }
                GroupMember groupMember = groupViewModel.getGroupMember(message.conversation.target, ChatManager.Instance().getUserId());
                if (groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Manager
                        || groupMember.type == GroupMember.GroupMemberType.Owner)) {
                    return false;
                }
            }

            long delta = ChatManager.Instance().getServerDeltaTime();
            long now = System.currentTimeMillis();

            return message.direction != MessageDirection.Send
                    || !TextUtils.equals(message.sender, ChatManager.Instance().getUserId())
                    || now - (message.serverTime - delta) >= Config.RECALL_TIME_LIMIT * 1000;
            //     return true;
            /**----------------- 结束 ------------------*/
        }

        if(MessageContextMenuItemTags.TAG_DELETE.equals(tag)){
            /*if(message.direction != MessageDirection.Send){
                return true;
            }*/
        }

        if (MessageContextMenuItemTags.TAG_DELETETO.equals(tag)) {

            //暂时隐藏删除对方消息
        //    return true;

            String userId = ChatManager.Instance().getUserId();
            if (message.conversation.type == Conversation.ConversationType.Single) {
                if (message.direction != MessageDirection.Send) {
                    return true;
                }
            }else if(message.conversation.type == Conversation.ConversationType.Group){
                GroupViewModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
                GroupMember groupMember = groupViewModel.getGroupMember(message.conversation.target, ChatManager.Instance().getUserId());
                if (groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Manager
                        || groupMember.type == GroupMember.GroupMemberType.Owner)) {
                    return false;
                }
                return true;
            }

            if(uiMessage.message.content.messageType == null){
                return true;
            }
        }
        if (MessageContextMenuItemTags.TAG_CALLBACK.equals(tag)) {

            //暂时隐藏撤回
          //  return true;

            if (message.direction == MessageDirection.Send) {
                long delta = ChatManager.Instance().getServerDeltaTime();
                long now = System.currentTimeMillis();
                if(now - (message.serverTime - delta) >= Config.RECALL_TIME_LIMIT * 1000){
                    return true;
                }else{
                    if(uiMessage.message.content.messageType == null){
                        return true;
                    }else{
                        return false;
                    }
                }

            } else {
                return true;
            }

        }
        if(MessageContextMenuItemTags.TAG_DETAILS.equals(tag)){
            if(message.content.messageType == MessageContentMediaType.FILE
                    ||message.content.messageType == MessageContentMediaType.IMAGE
                    ||message.content.messageType == MessageContentMediaType.VIDEO
                    ||message.content.messageType == MessageContentMediaType.VOICE ){
                return false;
            }
            return true;
        }

        if(MessageContextMenuItemTags.TAG_SAVE.equals(tag)){
            if(groupInfo1.isGroupCopy()){
                if(message.content.messageType == MessageContentMediaType.FILE
                        ||message.content.messageType == MessageContentMediaType.IMAGE
                        ||message.content.messageType == MessageContentMediaType.VIDEO
                        /*||message.content.messageType == MessageContentMediaType.VOICE*/ ){
                    return false;
                }
                /*if(message.content.messageType == MessageContentMediaType.IMAGE ){
                    return false;
                }*/
                return true;

            }else{
                return true;
            }

        }

        // 只有channel 主可以发起
        if (MessageContextMenuItemTags.TAG_CHANEL_PRIVATE_CHAT.equals(tag)) {
            if (uiMessage.message.conversation.type == Conversation.ConversationType.Channel
                    && uiMessage.message.direction == MessageDirection.Receive) {
                return false;
            }
            return true;
        }

        // 只有部分消息支持引用
        if (MessageContextMenuItemTags.TAG_QUOTE.equals(tag)) {
            /**----------------- 开始 ------------------*/
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent
                || messageContent instanceof FileMessageContent
                || messageContent instanceof VideoMessageContent
                || messageContent instanceof StickerMessageContent
                || messageContent instanceof ImageMessageContent
                || messageContent.messageType == MessageContentMediaType.VOICE) {
                return false;
            }
            return true;
            /**----------------- 结束 ------------------*/
        }

        // 只有部分消息支持收藏
        if (MessageContextMenuItemTags.TAG_FAV.equals(tag)) {
            /**----------------- 开始 ------------------*/
            //暂时隐藏收藏
//            MessageContent messageContent = message.content;
//            if (messageContent instanceof TextMessageContent
//                || messageContent instanceof FileMessageContent
//                || messageContent instanceof CompositeMessageContent
//                || messageContent instanceof VideoMessageContent
//                || messageContent instanceof SoundMessageContent
//                || messageContent instanceof ImageMessageContent) {
//                return false;
//            }
            return true;
            /**----------------- 结束 ------------------*/
        }
        return false;
    }

    private void setSenderAvatar(Message item) {
        // TODO get user info from viewModel
        if (item == null)
            return;
        UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(item.sender, false);
        if (portraitImageView != null && userInfo != null) {
            //单聊隐藏头像，群聊头像是圆形
            if (item.conversation.type == Conversation.ConversationType.Single) {
                ((View) portraitImageView.getParent()).setVisibility(View.GONE);
            } else if (item.conversation.type == Conversation.ConversationType.Group) {

                GlideApp
                        .with(fragment)
                        .load(userInfo.portrait)
                        .transforms(new CircleCrop(), new RoundedCorners(10))
                        .placeholder(R.mipmap.avatar_def_yuan)
                        .into(portraitImageView);

                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GroupViewModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
                        GroupMember groupMember = groupViewModel.getGroupMember(item.conversation.target, userInfo.uid);
                        System.out.println("@@@            groupMember= "+groupMember.type);
                        System.out.println("@@@            groupMember.memberId= "+groupMember.memberId);
                        if (groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Manager)){
                            frameLayout.setBackground(fragment.getResources().getDrawable(R.mipmap.ic_bk_manager));
                        }else if(groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Owner)){
                            frameLayout.setBackground(fragment.getResources().getDrawable(R.mipmap.ic_bk_qunzhu));
                        }else{
                            frameLayout.setBackground(fragment.getResources().getDrawable(R.mipmap.ic_bk_normal));
                        }
                    }
                });
            }
        }
    }

    private void setSenderName(Message item) {
        if (item == null)
            return;
        if (item.conversation.type == Conversation.ConversationType.Single) {
            nameTextView.setVisibility(View.GONE);
        } else if (item.conversation.type == Conversation.ConversationType.Group) {
            showGroupMemberAlias(message.message.conversation, message.message, message.message.sender);
        } else {
            // todo
        }
    }

    private void showGroupMemberAlias(Conversation conversation, Message message, String sender) {
        UserViewModel userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        if (!"1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, conversation.target)) || message.direction == MessageDirection.Send) {
            nameTextView.setVisibility(View.GONE);
            return;
        }
        nameTextView.setVisibility(View.VISIBLE);
        // TODO optimize 缓存userInfo吧
//        if (Conversation.equals(nameTextView.getTag(), sender)) {
//            return;
//        }
        GroupViewModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);

        nameTextView.setText(groupViewModel.getGroupMemberDisplayName(conversation.target, sender));
        nameTextView.setTag(sender);
    }

    protected boolean showMessageReceipt(Message message) {
        ContentTag tag = message.content.getClass().getAnnotation(ContentTag.class);
        return (tag != null && (tag.flag() == PersistFlag.Persist_And_Count));
    }

    protected void setSendStatus(Message item) {
        if (item == null)
            return;
        MessageStatus sentStatus = item.status;
        if (item.direction == MessageDirection.Receive) {
            return;
        }
        if (sentStatus == MessageStatus.Sending) {
            progressBar.setVisibility(View.VISIBLE);
            errorLinearLayout.setVisibility(View.GONE);
            return;
        } else if (sentStatus == MessageStatus.Send_Failure) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.VISIBLE);
            return;
        } else if (sentStatus == MessageStatus.Sent) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.GONE);
        } else if (sentStatus == MessageStatus.Readed) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.GONE);
            return;
        }

        if (!ChatManager.Instance().isReceiptEnabled() || !ChatManager.Instance().isUserEnableReceipt() || !showMessageReceipt(message.message)) {
            return;
        }

        Map<String, Long> deliveries = ((ConversationMessageAdapter) adapter).getDeliveries();
        Map<String, Long> readEntries = ((ConversationMessageAdapter) adapter).getReadEntries();

        if (item.conversation.type == Conversation.ConversationType.Single) {
            singleReceiptImageView.setVisibility(View.VISIBLE);
            groupReceiptFrameLayout.setVisibility(View.GONE);
            Long readTimestamp = readEntries != null && !readEntries.isEmpty() ? readEntries.get(message.message.conversation.target) : null;
            Long deliverTimestamp = deliveries != null && !deliveries.isEmpty() ? deliveries.get(message.message.conversation.target) : null;


            if (readTimestamp != null && readTimestamp >= message.message.serverTime) {
                ImageViewCompat.setImageTintList(singleReceiptImageView, null);
                return;
            }
            if (deliverTimestamp != null && deliverTimestamp >= message.message.serverTime) {
                ImageViewCompat.setImageTintList(singleReceiptImageView, ColorStateList.valueOf(ContextCompat.getColor(fragment.getContext(), R.color.gray)));
            }
        } else if (item.conversation.type == Conversation.ConversationType.Group) {
            singleReceiptImageView.setVisibility(View.GONE);
            groupReceiptFrameLayout.setVisibility(View.VISIBLE);

            if (sentStatus == MessageStatus.Sent) {
                int deliveryCount = 0;
                if (deliveries != null) {
                    for (Map.Entry<String, Long> delivery : deliveries.entrySet()) {
                        if (delivery.getValue() >= item.serverTime) {
                            deliveryCount++;
                        }
                    }
                }
                int readCount = 0;
                if (readEntries != null) {
                    for (Map.Entry<String, Long> readEntry : readEntries.entrySet()) {
                        if (readEntry.getValue() >= item.serverTime) {
                            readCount++;
                        }
                    }
                }

                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(item.conversation.target, false);
                if (groupInfo == null) {
                    return;
                }
                deliveryProgressBar.setMax(groupInfo.memberCount - 1);
                deliveryProgressBar.setProgress(deliveryCount);
                readProgressBar.setMax(groupInfo.memberCount - 1);
                readProgressBar.setProgress(readCount);
            } else {
                groupReceiptFrameLayout.setVisibility(View.GONE);
            }
        }
    }

    public static boolean copyFile(String oldPathName, String newPathName) {
        try {
            File oldFile = new File(oldPathName);
            if (!oldFile.exists()) {
                Log.e("copyFile", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("copyFile", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("copyFile", "copyFile:  oldFile cannot read.");
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(oldPathName);
            FileOutputStream fileOutputStream = new FileOutputStream(newPathName);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("@@@          保存成功");
            return true;
        } catch (Exception e) {
            System.out.println("@@@          保存失败："+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取文件名及后缀
     */
    public String getFileNameWithSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        int start = path.lastIndexOf("/");
        if (start != -1) {
            return path.substring(start + 1);
        } else {
            return "";
        }
    }


    public void saveBitmap(Bitmap bitmap) {
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
            Uri saveUri = fragment.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (TextUtils.isEmpty(saveUri.toString())) {
                Toast.makeText(fragment.getActivity(), "保存失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            OutputStream outputStream = fragment.getActivity().getContentResolver().openOutputStream(saveUri);
            //将位图写出到指定的位置
            //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
            //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
            //第三个参数：具体的输出流
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                Toast.makeText(fragment.getActivity(), "保存成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(fragment.getActivity(), "保存失败！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Bitmap bitmap;
    private ProgresDialog progresDialog;
    public Bitmap returnBitMap(final String url){

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL imageurl = null;
                try {
                    imageurl = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection conn = (HttpURLConnection)imageurl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return bitmap;
    }
}

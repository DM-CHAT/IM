/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.CardMessageContent.CardType_Channel;
import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;
import static cn.wildfirechat.model.Conversation.ConversationType.Group;

public class UserCardExt extends ConversationExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuItem
    public void pickContact(View containerView, Conversation conversation) {
        Message msg = new Message();
        /*if(conversation.type == Group){
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target,false);
            msg.content = new CardMessageContent(
                    CardType_Group,
                    groupInfo.target,
                    "",
                    groupInfo.name,
                    groupInfo.portrait);
            msg.conversation = conversation;
            msg.sender = ChatManager.Instance().getUserId();
            msg.direction = MessageDirection.Send;
            msg.status = MessageStatus.Sending;
            msg.serverTime = System.currentTimeMillis();
        }else{
            UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target,false);
            msg.content = new CardMessageContent(
                    CardType_User,
                    userInfo.uid,
                    "",
                    userInfo.displayName,
                    userInfo.portrait);
            msg.conversation = conversation;
            msg.sender = ChatManager.Instance().getUserId();
            msg.direction = MessageDirection.Send;
            msg.status = MessageStatus.Sending;
            msg.serverTime = System.currentTimeMillis();
        }

        Intent intent = new Intent(fragment.getActivity(), ForwardActivity.class);
        intent.putExtra("message", msg);
        startActivity(intent);*/
        if(conversation.type == Group) {
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target,false);
            int timeInterval = groupInfo.getTimeInterval();
            String time1 = groupInfo.timeInterval;
            long timeNow = System.currentTimeMillis();
            long timeLast = 0;
            if(time1 != null){
                timeLast = Long.valueOf(time1);
            }

            if (timeLast + (long)timeInterval * 1000 > timeNow) {
                Toast.makeText(activity, R.string.input_speed_too_fast, Toast.LENGTH_SHORT).show();
                return;
            }

        }

        Intent intent = new Intent(fragment.getActivity(), ContactListActivity.class);
        if (conversation.type == Conversation.ConversationType.Single || conversation.type == Conversation.ConversationType.Group) {
            ArrayList<String> filterUserList = new ArrayList<>();
            filterUserList.add(conversation.target);
            intent.putExtra("pick",true);
            intent.putExtra("share",true);
            intent.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);
        }
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            GroupInfo groupInfo =  data.getParcelableExtra("groupInfo");
            LitappInfo litappInfo = data.getParcelableExtra("litappInfo");
            if (userInfo != null) {
                sendUserCard(new CardMessageContent(CardType_User, userInfo.uid, userInfo.name, userInfo.displayName, userInfo.portrait));
            } else if (channelInfo != null) {
                sendUserCard(new CardMessageContent(CardType_Channel, channelInfo.channelId, channelInfo.name, channelInfo.name, channelInfo.portrait));
            } else if(groupInfo != null){
                sendUserCard(new CardMessageContent(CardType_Group, groupInfo.target, groupInfo.name, groupInfo.name, groupInfo.portrait));
            } else if(litappInfo != null){
                sendUserCard(new CardMessageContent(CardType_Litapp, litappInfo.target, litappInfo.name, litappInfo.displayName, litappInfo.portrait, litappInfo.theme, litappInfo.url));
            }
        }
    }

    private void sendUserCard(CardMessageContent cardMessageContent) {

        ForwardPromptView view = new ForwardPromptView(fragment.getActivity());
        String desc = "";
        switch (cardMessageContent.getType()) {
            case 0:
                desc = WfcUIKit.getString(R.string.card_user);
                break;
            case 1:
                desc = WfcUIKit.getString(R.string.card_group);
                break;
            case 2:
                desc = WfcUIKit.getString(R.string.card_room);
                break;
            case 3:
                desc = WfcUIKit.getString(R.string.card_channel);
                break;
            case 4:
                desc = WfcUIKit.getString(R.string.card_litapp);
                break;
            default:
                break;
        }

        desc += cardMessageContent.getDisplayName();

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo targetUser = ChatManager.Instance().getUserInfo(conversation.target, false);
            view.bind(targetUser.displayName, targetUser.portrait, desc);
        } else if (conversation.type == Conversation.ConversationType.Group) {
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target, false);
            view.bind(groupInfo.name, groupInfo.portrait, desc);
        //    view.bind(cardMessageContent.getDisplayName(), cardMessageContent.getPortrait(), desc);
        }
        MaterialDialog dialog = new MaterialDialog.Builder(fragment.getActivity())
            .customView(view, false)
            .negativeText(WfcUIKit.getString(R.string.cancel))
            .positiveText(WfcUIKit.getString(R.string.send))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    //CardMessageContent cardMessageContent = new CardMessageContent(type, target, name, displayName, portrait);
                    //cardMessageContent.setName(name);
                    messageViewModel.sendMessage(conversation, cardMessageContent);
                    if (!TextUtils.isEmpty(view.getEditText())) {
                        TextMessageContent content = new TextMessageContent(view.getEditText());
                        messageViewModel.sendMessage(conversation, content);
                    }
                    dialog.dismiss();
                }
            })
            .build();
        dialog.show();
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_user_card;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.card);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }

    @Override
    public boolean filter(Conversation conversation) {

        GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target,false);
        if (groupInfo.AllowAddFriend()) {
            return false;
        } else {
            return true;
        }
    }
}

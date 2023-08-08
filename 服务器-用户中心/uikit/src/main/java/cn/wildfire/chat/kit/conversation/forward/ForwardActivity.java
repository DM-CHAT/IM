/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.forward;

import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.conversation.pick.PickOrCreateConversationActivity;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.WfcTextUtils;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class ForwardActivity extends PickOrCreateConversationActivity {
    private List<Message> messages;
    private ForwardViewModel forwardViewModel;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;

    @Override
    protected void afterViews() {
        super.afterViews();
        System.out.println("@@@@@ forward activity begin.");
        messages = getIntent().getParcelableArrayListExtra("messages");
        if (messages == null || messages.isEmpty()) {
            Message message = getIntent().getParcelableExtra("message");
            if (message != null) {
                messages = new ArrayList<>();
                System.out.println("@@@@@ conversation id:" + message.conversation.target);
                messages.add(message);
            }
        }
        if (messages == null || messages.isEmpty()) {
            finish();
        }



        forwardViewModel = ViewModelProviders.of(this).get(ForwardViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
    }

    @Override
    protected void onPickOrCreateConversation(Conversation conversation) {
        forward(conversation);
    }

    public void forward(Conversation conversation) {
        switch (conversation.type) {
            case Single:
                UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
                forward(userInfo.displayName, userInfo.portrait, conversation);
                break;
            case Group:
                GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
                forward(groupInfo.name, groupInfo.portrait, conversation);
                break;
            default:
                break;
        }

    }

    private void forward(String targetName, String targetPortrait, Conversation targetConversation) {
        ForwardPromptView view = new ForwardPromptView(this);
        if (messages.size() == 1) {
            Message message = messages.get(0);
            if (message.content instanceof ImageMessageContent) {
                view.bind(targetName, targetPortrait, ((ImageMessageContent) message.content).getThumbnail());
            } else if (message.content instanceof VideoMessageContent) {
                view.bind(targetName, targetPortrait, ((VideoMessageContent) message.content).getThumbnail());
            } else if (message.content instanceof CompositeMessageContent) {
                view.bind(targetName, targetPortrait, getString(R.string.message_record) + ((CompositeMessageContent) message.content).getTitle());
            } else {
                view.bind(targetName, targetPortrait, WfcTextUtils.htmlToText(message.digest(WfcUIKit.getActivity())));
            }
        } else {
            view.bind(targetName, targetPortrait, getString(R.string.step_forward) + messages.size() + getString(R.string.no_message));
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .customView(view, false)
            .negativeText(getString(R.string.cancel))
            .positiveText(getString(R.string.send))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Message extraMsg = null;
                    if (!TextUtils.isEmpty(view.getEditText())) {
                        TextMessageContent content = new TextMessageContent(view.getEditText());
                        extraMsg = new Message();
                        extraMsg.content = content;
                    }
                    if (extraMsg != null) {
                        extraMsg.messageHash0=null;
                        extraMsg.messageHash=null;
                        messages.add(extraMsg);
                    }


                    List<Message> sendList = new ArrayList<>();
                    for (Message msg : messages){
                        Message newMsg = new Message(msg);
                        newMsg.transferForward();
                        sendList.add(newMsg);
                    }
                    ProgresDialog progresDialog = new ProgresDialog(ForwardActivity.this);
                    progresDialog.show();



                        forwardViewModel.forward(ForwardActivity.this, targetConversation, sendList.toArray(new Message[0]))
                                .observe(ForwardActivity.this, new Observer<OperateResult<Integer>>() {
                                    @Override
                                    public void onChanged(@Nullable OperateResult<Integer> integerOperateResult) {
                                        progresDialog.dismiss();
                                        if (integerOperateResult.isSuccess()) {
                                            Toast.makeText(ForwardActivity.this, getString(R.string.forward_success), Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(ForwardActivity.this, getString(R.string.forward_failure) + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });





                }
            })
            .build();
        dialog.show();
    }
}

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact;

import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.model.ContactCountFooterValue;
import cn.wildfire.chat.kit.contact.model.FriendRequestValue;
import cn.wildfire.chat.kit.contact.model.GroupValue;
import cn.wildfire.chat.kit.contact.model.HeaderValue;
import cn.wildfire.chat.kit.contact.model.LitappValue;
import cn.wildfire.chat.kit.contact.model.NotifyValue;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.chat.kit.contact.viewholder.footer.ContactCountViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.FriendRequestViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.GroupViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.KefuViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.LitappViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.NotifyViewHolder;
import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfire.chat.kit.conversation.forward.ForwardViewModel;
import cn.wildfire.chat.kit.group.GroupListActivity;
import cn.wildfire.chat.kit.kefu.KefuListActivity;
import cn.wildfire.chat.kit.litapp.LitappListActivity;
import cn.wildfire.chat.kit.litapp.LitappListActivity1;
import cn.wildfire.chat.kit.notifylist.NotifyListActivity;
import cn.wildfire.chat.kit.search.SearchPortalActivity;
import cn.wildfire.chat.kit.tag.TagActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.utils.WfcTextUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfire.chat.kit.widget.QuickIndexBar;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class ContactListFragment2 extends BaseUserListFragment2 implements QuickIndexBar.OnLetterUpdateListener {
    private boolean pick = false;
    private boolean share = false;
    private List<String> filterUserList;
    private static final int REQUEST_CODE_PICK = 100;
    private static final int REQUEST_FORWARD = 200;
    private MessageViewModel messageViewModel;
    private UserInfo userInfo;
    private LitappInfo litappInfo;
    private GroupInfo groupInfo;
    private boolean forward = false;
    private ArrayList<Message> messages;
    private ForwardViewModel forwardViewModel;
    private RelativeLayout rl_search;
    private LinearLayout ll_contact_title;
    @BindView(R2.id.unreadFriendRequestCountTextView)
    ImageView unreadFriendRequestCountTextView;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (userListAdapter != null && isVisibleToUser) {
            contactViewModel.reloadContact();
            contactViewModel.reloadFriendRequestStatus();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            userInfo = bundle.getParcelable("userinfo");
            litappInfo = bundle.getParcelable("litappInfo");
            groupInfo = bundle.getParcelable("groupInfo");
            pick = bundle.getBoolean("pick", false);
            share = bundle.getBoolean("share",false);
            filterUserList = bundle.getStringArrayList("filterUserList");
            forward = bundle.getBoolean("forward",false);
            if (forward){
                messages = bundle.getParcelableArrayList("messages");
                if (messages == null || messages.isEmpty()) {
                    Message message = bundle.getParcelable("message");
                    if (message != null) {
                        messages = new ArrayList<>();
                        messages.add(message);
                    }
                }
                forwardViewModel = ViewModelProviders.of(this).get(ForwardViewModel.class);
            }


        }

        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        contactViewModel.reloadContact();
        contactViewModel.reloadFriendRequestStatus();
        contactViewModel.reloadFavContact();
        /*ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                ChatManager.Instance().syncFriend();
            }
        });*/
        int friendMessage = ChatManager.Instance().getUnreadFriendRequestStatus();
        if( friendMessage != 0){
            unreadFriendRequestCountTextView.setVisibility(View.VISIBLE);
        }else{
            unreadFriendRequestCountTextView.setVisibility(View.GONE);
        }
    }


    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            if (filterUserList != null) {
                userInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            userListAdapter.setUsers(userInfos);
        });
        contactViewModel.friendRequestUpdatedLiveData().observe(getActivity(), integer -> userListAdapter.updateHeader(0, new FriendRequestValue(integer)));
        contactViewModel.favContactListLiveData().observe(getActivity(), uiUserInfos -> {
            if (filterUserList != null) {
                uiUserInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            userListAdapter.setFavUsers(uiUserInfos);
        });

        rl_search = view.findViewById(R.id.rl_search);
        rl_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchPortal();
            }
        });

        ll_contact_title = view.findViewById(R.id.ll_contact_title);


    }

    private void showSearchPortal() {
        Intent intent = new Intent(getActivity(), SearchPortalActivity.class);
        startActivity(intent);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void initHeaderViewHolders() {
        if (pick || forward) {
            addHeaderViewHolder(FriendRequestViewHolder.class, R.layout.contact_header_friend, new FriendRequestValue(contactViewModel.getUnreadFriendRequestCount()));
            addHeaderViewHolder(GroupViewHolder.class, R.layout.contact_header_group, new GroupValue());
            addHeaderViewHolder(LitappViewHolder.class, R.layout.contact_header_litapp, new NotifyValue());
        }
        //addHeaderViewHolder(ChannelViewHolder.class, R.layout.contact_header_channel, new HeaderValue());
        addHeaderViewHolder(NotifyViewHolder.class, R.layout.contact_header_notify, new LitappValue());;
        addHeaderViewHolder(KefuViewHolder.class, R.layout.contact_header_kefu, new HeaderValue());
    }

    @Override
    public void initFooterViewHolders() {
        addFooterViewHolder(ContactCountViewHolder.class, R.layout.contact_item_footer, new ContactCountFooterValue());
    }

    @Override
    public void onUserClick(UIUserInfo uiUserInfo) {
        UserInfo targetUserInfo = uiUserInfo.getUserInfo();
        if (pick) {
            if (userInfo != null){
                Conversation conversation = new Conversation(Conversation.ConversationType.Single,targetUserInfo.uid);
                sendUserCard(conversation ,new CardMessageContent(CardType_User, userInfo.uid, userInfo.name, userInfo.displayName, userInfo.portrait));
                return;
            }
            if (litappInfo != null){
                Conversation conversation = new Conversation(Conversation.ConversationType.Single,targetUserInfo.uid);
                sendUserCard(conversation,new CardMessageContent(CardType_Litapp, litappInfo.target, litappInfo.name, litappInfo.displayName, litappInfo.portrait, litappInfo.theme, litappInfo.url));
                return;
            }
            if(groupInfo != null){
                Conversation conversation = new Conversation(Conversation.ConversationType.Group,targetUserInfo.uid);
                sendUserCard(conversation,new CardMessageContent(CardType_Group,groupInfo.target,groupInfo.name,"",groupInfo.portrait));
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("userInfo", uiUserInfo.getUserInfo());
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else if(forward){
            Conversation conversation = new Conversation(Conversation.ConversationType.Single,targetUserInfo.uid);
            forward(targetUserInfo.displayName,targetUserInfo.portrait,conversation);
        } else {
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            intent.putExtra("userInfo", targetUserInfo);
            startActivity(intent);
        }
    }

    private void sendUserCard(Conversation conversation, CardMessageContent cardMessageContent) {
        ForwardPromptView view = new ForwardPromptView(getActivity());
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
      //      view.bind(cardMessageContent.getName(), cardMessageContent.getPortrait(), desc);
        }
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
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
                        getActivity().finish();
                    }
                })
                .build();
        dialog.show();
    }

    private void forward(String targetName, String targetPortrait, Conversation targetConversation) {
        ForwardPromptView view = new ForwardPromptView(getActivity());
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
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(view, false)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.send))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Message extraMsg = null;
                        if (!TextUtils.isEmpty(view.getEditText())) {
                            TextMessageContent content = new TextMessageContent(view.getEditText());
                            extraMsg = new Message();
                            extraMsg.content = content;
                        }
                        if (extraMsg != null) {
                            messages.add(extraMsg);
                        }
                        forwardViewModel.forward(targetConversation, messages.toArray(new Message[0]))
                                .observe(getActivity(), new Observer<OperateResult<Integer>>() {
                                    @Override
                                    public void onChanged(@Nullable OperateResult<Integer> integerOperateResult) {
                                        if (integerOperateResult.isSuccess()) {
                                            Toast.makeText(getActivity(), getString(R.string.forward_success), Toast.LENGTH_SHORT).show();
                                            getActivity().finish();
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.forward_failure) + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                })
                .build();
        dialog.show();
    }

    @Override
    public void onHeaderClick(int index) {
        if (pick || forward) {
            switch (index) {
                case 0:
                    showFriendRequest();
                    break;
                case 1:
                    showGroupList();
                    break;
                case 2:
                    showLitappList();
                    break;
                case 3:
                    showNotify();
                    break;
                case 4:
                    showKefuList();
                    break;
                default:
                    break;
            }
        }else {
            switch (index) {
                case 0:
                    showNotify();
                    break;
                case 1:
                    showKefuList();
                    break;
                default:
                    break;
            }
        }
    }
    @OnClick(R2.id.btn_new_friends)
    void showFriendRequest() {
        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
        startActivity(intent);
    }
    @OnClick(R2.id.btn_groups)
    void showGroupList() {
        Intent intent = new Intent(getActivity(), GroupListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }
    @OnClick(R2.id.btn_litapp)
    void showLitappList() {
        /*Intent intent = new Intent(getActivity(), LitappListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }*/
        Intent intent = new Intent(getActivity(), LitappListActivity1.class);
        startActivity(intent);

    }
    @OnClick(R2.id.btn_tag)
    void showTag(){
        Intent intent = new Intent(getActivity(), TagActivity.class);
        startActivity(intent);
    }
    /*private void showFriendRequest() {
        FriendRequestValue value = new FriendRequestValue(0);
        userListAdapter.updateHeader(0, value);

        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
        startActivity(intent);
    }

    private void showGroupList() {
        Intent intent = new Intent(getActivity(), GroupListActivity.class);
        if (forward){
            intent.putExtra("forward", true);
            if (messages.size() > 1){
                intent.putExtra("messages", messages);
            }else{
                intent.putExtra("message",messages.get(0));
            }
            startActivityForResult(intent, REQUEST_FORWARD);
        }else{
            intent.putExtra("pick", pick);
            if (userInfo == null) {
                startActivityForResult(intent, REQUEST_CODE_PICK);
            } else {
                intent.putExtra("userinfo", userInfo);
                startActivity(intent);
            }
        }
    }

    private void showLitappList() {
        Intent intent = new Intent(getActivity(), LitappListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }*/
    private void showKefuList(){
        Intent intent = new Intent(getActivity(), KefuListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }
    private void showNotify() {
        Intent intent = new Intent(getActivity(), NotifyListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_PICK && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            GroupInfo groupInfo = data.getParcelableExtra("groupInfo");
            LitappInfo litappInfo = data.getParcelableExtra("litappInfo");
            if(groupInfo != null)
                intent.putExtra("groupInfo", groupInfo);
            else if(userInfo != null)
                intent.putExtra("userInfo", userInfo);
            else if(litappInfo != null)
                intent.putExtra("litappInfo",litappInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else if(requestCode == REQUEST_FORWARD && resultCode == Activity.RESULT_OK){
            getActivity().finish();
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void refreshData() {
        if(userListAdapter != null) {
            userListAdapter.notifyDataSetChanged();
        }
        try {
            int friendMessage = ChatManager.Instance().getUnreadFriendRequestStatus();
            if( friendMessage != 0){
                unreadFriendRequestCountTextView.setVisibility(View.VISIBLE);
            }else{
                unreadFriendRequestCountTextView.setVisibility(View.GONE);
            }
        }catch (Exception e){

        }


    }
}

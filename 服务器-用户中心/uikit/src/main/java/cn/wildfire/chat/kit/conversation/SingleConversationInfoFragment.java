/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.contact.pick.PickConversationTargetActivity;
import cn.wildfire.chat.kit.conversation.file.FileRecordActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.chat.kit.search.SearchMessageActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.utils.SelfDialog;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class SingleConversationInfoFragment extends Fragment implements ConversationMemberAdapter.OnMemberClickListener, CompoundButton.OnCheckedChangeListener {

    // common
    @BindView(R2.id.memberRecyclerView)
    RecyclerView memberReclerView;
    @BindView(R2.id.stickTopSwitchButton)
    SwitchButton stickTopSwitchButton;
    @BindView(R2.id.silentSwitchButton)
    SwitchButton silentSwitchButton;

    private ConversationInfo conversationInfo;
    private ConversationMemberAdapter conversationMemberAdapter;
    private ConversationViewModel conversationViewModel;
    private UserViewModel userViewModel;
    private SelfDialog selfDialog;
    private OptionItemView share_cardInfo;
    private View view;
    private UserInfo mUserInfo;


    public static SingleConversationInfoFragment newInstance(ConversationInfo conversationInfo) {
        SingleConversationInfoFragment fragment = new SingleConversationInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("conversationInfo", conversationInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        conversationInfo = args.getParcelable("conversationInfo");
        assert conversationInfo != null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.conversation_info_single_fragment, container, false);
        ButterKnife.bind(this, view);
        initView();
        init();
        return view;
    }

    private void initView(){
        share_cardInfo = view.findViewById(R.id.share_cardInfo);
        share_cardInfo.setOnClickListener(view -> {


            /*Intent intent = new Intent(getActivity(), ContactListActivity.class);
            intent.putExtra("userinfo", mUserInfo);
            intent.putExtra("pick",true);
            startActivity(intent);*/



            Message msg = new Message();
            msg.content = new CardMessageContent(
                    CardType_User,
                    mUserInfo.uid,
                    mUserInfo.displayName,
                    mUserInfo.displayName,
                    mUserInfo.portrait);
            msg.conversation = this.conversationInfo.conversation;
            msg.sender = ChatManager.Instance().getUserId();
            msg.direction = MessageDirection.Send;
            msg.status = MessageStatus.Sending;
            msg.serverTime = System.currentTimeMillis();

            Intent intent = new Intent(getActivity(), ForwardActivity.class);
            intent.putExtra("message", msg);
            startActivity(intent);





        });
    }

    private void init() {
        conversationViewModel = WfcUIKit.getAppScopeViewModel(ConversationViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        String userId = conversationInfo.conversation.target;
        conversationMemberAdapter = new ConversationMemberAdapter(conversationInfo, true, false);
        List<UserInfo> members = Collections.singletonList(userViewModel.getUserInfo(userId, true));
        conversationMemberAdapter.setMembers(members);
        conversationMemberAdapter.setOnMemberClickListener(this);

        memberReclerView.setAdapter(conversationMemberAdapter);
        memberReclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        stickTopSwitchButton.setChecked(conversationInfo.isTop);
        silentSwitchButton.setChecked(conversationInfo.isSilent);
        stickTopSwitchButton.setOnCheckedChangeListener(this);
        silentSwitchButton.setOnCheckedChangeListener(this);

        observerUserInfoUpdate();
    }

    private void observerUserInfoUpdate() {
        userViewModel.userInfoLiveData().observe(getActivity(), userInfos -> {
            for (UserInfo userInfo : userInfos) {
                if (userInfo.uid.equals(this.conversationInfo.conversation.target)) {
                    mUserInfo = userInfo;
                    List<UserInfo> members = Collections.singletonList(userInfo);
                    conversationMemberAdapter.setMembers(members);
                    conversationMemberAdapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @OnClick(R2.id.clearMessagesOptionItemView)
    void clearMessage() {
        selfDialog = new SelfDialog(getActivity());
        selfDialog.setTitle(getString(R.string.clear_chat_log));
        selfDialog.setMessage(getString(R.string.clean_chat_whether));
        selfDialog.setYesOnclickListener(getString(R.string.string_confirm), new SelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                selfDialog.dismiss();
                conversationViewModel.clearConversationMessage(conversationInfo.conversation);
            }
        });
        selfDialog.setNoOnclickListener(getString(R.string.string_cancel), new SelfDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                selfDialog.dismiss();
            }
        });
        selfDialog.show();

    }

    @OnClick(R2.id.searchMessageOptionItemView)
    void searchGroupMessage() {
        Intent intent = new Intent(getActivity(), SearchMessageActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        startActivity(intent);
    }

    @OnClick(R2.id.fileRecordOptionItemView)
    void fileRecord(){
        Intent intent = new Intent(getActivity(), FileRecordActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        startActivity(intent);
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    @Override
    public void onAddMemberClick() {
        Intent intent = new Intent(getActivity(), CreateConversationActivity.class);
        ArrayList<String> participants = new ArrayList<>();
        participants.add(conversationInfo.conversation.target);
        intent.putExtra(PickConversationTargetActivity.CURRENT_PARTICIPANTS, participants);
        startActivity(intent);
    }

    @Override
    public void onRemoveMemberClick() {
        // do nothing
    }

    private void stickTop(boolean top) {
        ConversationListViewModel conversationListViewModel = ViewModelProviders
            .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Arrays.asList(0)))
            .get(ConversationListViewModel.class);
        conversationListViewModel.setConversationTop(conversationInfo, top);
    }

    private void silent(boolean silent) {
        conversationViewModel.setConversationSilent(conversationInfo.conversation, silent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.stickTopSwitchButton) {
            stickTop(isChecked);
        } else if (id == R.id.silentSwitchButton) {
            silent(isChecked);
        }

    }
}

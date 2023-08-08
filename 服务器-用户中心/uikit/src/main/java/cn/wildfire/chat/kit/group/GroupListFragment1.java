/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetGroupsCallback;

public class GroupListFragment1 extends Fragment implements OnGroupItemClickListener {
    @BindView(R2.id.groupRecyclerView)
    RecyclerView recyclerView;
    @BindView(R2.id.tipTextView)
    TextView tipTextView;
    @BindView(R2.id.groupsLinearLayout)
    LinearLayout groupsLinearLayout;

    private GroupListAdapter groupListAdapter;
    private OnGroupItemClickListener onGroupItemClickListener;
    private boolean pick;
    private UserInfo userInfo;
    private MessageViewModel messageViewModel;

    public void setOnGroupItemClickListener(OnGroupItemClickListener onGroupItemClickListener) {
        this.onGroupItemClickListener = onGroupItemClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            pick = args.getBoolean("pick", false);
            userInfo = args.getParcelable("userinfo");
            messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_list_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        groupListAdapter = new GroupListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(groupListAdapter);
        groupListAdapter.setOnGroupItemClickListener(this);

        ChatManager.Instance().getFavGroups(new GetGroupsCallback() {
            @Override
            public void onSuccess(List<GroupInfo> groupInfos) {
                if (groupInfos == null || groupInfos.isEmpty()) {
                    groupsLinearLayout.setVisibility(View.GONE);
                    tipTextView.setVisibility(View.VISIBLE);
                    return;
                }
                groupListAdapter.setGroupInfos(groupInfos);
                groupListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int errorCode) {
                groupsLinearLayout.setVisibility(View.GONE);
                tipTextView.setVisibility(View.VISIBLE);
                tipTextView.setText("error: " + errorCode);
            }
        });
    }

    @Override
    public void onGroupClick(GroupInfo groupInfo) {
        if(pick){
            if (userInfo != null){
                Conversation conversation = new Conversation(Conversation.ConversationType.Group,groupInfo.target);
                sendUserCard(conversation ,new CardMessageContent(CardType_User, userInfo.uid, userInfo.name, userInfo.displayName, userInfo.portrait));
                return;
            }
            if (groupInfo != null){
                Conversation conversation = new Conversation(Conversation.ConversationType.Group,groupInfo.target);
                sendUserCard(conversation ,new CardMessageContent(CardType_Group, groupInfo.target, groupInfo.name, "", groupInfo.portrait));
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("groupInfo", groupInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }else {
            if (onGroupItemClickListener != null) {
                onGroupItemClickListener.onGroupClick(groupInfo);
                return;
            }
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
            Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupInfo.target);
            intent.putExtra("conversation", conversation);
            startActivity(intent);
            getActivity().finish();
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
                    }
                })
                .build();
        dialog.show();
    }
}

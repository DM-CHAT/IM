/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class ConversationMemberAdapter extends RecyclerView.Adapter<ConversationMemberAdapter.MemberViewHolder> {
    private List<UserInfo> members;
    private ConversationInfo conversationInfo;
    private boolean enableAddMember;
    private boolean enableRemoveMember;
    private OnMemberClickListener onMemberClickListener;

    public ConversationMemberAdapter(ConversationInfo conversationInfo, boolean enableAddMember, boolean enableRemoveMember) {
        this.conversationInfo = conversationInfo;
        this.enableAddMember = enableAddMember;
        this.enableRemoveMember = enableRemoveMember;
    }

    public void setMembers(List<UserInfo> members) {
        this.members = members;
    }


    public void addMembers(List<UserInfo> members) {
        int startIndex = this.members.size();
        this.members.addAll(members);
        notifyItemRangeInserted(startIndex, members.size());
    }

    public void updateMember(UserInfo userInfo) {
        if (this.members == null) {
            return;
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).uid.equals(userInfo.uid)) {
                members.set(i, userInfo);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeMembers(List<String> memberIds) {
        Iterator<UserInfo> iterator = members.iterator();
        while (iterator.hasNext()) {
            UserInfo userInfo = iterator.next();
            if (memberIds.contains(userInfo.uid)) {
                iterator.remove();
                memberIds.remove(userInfo.uid);
            }

            if (memberIds.size() == 0) {
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.conversation_item_member_info, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        if (position < members.size()) {
            holder.bindUserInfo(members.get(position));
            holder.bindNft(members.get(position));
        } else {
            if (position == members.size()) {
                if (enableAddMember) {
                    holder.bindAddMember();
                } else if (enableRemoveMember) {
                    holder.bindRemoveMember();
                }
            } else if (position == members.size() + 1 && enableRemoveMember) {
                holder.bindRemoveMember();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (members == null) {
            return 0;
        }
        int count = members.size();
        if (enableAddMember) {
            count++;
        }
        if (enableRemoveMember) {
            count++;
        }
        return count;
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.portraitImageView)
        ImageView portraitImageView;
        @BindView(R2.id.nameTextView)
        TextView nameTextView;
        @BindView(R2.id.nftFlag)
        ImageView nftImageView;
        private UserInfo userInfo;
        private int type = TYPE_USER;
        private static final int TYPE_USER = 0;
        private static final int TYPE_ADD = 1;
        private static final int TYPE_REMOVE = 2;

        @OnClick(R2.id.portraitImageView)
        void onClick() {
            if (onMemberClickListener == null) {
                return;
            }
            switch (type) {
                case TYPE_USER:
                    if (userInfo != null) {
                        onMemberClickListener.onUserMemberClick(userInfo);
                    }
                    break;
                case TYPE_ADD:
                    onMemberClickListener.onAddMemberClick();
                    break;
                case TYPE_REMOVE:
                    onMemberClickListener.onRemoveMemberClick();
                    break;
                default:
                    break;
            }
        }

        public MemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindUserInfo(UserInfo userInfo) {
            if (userInfo == null) {
                nameTextView.setText("");
                portraitImageView.setImageResource(R.mipmap.avatar_def);
                return;
            }
            this.userInfo = userInfo;
            this.type = TYPE_USER;
            nameTextView.setVisibility(View.VISIBLE);
            if (conversationInfo.conversation.type == Conversation.ConversationType.Group) {
                nameTextView.setText(ChatManager.Instance().getGroupMemberDisplayName(conversationInfo.conversation.target, userInfo.uid));
            } else {
                nameTextView.setText(ChatManager.Instance().getUserDisplayName(userInfo.uid));
            }
            Glide.with(portraitImageView).load(userInfo.portrait).apply(new RequestOptions().centerCrop().placeholder(R.mipmap.avatar_def)).into(portraitImageView);
        }

        public void bindAddMember() {

            /*if(conversationInfo.conversation.type == Conversation.ConversationType.Group){
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversationInfo.conversation.target,false);
                String joinTypeStr = groupInfo.getJoinType();
                int jt = getJoinType(joinTypeStr);
                if(jt == 0 || jt == 1){
                    portraitImageView.setVisibility(View.GONE);
                }

                List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupInfo.target);
                String myself = ChatManager.Instance().getUserId();
                GroupMember groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, myself);
                if (groupMember.type == GroupMember.GroupMemberType.Manager
                        || groupMember.type == GroupMember.GroupMemberType.Owner
                        || groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
                ) {
                    portraitImageView.setVisibility(View.VISIBLE);
                }
                for (GroupMember member : managers) {
                    if(member.memberId.equals(ChatManager.Instance().getUserId())){
                        portraitImageView.setVisibility(View.VISIBLE);
                    }
                }
            }*/
            nameTextView.setVisibility(View.GONE);
            portraitImageView.setImageResource(R.mipmap.ic_add_team_member);
            this.type = TYPE_ADD;

        }

        public void bindRemoveMember() {
            nameTextView.setVisibility(View.GONE);
            portraitImageView.setImageResource(R.mipmap.ic_remove_team_member);
            this.type = TYPE_REMOVE;
        }

        public void bindNft(UserInfo userInfo){
            if(userInfo == null){
                return;
            }
            if(userInfo.getNft() != null){
                nftImageView.setVisibility(View.VISIBLE);
            }else{
                nftImageView.setVisibility(View.GONE);
            }
        }
    }

    private int getJoinType(String type) {
        /**
         * 0  自由加群
         * 1  成员邀请
         * 2  管理员邀请
         * 3  申请加群
         * 4  密码加群
         * 5
         *
         * **/
        //String joinType = "free";
        switch (type) {
            case "free":
                return 0;
            case "member":
                return 1;
            case "admin":
                return 2;
            case "verify":
                return 3;
            case "password":
                return 4;
            case "none":
                return 5;
            default:
                break;
        }
        return 0;
    }

    public interface OnMemberClickListener {
        void onUserMemberClick(UserInfo userInfo);

        void onAddMemberClick();

        void onRemoveMemberClick();
    }
}

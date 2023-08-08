/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(CallMessageContent.class)
@EnableContextMenu
public class CallMessageViewHolder extends NormalMessageContentViewHolder {
    @BindView(R2.id.contentTextView)
    TextView textView;
    @BindView(R2.id.iv_audio)
    ImageView iv_audio;
    @BindView(R2.id.iv_video)
    ImageView iv_video;

    public CallMessageViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        CallMessageContent content = (CallMessageContent) message.message.content;
        if(content.type == 0){
            iv_video.setVisibility(View.VISIBLE);
        }else{
            iv_video.setVisibility(View.GONE);
        }
        if(content.type == 1){
            iv_audio.setVisibility(View.VISIBLE);
        }else{
            iv_audio.setVisibility(View.GONE);
        }
        if(content.status.equalsIgnoreCase("cancel")){
            textView.setText(fragment.getContext().getString(R.string.call_canceled));
        }else if(content.status.equalsIgnoreCase("reject")){
            textView.setText(fragment.getContext().getString(R.string.other_party_canceled));
        }else if(content.status.equalsIgnoreCase("refuse")){
            textView.setText(fragment.getContext().getString(R.string.call_canceled));
        }else if(content.status.equalsIgnoreCase("finish")){
            String text;
            long duration = content.duration / 1000;
            if (duration > 3600) {
                if(content.type == 0){
                    text = String.format(fragment.getContext().getString(R.string.video_call_duration)+" %d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
                }else{
                    text = String.format(fragment.getContext().getString(R.string.voice_call_duration)+" %d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
                }
            } else {
                if(content.type == 0){
                    text = String.format(fragment.getContext().getString(R.string.video_call_duration)+" %02d:%02d", duration / 60, (duration % 60));
                }else{
                    text = String.format(fragment.getContext().getString(R.string.voice_call_duration)+" %02d:%02d", duration / 60, (duration % 60));
                }

            }
            textView.setText(text);
        }else{
            textView.setText(fragment.getContext().getString(R.string.unknown_status));
        }
    }

    @OnClick(R2.id.contentTextView)
    public void call(View view) {
//        if (((CallStartMessageContent) message.message.content).getStatus() == 1) {
//            return;
//        }
//        CallStartMessageContent callStartMessageContent = (CallStartMessageContent) message.message.content;
//        if (message.message.conversation.type == Conversation.ConversationType.Single) {
//            WfcUIKit.singleCall(fragment.getContext(), message.message.conversation.target, callStartMessageContent.isAudioOnly());
//        } else {
//            fragment.pickGroupMemberToVoipChat(callStartMessageContent.isAudioOnly());
//        }
    }
}

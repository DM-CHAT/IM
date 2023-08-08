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
import android.view.View;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.redpacket.RedPacketActivity;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.model.Conversation;

public class RedPacketExt extends ConversationExt {
    @ExtContextMenuItem
    public void makePacket(View containerView, Conversation conversation) {
        Intent intent = new Intent(fragment.getActivity(), RedPacketActivity.class);
        intent.putExtra("type", "normal");
        intent.putExtra("conversation", conversation);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String id = data.getStringExtra("id");
            String info = data.getStringExtra("info");
            String text = data.getStringExtra("text");
            if(info != null){
                RedPacketMessageContent redPacketMessageContent = new RedPacketMessageContent(id, 0, text, info);
                messageViewModel.sendRedPacketMsg(conversation, redPacketMessageContent);
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_transfer;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.transfer);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }

    @Override
    public boolean filter(Conversation conversation) {
        /*if(conversation.type == Conversation.ConversationType.Single && conversation.enable2)
            return false;*/
        return true;
        //return super.filter(conversation);
    }
}

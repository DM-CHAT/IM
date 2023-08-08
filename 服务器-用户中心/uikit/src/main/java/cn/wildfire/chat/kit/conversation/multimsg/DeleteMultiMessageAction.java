/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.multimsg;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;

public class DeleteMultiMessageAction extends MultiMessageAction {

    @Override
    public void onClick(List<UiMessage> messages) {
        MessageViewModel messageViewModel = new ViewModelProvider(fragment).get(MessageViewModel.class);
        for (UiMessage message : messages) {
            messageViewModel.deleteMessage(message.message);
        }
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_delete2;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.delete);
    }

    @Override
    public boolean confirm() {
        return true;
    }

    @Override
    public String confirmPrompt() {
        return WfcUIKit.getString(R.string.is_delete);
    }
}

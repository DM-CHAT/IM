/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExtManager;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExtension;
import cn.wildfire.chat.kit.info.RedPacketBombInfo;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.redpacket.RedPacketActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedPacketBombExt extends ConversationExt {
    String TAG = RedPacketBombExt.class.getSimpleName();
    @ExtContextMenuItem
    public void makePacket(View containerView, Conversation conversation) {
        if(!conversation.target.startsWith("OSNG")){
            return;
        }
        GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target, false);
        if(groupInfo == null){
            return;
        }
        if(groupInfo.redPacket != 1){
            String url_GROUP_UPDATE_COUNT = (String) SPUtils.get(fragment.getActivity(),"GROUP_UPDATE_COUNT","");

            OKHttpHelper.postJson(url_GROUP_UPDATE_COUNT, "{}", new SimpleCallback<String>(){
                @Override
                public void onSuccess1(String t) {

                }

                @Override
                public void onUiSuccess(String s) {
                    Log.d(TAG, "group update count result: "+s);
                    JSONObject json = RedPacketUtils.getData(fragment.getContext(), s);
                    if(json == null)
                        return;
                    String upGroupSize = json.getString("upGroupSize");
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                    builder.setTitle(WfcUIKit.getString(R.string.hint))
                            .setMessage(String.format(WfcUIKit.getString(R.string.bomb_update_tip), upGroupSize))
                            .setPositiveButton(WfcUIKit.getString(R.string.confirm), (dialog, which) -> {
                            }).show();
                }
                @Override
                public void onUiFailure(int code, String msg) {
                    Toast.makeText(fragment.getContext(), "code: "+code+", msg: "+msg, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        Intent intent = new Intent(fragment.getActivity(), RedPacketActivity.class);
        intent.putExtra("type", "bomb");
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
        return R.mipmap.ic_red_packet;
    }

    @Override
    public String title(Context context) {
        return WfcUIKit.getString(R.string.redPacket_bomb);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }

    @Override
    public boolean filter(Conversation conversation) {

        if(conversation.type != Conversation.ConversationType.Group){
            return true;
        }
        {
            if(conversation.redPacketBomb){
                return false;
            }else{
                return true;
            }
        }
    //    return super.filter(conversation);
    }
}

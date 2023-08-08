package cn.wildfire.chat.kit.conversation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.third.location.ui.activity.WalletTransferWebViewActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;

public class DappTransferExt1 extends ConversationExt {

    private MessageReminderAdapter messageReminderAdapter;

    @ExtContextMenuItem
    public void makeDappTransfer(View containerView, Conversation conversation) {
        /*Intent intent = new Intent(fragment.getActivity(), WalletsActivity.class);
        intent.putExtra("conversation", conversation);
        startActivity(intent);*/
        /*String wallets = (String) SPUtils.get(fragment.getActivity(),"Wallet","");
        System.out.println("@@@   wallets="+wallets);*/
        {
            String Wallet = (String) SPUtils.get(fragment.getActivity(), "Wallet", "");
            if (!(Wallet.length() < 5)) {
                try {
                    ChatManager.Instance().insertWallets(Wallet);
                    SPUtils.remove(fragment.getActivity(), "Wallet");
                } catch (Exception e) {

                }
            }
        }

        List<WalletsInfo> list2 = ChatManager.Instance().getWalletsInfo();
        System.out.println("@@@   list size : " + list2.size());
        
        View view = View.inflate(fragment.getActivity(), R.layout.dialog_wallets, null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getActivity(),RecyclerView.VERTICAL,false));
        messageReminderAdapter = new MessageReminderAdapter(R.layout.adapter_wallets,list2);
        recyclerView.setAdapter(messageReminderAdapter);
        messageReminderAdapter.notifyDataSetChanged();

        /*LinearLayoutManager layoutManager = new LinearLayoutManager(fragment.getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);*/

        /*DappTransferAdapter dappTransferAdapter = new DappTransferAdapter(fragment.getActivity(),list2);
        recyclerView.setAdapter(dappTransferAdapter);
        dappTransferAdapter.notifyDataSetChanged();*/
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setView(view);
        builder.setCancelable(true);
        Dialog dialog = builder.show();
        dialog.show();

        messageReminderAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {

                dialog.dismiss();
                String wallets = list2.get(position).wallets;
                JSONObject jsonObject = JSONObject.parseObject(wallets);
                String param = jsonObject.getString("param");
                JSONObject jsonObject1 = JSONObject.parseObject(param);
                String name = jsonObject1.getString("name");
                String url = jsonObject1.getString("transactionUrl");
                String payTo = conversation.target;
                String walletId = jsonObject.getString("target");

                Intent intent = new Intent(fragment.getActivity(), WalletTransferWebViewActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("payTo",payTo);
                intent.putExtra("walletId",walletId);
                intent.putExtra("conversation", conversation);
                startActivityForResult(intent, 100);

            }
        });


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
        return WfcUIKit.getString(R.string.redPacket);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String info = data.getStringExtra("info");
            String id = data.getStringExtra("id");
            String text = data.getStringExtra("text");
            System.out.println("@@@    info2 : " + info);
            if(info != null){
                RedPacketMessageContent redPacketMessageContent = new RedPacketMessageContent(id, 0, text, info);
                messageViewModel.sendRedPacketMsg(conversation, redPacketMessageContent);
            }
        }
    }

    @Override
    public boolean filter(Conversation conversation) {
        /*if (conversation.enable3) {
            if(conversation.type == Conversation.ConversationType.Group){
                return false;
            }
        }*/

        List<WalletsInfo> list = ChatManager.Instance().getWalletsInfo();
        if(list.size() > 0){
            if(conversation.type == Conversation.ConversationType.Group){
                return false;
            }
        }

        return true;
    }
}

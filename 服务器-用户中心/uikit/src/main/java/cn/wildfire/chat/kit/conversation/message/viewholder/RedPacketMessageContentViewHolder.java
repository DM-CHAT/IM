/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.TranferResultActivity;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.redpacket.RedPacketInfoActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.model.UnpackInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

@MessageContentType(value = {
        RedPacketMessageContent.class,

})
@EnableContextMenu
public class RedPacketMessageContentViewHolder extends NormalMessageContentViewHolder {
    @BindView(R2.id.text)
    TextView packetText;
    @BindView(R2.id.state)
    TextView packetState;
    @BindView(R2.id.image)
    ImageView packetImage;
    @BindView(R2.id.contentLayout)
    LinearLayout contentLayout;
    @BindView(R2.id.contentLayout1)
    LinearLayout contentLayout1;
    @BindView(R2.id.bubble_layout)
    LinearLayout bubbleLayout;
    @BindView(R2.id.tv_money)
    TextView tv_money;
    @BindView(R2.id.ll_tranfer_normal)
    LinearLayout ll_tranfer_normal;
    @BindView(R2.id.ll_tranfer_group)
    LinearLayout ll_tranfer_group;
    @BindView(R2.id.tv_shoukuan)
    TextView tv_shoukuan;

    RedPacketMessageContent redPacketMessageContent;
    RedPacketInfo redPacketInfo;

    public RedPacketMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }


    protected void onBind(UiMessage message) {
        redPacketMessageContent = (RedPacketMessageContent) message.message.content;
        packetText.setText(redPacketMessageContent.text);
        redPacketInfo = ChatManager.Instance().getRedPacket(redPacketMessageContent.id);
        if (redPacketInfo.type.equals("normal")) {
            //转账
            ll_tranfer_normal.setVisibility(View.VISIBLE);
            ll_tranfer_group.setVisibility(View.GONE);

            if(redPacketInfo.state == 1){
                if(ChatManager.Instance().getUserId().equals(redPacketInfo.user)){
                    tv_shoukuan.setText("对方已收款");
                }else{
                    tv_shoukuan.setText("已收款");
                }

                packetImage.setImageResource(R.mipmap.red_packet_get);
                //       contentLayout.setBackgroundColor(Color.rgb(252, 222, 194));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet0
                        : R.drawable.img_bubble_send_red_packet0);
            }else{
                if(ChatManager.Instance().getUserId().equals(redPacketInfo.user)){
                    tv_shoukuan.setText("你发起了一笔转账");
                }else {
                    tv_shoukuan.setText("请收款");
                }

                packetState.setVisibility(View.GONE);
                packetImage.setImageResource(R.mipmap.red_packet_wait);
                //      contentLayout.setBackgroundColor(fragment.getActivity().getResources().getColor(R.color.chengse));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet1
                        : R.drawable.img_bubble_send_red_packet1);
            }

            try {
                JSONObject infoJson = JSONObject.parseObject(redPacketMessageContent.info);


                String amount = infoJson.getString("balance");
                BigDecimal price = new BigDecimal(redPacketInfo.price).divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_DOWN);
                tv_money.setText(redPacketInfo.coinType + "  " + price);

            } catch (Exception e) {

            }

        } else {
            ll_tranfer_normal.setVisibility(View.GONE);
            ll_tranfer_group.setVisibility(View.VISIBLE);

            if (message.message.status != MessageStatus.Opened) {
                packetState.setVisibility(View.GONE);
                packetImage.setImageResource(R.mipmap.red_packet_wait);
                //      contentLayout.setBackgroundColor(fragment.getActivity().getResources().getColor(R.color.chengse));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet1
                        : R.drawable.img_bubble_send_red_packet1);
            } else {
                packetImage.setImageResource(R.mipmap.red_packet_get);
                //       contentLayout.setBackgroundColor(Color.rgb(252, 222, 194));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet0
                        : R.drawable.img_bubble_send_red_packet0);
            }
        }

    }

    /*@Override
    protected void onBind(UiMessage message) {
        redPacketMessageContent = (RedPacketMessageContent) message.message.content;
        packetText.setText(redPacketMessageContent.text);
        redPacketInfo = ChatManager.Instance().getRedPacket(redPacketMessageContent.id);
        if (redPacketInfo.type.equals("normal")) {
            //转账
            ll_tranfer_normal.setVisibility(View.VISIBLE);
            ll_tranfer_group.setVisibility(View.GONE);
            JSONObject data = new JSONObject();
            data.put("get", "txid");
            data.put("txid", redPacketInfo.packetID);
            OKHttpHelper.postJson(redPacketInfo.urlQuery, data.toString(), new SimpleCallback<String>() {
                @Override
                public void onSuccess1(String t) {

                }

                @Override
                public void onUiSuccess(String result) {
                    try {
                        JSONObject json = RedPacketUtils.getData(fragment.getActivity(), result);
                        if (json == null)
                            return;
                        float price = Float.valueOf(json.getString("balance")) / 100;
                        tv_money.setText(redPacketInfo.coinType + "  " + price);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUiFailure(int code, String msg) {
                    LoadingDialog.hideLoading();
                    Toast.makeText(fragment.getActivity(), fragment.getString(R.string.error_hit) + code, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            ll_tranfer_normal.setVisibility(View.GONE);
            ll_tranfer_group.setVisibility(View.VISIBLE);

            if (message.message.status != MessageStatus.Opened) {
                packetState.setVisibility(View.GONE);
                packetImage.setImageResource(R.mipmap.red_packet_wait);
                //      contentLayout.setBackgroundColor(fragment.getActivity().getResources().getColor(R.color.chengse));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet1
                        : R.drawable.img_bubble_send_red_packet1);
            } else {
                packetImage.setImageResource(R.mipmap.red_packet_get);
                //       contentLayout.setBackgroundColor(Color.rgb(252, 222, 194));
                bubbleLayout.setBackgroundResource(message.message.direction == MessageDirection.Receive
                        ? R.drawable.img_bubble_receive_red_packet0
                        : R.drawable.img_bubble_send_red_packet0);
            }
        }

    }*/

    @OnClick(R2.id.contentLayout)
    void onClick() {
      //  showOpenDialog2();
    //    System.out.println("@@@    money="+tv_money.getText().toString() +"   time="+redPacketInfo.timestamp);
        System.out.println("@@@             redPacketInfo="+redPacketInfo);
        if(redPacketInfo == null){
            return;
        }
        Intent intent = new Intent(fragment.getActivity(), TranferResultActivity.class);
        intent.putExtra("price",tv_money.getText().toString());
        intent.putExtra("time",redPacketInfo.timestamp);
        intent.putExtra("info", redPacketMessageContent.info);
        intent.putExtra("redPacketInfo",redPacketInfo);
        intent.putExtra("messageID",message.message.messageId);
        intent.putExtra("direction",message.message.direction.toString());
        fragment.startActivity(intent);
    }
    @OnClick(R2.id.contentLayout1)
    void onClick1() {
        showOpenDialog2();
    }

    void showOpenDialog2() {
        UserInfo userOwner = null;
        try {
            //        redPacketMessageContent = (RedPacketMessageContent) message.message.content;
            if (redPacketMessageContent.state != 0) {
                openPacket();
                return;
            }

            if (message.message.status == MessageStatus.Opened) {
                openPacket();
                return;
            }
            UserInfo userInfo = ChatManager.Instance().getUserInfo(null, false);
            RedPacketInfo redPacketInfo = ChatManager.Instance().getRedPacket(redPacketMessageContent.id);
            if (redPacketInfo.type.equalsIgnoreCase("bomb")
                    || (redPacketInfo.type.equalsIgnoreCase("normal")
                    && redPacketInfo.user.equalsIgnoreCase(userInfo.uid))) {
                openPacket();
                return;
            }
            userOwner = ChatManager.Instance().getUserInfo(redPacketInfo.user, false);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Activity activity = fragment.getActivity();
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_redpacket_open2, null);
        AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(true).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(view);
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        lp.width = (int) (displayMetrics.widthPixels * 0.8);
        //lp.height = (int) (displayMetrics.heightPixels*0.8);
        lp.height = (int) (lp.width * 1.388f);
        lp.dimAmount = 0;
        window.setAttributes(lp);
        window.setBackgroundDrawableResource(R.drawable.shape_red_packet_corner2);

        AppCompatTextView tvOpen = view.findViewById(R.id.open);
        tvOpen.setText(fragment.getString(R.string.open));
        tvOpen.setOnClickListener(v -> {
            dialog.dismiss();
            openPacket();
        });
        if (userOwner != null) {
            ImageView imgUser = view.findViewById(R.id.portrait);
            Glide.with(fragment).load(userOwner.portrait).into(imgUser);
            TextView tvUser = view.findViewById(R.id.user);
            //   tvUser.setText(userOwner.displayName+fragment.getString(R.string.de_packet));
            tvUser.setText(fragment.getString(R.string.red_come_from) + " " + userOwner.displayName);
        }
    }

    void openPacket() {
        Intent intent = new Intent(fragment.getActivity(), RedPacketInfoActivity.class);
        intent.putExtra("packetID", redPacketMessageContent.id);
        intent.putExtra("messageID", message.message.messageId);
        intent.putExtra("groupID", message.message.conversation.target);
        intent.putExtra("info", redPacketMessageContent.info);
        fragment.startActivity(intent);

        if (message.message.status != MessageStatus.Opened) {
            message.message.status = MessageStatus.Opened;
            messageViewModel.openRedPacket(message);
        }
    }
}

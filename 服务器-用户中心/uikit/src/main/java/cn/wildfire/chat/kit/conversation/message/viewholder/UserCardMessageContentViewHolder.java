/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.group.CreatGroupInfo;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_Share;

import java.io.IOException;

@MessageContentType(value = {
    CardMessageContent.class,

})
@EnableContextMenu
public class UserCardMessageContentViewHolder extends NormalMessageContentViewHolder {
    @BindView(R2.id.contentLayout)
    RelativeLayout contentLayout;
    @BindView(R2.id.userCardPortraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.userCardNameTextView)
    TextView nameTextView;
    @BindView(R2.id.userIdTextView)
    TextView userIdTextView;
    @BindView(R2.id.cardType)
    TextView cardType;


    @BindView(R2.id.contentLayout2)
    RelativeLayout contentLayout2;
    @BindView(R2.id.userCardPortraitImageView2)
    ImageView portraitImageView2;
    @BindView(R2.id.userCardNameTextView2)
    TextView nameTextView2;
    @BindView(R2.id.content)
    TextView content;
    @BindView(R2.id.theme)
    ImageView theme;
    @BindView(R2.id.cardType2)
    TextView cardType2;
    @BindView(R2.id.icon)
    ImageView icon;

    CardMessageContent userCardMessageContent;

    public UserCardMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBind(UiMessage message) {
        userCardMessageContent = (CardMessageContent) message.message.content;
        if(userCardMessageContent.getType() == CardType_Litapp){
            contentLayout.setVisibility(View.GONE);
            nameTextView2.setText(userCardMessageContent.getName());
            content.setText(userCardMessageContent.getDisplayName());
            //content.setVisibility(View.GONE);


            cardType2.setText("小程序名片");
            GlideApp
                    .with(fragment)
                    .load(userCardMessageContent.getPortrait())
                    .transforms(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.avatar_def)
                    .into(portraitImageView2);


            String themes = userCardMessageContent.getTheme();
            if(themes == null || themes.isEmpty())
                theme.setVisibility(View.GONE);
            else{
                GlideApp
                        .with(fragment)
                        .load(themes)
                        .transforms(new CenterCrop(), new RoundedCorners(10))
                        .placeholder(R.mipmap.avatar_def)
                        .into(theme);
            }

        }
        else if(userCardMessageContent.getType() == CardType_Share){
            contentLayout.setVisibility(View.GONE);
            if(userCardMessageContent.getName().equals("(null)")){
                nameTextView2.setText("");
            }else{
                nameTextView2.setText(userCardMessageContent.getName());
            }

            if(userCardMessageContent.getDisplayName().equals("(null)")){
                content.setText("");
            }else{
                content.setText(userCardMessageContent.getDisplayName());
            }

            cardType2.setText(userCardMessageContent.getTarget());
            GlideApp
                    .with(fragment)
                    .load(userCardMessageContent.getPortrait())
                    .transforms(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.avatar_def)
                    .into(portraitImageView2);
            GlideApp
                    .with(fragment)
                    .load(userCardMessageContent.getPortrait())
                    .transforms(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.avatar_def)
                    .into(icon);

            /*String themes = userCardMessageContent.getTheme();
            if(themes == null || themes.isEmpty())
                theme.setVisibility(View.GONE);
            else{
                GlideApp
                        .with(fragment)
                        .load(themes)
                        .transforms(new CenterCrop(), new RoundedCorners(10))
                        .placeholder(R.mipmap.avatar_def)
                        .into(theme);
            }*/

        }
        else {
            contentLayout.setVisibility(View.VISIBLE);
            contentLayout2.setVisibility(View.GONE);
            if(userCardMessageContent.getDisplayName().equals("(null)")){
                nameTextView.setText("");
            }else{
                nameTextView.setText(userCardMessageContent.getDisplayName());
            }
            if(userCardMessageContent.getName().equals("(null)")){
                userIdTextView.setText("");
            }else{
                userIdTextView.setText(userCardMessageContent.getName());
            }
            switch (userCardMessageContent.getType()) {
                case 0:
                    cardType.setText(WfcUIKit.getString(R.string.text_user_card));
                    break;
                case 1:
                    cardType.setText(WfcUIKit.getString(R.string.text_group_card));
                    break;
                case 2:
                    cardType.setText(WfcUIKit.getString(R.string.text_room_card));
                    break;
                case 3:
                    cardType.setText(WfcUIKit.getString(R.string.text_channel_card));
                    break;
            }
            GlideApp
                    .with(fragment)
                    .load(userCardMessageContent.getPortrait())
                    .transforms(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.avatar_def)
                    .into(portraitImageView);
        }
    }

    @OnClick(R2.id.contentLayout)
    void onUserCardClick() {
        if(userCardMessageContent.getType() == 1){
            Intent intent = new Intent(fragment.getContext(), GroupInfoActivity.class);
            intent.putExtra("groupId", userCardMessageContent.getTarget());
            fragment.startActivity(intent);
        }
        else{
            Intent intent = new Intent(fragment.getContext(), UserInfoActivity.class);
            UserInfo userInfo = ChatManager.Instance().getUserInfo(userCardMessageContent.getTarget(), false);
            intent.putExtra("userInfo", userInfo);
            fragment.startActivity(intent);
        }
    }
    @OnClick(R2.id.contentLayout2)
    void onUserCardClick2() {
        if(userCardMessageContent.getType() == CardType_Share){
            String url = userCardMessageContent.getUrl();
            if(url.startsWith("app://")){
                MessageViewModel messageViewModel = ViewModelProviders.of(fragment).get(MessageViewModel.class);
                messageViewModel.onReportMessage(url);
            }
        }
        else{
            LitappInfo litappInfo = new LitappInfo();
            litappInfo.target = userCardMessageContent.getTarget();
            litappInfo.name = userCardMessageContent.getName();
            litappInfo.displayName = userCardMessageContent.getDisplayName();
            litappInfo.portrait = userCardMessageContent.getPortrait();
            litappInfo.theme = userCardMessageContent.getTheme();
            litappInfo.url = userCardMessageContent.getUrl();

            Gson gson = new Gson();
            String data_json = gson.toJson(litappInfo);
            System.out.println("@@@     data_json="+data_json);

            String PREFIX = (String) SPUtils.get(fragment.getContext(),"PREFIX","");
            SharedPreferences sp = fragment.getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
            String token = sp.getString("token", null);
            OkHttpClient okHttpClient = new OkHttpClient();
            JSONObject requestData = new JSONObject();
            String json = "";
            try {
                requestData.put("data_json",data_json);
                json = requestData.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , json);

            if(PREFIX.length() > 0){
                try {
                    final Request request = new Request.Builder()
                            .url(PREFIX + "/im/saveProgram")
                            .post(requestBody)
                            .addHeader("X-Token",token)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            System.out.println("@@@   保存小程序失败： "+e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            String result = response.body().string();
                            System.out.println("@@@   保存小程序成功: "+result);
                        }
                    });
                }catch (Exception e){

                }
            }




            /*ChatManager.Instance().addLitapp(litappInfo, new GeneralCallback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onFail(int errorCode) {
                }
            });*/
            Intent intent = new Intent(fragment.getContext(), LitappActivity.class);
            intent.putExtra("litappInfo", litappInfo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fragment.startActivity(intent);
        }
    }
}

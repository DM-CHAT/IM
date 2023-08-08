/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.settings;

import android.content.Intent;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Collections;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.group.manage.GroupManageFragment;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.settings.blacklist.BlacklistListActivity;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class PrivacySettingActivity extends WfcBaseActivity implements CompoundButton.OnCheckedChangeListener{

    @BindView(R2.id.sbt_allowPartyDeleteMessage)
    SwitchButton allowPartyDeleteMessage;
    @BindView(R2.id.sbt_allowPartyRecallMessage)
    SwitchButton allowPartyRecallMessage;
    @BindView(R2.id.addFriend)
    OptionItemView addFriend;
    @BindView(R2.id.sbt_allowStrangersSendMessage)
    SwitchButton allowStrangersSendMessage;

    private String[] addFriends;
    private UserInfo userInfo;
    private static boolean recall = false;
    private static boolean delete = false;
    private static boolean sendMessage = false;
    private String AddFriendFlag;

    @Override
    protected int contentLayout() {
        return R.layout.privacy_setting_activity;
    }

    @Override
    protected void afterViews() {
        super.afterViews();

        allowPartyDeleteMessage.setChecked(true);
        allowPartyRecallMessage.setChecked(true);
        allowStrangersSendMessage.setChecked(true);

        allowPartyRecallMessage.setOnCheckedChangeListener(this);
        allowPartyDeleteMessage.setOnCheckedChangeListener(this);
        allowStrangersSendMessage.setOnCheckedChangeListener(this);

        addFriends = new String[]{getString(R.string.add_friends_verify1),getString(R.string.add_friends_verify2),getString(R.string.add_friends_verify3)};

        addFriend.setDesc(addFriends[0]);
        userInfo = getIntent().getParcelableExtra("userInfo");


        AddFriendFlag = userInfo.getRole("AddFriend");
        if (AddFriendFlag == null) {
            AddFriendFlag = "0";
        }

        if (AddFriendFlag.equals("1")) {
            addFriend.setDesc(getString(R.string.add_friends_verify2));
        } else if (AddFriendFlag.equals("2")) {
            addFriend.setDesc(getString(R.string.add_friends_verify3));
        } else {
            // 0
            addFriend.setDesc(getString(R.string.add_friends_verify1));
        }



        ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                recall = ChatManager.Instance().getEnable("recall");
                delete = ChatManager.Instance().getEnable("delete");
            }
        });

        if(userInfo.getAllowTemporaryChat() == null || "".equals(userInfo.getAllowTemporaryChat())){
            sendMessage = false;
        }else{
            if(userInfo.getAllowTemporaryChat().equals("yes")){
                sendMessage = true;
            }else{
                sendMessage = false;
            }
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        allowPartyDeleteMessage.setChecked(delete);
        allowPartyRecallMessage.setChecked(recall);
        allowStrangersSendMessage.setChecked(sendMessage);
    }

    @OnClick(R2.id.blacklistOptionItemView)
    void blacklistSettings() {
        Intent intent = new Intent(this, BlacklistListActivity.class);
        startActivity(intent);
    }

    @OnClick(R2.id.momentsPrivacyOptionItemView)
    void mementsSettings() {

    }

    @OnClick(R2.id.addFriend)
    void addFriend(){
        new MaterialDialog.Builder(PrivacySettingActivity.this)
                .items(addFriends)
                .itemsCallback((dialog, itemView, position, text) -> {
                    addFriend.setDesc((String) text);
                    ChatManager.Instance().getWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            ChatManager.Instance().setRole("AddFriend", String.valueOf(position));
                        }
                    });

                })
                .show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        String value = null;
        String value2 = null;

        if (isChecked) {
            value = "1";
        } else {
            value = "0";
        }

        if(isChecked){
            value2 = "yes";
        }else {
            value2 = "no";
        }


        if (id == R.id.sbt_allowPartyRecallMessage) {

            // 怎么取状态

            String finalValue = value;
            ChatManager.Instance().getWorkHandler().post(new Runnable() {
                @Override
                public void run() {
                    ChatManager.Instance().setEnable("recall", finalValue);
                }
            });


        } else if (id == R.id.sbt_allowPartyDeleteMessage) {

            String finalValue1 = value;
            ChatManager.Instance().getWorkHandler().post(new Runnable() {
                @Override
                public void run() {
                    ChatManager.Instance().setEnable("delete", finalValue1);
                }
            });

        }else if (id == R.id.sbt_allowStrangersSendMessage){
            String finalValue2 = value2;
            ChatManager.Instance().getWorkHandler().post(new Runnable() {
                @Override
                public void run() {
                    ChatManager.Instance().upDescribes("AllowTemporaryChat", finalValue2, null, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                             //       Toast.makeText(PrivacySettingActivity.this, getString(R.string.set_ok), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onFail(int errorCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                            //        Toast.makeText(PrivacySettingActivity.this, getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}

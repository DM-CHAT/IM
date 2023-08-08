/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.newfriend;

import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.UserInfo;

/**
 * 添加好友设置备注信息
 */
public class InviteFriendActivity extends WfcBaseActivity {

    @BindView(R2.id.introTextView)
    TextView introTextView;
    @BindView(R2.id.rl_back)
    RelativeLayout rl_back;

    private UserInfo userInfo;

    @Override
    protected void afterViews() {
        super.afterViews();
        yincangToolbar();
        userInfo = getIntent().getParcelableExtra("userInfo");
        if (userInfo == null) {
            finish();
        }
        UserViewModel userViewModel =ViewModelProviders.of(this).get(UserViewModel.class);
        UserInfo me = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        introTextView.setText(getString(R.string.im) + (me == null ? "" : me.displayName));
    }

    @Override
    protected int contentLayout() {
        return R.layout.contact_invite_activity1;
    }

   /* @Override
    protected int menu() {
        return R.menu.contact_invite;
    }*/

    @OnClick(R2.id.rl_back)
    void back(){
        finish();
    }
    @OnClick(R2.id.btn_confirm)
    void submit(){
        invite();
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            invite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    void invite() {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        contactViewModel.invite(userInfo.uid, introTextView.getText().toString())
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(InviteFriendActivity.this, getString(R.string.invite_sent), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(InviteFriendActivity.this, getString(R.string.add_friend_failure), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

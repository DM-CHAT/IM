/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.user;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;

/**
 * 修改备注
 */
public class SetAliasActivity extends WfcBaseActivity {

    private String userId;
//    private Friend mFriend;

    @BindView(R2.id.aliasEditText)
    EditText aliasEditText;
    @BindView(R2.id.clearImageButton)
    ImageButton clearImageButton;
    @BindView(R2.id.tv_save)
    TextView tv_save;

    private MenuItem menuItem;
    private ContactViewModel contactViewModel;

    @Override
    protected int contentLayout() {
        return R.layout.contact_set_alias_activity;
    }

    @Override
    protected void afterViews() {
        yincangToolbar();
        userId = getIntent().getStringExtra("userId");
        if (TextUtils.isEmpty(userId)) {
            finish();
            return;
        }
        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        String alias = contactViewModel.getFriendAlias(userId);
        if (!TextUtils.isEmpty(alias)) {
            aliasEditText.setText(alias);
        }
    }

    @Override
    protected int menu() {
        return R.menu.user_set_alias;
    }

    @Override
    protected void afterMenus(Menu menu) {
        menuItem = menu.findItem(R.id.save);
        menuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            changeAlias();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R2.id.tv_save)
    void save(){
        changeAlias();
    }

    @OnClick(R2.id.rl_back)
    void back(){
        finish();
    }

    @OnTextChanged(R2.id.aliasEditText)
    void onAliasEditTextChange() {
   //     menuItem.setEnabled(aliasEditText.getText().toString().trim().length() > 0 ? true : false);
    }

    @OnClick(R2.id.clearImageButton)
    void clearImageButton(){
        aliasEditText.setText("");
    }

    private void changeAlias() {
        String displayName = aliasEditText.getText().toString().trim();
        if (TextUtils.isEmpty(displayName)) {
            Toast.makeText(this, getString(R.string.alias_no_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        contactViewModel.setFriendAlias(userId, displayName).observe(this, new Observer<OperateResult<Integer>>() {
            @Override
            public void onChanged(OperateResult<Integer> integerOperateResult) {
                if (integerOperateResult.isSuccess()) {
                    Toast.makeText(SetAliasActivity.this, R.string.modify_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SetAliasActivity.this, getString(R.string.network_congestion), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

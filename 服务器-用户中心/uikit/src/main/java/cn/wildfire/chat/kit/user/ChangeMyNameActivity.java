/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.user;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;

import java.util.Collections;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.UserInfo;

import static cn.wildfirechat.model.ModifyMyInfoType.Modify_DisplayName;

public class ChangeMyNameActivity extends WfcBaseActivity {
    String TAG = ChangeMyNameActivity.class.getSimpleName();

    private MenuItem confirmMenuItem;
    @BindView(R2.id.nameEditText)
    EditText nameEditText;
    @BindView(R2.id.clearImageButton)
    ImageButton clearImageButton;

    private UserViewModel userViewModel;
    private UserInfo userInfo;

    @Override
    protected void afterViews() {
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        if (userInfo == null) {
            Toast.makeText(this, R.string.user_no_exist, Toast.LENGTH_SHORT).show();
            finish();
        }
        initView();
    }

    @Override
    protected int contentLayout() {
        return R.layout.user_change_my_name_activity;
    }

    @Override
    protected int menu() {
        return R.menu.user_change_my_name;
    }

    @Override
    protected void afterMenus(Menu menu) {
        confirmMenuItem = menu.findItem(R.id.save);
        confirmMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            changeMyName();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        if (userInfo != null) {
            nameEditText.setText(userInfo.displayName);
        }
        nameEditText.setSelection(nameEditText.getText().toString().trim().length());
    }

    @OnTextChanged(value = R2.id.nameEditText, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void inputNewName(CharSequence s, int start, int before, int count) {
        if (confirmMenuItem != null) {
            if (nameEditText.getText().toString().trim().length() > 0) {
                confirmMenuItem.setEnabled(true);
            } else {
                confirmMenuItem.setEnabled(false);
            }
        }
    }

    @OnClick(R2.id.clearImageButton)
    void clearImageButton(){
        nameEditText.setText("");
    }


    private void changeMyName() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.modifying)
                .progress(true, 100)
                .build();
        dialog.show();
        String nickName = nameEditText.getText().toString().trim();
        ModifyMyInfoEntry entry = new ModifyMyInfoEntry(Modify_DisplayName, nickName);
        userViewModel.modifyMyInfo(Collections.singletonList(entry)).observe(this, new Observer<OperateResult<Boolean>>() {
            @Override
            public void onChanged(@Nullable OperateResult<Boolean> booleanOperateResult) {
                if (booleanOperateResult.isSuccess()) {
                    Toast.makeText(ChangeMyNameActivity.this, R.string.modify_success, Toast.LENGTH_SHORT).show();
                    JSONObject json = new JSONObject();
                    json.put("name", nickName);
                    Log.d(TAG, "set name data: "+json.toString());
                    String url_SET_NAME = (String) SPUtils.get(ChangeMyNameActivity.this,"SET_NAME","");
                    OKHttpHelper.postJsonWithToken(url_SET_NAME, json.toString(), new SimpleCallback<String>() {
                        @Override
                        public void onSuccess1(String t) {

                        }

                        @Override
                        public void onUiSuccess(String data) {
                            Log.d(TAG, "set name result: "+data);
                        }

                        @Override
                        public void onUiFailure(int code, String msg) {
                            Log.d(TAG, "set name result: "+code+", msg: "+msg);
                        }
                    });
                } else {
                    Toast.makeText(ChangeMyNameActivity.this, R.string.modify_failure, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                finish();
            }
        });
    }
}

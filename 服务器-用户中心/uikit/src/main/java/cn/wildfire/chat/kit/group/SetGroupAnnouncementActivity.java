/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.wildfire.chat.kit.AppServiceProvider;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcBaseActivity2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;

import static cn.wildfirechat.model.ModifyGroupInfoType.Modify_Group_Notice;

public class SetGroupAnnouncementActivity extends WfcBaseActivity2 {
    @BindView(R2.id.announcementEditText)
    EditText announcementEditText;

    private MenuItem confirmMenuItem;
    private GroupInfo groupInfo;

    @Override
    protected int contentLayout() {
        return R.layout.group_set_announcement_activity;
    }

    @Override
    protected void afterViews() {
        yincangToolbar();
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        if (groupInfo == null) {
            finish();
            return;
        }
        announcementEditText.setText(groupInfo.notice);
    }

    @Override
    protected int menu() {
        return R.menu.group_set_group_name;
    }

    @Override
    protected void afterMenus(Menu menu) {
        confirmMenuItem = menu.findItem(R.id.confirm);
        /*if (announcementEditText.getText().toString().trim().length() > 0) {
            confirmMenuItem.setEnabled(true);
        } else {
            confirmMenuItem.setEnabled(false);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            setGroupName();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R2.id.announcementEditText)
    void onTextChanged() {
        if (confirmMenuItem != null) {
            confirmMenuItem.setEnabled(announcementEditText.getText().toString().trim().length() > 0);
        }
    }

    @OnClick(R2.id.rl_back)
    void rlBack(){
        finish();
    }

    @OnClick(R2.id.btn_confirm)
    void btnConfirm(){
        /*if(announcementEditText.getText().toString().trim().length() >0){

        }*/
        setGroupName();
    }
    @OnClick(R2.id.iv_delete)
    void deleteContent(){
        announcementEditText.setText("");
    }

    private void setGroupName() {
        String announcement = announcementEditText.getText().toString().trim();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.waiting)
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();

        ChatManager.Instance().modifyGroupInfo(groupInfo.target, Modify_Group_Notice, announcement, Collections.singletonList(0), null, new GeneralCallback() {
            @Override
            public void onSuccess() {
                if (!isFinishing()) {
                    dialog.dismiss();
                    Toast.makeText(SetGroupAnnouncementActivity.this, R.string.set_notice_success, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFail(int errorCode) {
                if (!isFinishing()) {
                    dialog.dismiss();
                    Toast.makeText(SetGroupAnnouncementActivity.this, getString(R.string.set_notice_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });
//        WfcUIKit.getWfcUIKit().getAppServiceProvider().updateGroupAnnouncement(groupInfo.target, announcement, new AppServiceProvider.UpdateGroupAnnouncementCallback() {
//            @Override
//            public void onUiSuccess(GroupAnnouncement announcement) {
//                if (isFinishing()) {
//                    return;
//                }
//                dialog.dismiss();
//                Toast.makeText(SetGroupAnnouncementActivity.this, R.string.set_notice_success, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//
//            @Override
//            public void onUiFailure(int code, String msg) {
//                if (isFinishing()) {
//                    return;
//                }
//                dialog.dismiss();
//                Toast.makeText(SetGroupAnnouncementActivity.this, getString(R.string.set_notice_failure) + code + msg, Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}

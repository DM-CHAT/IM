/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.user;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.contact.newfriend.InviteFriendActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class UserInfoActivity extends WfcBaseActivity {
    private UserInfo userInfo;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        if (!isDarkTheme()) {
            setTitleBackgroundResource(R.color.white, false);
        }
        userInfo = getIntent().getParcelableExtra("userInfo");
        if (userInfo == null) {
            finish();
            return;
        }



        if (userInfo.displayName == null){
            try {
                userInfo = ChatManager.Instance().getUserInfo(userInfo.uid, false);
                System.out.println("@@@@ UserInfoActivity user name:" + userInfo.name);
                System.out.println("@@@@ UserInfoActivity user display name:" + userInfo.displayName);
                //Thread.sleep(100);
            } catch (Exception e) {
                System.out.println("@@@@ UserInfoActivity Exception");
            }

        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, UserInfoFragment.newInstance(userInfo))
                .commit();

    }

    @Override
    protected int menu() {
        return R.menu.user_info;
    }

    @Override
    protected void afterMenus(Menu menu) {
        super.afterMenus(menu);
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        MenuItem itemDelete = menu.findItem(R.id.delete);
        MenuItem itemAddFriend = menu.findItem(R.id.addFriend);
        MenuItem itemAddBlacklist = menu.findItem(R.id.addBlacklist);
        MenuItem itemRemoveBlacklist = menu.findItem(R.id.removeBlacklist);
        MenuItem itemSetAlias = menu.findItem(R.id.setAlias);
        MenuItem itemSetFav = menu.findItem(R.id.setFav);
        MenuItem itemRemoveFav = menu.findItem(R.id.removeFav);
        MenuItem itemSetName = menu.findItem(R.id.setName);

        if (ChatManager.Instance().getUserId().equals(userInfo.uid)) {
            itemAddBlacklist.setEnabled(false);
            itemAddBlacklist.setVisible(false);
            itemAddFriend.setEnabled(false);
            itemAddFriend.setVisible(false);
            itemDelete.setEnabled(false);
            itemDelete.setVisible(false);
            itemRemoveBlacklist.setEnabled(false);
            itemRemoveBlacklist.setVisible(false);
            itemSetAlias.setEnabled(false);
            itemSetAlias.setVisible(false);

            /** 修改过：自己不显示修改备注 */
            /**----------------- 开始 ------------------*/
            itemSetName.setEnabled(false);
            itemSetName.setVisible(false);
            /**----------------- 结束 ------------------*/
        } else {
            if (contactViewModel.isFriend(userInfo.uid)) {
                itemAddFriend.setEnabled(false);
                itemAddFriend.setVisible(false);
                itemDelete.setEnabled(true);
                itemDelete.setVisible(true);
                itemSetAlias.setEnabled(true);
                itemSetAlias.setVisible(true);
            } else {
                itemAddFriend.setEnabled(true);
                itemAddFriend.setVisible(true);
                itemDelete.setEnabled(false);
                itemDelete.setVisible(false);
            }

            if(!ChatManager.Instance().isMyFriend(userInfo.uid)){
                itemSetAlias.setVisible(false);
                itemRemoveBlacklist.setVisible(false);
                itemAddBlacklist.setVisible(false);
                itemRemoveBlacklist.setVisible(true);
            } else {
                // 显示黑名单
                if (contactViewModel.isBlacklisted(userInfo.uid)) {
                    itemAddBlacklist.setEnabled(false);
                    itemAddBlacklist.setVisible(false);
                    itemRemoveBlacklist.setEnabled(true);
                    itemRemoveBlacklist.setVisible(true);
                } else {
                    itemAddBlacklist.setEnabled(true);
                    itemAddBlacklist.setVisible(true);
                    itemRemoveBlacklist.setEnabled(false);
                    itemRemoveBlacklist.setVisible(false);
                }
                if (contactViewModel.isFav(userInfo.uid)) {
                    itemSetFav.setEnabled(false);
                    itemSetFav.setVisible(false);
                    itemRemoveFav.setEnabled(false);
                    itemRemoveFav.setVisible(false);
                } else {
                    itemSetFav.setEnabled(false);
                    itemSetFav.setVisible(false);
                    itemRemoveFav.setEnabled(false);
                    itemRemoveFav.setVisible(false);
                }
            }




        }
    }

//    android:id="@+id/delete"
//    android:id="@+id/addFriend"
//    android:id="@+id/addBlacklist"
//    android:id="@+id/removeBlacklist"
//    android:id="@+id/setAlias"

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ContactViewModel contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        if (item.getItemId() == R.id.delete) {

            //getString(R.string.delete_friend_whether)
            new MaterialDialog.Builder(UserInfoActivity.this)
                    .content(getString(R.string.delete_friend_whether))
                    .positiveText(getString(R.string.string_confirm))
                    .negativeText(getString(R.string.string_cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //  System.out.println("@@@    点击了确认");
                            contactViewModel.deleteFriend(userInfo.uid).observe(
                                    UserInfoActivity.this, booleanOperateResult -> {
                                        if (booleanOperateResult.isSuccess()) {
                                            Toast.makeText(UserInfoActivity.this, getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
                                            String MainDapp = ChatManager.Instance().getMainDapp();
                                            Intent intent = null;
                                            if(MainDapp.length() == 0){
                                               intent  = new Intent(getPackageName() + ".main1");
                                            }else{
                                                intent  = new Intent(getPackageName() + ".main");
                                            }

                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(UserInfoActivity.this, "delete friend error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //  System.out.println("@@@    点击了取消");
                        }
                    })
                    .show();
            return true;
        } else if (item.getItemId() == R.id.addFriend) {
            Intent intent = new Intent(this, InviteFriendActivity.class);
            intent.putExtra("userInfo", userInfo);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.addBlacklist) {
            contactViewModel.setBlacklist(userInfo.uid, true).observe(
                    this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            Toast.makeText(this, R.string.set_ok, Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(this, "add blacklist error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return true;
        } else if (item.getItemId() == R.id.removeBlacklist) {
            contactViewModel.setBlacklist(userInfo.uid, false).observe(
                    this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            Toast.makeText(this, R.string.set_ok, Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(this, "remove blacklist error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return true;
        } else if (item.getItemId() == R.id.setAlias) {
            Intent intent = new Intent(this, SetAliasActivity.class);
            intent.putExtra("userId", userInfo.uid);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.setFav) {
            contactViewModel.setFav(userInfo.uid, true).observe(
                    this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            Toast.makeText(this, R.string.set_ok, Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(this, "set fav error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return true;

        } else if (item.getItemId() == R.id.removeFav) {
            contactViewModel.setFav(userInfo.uid, false).observe(
                    this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            Toast.makeText(this, R.string.set_ok, Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(this, "remove fav error " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return true;
        } else if (item.getItemId() == R.id.setName) {
            Intent intent = new Intent(this, SetNameActivity.class);
            intent.putExtra("userInfo", userInfo);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

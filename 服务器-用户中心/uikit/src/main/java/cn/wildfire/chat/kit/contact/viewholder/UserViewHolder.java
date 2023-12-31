/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.contact.UserListAdapter;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.UserInfo;

public class UserViewHolder extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected UserListAdapter adapter;
    @BindView(R2.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.nftFlag)
    ImageView nftImageView;
    @BindView(R2.id.nameTextView)
    TextView nameTextView;
    @BindView(R2.id.categoryTextView)
    protected TextView categoryTextView;

    protected UIUserInfo userInfo;

    public UserViewHolder(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    public void onBind(UIUserInfo userInfo) {
        this.userInfo = userInfo;
        if (userInfo.isShowCategory()) {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryTextView.setText(userInfo.getCategory());
        } else {
            categoryTextView.setVisibility(View.GONE);
        }
        UserViewModel userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        nameTextView.setText(userViewModel.getUserDisplayName(userInfo.getUserInfo()));
        GlideApp.with(fragment).load(userInfo.getUserInfo().portrait).placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(10))
            .into(portraitImageView);

        UserInfo u = userInfo.getUserInfo();
        if(u.getNft() != null)
            nftImageView.setVisibility(View.VISIBLE);
        else
            nftImageView.setVisibility(View.GONE);
    }

    public UIUserInfo getBindContact() {
        return userInfo;
    }
}

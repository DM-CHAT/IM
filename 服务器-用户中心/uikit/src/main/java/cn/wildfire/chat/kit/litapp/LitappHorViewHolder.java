package cn.wildfire.chat.kit.litapp;

import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.utils.ImageRound;
import cn.wildfirechat.model.LitappInfo;

public class LitappHorViewHolder extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    private LitappHorListAdapter adapter;
    @BindView(R2.id.imageRound)
    ImageView imageRound;

    protected LitappInfo litappInfo;

    public LitappHorViewHolder(Fragment fragment, LitappHorListAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    // TODO hide the last diver line
    public void onBind(LitappInfo litappInfo) {
        this.litappInfo = litappInfo;
        GlideApp.with(fragment).load(litappInfo.portrait).placeholder(R.mipmap.ic_channel_1).into(imageRound);
    }

    public LitappInfo getLitappInfo() {
        return litappInfo;
    }
}


package cn.wildfire.chat.kit.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfirechat.model.WalletsInfo;

public class MessageReminderAdapter extends BaseQuickAdapter<WalletsInfo , BaseViewHolder> {

    public MessageReminderAdapter(int layoutResId, @Nullable List<WalletsInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, WalletsInfo walletsInfos) {
        JSONObject jsonObject = JSONObject.parseObject(walletsInfos.wallets);
        String param = jsonObject.getString("param");
        JSONObject jsonObject1 = JSONObject.parseObject(param);
        String name = jsonObject1.getString("name");
        baseViewHolder.setText(R.id.tv_name,name);
    }
}

package cn.wildfire.chat.kit.group.manage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import cn.wildfire.chat.kit.R;

public class MessageSetKeyWordAdapter extends BaseQuickAdapter<KeyWordListInfo.DataBean.KeywordListBean , BaseViewHolder> {
    public MessageSetKeyWordAdapter(int layoutResId) {
        super(layoutResId);
    }

    public MessageSetKeyWordAdapter(int layoutResId, @Nullable List<KeyWordListInfo.DataBean.KeywordListBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, KeyWordListInfo.DataBean.KeywordListBean keywordListBean) {
        baseViewHolder.setText(R.id.tv_number,keywordListBean.getId()+"");
        baseViewHolder.setText(R.id.tv_example,"屏蔽关键词");
        baseViewHolder.setText(R.id.tv_keywords,keywordListBean.getContent());

    }
}

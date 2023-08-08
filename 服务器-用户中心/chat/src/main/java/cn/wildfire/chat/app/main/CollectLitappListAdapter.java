package cn.wildfire.chat.app.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.litapp.LitappListActivity1;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.remote.ChatManager;

public class CollectLitappListAdapter extends RecyclerView.Adapter<CollectLitappListAdapter.ViewHolder> {

    private Context context;
    private List<LitappInfo> list;

    public CollectLitappListAdapter(Context context, List<LitappInfo> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.adapter_collectlitapp,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.tv_name.setText(list.get(position).name);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(cn.wildfire.chat.kit.R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(context, 10)));
        Glide.with(context)
                .load(list.get(position).portrait)
                .apply(requestOptions)
                .into(holder.iv_title);

        holder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LitappInfo litappInfo = new LitappInfo();
                litappInfo.url = list.get(position).url;
                litappInfo.info = list.get(position).info;
                litappInfo.target = list.get(position).target;
                litappInfo.name = list.get(position).name;
                litappInfo.portrait = list.get(position).portrait;
                litappInfo.displayName = list.get(position).displayName;
                litappInfo.theme = list.get(position).theme;
                litappInfo.param = list.get(position).param;
                litappInfo.themeUrl = list.get(position).themeUrl;
                litappInfo.urlParam = list.get(position).urlParam;

                boolean isCollect = false;
                LitappInfo litappInfo1 = ChatManager.Instance().getCollectLitapp(litappInfo.target);
                if(litappInfo1 != null){
                    isCollect = true;
                }

                Intent intent = new Intent(context, LitappActivity.class);
                intent.putExtra("litappInfo", litappInfo);
                intent.putExtra("isCollect",isCollect);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name;
        ImageView iv_title;
        RelativeLayout rl_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_title = itemView.findViewById(R.id.iv_title);
            rl_item = itemView.findViewById(R.id.rl_item);
        }
    }
}

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;

public class WallListAdapter extends RecyclerView.Adapter<WallListAdapter.ViewHolder> {

    private Context context;
    private List<WalletsInfo> list;

    public WallListAdapter(Context context,List<WalletsInfo> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_wallet_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject jsonObject = JSONObject.parseObject(list.get(position).wallets);
        String param = jsonObject.getString("param");
        JSONObject jsonObject1 = JSONObject.parseObject(param);
        String name = jsonObject1.getString("name");
        holder.tv_name.setText(name);

        JSONObject jsonObject2 = JSONObject.parseObject(list.get(position).wallets);
        String portrait = jsonObject2.getString("portrait");
        Glide.with(context)
                .load(portrait)
                .into(holder.iv_title);

        holder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wallets = list.get(position).wallets;
                JSONObject jsonObject = JSONObject.parseObject(wallets);
                String param = jsonObject.getString("param");

                LitappInfo litappInfo = new LitappInfo();
                litappInfo.target = jsonObject.getString("target");
                litappInfo.info = jsonObject.getString("info");
                litappInfo.url = jsonObject.getString("url");
                litappInfo.param = param;
                Intent intent = new Intent(context, LitappActivity.class);
                intent.putExtra("litappInfo",litappInfo);
                context.startActivity(intent);
            }
        });
        holder.rl_item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                List<String> titles = new ArrayList<>();
                titles.add(context.getString(R.string.delete));
                new MaterialDialog.Builder(context).items(titles).itemsCallback((dialog, itemView, position1, text) -> {
                    switch (position1){
                        case 0:
                            String osnID = list.get(position).OsnID;
                            String name = list.get(position).name;
                            ChatManager.Instance().deleteWallets(name);
                            list.remove(position);
                            notifyDataSetChanged();
                            break;
                    }
                }).show();

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout rl_item;
        TextView tv_name;
        ImageView iv_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_item = itemView.findViewById(R.id.rl_item);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_title = itemView.findViewById(R.id.iv_title);
        }
    }
}

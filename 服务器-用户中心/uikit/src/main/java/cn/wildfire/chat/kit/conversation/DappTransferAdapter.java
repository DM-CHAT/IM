package cn.wildfire.chat.kit.conversation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.litapp.AddanApletsAdapter;
import cn.wildfire.chat.kit.third.location.ui.activity.WalletTransferWebViewActivity;
import cn.wildfirechat.model.WalletsInfo;

public class DappTransferAdapter extends RecyclerView.Adapter<DappTransferAdapter.ViewHolder> {

    private Context context;
    private List<WalletsInfo> walletsInfos;
    private LayoutInflater mInflater;

    public DappTransferAdapter(Context context,List<WalletsInfo> walletsInfos){
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.walletsInfos = walletsInfos;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_wallets,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject jsonObject = JSONObject.parseObject(walletsInfos.get(0).wallets);
        String param = jsonObject.getString("param");
        JSONObject jsonObject1 = JSONObject.parseObject(param);
        String name = jsonObject1.getString("name");
        holder.tv_name.setText(name);

    }

    @Override
    public int getItemCount() {
        return walletsInfos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
        }
    }
}

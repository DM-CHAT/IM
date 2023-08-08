package cn.wildfire.chat.app.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.wildfire.chat.app.login.model.AnnouncementInfo;
import cn.wildfirechat.chat.R;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private Context context;
    private List<AnnouncementInfo.DataBean> list;

    public AnnouncementAdapter(Context context,List<AnnouncementInfo.DataBean> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_announcement,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tv_title.setText(list.get(position).getTitle());
            holder.tv_notice.setText(list.get(position).getRemark());
            if(list.get(position).getIsRead() == 0){
                holder.iv_yuandian.setVisibility(View.VISIBLE);
            }else{
                holder.iv_yuandian.setVisibility(View.GONE);
            }
            holder.ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,AnnouncementXiangQingActivity.class);
                    intent.putExtra("title",list.get(position).getTitle());
                    intent.putExtra("time",list.get(position).getCreate_time());
                    intent.putExtra("content",list.get(position).getRemark());
                    intent.putExtra("id",list.get(position).getId());
                    context.startActivity(intent);
                }
            });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_title;
        TextView tv_notice;
        ImageView iv_yuandian;
        LinearLayout ll_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_notice = itemView.findViewById(R.id.tv_notice);
            iv_yuandian = itemView.findViewById(R.id.iv_yuandian);
            ll_item = itemView.findViewById(R.id.ll_item);
        }
    }
}

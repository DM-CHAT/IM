package cn.wildfire.chat.kit.group.manage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.litapp.AddanApletsAdapter;

public class SetKeyWordAdapter extends RecyclerView.Adapter<SetKeyWordAdapter.ViewHolder> {

    private List<KeyWordListInfo.DataBean.KeywordListBean> list;
    private Context context;
    private LayoutInflater inflater;

    private SetKeyWordViewItemOnClick setKeyWordViewItemOnClick;

    public SetKeyWordAdapter(Context context,List<KeyWordListInfo.DataBean.KeywordListBean> list,SetKeyWordViewItemOnClick setKeyWordViewItemOnClick){
        this.context =context;
        this.list = list;
        this.setKeyWordViewItemOnClick = setKeyWordViewItemOnClick;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_setkey_word,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int index = position+1;
        holder.tv_number.setText("0"+index);
        holder.tv_example.setText("屏蔽关键词");
        holder.tv_keywords.setText(list.get(position).getContent());

        holder.rl_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setKeyWordViewItemOnClick.itemOnClick(100,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_number;
        TextView tv_example;
        TextView tv_keywords;
        RelativeLayout rl_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_number = itemView.findViewById(R.id.tv_number);
            tv_example = itemView.findViewById(R.id.tv_example);
            tv_keywords = itemView.findViewById(R.id.tv_keywords);
            rl_delete = itemView.findViewById(R.id.rl_delete);
        }
    }




    public interface SetKeyWordViewItemOnClick{
        void itemOnClick(int title ,int position);
    }
}

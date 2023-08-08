package cn.wildfire.chat.kit.tag;

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

public class DeleteTagAdapter extends RecyclerView.Adapter<DeleteTagAdapter.ViewHolder> {

    private Context context;
    private List<String> list;
    private LayoutInflater inflater;

    public DeleteTagAdapter(Context context, List<String> list){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_delete_tag,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkbox;
        ImageView iv_user_head;
        TextView tv_userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            iv_user_head = itemView.findViewById(R.id.iv_user_head);
            tv_userName = itemView.findViewById(R.id.tv_userName);
        }
    }
}

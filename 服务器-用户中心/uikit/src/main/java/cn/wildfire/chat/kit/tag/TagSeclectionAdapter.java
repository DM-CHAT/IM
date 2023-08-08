package cn.wildfire.chat.kit.tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.R;
import cn.wildfirechat.model.OrgTag;

public class TagSeclectionAdapter extends RecyclerView.Adapter<TagSeclectionAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<OrgTag> list;
    private Map<Integer, Boolean> map = new HashMap<>();
    private boolean onBind;
    private CheckBox currentCheckBox;
    private int checkedPosition = -1;
    public int tagId = -1;

    public TagSeclectionAdapter(Context context,List<OrgTag> list){
        this.context = context;
        this.list = list;
        /*OrgTag tag = new OrgTag();
        tag.tagName = "";
        tag.id = -1;
        tag.tagId = -1;
        this.list.add(tag);*/


        inflater = LayoutInflater.from(context);
    }

    public int getCheckedPosition(){
        return checkedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_tag_selection,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list.get(position).id == -1) {
            holder.tv_tagName.setText("Cancel");
        } else {
            holder.tv_tagName.setText(list.get(position).tagName);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked() == true){

                    if (currentCheckBox != null) {
                        currentCheckBox.setChecked(false);
                    }
                    currentCheckBox = holder.checkBox;
                    if (position <= list.size()){
                        tagId = list.get(position).id;
                    } else {
                        tagId = -1;
                    }


                    /*map.clear();
                    map.put(position,true);
                    checkedPosition = position;*/
                }else {
                    /*map.remove(position);
                    if(map.size() == 0){
                        checkedPosition = -1;
                    }*/
                }

                /*if(!onBind){
                    notifyDataSetChanged();
                }*/
            }
        });

        /*onBind = true;
        if(map != null && map.containsKey(position)){
            holder.checkBox.setChecked(true);
        }else{
            holder.checkBox.setChecked(false);
        }
        onBind = false;*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_tagName;
        public CheckBox checkBox;
        LinearLayout ll_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_tagName = itemView.findViewById(R.id.tv_tagName);
            checkBox = itemView.findViewById(R.id.checkbox);
            ll_item = itemView.findViewById(R.id.ll_item);
        }
    }
}

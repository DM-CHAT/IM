package cn.wildfire.chat.kit.litapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.info.AddanApletsInfo;

public class AddanApletsAdapter extends RecyclerView.Adapter<AddanApletsAdapter.ViewHolder> {
    private Context context;
    private List<AddanApletsInfo.DataBean.RecordsBean> list;
    private LayoutInflater mInflater;
    private boolean onBind;
    private Map<Integer, Boolean> map = new HashMap<>();
    private int checkedPosition = -1;
    private boolean isFirst = true;

    public AddanApletsAdapter(Context context,List<AddanApletsInfo.DataBean.RecordsBean> list){
        mInflater = LayoutInflater.from(context);
        this.context =context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_addanaplets,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    //得到当前选中的位置
    public int getCheckedPosition() {
        return checkedPosition;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        AddanApletsInfo.DataBean.RecordsBean dataBean =list.get(position);
        String str = dataBean.getDapp_url_front();
        JSONObject jsonObject = JSONObject.parseObject(str);
        String name = (String) jsonObject.getString("name");
        holder.tv_name.setText(name);
        String imageUrl = jsonObject.getString("portrait");
        GlideApp.with(context).load(imageUrl).transform(new CircleCrop()).
                placeholder(R.mipmap.default_image).fallback(R.mipmap.default_image)
                .error(R.mipmap.default_image).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(holder.iv_image);
        String ospnId = jsonObject.getString("ospnId");
        System.out.println("@@@   ospnId="+ospnId);

        if(isFirst){
            if(ospnId == null || ospnId.equals("")){
            }else{
                //   map.clear();
                map.put(position,true);
                checkedPosition = position;
            }
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                if(isCheck == true){
                    map.clear();
                    map.put(position,true);
                    checkedPosition = position;
                    isFirst = false;
                }else{
                    map.remove(position);
                    if(map.size() == 0){
                        checkedPosition = -1;  //-1代表一个都未选择
                    }
                }
                if(!onBind){
                    notifyDataSetChanged();
                }
            }
        });

        onBind = true;
        if(map != null && map.containsKey(position)){
            holder.checkBox.setChecked(true);
        }else{
            holder.checkBox.setChecked(false);
        }
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name;
        CheckBox checkBox;
        ImageView iv_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            checkBox = itemView.findViewById(R.id.checkbox);
            iv_image = itemView.findViewById(R.id.iv_image);
        }
    }
}

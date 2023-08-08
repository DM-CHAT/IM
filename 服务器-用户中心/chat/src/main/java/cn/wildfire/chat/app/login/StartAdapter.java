package cn.wildfire.chat.app.login;

import static cn.wildfire.chat.app.BaseApp.getContext;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.Utils;
import cn.wildfire.chat.app.login.model.StartInfo;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.remote.ChatManager;

public class StartAdapter extends RecyclerView.Adapter<StartAdapter.ViewHolder> {

    private List<StartInfo.DataBean> list = new ArrayList<>();
    private Context context;
    private String device;

    public StartAdapter(Context context, List<StartInfo.DataBean> list){
        this.context =context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_start,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_name.setText(list.get(position).getServiceName());
        String imageUrl = list.get(position).getIconUrl();
        GlideApp.with(context).load(imageUrl).transform(new CircleCrop()).
                placeholder(cn.wildfire.chat.kit.R.mipmap.default_image).fallback(cn.wildfire.chat.kit.R.mipmap.default_image)
                .error(cn.wildfire.chat.kit.R.mipmap.default_image).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(holder.iv_title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("@@@   position="+position);
                Resources resources = context.getResources();
                Configuration config = resources.getConfiguration();
                String old_lang = config.getLocales().toLanguageTags();
                String url = null;
                device = (String) SPUtils.get(context,"UUID","");
                if(device.equals("") ||device == null){
                    device = UUID.randomUUID().toString();
                }
                SPUtils.put(context,"UUID",device);
                if(old_lang.startsWith("en")){
                    url = list.get(position).getUrl()+"?language=2"+"&device="+device;
                }else if(old_lang.startsWith("zh")){
                    url = list.get(position).getUrl()+"?language=0"+"&device="+device;
                }else if(old_lang.startsWith("vn")){
                    url = list.get(position).getUrl()+"?language=1"+"&device="+device;
                }
                System.out.println("@@@   url: "+url);
                Intent intent = new Intent(context, LoginJSActivity.class);
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(),
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                intent.putExtra("url",url);
                context.startActivity(intent, bundle);

                /*if(StartActivity.class.isInstance(context)){
                    StartActivity startActivity = (StartActivity) context;
                    startActivity.finish();
                }*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name;
        ImageView iv_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_title = itemView.findViewById(R.id.iv_title);
        }
    }
}

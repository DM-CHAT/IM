package cn.wildfire.chat.kit.tag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.group.manage.SetKeyWordAdapter;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private List<OrgTag> list;
    private Context context;
    private LayoutInflater inflater;

    public TagAdapter(Context context,List<OrgTag> list){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.adapter_tag,parent,false);
        ViewHolder viewHolder =new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_tagName.setText(list.get(position).tagName);
        System.out.println("@@@    tagId="+list.get(position).id);
        System.out.println("@@@     tagUser="+ChatManager.Instance().listTagUser(list.get(position).id));
        List<String> list1 = ChatManager.Instance().listTagUser(list.get(position).id);
        String name = "";
        if(list1.size()>0){
            for (int i=0;i<list1.size();i++){
                if(list1.get(i).startsWith("OSNU")){
                    UserInfo userInfo = ChatManager.Instance().getUserInfo(list1.get(i),false);
                    name = userInfo.displayName +"; "+ name;
                }else{
                    GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(list1.get(i),false);
                    name = groupInfo.name +", "+name;
                }
            }
        }
        if(name.equals("") || name == null){

        }else{
            holder.tv_names.setText(name);
        }
        holder.rl_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
          //      if(list1.size() > 0){
                    Intent intent = new Intent(context,DeleteTagActivity.class);
                    intent.putStringArrayListExtra("list", (ArrayList<String>) list1);
                    intent.putExtra("tagId",list.get(position).id);
                    context.startActivity(intent);
                    Activity activity = (Activity) context;
                    activity.finish();
       //         }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_tagName;
        TextView tv_names;
        RelativeLayout rl_tag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_tagName = itemView.findViewById(R.id.tv_tagName);
            tv_names = itemView.findViewById(R.id.tv_names);
            rl_tag = itemView.findViewById(R.id.rl_tag);
        }
    }
}

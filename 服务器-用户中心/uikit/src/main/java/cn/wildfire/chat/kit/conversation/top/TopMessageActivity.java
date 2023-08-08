package cn.wildfire.chat.kit.conversation.top;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class TopMessageActivity extends AppCompatActivity {

    private CommonAdapter commonAdapter;
    private List<String> list = new ArrayList<>();
    private GroupInfo groupInfo;
    private String str_list = null;
    private RelativeLayout rl_back;
    private RecyclerView recyclerView;
    private int space = 16;  //item间距
    private ImageView iv_delete;
    private boolean isDelete = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_message);

        list = getIntent().getStringArrayListExtra("list");
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        isDelete = getIntent().getBooleanExtra("isDelete",false);

        initView();
    }

    private void initView(){
        rl_back = findViewById(R.id.rl_back);

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        commonAdapter = new CommonAdapter<String>(TopMessageActivity.this, R.layout.adapter_top_message, list) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {

                String key = groupInfo.getTopMessage(list.get(position));
                JSONObject attrJson = JSONObject.parseObject(key);
                String userID = attrJson.getString("fromUser");
                String topMessage = attrJson.getString("data");
                UserInfo userInfo = ChatManager.Instance().getUserInfo(userID, false);

                holder.setText(R.id.tv_name,userInfo.displayName);
                holder.setText(R.id.tv_message,topMessage);

                iv_delete = holder.getView(R.id.iv_delete);

                if(isDelete){
                    iv_delete.setVisibility(View.VISIBLE);
                }else{
                    iv_delete.setVisibility(View.GONE);
                }

                iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String str_list = groupInfo.getTopMessage(list.get(position));
                        JSONObject attrJson = JSONObject.parseObject(str_list);
                        String key = attrJson.getString("key");
                        List<String> list1 = new ArrayList<>();
                        list1.add(key);
                        ChatManager.Instance().deleteTopMessage(list1, groupInfo.target, new GeneralCallback2() {
                            @Override
                            public void onSuccess(String result) {
                                System.out.println("@@@   删除置顶消息成功");
                                list.remove(position);
                                TopMessageActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyDataSetChanged();
                                    }
                                });

                            }

                            @Override
                            public void onFail(int errorCode) {
                                System.out.println("@@@   删除置顶消息失败");
                            }
                        });
                    }
                });
            }
        };

        recyclerView.setAdapter(commonAdapter);
    }
}

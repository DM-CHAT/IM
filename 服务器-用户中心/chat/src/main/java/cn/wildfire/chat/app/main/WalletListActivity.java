package cn.wildfire.chat.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.conversation.MessageReminderAdapter;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.user.NftActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.ChatManager;

public class WalletListActivity extends AppCompatActivity {

    private MessageReminderAdapter messageReminderAdapter;
    private RecyclerView recyclerView;
    private RelativeLayout rl_back;
    private List<WalletsInfo> list2 = new ArrayList<>();
    private WallListAdapter wallListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);

        recyclerView =findViewById(R.id.recyclerView);
        list2 = ChatManager.Instance().getWalletsInfo();

        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));

        wallListAdapter = new WallListAdapter(WalletListActivity.this,list2);
        recyclerView.setAdapter(wallListAdapter);
        wallListAdapter.notifyDataSetChanged();
        //messageReminderAdapter = new MessageReminderAdapter(R.layout.adapter_wallet_list,list2);
        //recyclerView.setAdapter(messageReminderAdapter);
        //messageReminderAdapter.notifyDataSetChanged();

        /*messageReminderAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String wallets = list2.get(position).wallets;
                JSONObject jsonObject = JSONObject.parseObject(wallets);
                String param = jsonObject.getString("param");

                LitappInfo litappInfo = new LitappInfo();
                litappInfo.target = jsonObject.getString("target");
                litappInfo.info = jsonObject.getString("info");
                litappInfo.url = jsonObject.getString("url");
                litappInfo.param = param;
                Intent intent = new Intent(WalletListActivity.this, LitappActivity.class);
                intent.putExtra("litappInfo",litappInfo);
                startActivity(intent);
                finish();
            }
        });
        messageReminderAdapter.setOnItemChildLongClickListener(new OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                list2.remove(position);
                messageReminderAdapter.notifyDataSetChanged();
                return false;
            }
        });*/
        rl_back = findViewById(R.id.rl_back);
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

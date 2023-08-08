package cn.wildfire.chat.app.main;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.remote.ChatManager;

public class CollectLitappListActivity extends AppCompatActivity {

    private RelativeLayout rl_back;
    private List<LitappInfo> litappCollectInfoList;
    private RecyclerView recyclerView;
    private CollectLitappListAdapter collectLitappListAdapter;
    private int space = 16;  //item间距

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_litapplist);

        litappCollectInfoList = ChatManager.Instance().getCollectLitappList();
        System.out.println("@@@          litappCollectInfoList="+litappCollectInfoList.size());

        initView();
        initOnClick();
    }

    private void initView(){
        rl_back = findViewById(R.id.rl_back);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setLayoutManager(layoutManager);
        collectLitappListAdapter = new CollectLitappListAdapter(CollectLitappListActivity.this,litappCollectInfoList);
        recyclerView.setAdapter(collectLitappListAdapter);
        collectLitappListAdapter.notifyDataSetChanged();
    }

    private void initOnClick(){
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

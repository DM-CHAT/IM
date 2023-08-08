package cn.wildfire.chat.kit.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.remote.ChatManager;

public class TagSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TagSeclectionAdapter tagSeclectionAdapter;
    private List<OrgTag> list = new ArrayList<>();
    private RelativeLayout rl_back;
    private Button btn_confirm;
    private String uid = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        initView();
        initData();
    }

    private void initView(){
        recyclerView = findViewById(R.id.recyclerView);
        rl_back = findViewById(R.id.rl_back);
        btn_confirm = findViewById(R.id.btn_confirm);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (uid == null) {
                    Toast.makeText(TagSelectionActivity.this,"input error null.",Toast.LENGTH_SHORT).show();
                    return;
                }

                int tagId = tagSeclectionAdapter.tagId;
                if (tagId == -1) {
                    if (uid.startsWith("OSNG")) {
                        tagId = -2;
                    }
                }


                if (!ChatManager.Instance().updateTagUser(uid, tagId)){
                    Toast.makeText(TagSelectionActivity.this,"change tag error failed.",Toast.LENGTH_SHORT).show();
                }else{
                    finish();
                }
            }
        });

    }
    private void initData(){
        list = ChatManager.Instance().listTag();
        tagSeclectionAdapter = new TagSeclectionAdapter(TagSelectionActivity.this,list);
        recyclerView.setAdapter(tagSeclectionAdapter);
        tagSeclectionAdapter.notifyDataSetChanged();
    }
}

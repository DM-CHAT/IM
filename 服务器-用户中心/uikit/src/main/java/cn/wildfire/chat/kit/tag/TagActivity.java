package cn.wildfire.chat.kit.tag;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.group.manage.SetKeyWordActivity;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.remote.ChatManager;

public class TagActivity extends AppCompatActivity {

    private RelativeLayout rl_add,rl_back;
    private Dialog dialog;
    private List<OrgTag> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private TagAdapter tagAdapter;
    private RelativeLayout rl_default;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        initView();
        initData();
    }

    private void initView(){
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        rl_back = findViewById(R.id.rl_back);
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rl_default = findViewById(R.id.rl_default);

        rl_add = findViewById(R.id.rl_add);
        rl_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View views = View.inflate(TagActivity.this, R.layout.dialog_tag, null);
                Button btn_cancel = views.findViewById(R.id.btn_cancel);
                Button btn_confirm= views.findViewById(R.id.btn_confirm);
                EditText edt_keywords = views.findViewById(R.id.edt_keywords);
                AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);
                builder.setView(views);
                builder.setCancelable(true);
                dialog = builder.create();
                dialog.show();

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                btn_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String content = edt_keywords.getText().toString();
                        list = ChatManager.Instance().listTag();
                        int tagId = list.size()+1;
                        if(!content.isEmpty()){
                            ChatManager.Instance().insertTag(content,tagId);
                            list.clear();
                            initData();
                            dialog.dismiss();
                        }else{
                            Toast.makeText(TagActivity.this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void initData(){
        list = ChatManager.Instance().listTag();
        if(list.size() == 0){
            rl_default.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            rl_default.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        tagAdapter = new TagAdapter(TagActivity.this,list);
        recyclerView.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }

}

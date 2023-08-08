package cn.wildfire.chat.kit.tag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class DeleteTagActivity extends AppCompatActivity {

    private RelativeLayout rl_back;
    private RecyclerView recyclerView;
    private CheckBox cb_all,checkbox;
    private Button btn_delete,btn_delete_tag;
    private DeleteTagAdapter deleteTagAdapter;
    private List<String> list = new ArrayList<>();
    private CommonAdapter<String> commonAdapter;
    private Map<Integer, Boolean> checkStatus = new HashMap<>();//用来记录所有checkbox的状态
    private ImageView iv_user_head;
    private int space = 16;  //item间距
    private int tagId;
    private GroupInfo groupInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_tag);
        Intent intent = getIntent();
        list = intent.getStringArrayListExtra("list");
        tagId = intent.getIntExtra("tagId",-1);
        for (int i = 0; i < list.size(); i++) {
            checkStatus.put(i, false);// 默认所有的checkbox都是没选中
        }
        initView();
        initData(list);
    }

    private void initView(){
        rl_back = findViewById(R.id.rl_back);
        recyclerView = findViewById(R.id.recyclerView);
        cb_all = findViewById(R.id.cb_all);
        btn_delete = findViewById(R.id.btn_delete);
        btn_delete_tag = findViewById(R.id.btn_delete_tag);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i<list.size(); i++){
                    if(checkStatus.get(i)){
                        String osnId = list.get(i);
                        int tagId = -1;
                        if (osnId != null) {
                            if (osnId.startsWith("OSNG")) {
                                tagId = -2;
                            }
                            if(ChatManager.Instance().updateTagUser(osnId, tagId)){
                                finish();
                            }

                        }

                        /*if(ChatManager.Instance().deleteTagUser()){
                            finish();
                        }*/
                    }
                }
                finish();
            }
        });

        btn_delete_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new android.app.AlertDialog.Builder(DeleteTagActivity.this)
                        .setMessage(getString(R.string.confirm_delete_tag))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(ChatManager.Instance().deleteTag(tagId)){
                                    finish();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setCancelable(true)
                        .create().show();


            }
        });


        /*deleteTagAdapter = new DeleteTagAdapter(DeleteTagActivity.this,list);
        recyclerView.setAdapter(deleteTagAdapter);
        deleteTagAdapter.notifyDataSetChanged();*/
        cb_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < list.size(); i++) {
                    if (cb_all.isChecked()) {
                        checkStatus.put(i, true);
                    } else {
                        checkStatus.put(i, false);
                    }
                }
                commonAdapter.notifyDataSetChanged();
            }
        });

        commonAdapter = new CommonAdapter<String>(DeleteTagActivity.this,R.layout.adapter_delete_tag,list) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {

                checkbox =holder.getView(R.id.checkbox);
                checkbox.setChecked(checkStatus.get(position));
                iv_user_head = holder.getView(R.id.iv_user_head);

                if(list.get(position).startsWith("OSNG")){
                    groupInfo = ChatManager.Instance().getGroupInfo(list.get(position),false);
                    Glide.with(DeleteTagActivity.this).load(groupInfo.portrait)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop())).into(iv_user_head);

                    holder.setText(R.id.tv_userName,groupInfo.name);
                }else{
                    Glide.with(DeleteTagActivity.this).load(ChatManager.Instance().getUserInfo(list.get(position),false).portrait)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop())).into(iv_user_head);

                    holder.setText(R.id.tv_userName,ChatManager.Instance().getUserInfo(list.get(position),false).displayName);
                }

                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        checkStatus.put(position,isChecked);

                        boolean allCheck = true;
                        Iterator it =checkStatus.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            Boolean value = (Boolean) entry.getValue();
                            if (!value) {
                                allCheck = false;
                                break;
                            }
                        }
                        cb_all.setChecked(allCheck);
                    }
                });

            }
        };
        recyclerView.setAdapter(commonAdapter);

    }

    private void initData(List<String> list1){

    }
}

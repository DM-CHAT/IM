package cn.wildfire.chat.app.login;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfire.chat.kit.utils.SpacesItemDecoration;
import cn.wildfirechat.chat.R;

public class AccountListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountListAdapter accountListAdapter;
    private int space = 16;  //item间距
    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accoutlist);

        String[] arr = KeyStore.listAccount(this);
        list =  Arrays.asList(arr);
        initView();
    }

    private void initView(){
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));

        accountListAdapter = new AccountListAdapter(AccountListActivity.this,list);
        recyclerView.setAdapter(accountListAdapter);
        accountListAdapter.notifyDataSetChanged();
    }
}

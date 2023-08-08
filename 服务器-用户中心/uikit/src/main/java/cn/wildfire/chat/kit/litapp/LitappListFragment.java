/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.litapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.zxing.Result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.info.LitappListInfo;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.client.SqliteUtils;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetLitappsCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LitappListFragment extends Fragment implements LitappListAdapter.OnLitappClickListener, LitappListAdapter.OnLitappLongCLickListener {
    @BindView(R2.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R2.id.horizontal_recycler)
    RecyclerView horizontal_recycler;
    @BindView(R2.id.litapp_size)
    TextView litapp_size;
    private LitappListAdapter litappListAdapter;
    private LitappHorListAdapter litappHorListAdapter;
    private boolean pick;
    private  List<LitappInfo> litappInfos;
    private LitappListInfo litappListInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            pick = args.getBoolean("pick", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.channel_list_frament, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void init() {
        litappListAdapter = new LitappListAdapter(this);
        litappListAdapter.setOnLitappItemClickListener(this);
        litappListAdapter.setOnLitappItemLongClickListener(this);

        recyclerView.setAdapter(litappListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        litappHorListAdapter = new LitappHorListAdapter(this);
        litappHorListAdapter.setOnLitappItemClickListener(this);
        litappHorListAdapter.setOnLitappItemLongCLickListener(this);

        horizontal_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        horizontal_recycler.setAdapter(litappHorListAdapter);
        litappHorListAdapter.setOnLitappItemClickListener(this);

        String PREFIX = (String) SPUtils.get(getActivity(),"PREFIX","");
        if (PREFIX.length() < 5) {
            initLocalDappList();
        } else {
            initRemoteDappList(PREFIX);
        }
    }

    private void initLocalDappList(){

        ChatManager.Instance().getLitappList(new GetLitappsCallback() {
            @Override
            public void onSuccess(List<LitappInfo> litappInfos) {
                if(litappInfos == null)
                    litappInfos = new ArrayList<>();
                litappListAdapter.setLitappInfos(litappInfos);
                litappListAdapter.notifyDataSetChanged();

                litappHorListAdapter.setLitappInfos(litappInfos);
                litappHorListAdapter.notifyDataSetChanged();

                litapp_size.setText(litappInfos.size() + " >");
            }

            @Override
            public void onFail(int errorCode) {
            }
        });
    }

    private void initRemoteDappList(String PREFIX){
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", null);

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(PREFIX+"/im/programList")
                .get()
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   小程序列表失败： "+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   小程序列表成功: "+result);

                if(result == null || result.equals("")){
                    return;
                }
                Gson gson = new Gson();
                litappListInfo = gson.fromJson(result,LitappListInfo.class);
                if(litappListInfo.getCode() == 200){
                    if(litappListInfo.getData().size() == 0){
                        Looper.prepare();
                        Toast.makeText(getActivity(),getString(R.string.litapp_info)+":",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return;
                    }
                    String data_json = litappListInfo.getData().get(0).getData_json();
                    LitappInfo litappInfo = gson.fromJson(data_json,LitappInfo.class);
                    litappInfos = new ArrayList<>();
                    litappInfos.add(litappInfo);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(litappInfos == null)
                                litappInfos = new ArrayList<>();
                            litappListAdapter.setLitappInfos(litappInfos);
                            litappListAdapter.notifyDataSetChanged();

                            litappHorListAdapter.setLitappInfos(litappInfos);
                            litappHorListAdapter.notifyDataSetChanged();

                            litapp_size.setText(litappInfos.size() + " >");
                        }
                    });

                }
            }
        });
    }


    @Override
    public void onLitappClick(LitappInfo litappInfo) {
        if (pick) {
            Intent intent = new Intent();
            intent.putExtra("litappInfo", litappInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            Intent intent = new Intent(getContext(),LitappActivity.class);
            intent.putExtra("litappInfo", litappInfo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onLitappLongClick(LitappInfo litappInfo) {
        if (!pick) {
            List<String> titles = new ArrayList<>();
            titles.add(getString(R.string.forward));
            titles.add(getString(R.string.delete));
            new MaterialDialog.Builder(getActivity()).items(titles).itemsCallback((dialog, v, position, text) -> {
                switch (position){
                    case 0:
                        Intent intent = new Intent(getActivity(), ContactListActivity.class);
                        intent.putExtra("litappInfo", litappInfo);
                        intent.putExtra("pick",true);
                        startActivity(intent);
                        break;
                    case 1:
                        //删除
                        String PREFIX = (String) SPUtils.get(getActivity(),"PREFIX","");
                        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
                        String token = sp.getString("token", null);
                        System.out.println("@@@    litappListInfo.getData().get(position).getId()= "+litappListInfo.getData().get(position).getId());

                        OkHttpClient okHttpClient = new OkHttpClient();
                        final Request request = new Request.Builder()
                                .url(PREFIX+"/im/delProgram?id="+litappListInfo.getData().get(position).getId())
                                .get()
                                .addHeader("X-Token",token)
                                .build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                System.out.println("@@@   删除小程序列表失败： "+e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String result = response.body().string();
                                System.out.println("@@@   删除小程序列表成功: "+result);
                                if(result == null || result.equals("")){
                                    return;
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        litappListAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });

                       /* ChatManager.Instance().getWorkHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                ChatManager.Instance().deleteLitapp(litappInfo.target);
                            }
                        });
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ChatManager.Instance().getLitappList(new GetLitappsCallback() {
                                    @Override
                                    public void onSuccess(List<LitappInfo> litappInfos) {
                                        if(litappInfos == null)
                                            litappInfos = new ArrayList<>();
                                        litappListAdapter.setLitappInfos(litappInfos);
                                        litappListAdapter.notifyDataSetChanged();

                                        litappHorListAdapter.setLitappInfos(litappInfos);
                                        litappHorListAdapter.notifyDataSetChanged();

                                        litapp_size.setText(litappInfos.size() + " >");
                                    }

                                    @Override
                                    public void onFail(int errorCode) {
                                    }
                                });
                            }
                        });*/
                        break;
                }
            }).show();

        }
    }
}

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.kefu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.contact.newfriend.InviteFriendActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.redpacket.RedPacketActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class KefuListFragment extends Fragment implements OnKefuItemClickListener {
    @BindView(R2.id.kefuRecyclerView)
    RecyclerView recyclerView;

    String TAG = KefuListFragment.class.getSimpleName();
    private KefuListAdapter kefuListAdapter;
    private boolean pick;

    public class KefuViewHolder extends RecyclerView.ViewHolder {
        protected Fragment fragment;
        private KefuListAdapter adapter;
        @BindView(R2.id.portraitImageView)
        ImageView portraitImageView;
        @BindView(R2.id.nameTextView)
        TextView nameTextView;
        @BindView(R2.id.categoryTextView)
        TextView categoryTextView;
        @BindView(R2.id.dividerLine)
        View dividerLine;

        UserInfo userInfo;

        public KefuViewHolder(Fragment fragment, KefuListAdapter adapter, View itemView) {
            super(itemView);
            this.fragment = fragment;
            this.adapter = adapter;
            ButterKnife.bind(this, itemView);
        }
        public void onBind(UserInfo userInfo) {
            this.userInfo = userInfo;
            categoryTextView.setVisibility(View.GONE);
            nameTextView.setText(this.userInfo.displayName);
            GlideApp.with(fragment).load(this.userInfo.portrait).placeholder(R.mipmap.default_header).into(portraitImageView);
        }

        public UserInfo getKefuInfo() {
            return userInfo;
        }
    }

    public class KefuListAdapter extends RecyclerView.Adapter<KefuViewHolder> {
        private List<UserInfo> kefuInfos;
        private Fragment fragment;

        public KefuListAdapter(Fragment fragment) {
            this.fragment = fragment;
        }
        public void setKefuInfos(List<UserInfo> kefuInfos) {
            this.kefuInfos = kefuInfos;
        }
        public List<UserInfo> getKefuInfos() {
            return kefuInfos;
        }

        @NonNull
        @Override
        public KefuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item_contact, parent, false);
            KefuViewHolder viewHolder = new KefuViewHolder(fragment, this, view);
            view.findViewById(R.id.contactLinearLayout).setOnClickListener(v -> {
                onKefuClick(viewHolder.getKefuInfo());
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull KefuViewHolder holder, int position) {
            holder.onBind(kefuInfos.get(position));
        }

        @Override
        public int getItemCount() {
            return kefuInfos == null ? 0 : kefuInfos.size();
        }
    }

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
        View view = inflater.inflate(R.layout.kefu_list_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        kefuListAdapter = new KefuListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(kefuListAdapter);
        String url_KEFU_LIST = (String) SPUtils.get(getActivity(),"KEFU_LIST","");
        OKHttpHelper.postJson(url_KEFU_LIST, "", new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try{
                    Log.d(TAG, "kefu result: "+result);
                    JSONObject json = RedPacketUtils.getData(KefuListFragment.this.getActivity(), result);
                    if(json == null)
                        return;
                    List<String> kefuList = json.getJSONArray("kf").toJavaList(String.class);
                    List<UserInfo> userInfos = new ArrayList<>();
                    for(String kefu : kefuList){
                        UserInfo userInfo = ChatManager.Instance().getUserInfo(kefu, false);
                        if(!ChatManager.Instance().isMyFriend(kefu)){
                            ContactViewModel contactViewModel = ViewModelProviders.of(getActivity()).get(ContactViewModel.class);
                            contactViewModel.invite(userInfo.uid, "")
                                    .observe(getActivity(), new Observer<Boolean>() {
                                        @Override
                                        public void onChanged(@Nullable Boolean aBoolean) {
                                            System.out.println("@@@    好友请求发送");
                                        }
                                    });
                        }
                        if(userInfo != null){
                            userInfos.add(userInfo);
                        }
                    }
                    if(!userInfos.isEmpty()){
                        kefuListAdapter.setKefuInfos(userInfos);
                        kefuListAdapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(KefuListFragment.this.getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onKefuClick(UserInfo kefuInfo) {
        if(pick){
            Intent intent = new Intent();
            intent.putExtra("kefuInfo", kefuInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
//            getActivity().finish();
        }
        else {
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
            Conversation conversation = new Conversation(Conversation.ConversationType.Single, kefuInfo.uid);
            intent.putExtra("conversation", conversation);
            startActivity(intent);
//            getActivity().finish();
        }
    }
}

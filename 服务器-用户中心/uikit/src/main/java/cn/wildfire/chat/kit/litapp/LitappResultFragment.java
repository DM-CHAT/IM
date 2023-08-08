package cn.wildfire.chat.kit.litapp;

import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.web.WebCrossGoActivity;
import cn.wildfire.chat.kit.web.WebViewActivity;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfirechat.model.LitappInfo;
import q.rorbin.badgeview.DisplayUtil;

public class LitappResultFragment extends ProgressFragment {
    private RecyclerView recyclerView;
    private LitappResultFragment.LitappResultAdapter adapter;
    String TAG = LitappResultFragment.class.getSimpleName();

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = space;
            outRect.left = space;
            outRect.right = space;
            outRect.top = space;
        }
    }
    public static class LitappResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static Fragment fragment;
        private final List<LitappInfo> litappInfoList = new ArrayList<>();
        private final int itemWidth;

        public LitappResultAdapter(LitappResultFragment litappResultFragment) {
            super();
            fragment = litappResultFragment;
            itemWidth = (fragment.getResources().getDisplayMetrics().widthPixels - DisplayUtil.dp2px(fragment.getContext(),24)) / 2;
        }
        public void addLitappInfo(LitappInfo litappInfo){
            litappInfoList.add(litappInfo);
            notifyDataSetChanged();
        }
        public void setLitappInfo(LitappInfo litappInfo){
            boolean finded = false;
            for(int i = 0; i < litappInfoList.size(); ++i){
                LitappInfo info = litappInfoList.get(i);
                if(litappInfo.target.equalsIgnoreCase(info.target)){
                    info.target = litappInfo.target;
                    info.name = litappInfo.name;
                    info.displayName = litappInfo.displayName;
                    info.portrait = litappInfo.portrait;
                    info.theme = litappInfo.theme;
                    info.url = litappInfo.url;
                    info.info = litappInfo.info;
                    finded = true;
                }
            }
            if(!finded)
                litappInfoList.add(litappInfo);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.litapp_grid_item, parent, false);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.litapp_list_item, parent, false);
            view.setOnClickListener(LitappResultFragment.LitappResultAdapter::onClick);
            //return new Holder(fragment, view, itemWidth);
            return new Holder(fragment, view);
        }

        private static void onClick(View view) {
            LitappResultFragment.LitappResultAdapter.Holder holder = (LitappResultFragment.LitappResultAdapter.Holder)view.getTag();
            Intent intent;
            if(holder.litappInfo.target == null || holder.litappInfo.target.isEmpty()){
                intent = new Intent(fragment.getContext(), WebCrossGoActivity.class);
                intent.putExtra("url", holder.litappInfo.url);
            }else {
                intent = new Intent(fragment.getContext(), LitappActivity.class);
                intent.putExtra("litappInfo", holder.litappInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            fragment.startActivity(intent);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            LitappResultFragment.LitappResultAdapter.Holder holder = (LitappResultFragment.LitappResultAdapter.Holder)viewHolder;
            if(position < litappInfoList.size()){
                holder.onBind(litappInfoList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return litappInfoList.size();
        }

        public static class Holder extends RecyclerView.ViewHolder {
            protected Fragment fragment;
            @BindView(R2.id.portraitImageView)
            ImageView portraitImageView;
            @BindView(R2.id.nameTextView)
            TextView nameTextView;

            protected LitappInfo litappInfo;

            public Holder(Fragment fragment, View itemView) {
                super(itemView);
                this.fragment = fragment;
                itemView.setTag(this);
                ButterKnife.bind(this, itemView);
            }

            // TODO hide the last diver line
            public void onBind(LitappInfo litappInfo) {
                this.litappInfo = litappInfo;
                nameTextView.setText(litappInfo.name);
                GlideApp.with(fragment).load(litappInfo.portrait).placeholder(R.mipmap.ic_channel_1).into(portraitImageView);
            }

            public LitappInfo getLitappInfo() {
                return litappInfo;
            }
        }
//        static class Holder extends RecyclerView.ViewHolder {
//            private Fragment fragment;
//            private LitappInfo litappInfo;
//
//            @BindView(R2.id.contentLayout)
//            RelativeLayout contentLayout;
//            @BindView(R2.id.userCardPortraitImageView)
//            ImageView portraitImageView;
//            @BindView(R2.id.theme)
//            ImageView theme;
//            @BindView(R2.id.userCardNameTextView)
//            TextView nameTextView;
//            @BindView(R2.id.content)
//            TextView content;
//            @BindView(R2.id.cardType)
//            TextView cardType;
//            public Holder(Fragment fragment, View itemView, int itemWidth) {
//                super(itemView);
//                this.fragment = fragment;
//                itemView.setTag(this);
//                ButterKnife.bind(this, itemView);
//
//                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
//                layoutParams.height = itemWidth*16/9;
//                layoutParams.width = itemWidth;
//                itemView.setLayoutParams(layoutParams);
//            }
//            public void onBind(LitappInfo litappInfo){
//                this.litappInfo = litappInfo;
//                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
//                nameTextView.setText(litappInfo.name);
//                content.setText(litappInfo.displayName);
//                cardType.setText(R.string.litapp_card1);
//                GlideApp
//                        .with(fragment)
//                        .load(litappInfo.portrait)
//                        .transforms(new CenterCrop(), new RoundedCorners(10))
//                        .placeholder(R.mipmap.avatar_def)
//                        .into(portraitImageView);
//                GlideApp
//                        .with(fragment)
//                        .load(litappInfo.theme)
//                        .transforms(new CenterCrop(), new RoundedCorners(10))
//                        .placeholder(R.mipmap.avatar_def)
//                        .into(theme);
//            }
//        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.litapp_grid_fragment;
    }
    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);

//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
//        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new LitappResultFragment.SpacesItemDecoration(DisplayUtil.dp2px(this.getContext(), 4)));
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                //防止第一行到顶部有空白区域
//                layoutManager.invalidateSpanAssignments();
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });

        adapter = new LitappResultFragment.LitappResultAdapter(this);
        recyclerView.setAdapter(adapter);

        LitappViewModel litappViewModel = ViewModelProviders.of(this).get(LitappViewModel.class);
        litappViewModel.litappResultLiveData().observe(this, infos -> {
            for (LitappInfo info : infos) {
                Log.d(TAG, info.target);
                //adapter.setLitappInfo(info);
                adapter.addLitappInfo(info);
            }
            showContent();
        });
    }
}

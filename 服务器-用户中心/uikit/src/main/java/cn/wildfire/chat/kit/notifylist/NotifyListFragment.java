package cn.wildfire.chat.kit.notifylist;

import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.Collections;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.conversationlist.ConversationListAdapter;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.remote.ChatManager;

public class NotifyListFragment extends ProgressFragment {
    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;
    private static final List<Conversation.ConversationType> types = Collections.singletonList(Conversation.ConversationType.Service);
    private static final List<Integer> lines = Collections.singletonList(0);

    private ConversationListViewModel conversationListViewModel;
    private LinearLayoutManager layoutManager;
    private RelativeLayout rl_title;
    private LinearLayout ll_conversation_select;

    @Override
    protected int contentLayout() {
        return R.layout.conversationlist_frament;
    }

    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        rl_title = view.findViewById(R.id.rl_title);
        ll_conversation_select = view.findViewById(R.id.ll_conversation_select);
        rl_title.setVisibility(View.GONE);
        ll_conversation_select.setVisibility(View.GONE);
        init();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null && isVisibleToUser) {
            reloadConversations();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadConversations();
    }

    private void init() {
        adapter = new ConversationListAdapter(this);
        conversationListViewModel = ViewModelProviders
                .of(getActivity(), new ConversationListViewModelFactory(types, lines))
                .get(ConversationListViewModel.class);

        ChatManager.Instance().getWorkHandler().post(() -> {
            List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(types, lines, -1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Looper.prepare();
                        showContent();
                        //Looper.loop();
                        adapter.setConversationInfos(conversationInfos);
                    } catch (Exception e) {

                    }

                }
            });
        });

        /*conversationListViewModel.conversationListLiveData().observe(this, conversationInfos -> {
            showContent();
            adapter.setConversationInfos(conversationInfos);

        });*/
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void reloadConversations() {
        conversationListViewModel.reloadConversationList();
        conversationListViewModel.reloadConversationUnreadStatus();
    }
}

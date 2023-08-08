package cn.wildfire.chat.kit.kefu;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.notifylist.NotifyListFragment;

public class KefuListActivity extends WfcBaseActivity {
    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
    @Override
    protected void afterViews() {
        KefuListFragment fragment = new KefuListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }
}

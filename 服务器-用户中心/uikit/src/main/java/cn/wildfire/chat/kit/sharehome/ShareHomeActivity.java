package cn.wildfire.chat.kit.sharehome;

import androidx.fragment.app.Fragment;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;

public class ShareHomeActivity extends WfcBaseActivity {
    @Override
    protected int contentLayout() {
        return R.layout.activity_share_home;
    }

    @Override
    protected void afterViews() {
        Fragment fragment = new ShareHomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
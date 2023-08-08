package cn.wildfire.chat.kit.litapp;

import android.os.Bundle;
import android.view.MenuItem;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;

public class LitappResultActivity  extends WfcBaseActivity {
    @Override
    protected int menu() {
        return R.menu.channel_list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        Bundle bundle = new Bundle();
        //bundle.putBoolean("pick", pick);
        LitappResultFragment fragment = new LitappResultFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }
}

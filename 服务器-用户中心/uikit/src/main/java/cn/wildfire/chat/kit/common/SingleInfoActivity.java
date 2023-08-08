package cn.wildfire.chat.kit.common;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;


public class SingleInfoActivity extends WfcBaseActivity {
    @BindView(R2.id.et_content)
    EditText et_content;

    @Override
    protected void afterViews() {
    }
    @Override
    protected int contentLayout() {
        return R.layout.activity_single_info;
    }
    @OnClick(R2.id.btn_submit)
    protected void onClick(View view) {
        String info = et_content.getText().toString();
        if (TextUtils.isEmpty(info)) {
            Toast.makeText(this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("info", info);
        setResult(0x1000, intent);
        finish();
    }
}
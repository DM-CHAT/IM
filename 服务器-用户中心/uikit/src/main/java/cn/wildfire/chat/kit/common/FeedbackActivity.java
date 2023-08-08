package cn.wildfire.chat.kit.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.utils.LoadingDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.remote.ChatManager;

public class FeedbackActivity extends WfcBaseActivity {

    @BindView(R2.id.context)
    EditText etContext;

    @Override
    protected void afterViews() {
    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_feedback;
    }

    @OnClick(R2.id.btn_submit)
    protected void onClick(View view) {

        String context = etContext.getText().toString();

        if(!TextUtils.isEmpty(context)){
            /**不准改，没文化的家伙**/
            if (context.equals("show me the money")) {
                SPUtils.put(FeedbackActivity.this,"enable2",true);
                boolean enable2 =  (boolean) SPUtils.get(FeedbackActivity.this,"enable2",false);
                System.out.println("@@@ set enable2 : " + enable2);
                ChatManager.Instance().setHideEnable2("1");
                //ChatManager.Instance().setEnable();
             //   Toast.makeText(this,"Please restart!", Toast.LENGTH_SHORT).show();
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //android.os.Process.killProcess(android.os.Process.myPid());
                //System.exit(0);
            }else if(context.equals("close money")){
                SPUtils.put(FeedbackActivity.this,"enable2",false);
                ChatManager.Instance().setHideEnable2("0");
                //ChatManager.Instance().setEnable();
                //   Toast.makeText(this,"Please restart!", Toast.LENGTH_SHORT).show();
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else if (context.equals("dark magic")) {
                SPUtils.put(FeedbackActivity.this,"enable3",true);
                ChatManager.Instance().setHideEnable3("1");
                //   Toast.makeText(this,"Please restart!", Toast.LENGTH_SHORT).show();
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else if(context.equals("take taxi")){
                SPUtils.put(FeedbackActivity.this,"takeTaxi",true);
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else if(context.equals("open crossgo")){
                SPUtils.put(FeedbackActivity.this,"openCrossgo",true);
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else if(context.equals("close crossgo")){
                SPUtils.put(FeedbackActivity.this,"openCrossgo",false);
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if(context.equals("open vpn")){
                SPUtils.put(FeedbackActivity.this,"openVPN",true);
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }else if(context.equals("close vpn")){
                SPUtils.put(FeedbackActivity.this,"openVPN",false);
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        Toast.makeText(FeedbackActivity.this, getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
        finish();

        /*if (TextUtils.isEmpty(user) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(context)) {
            Toast.makeText(this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
            LoadingDialog.hideLoading();
            return;
        }*/


        /*AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.submitting))//标题
                .setMessage(getString(R.string.submit_wait))//内容
                .setIcon(R.mipmap.ic_channel)//图标
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(FeedbackActivity.this, "", Toast.LENGTH_SHORT).show();
                    }})
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(FeedbackActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        alertDialog.show();*/



        /*SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String hostIp = sp.getString("hostip", null);
        OKHttpHelper.get("http://" + hostIp + ":8300/userFeedback", null, new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String s) {
                runOnUiThread(()->{
                    Random random = new Random();
                    int delayed = random.nextInt(3000);
                    try {
                        Thread.sleep(delayed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(FeedbackActivity.this, getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    finish();
                });
            }
            @Override
            public void onUiFailure(int code, String msg) {
                runOnUiThread(()->{
                    Toast.makeText(FeedbackActivity.this, getString(R.string.submit_failure), Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    finish();
                });
            }
        });*/
    }
}

package cn.wildfire.chat.app.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.wildfire.chat.app.utils.KeyStore;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.chat.R;

public class AccountPasswordActivity1 extends AppCompatActivity {

    private Button btn_confirm;
    private TextView tv_zhanghao,tv_userName;
    private EditText edt_old_pwd,edt_new1,edt_new2;
    private String PREFIX,token;
    private ImageView img_back;
    private String account;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_password1);
        account = (String) SPUtils.get(AccountPasswordActivity1.this,"account","");
        initView();
    }
    private void initView(){
        btn_confirm = findViewById(R.id.btn_confirm);
        tv_zhanghao = findViewById(R.id.tv_zhanghao);
        tv_userName = findViewById(R.id.tv_userName);
        edt_old_pwd = findViewById(R.id.edt_old_pwd);
        edt_new1 = findViewById(R.id.edt_new1);
        edt_new2 = findViewById(R.id.edt_new2);
        img_back = findViewById(R.id.img_back);

        tv_zhanghao.setText(getString(R.string.account)+":");
        tv_userName.setText(account);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassWord();
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void changePassWord(){
        String pwd1 = edt_new1.getText().toString();
        String pwd2 = edt_new2.getText().toString();
        String oldPwd = edt_old_pwd.getText().toString();

        if(pwd1.equals("") || pwd1 == null){
            Toast.makeText(AccountPasswordActivity1.this,getString(R.string.password_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(pwd2.equals("")|| pwd2 == null){
            Toast.makeText(AccountPasswordActivity1.this,getString(R.string.password_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(edt_old_pwd.equals("") || edt_old_pwd == null){
            Toast.makeText(AccountPasswordActivity1.this,getString(R.string.password_not_empty),Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pwd1.equals(pwd2)){
            Toast.makeText(AccountPasswordActivity1.this,getString(R.string.password_is_different),Toast.LENGTH_SHORT).show();
            return;
        }
        if(KeyStore.updatePassword(AccountPasswordActivity1.this,account,oldPwd,pwd2)){
            Toast.makeText(AccountPasswordActivity1.this,getString(R.string.modify_success),Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(AccountPasswordActivity1.this,"密码错误,修改失败",Toast.LENGTH_SHORT).show();
        }
    }
}

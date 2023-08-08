package cn.wildfire.chat.app.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.wildfire.chat.app.setting.BaiBeiWalletUtils;
import cn.wildfirechat.chat.R;

public class ImportBip39Activity extends AppCompatActivity {

    private EditText edt_mnemonic;
    private Button btn_confirm;
    private int type = 1;
    private String PREFIX;
    private String[] fromMnemonic;
    private String mnemonicLoginUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daorubip39);
        type = getIntent().getIntExtra("type",1);
        mnemonicLoginUrl = getIntent().getStringExtra("mnemonicLoginUrl");

        initView();
    }

    private void initView(){
        edt_mnemonic = findViewById(R.id.edt_mnemonic);
        btn_confirm = findViewById(R.id.btn_confirm);

        String mnemonic = BaiBeiWalletUtils.generateBip39();

        if(type ==1 ){
            edt_mnemonic.setText(mnemonic);
            edt_mnemonic.setFocusable(false);
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(ImportBip39Activity.this, PwdBit39Activity.class);
                    intent.putExtra("mnemonicLoginUrl",mnemonicLoginUrl);
                    intent.putExtra("mnemonic",mnemonic);
                    startActivity(intent);
                }
            });
        }else{
            edt_mnemonic.setFocusable(true);
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String mnemonic = edt_mnemonic.getText().toString();
                    if(mnemonic.equals("") || mnemonic == null){
                        Toast.makeText(ImportBip39Activity.this,"助记词不能为空",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int length = mnemonic.split(" ").length;
                    if(length < 12){
                        Toast.makeText(ImportBip39Activity.this,"助记词不合规",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(ImportBip39Activity.this, PwdBit39Activity.class);
                    intent.putExtra("mnemonicLoginUrl",mnemonicLoginUrl);
                    intent.putExtra("mnemonic",mnemonic);
                    startActivity(intent);
                }
            });


        }

    }
}

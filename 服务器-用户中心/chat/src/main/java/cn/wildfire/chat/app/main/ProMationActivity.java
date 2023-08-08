package cn.wildfire.chat.app.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.wildfire.chat.app.Utils;
import cn.wildfire.chat.app.login.model.ShareContentInfo;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfirechat.chat.BuildConfig;

import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.litapp.AddanApletsAdapter;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.chat.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProMationActivity extends AppCompatActivity {

    private ImageView iv_promation_zh,img_back,iv_share;
    private String img_url,copyUrl = "1";
    private RelativeLayout rl_copy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        img_back = findViewById(R.id.img_back);
        iv_promation_zh = findViewById(R.id.iv_promation_zh);
        iv_share = findViewById(R.id.iv_share);
        rl_copy = findViewById(R.id.rl_copy);
        //0中文,1越南语,2英语
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String language = sp.getString("language", "0");
        String token = sp.getString("token", "");

        //https://luckmoney8888.com/im_img/android/share_zh.png
        img_url = "https://luckmoney8888.com/im_img/android/share";
        if(language.equals("0")){
            img_url = img_url + "_zh.png";
        }else if(language.equals("1")){
            img_url = img_url + "_vn.png";
        }else{
            img_url = img_url + "_en.png";
        }
        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.default_image);
        Glide.with(ProMationActivity.this)
                .load(img_url)
                .apply(options)
                .into(iv_promation_zh);

        /*if(language.equals("0")){
            iv_promation_en.setVisibility(View.GONE);
            iv_promation_vn.setVisibility(View.GONE);
            iv_promation_zh.setVisibility(View.VISIBLE);
        }else if(language.equals("1")){
            iv_promation_en.setVisibility(View.GONE);
            iv_promation_vn.setVisibility(View.VISIBLE);
            iv_promation_zh.setVisibility(View.GONE);
        }else{
            iv_promation_en.setVisibility(View.VISIBLE);
            iv_promation_vn.setVisibility(View.GONE);
            iv_promation_zh.setVisibility(View.GONE);
        }*/


    //    getData(token,language);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //分享图片
                Bitmap bm;
                /*if(language.equals("0")){
                    bm =((BitmapDrawable) ((ImageView) iv_promation_zh).getDrawable()).getBitmap();
                }else if(language.equals("1")){
                    bm = ((BitmapDrawable) ((ImageView) iv_promation_vn).getDrawable()).getBitmap();
                }else{
                    bm = ((BitmapDrawable) ((ImageView) iv_promation_en).getDrawable()).getBitmap();
                }*/

                if (iv_promation_zh.getDrawable()==null){
                    Toast.makeText(ProMationActivity.this,getString(R.string.error_network),Toast.LENGTH_SHORT).show();
                    return;
                }
                bm =((BitmapDrawable) ((ImageView) iv_promation_zh).getDrawable()).getBitmap();
                if(bm!=null){
                    shareImage(bm);
                }
            }
        });

        rl_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //https://luckmoney8888.com/dmChat/index.html
                copyUrl = "https://luckmoney8888.com/dmChat/index.html";
                Utils.copyToClipboard(ProMationActivity.this,copyUrl);
                Toast.makeText(ProMationActivity.this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void shareImage(Bitmap bitmap) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "IMG" + Calendar.getInstance().getTime(), null));
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "title"));
    }

    private void getData(String token,String language){
        String url = (String) SPUtils.get(ProMationActivity.this,"PREFIX","");
        if (url.length()<5){
            return;
        }
        String url1 = url+BuildConfig.ShareContent;
        System.out.println("@@@      活动url  :"+url1);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url1)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   活动失败： "+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   活动成功: "+result);
                if(result == null || result.equals("")){
                    return;
                }
                Gson gson = new Gson();
                ShareContentInfo shareContentInfo = gson.fromJson(result,ShareContentInfo.class);
                if(shareContentInfo.getCode() == 200){
                    copyUrl = shareContentInfo.getData().getUrl();
                    img_url = shareContentInfo.getData().getUrl_android();
                    if(language.equals("0")){
                        img_url = img_url + "_zh.png";
                    }else if(language.equals("1")){
                        img_url = img_url + "_vn.png";
                    }else{
                        img_url = img_url + "_en.png";
                    }
                    System.out.println("@@@   活动  img_url :"+img_url);
                    RequestOptions options = new RequestOptions()
                            .placeholder(R.mipmap.default_image);
                    ProMationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(ProMationActivity.this)
                                    .load(img_url)
                                    .apply(options)
                                    .into(iv_promation_zh);
                        }
                    });

                }
            }
        });
    }
}

package cn.wildfire.chat.kit.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.third.utils.UIUtils;

public class ImageActivity extends AppCompatActivity {

    private LinearLayout ll_image;
    private ImageView iv_title;
    private String img_url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent intent = getIntent();
        img_url = intent.getStringExtra("img_url");

        initView();
    }

    private void initView(){
        ll_image = findViewById(R.id.ll_image);
        iv_title = findViewById(R.id.iv_title);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.avatar_def);
        Glide.with(this)
                .load(img_url)
                .apply(requestOptions)
                .into(iv_title);

        ll_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

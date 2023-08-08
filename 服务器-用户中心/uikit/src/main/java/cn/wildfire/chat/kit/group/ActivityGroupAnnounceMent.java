package cn.wildfire.chat.kit.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.wildfire.chat.kit.R;

public class ActivityGroupAnnounceMent extends AppCompatActivity {

    private RelativeLayout rl_back;
    private TextView announcementTextView;
    private String content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_announcement);

        Intent intent = getIntent();
        content = intent.getStringExtra("content");
        rl_back = findViewById(R.id.rl_back);
        announcementTextView = findViewById(R.id.announcementTextView);

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        announcementTextView.setText(content);

    }

}
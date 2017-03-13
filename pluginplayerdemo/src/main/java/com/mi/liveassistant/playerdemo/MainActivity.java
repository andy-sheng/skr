package com.mi.liveassistant.playerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.mi.liveassistant.player.VideoPlayerWrapperView;

public class MainActivity extends AppCompatActivity {
    private VideoPlayerWrapperView mPlayerWrapperView;
    private TextView mPlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalData.setApplication(getApplication());

        mPlayerWrapperView = (VideoPlayerWrapperView) findViewById(R.id.player_view);

        mPlayBtn = (TextView) findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerWrapperView.play("http://v2.zb.mi.com/live/21050016_1489389103.flv?playui=0");
            }
        });
    }
}

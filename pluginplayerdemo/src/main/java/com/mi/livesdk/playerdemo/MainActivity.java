package com.mi.livesdk.playerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mi.liveassistant.player.VideoPlayerWrapperView;
import com.mi.liveassistant.playerdemo.R;

public class MainActivity extends AppCompatActivity {
    private VideoPlayerWrapperView mPlayerWrapperView;
    private TextView mPlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayerWrapperView = (VideoPlayerWrapperView) findViewById(R.id.player_view);
        mPlayerWrapperView.setVideoTransMode(VideoPlayerWrapperView.TRANS_MODE_CENTER_INSIDE);

        mPlayBtn = (TextView) findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerWrapperView.play("http://v2.pandora.zb.mi.com/live/20348178_1489630909.flv?playui=1");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayerWrapperView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayerWrapperView.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayerWrapperView.release();
    }
}

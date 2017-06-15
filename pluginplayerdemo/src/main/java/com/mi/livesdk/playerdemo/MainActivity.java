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
    private TextView mMuteBtn;

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
                mPlayerWrapperView.play("http://v2.zb.mi.com/live/100415_1497519404.flv?playui=0");
            }
        });

        mMuteBtn = (TextView) findViewById(R.id.mute_btn);
        mMuteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMute = !mPlayerWrapperView.isMute();
                mPlayerWrapperView.mute(isMute);
                mMuteBtn.setText("mute:" + (isMute ? "on" : "off"));
            }
        });
        mMuteBtn.setText("mute:" + (mPlayerWrapperView.isMute() ? "on" : "off"));
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

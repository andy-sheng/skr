package com.mi.liveassistant.playerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wali.live.VideoPlayerWrapperView;

public class MainActivity extends AppCompatActivity {
    private VideoPlayerWrapperView mPlayerWrapperView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayerWrapperView = (VideoPlayerWrapperView) findViewById(R.id.player_view);
        mPlayerWrapperView.play("http://v2.zb.mi.com/live/28497027_1489370305.flv?playui=0");
    }
}

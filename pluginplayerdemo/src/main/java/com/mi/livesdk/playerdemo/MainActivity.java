package com.mi.livesdk.playerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mi.liveassistant.player.VideoPlayerWrapperView;
import com.mi.liveassistant.player.VideoPlayerWrapperView.LoadLibraryException;
import com.mi.liveassistant.playerdemo.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoPlayerWrapperView mPlayerWrapperView;
    private TextView mPlayBtn;
    private TextView mMuteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayerWrapperView = (VideoPlayerWrapperView) findViewById(R.id.player_view);
        mPlayerWrapperView.setVideoTransMode(VideoPlayerWrapperView.TRANS_MODE_CENTER_INSIDE);
        mPlayerWrapperView.setOuterCallBack(new VideoPlayerWrapperView.IOuterCallBack() {
            @Override
            public void bufferingStart() {
                Log.d(TAG, "bufferingStart");
            }

            @Override
            public void bufferingEnd() {
                Log.d(TAG, "bufferingEnd");
            }

            @Override
            public void onPrepared() {
                Log.d(TAG, "onPrepared");
            }

            @Override
            public void onError() {
                Log.d(TAG, "onError");
            }

            @Override
            public void onCompletion() {
                Log.d(TAG, "onCompletion");
            }
        });

        mPlayBtn = (TextView) findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPlayerWrapperView.play("http://v2.zb.mi.com/live/34167171_1502867226.flv?playui=0");
                } catch (LoadLibraryException e) {
                    Log.d(TAG, "load library=" + mPlayerWrapperView.checkLibrary());
                }
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

        Log.d(TAG, "load library=" + mPlayerWrapperView.checkLibrary());
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

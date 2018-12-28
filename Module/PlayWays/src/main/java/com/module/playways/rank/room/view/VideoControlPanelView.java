package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;

import com.common.view.ex.ExButton;
import com.engine.EngineManager;
import com.module.rank.R;
import com.xw.repo.BubbleSeekBar;

public class VideoControlPanelView extends ScrollView {
    public final static String TAG = "MixControlPanelView";
    public final static int UPDATE_MUSIC_PROGRESS = 100;

    ExButton mSwitchCameraBtn;
    CheckBox mCameraTorchOn;
    CheckBox mMuteAllRemoteVideo;
    CheckBox mMuteSelfVideo;

    public VideoControlPanelView(Context context) {
        super(context);
        init();
    }

    public VideoControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private ViewTreeObserver.OnScrollChangedListener scrollListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            correct(VideoControlPanelView.this);
        }
    };

    void correct(View view) {
        // 矫正气泡
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                correct(child);
            }
        } else if (view instanceof BubbleSeekBar) {
            ((BubbleSeekBar) view).correctOffsetWhenContainerOnScrolling();
        }
    }

    void init() {
        inflate(getContext(), R.layout.video_control_panel_layout, this);

        this.getViewTreeObserver().addOnScrollChangedListener(scrollListener);

        mSwitchCameraBtn = (ExButton) this.findViewById(R.id.switch_camera_btn);
        mSwitchCameraBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EngineManager.getInstance().switchCamera();
            }
        });

        mCameraTorchOn = (CheckBox) this.findViewById(R.id.camera_torch_on);
        mCameraTorchOn.setChecked(EngineManager.getInstance().getParams().getCameraTorchOn());
        mCameraTorchOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EngineManager.getInstance().setCameraTorchOn(b);
            }
        });

        mMuteAllRemoteVideo = (CheckBox) this.findViewById(R.id.mute_all_remote_video);
        mMuteAllRemoteVideo.setChecked(EngineManager.getInstance().getParams().getAllRemoteVideoStreamsMute());
        mMuteAllRemoteVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EngineManager.getInstance().muteAllRemoteVideoStreams(b);
            }
        });

        mMuteSelfVideo = (CheckBox) this.findViewById(R.id.mute_self_video);
        mMuteSelfVideo.setChecked(EngineManager.getInstance().getParams().getLocalVideoStreamMute());
        mMuteSelfVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EngineManager.getInstance().muteLocalVideoStream(b);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
    }
}

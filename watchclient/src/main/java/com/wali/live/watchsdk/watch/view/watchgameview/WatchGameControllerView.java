package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_BRIGHTNESS;
import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_DEFAULT;
import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_VOLUME;

public class WatchGameControllerView extends RelativeLayout {

    RelativeLayout rootView;
    ImageView mImageView;
    ProgressBar mProgressBar;

    public WatchGameControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void init(Context context) {
        inflate(context, R.layout.watch_game_controller_pop_layout, this);

        rootView = (RelativeLayout) findViewById(R.id.controller_pop_root);
        mImageView = (ImageView) findViewById(R.id.image);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.WatchGameControllChangeEvent event) {
        if (event == null) {
            return;
        }

        adjust(event.type, event.percent, event.isShow);
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if(event == null) {
            return;
        }

        if(!event.isLandscape()) {
            rootView.setVisibility(GONE);
        }
    }

    private void adjust(int type, float percent, boolean isShow) {
        switch (type) {
            case WATCH_GAME_CONTROLL_VOLUME:
                if (isShow) {
                    rootView.setVisibility(VISIBLE);
                    if (percent != 0){
                        mImageView.setImageResource(R.drawable.live_video_fullscreen_control_icon_sound);
                    }else {
                        mImageView.setImageResource(R.drawable.live_video_fullscreen_control_icon_mute);
                    }
                    mProgressBar.setProgress((int) (percent * 100));
                }
                break;
            case WATCH_GAME_CONTROLL_BRIGHTNESS:
                if (isShow) {
                    rootView.setVisibility(VISIBLE);
                    mImageView.setImageResource(R.drawable.live_video_fullscreen_control_icon_brightness);
                    mProgressBar.setProgress((int) (percent * 100));
                }
                break;
            case WATCH_GAME_CONTROLL_DEFAULT:
                if (isShow) {
                    rootView.setVisibility(VISIBLE);
                } else {
                    rootView.setVisibility(GONE);
                }
                break;
            default:
                break;
        }

    }
}

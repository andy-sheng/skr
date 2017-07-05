package com.wali.live.video.widget.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.wali.live.proto.HotSpotProto;

import java.util.List;

/**
 * 播放器
 * Created by yurui on 16-10-18.
 *
 * @module feeds
 */
public abstract class VideoPlayBaseSeekBar extends RelativeLayout implements View.OnClickListener {

    protected static final int CLICK_TAG_PLAY = 1001;
    protected static final int CLICK_TAG_FULLSCREEN = 1002;

    protected VideoPlaySeekBarListener mListener;

    public VideoPlayBaseSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, getLayoutResId(context, attrs), this);
        //lib module不能使用butterknife了
        //ButterKnife.bind(this);
        initView();
    }

    protected abstract int getLayoutResId(Context context, AttributeSet attrs);

    protected abstract void initView();

    public abstract void showOrHideFullScreenBtn(boolean isShow);

    public abstract void showOrHidePlayBtn(boolean isShow);

    public abstract void setPlayBtnSelected(boolean playing);

    public abstract void setProgress(long now, long total, boolean updateProgress);

    public abstract long getMax();

    public abstract void setVideoPlaySeekBarListener(VideoPlaySeekBarListener listener);

    public interface VideoPlaySeekBarListener extends SeekBar.OnSeekBarChangeListener {
        void onClickPlayBtn();

        void onClickFullScreenBtn(boolean smallScreen);
    }

    //热点相关方法
    public abstract void addPoints(long point);

    public abstract void setHotSpotInfoList(List<HotSpotProto.HotSpotInfo> list);

    public abstract void setFullscreen(boolean isFullScreen);
}


package com.wali.live.video.widget.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.live.module.common.R;
import com.wali.live.proto.HotSpotProto;
import com.wali.live.utils.ItemDataCommonFormatUtils;

import java.util.List;

/**
 * 播放器
 * Created by yurui on 16-10-18.
 *
 * @module feeds
 */
public class FeedsSeekBar extends VideoPlayBaseSeekBar{

    protected SeekBar mSeekBar;

    protected View mPlayBtn;

    protected View mFullScreenBtn;

    protected TextView mCurTimeTv;
    protected TextView mTotalTimeTv;


    public FeedsSeekBar(Context context) {
        this(context, null);
    }

    public FeedsSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.feeds_seekbar_container;
    }

    @Override
    protected void initView() {
        mSeekBar = (SeekBar)findViewById(R.id.seek_bar);
        mPlayBtn = findViewById(R.id.play_btn);
        mFullScreenBtn = findViewById(R.id.full_screen_btn);
        mCurTimeTv = (TextView)findViewById(R.id.cur_time_tv);
        mTotalTimeTv = (TextView)findViewById(R.id.total_time_tv);
        mPlayBtn.setOnClickListener(this);
        mPlayBtn.setTag(CLICK_TAG_PLAY);
        mFullScreenBtn.setOnClickListener(this);
        mFullScreenBtn.setTag(CLICK_TAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() == null || !(view.getTag() instanceof Integer)) {
            return;
        }
        switch ((int)view.getTag()) {
            case CLICK_TAG_PLAY:
                if (mListener != null) {
                    mListener.onClickPlayBtn();
                }
                break;
            case CLICK_TAG_FULLSCREEN:
                if (mListener != null) {
                    mListener.onClickFullScreenBtn();
                }
                break;
        }
    }

    @Override
    public void showOrHideFullScreenBtn(boolean isShow) {
        mFullScreenBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showOrHidePlayBtn(boolean isShow) {
        mPlayBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPlayBtnSelected(boolean playing) {
        mPlayBtn.setSelected(playing);
    }

    @Override
    public void setProgress(long now, long total, boolean updateProgress) {
        if (total > 0 && now >= 0) {
            mCurTimeTv.setText(ItemDataCommonFormatUtils.formatVideodisplayTime(now));
            mTotalTimeTv.setText(ItemDataCommonFormatUtils.formatVideodisplayTime(total));
            if (updateProgress) {
                mSeekBar.setProgress((int) (100.0 * now / total));
            }
        }
    }

    @Override
    public long getMax() {
        return mSeekBar.getMax();
    }

    @Override
    public void setVideoPlaySeekBarListener(VideoPlaySeekBarListener listener) {
        mListener = listener;
        mSeekBar.setOnSeekBarChangeListener(mListener);
    }

    @Override
    public void addPoints(long point) {
        //do nothing
    }

    @Override
    public void setHotSpotInfoList(List<HotSpotProto.HotSpotInfo> list) {
        //do nothing
    }
}


package com.wali.live.video.widget.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.base.view.RotatedSeekBar;
import com.live.module.common.R;
import com.wali.live.proto.HotSpotProto;
import com.wali.live.utils.ItemDataFormatUtils;

import java.util.List;

/**
 * 播放器
 * Created by yurui on 16-10-18.
 *
 * @module feeds
 */
public class DetailSeekBar extends VideoPlayBaseSeekBar {

    protected HotSpotSeekBar mSeekBar;

    protected View mPlayBtn;

    protected View mFullScreenBtn;

    protected TextView mCurTimeTv;
    protected TextView mTotalTimeTv;

    protected RotatedSeekBar.OnRotatedSeekBarChangeListener mRotatedSeekBarChangeListener = new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
        @Override
        public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
            float curProgress = rotatedSeekBar.getPercent();
            float maxProgress = rotatedSeekBar.getMaxPercent();
            if (curProgress < 0) {
                curProgress = 0;
            }
            if (curProgress > maxProgress) {
                curProgress = maxProgress;
            }
            if (mListener != null) {
                mListener.onProgressChanged(null, (int) (curProgress * 100f / maxProgress), fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {
            if (mListener != null) {
                mListener.onStartTrackingTouch(null);
            }
        }


        @Override
        public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {
            if (mListener != null) {
                mListener.onStopTrackingTouch(null);
            }
        }
    };


    public DetailSeekBar(Context context) {
        this(context, null);
    }

    public DetailSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.detail_seekbar_container;
    }

    @Override
    protected void initView() {
        mSeekBar = (HotSpotSeekBar) findViewById(R.id.seek_bar);
        mPlayBtn = findViewById(R.id.play_btn);
        mFullScreenBtn = findViewById(R.id.full_screen_btn);
        mCurTimeTv = (TextView) findViewById(R.id.cur_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
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
        switch ((int) view.getTag()) {
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
            mSeekBar.setTotalDuration(total);
            mCurTimeTv.setText(ItemDataFormatUtils.formatVideodisplayTime(now));
            mTotalTimeTv.setText(ItemDataFormatUtils.formatVideodisplayTime(total));
            if (updateProgress) {
                mSeekBar.setPercent(now * 1f / total);
            }
        }
    }

    @Override
    public long getMax() {
        return 100;
    }

    @Override
    public void setVideoPlaySeekBarListener(VideoPlaySeekBarListener listener) {
        mListener = listener;
        mSeekBar.setOnRotatedSeekBarChangeListener(mRotatedSeekBarChangeListener);
    }

    @Override
    public void addPoints(long point) {
        mSeekBar.addPoints(point);
    }

    @Override
    public void setHotSpotInfoList(List<HotSpotProto.HotSpotInfo> list) {
        mSeekBar.setHotSpotInfoList(list);
    }
}


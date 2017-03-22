package com.wali.live.livesdk.live.liveshow.presenter.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.base.view.RotatedSeekBar;
import com.wali.live.livesdk.R;

public class VolumeAdjuster implements View.OnClickListener {
    private static final String TAG = "VolumeAdjuster";

    private IAdjusterListener mListener;
    private int mSavedVolume = 50;
    private int mCurrVolume = 50;

    private ViewGroup mViewGroup;
    private View mMinimizeBtn;
    private View mMaximizeBtn;
    private RotatedSeekBar mSeekBar;

    private <T extends View> T $(@IdRes int resId) {
        return (T) mViewGroup.findViewById(resId);
    }

    private void $click(View view, View.OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.minimize_btn) {
            onMinimizeVoice(!v.isSelected());
        } else if (id == R.id.maximize_btn) {
            onMaximizeVoice();
        }
    }

    public VolumeAdjuster(IAdjusterListener listener) {
        mListener = listener;
    }

    public void setVolume(int volume) {
        mCurrVolume = volume;
        if (mSeekBar != null) {
            mSeekBar.setPercent(mCurrVolume / 100f);
        }
    }

    public void setup(@NonNull ViewGroup viewGroup, @IntRange(from = 0, to = 100) int volume) {
        mCurrVolume = mSavedVolume = volume;
        setup(viewGroup);
    }

    public void setup(@NonNull ViewGroup viewGroup) {
        if (mViewGroup != null && mViewGroup != viewGroup) {
            reset();
        }
        mViewGroup = viewGroup;

        mSeekBar = $(R.id.volume_seek_bar);
        mMinimizeBtn = $(R.id.minimize_btn);
        mMaximizeBtn = $(R.id.maximize_btn);
        $click(mMinimizeBtn, this);
        $click(mMaximizeBtn, this);

        mSeekBar.setPercent(mCurrVolume / 100f);
        mSeekBar.setOnRotatedSeekBarChangeListener(new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {
            }

            @Override
            public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {
                mCurrVolume = (int) (rotatedSeekBar.getPercent() * 100);
                if (mCurrVolume != 0) {
                    mSavedVolume = mCurrVolume;
                }
                onChangeVolume(mCurrVolume);
            }
        });
    }

    public void reset() {
        if (mSeekBar != null) {
            mSeekBar.setOnRotatedSeekBarChangeListener(null);
            mSeekBar = null;
        }
        if (mMinimizeBtn != null) {
            mMinimizeBtn.setTag(null);
            mMinimizeBtn.setOnClickListener(null);
            mMinimizeBtn = null;
        }
        if (mMaximizeBtn != null) {
            mMaximizeBtn.setTag(null);
            mMaximizeBtn.setOnClickListener(null);
            mMaximizeBtn = null;
        }
        mViewGroup = null;
    }

    private void onMinimizeVoice(boolean state) {
        MyLog.w(TAG, "onMinimizeVoice state=" + state);
        if (mListener != null) {
            mListener.onMinimizeBtn(state);
        }
        mCurrVolume = state ? 0 : mSavedVolume;
        mSeekBar.setPercent(mCurrVolume / 100f);
        onChangeVolume(mCurrVolume);
    }

    private void onMaximizeVoice() {
        MyLog.w(TAG, "onMaximizeVoice");
        if (mListener != null) {
            mListener.onMaximizeBtn();
        }
        mSavedVolume = (int) (mSeekBar.getMaxPercent() * 100);
        mCurrVolume = mSavedVolume;
        mSeekBar.setPercent(mSeekBar.getMaxPercent());
        onChangeVolume(mCurrVolume);
    }

    private void onChangeVolume(int volume) {
        mMinimizeBtn.setSelected(volume == 0 ? true : false);
        if (mListener != null) {
            mListener.onChangeVolume(volume);
        }
    }

    public interface IAdjusterListener {

        void onMinimizeBtn(boolean isSelected);

        void onMaximizeBtn();

        void onChangeVolume(@IntRange(from = 0, to = 100) int volume);

    }

    public static class AdjusterWrapper implements IAdjusterListener {

        @Override
        public void onMinimizeBtn(boolean isSelected) {
        }

        @Override
        public void onMaximizeBtn() {
        }

        @Override
        public void onChangeVolume(@IntRange(from = 0, to = 100) int volume) {
        }
    }
}
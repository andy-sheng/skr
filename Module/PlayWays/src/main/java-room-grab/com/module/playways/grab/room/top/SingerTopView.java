package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.event.SomeOneLightBurstEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SingerTopView extends RelativeLayout {
    ImageView mIvLight;
    ExTextView mTvCurLight;
    ExTextView mTvCountDown;
    ExTextView mTvTotalLight;
    GrabRoundInfoModel mCurGrabRoundInfoModel;
    AnimationDrawable mFlickerAnim;

    HandlerTaskTimer mCountDownTask;

    public SingerTopView(Context context) {
        super(context);
        init();
    }

    public SingerTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingerTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_singer_top_view, this);
        mIvLight = (ImageView) findViewById(R.id.iv_light);
        mTvCurLight = (ExTextView) findViewById(R.id.tv_cur_light);
        mTvCountDown = (ExTextView) findViewById(R.id.tv_count_down);
        mTvTotalLight = (ExTextView) findViewById(R.id.tv_total_light);
    }

    private void startSing(SongModel songModel) {
        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder()
                .take(songModel.getTotalMs() / 1000)
                .interval(1000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int remainTime = songModel.getTotalMs() / 1000 - integer;
                        if (remainTime < 0) {
                            mTvCountDown.setText(remainTime);
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightOffEvent event) {
        if (mCurGrabRoundInfoModel != null && mCurGrabRoundInfoModel.getRoundSeq() == event.getRoundInfo().getRoundSeq()) {
            mTvCurLight.setText(mCurGrabRoundInfoModel.getPlayUsers().size() - mCurGrabRoundInfoModel.getMLightInfos().size());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightBurstEvent event) {
        playFlickerAnim();
    }

    private void playFlickerAnim() {
        stopFlickerAnim();
        mFlickerAnim = new AnimationDrawable();
        mFlickerAnim.setOneShot(false);
        Drawable drawable = U.getDrawable(R.drawable.liangdeng_shan);
        mFlickerAnim.addFrame(drawable, 100);
        drawable = U.getDrawable(R.drawable.liangdeng);
        mFlickerAnim.addFrame(drawable, 100);
        mIvLight.setImageDrawable(mFlickerAnim);
        mFlickerAnim.start();
    }

    private void stopFlickerAnim() {
        if (mFlickerAnim != null) {
            mFlickerAnim.stop();
        }
    }

    private void cancelCountDownTask() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == GONE){
            cancelCountDownTask();
            stopFlickerAnim();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCountDownTask();
        stopFlickerAnim();
    }

    public void startSelfShow(GrabRoundInfoModel grabRoundInfoModel) {
        cancelCountDownTask();
        stopFlickerAnim();
        mIvLight.setImageDrawable(U.getDrawable(R.drawable.liangdeng));
        mCurGrabRoundInfoModel = grabRoundInfoModel;
        mTvTotalLight.setText("/" + mCurGrabRoundInfoModel.getPlayUsers().size());
        mTvCurLight.setText(mCurGrabRoundInfoModel.getPlayUsers().size() - grabRoundInfoModel.getMLightInfos().size());
        startSing(grabRoundInfoModel.getSongModel());
    }
}

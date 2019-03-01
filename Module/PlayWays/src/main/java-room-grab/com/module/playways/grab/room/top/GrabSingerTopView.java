package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.SomeOneLightBurstEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.room.view.MoreOpView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GrabSingerTopView extends FrameLayout {
    ImageView mIvLight;
    ExTextView mTvCurLight;
    ExTextView mTvCountDown;
    ExTextView mTvTotalLight;
    AnimationDrawable mFlickerAnim;
    ImageView mMoreBtn;
    MoreOpView mMoreOpView;
    GrabTopContainerView.Listener mListener;
    GrabRoomData mRoomData;

    HandlerTaskTimer mCountDownTask;

    public GrabSingerTopView(Context context) {
        super(context);
        init();
    }

    public GrabSingerTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabSingerTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_singer_top_view, this);
        mIvLight = (ImageView) findViewById(R.id.iv_light);
        mTvCurLight = (ExTextView) findViewById(R.id.tv_cur_light);
        mTvCountDown = (ExTextView) findViewById(R.id.tv_count_down);
        mTvTotalLight = (ExTextView) findViewById(R.id.tv_total_light);
        mMoreBtn = (ImageView) findViewById(R.id.iv_more);

        mMoreBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    public void setListener(GrabTopContainerView.Listener l) {
        mListener = l;
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    private void startSing(SongModel songModel) {
        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder()
                .take(songModel.getTotalMs() == 0 ? 12000 / 1000 : songModel.getTotalMs())
                .interval(1000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int remainTime = (songModel.getTotalMs() == 0 ? 12000 : songModel.getTotalMs()) - integer * 1000;
                        if (remainTime >= 0) {
                            mTvCountDown.setText(U.getDateTimeUtils().formatTimeStringForDate(remainTime, "mm:ss"));
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightOffEvent event) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        int num = grabRoundInfoModel.getPlayUsers().size() - grabRoundInfoModel.getMLightInfos().size();
        mTvCurLight.setText(String.valueOf(num));
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
        if (visibility == GONE) {
            cancelCountDownTask();
            stopFlickerAnim();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCountDownTask();
        stopFlickerAnim();
        EventBus.getDefault().unregister(this);
    }

    public void startSelfShow() {
        cancelCountDownTask();
        stopFlickerAnim();
        mIvLight.setImageDrawable(U.getDrawable(R.drawable.liangdeng));
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        mTvTotalLight.setText("/" + grabRoundInfoModel.getPlayUsers().size());
        int num = grabRoundInfoModel.getPlayUsers().size() - grabRoundInfoModel.getMLightInfos().size();
        mTvCurLight.setText(String.valueOf(num));
        startSing(grabRoundInfoModel.getMusic());
    }
}

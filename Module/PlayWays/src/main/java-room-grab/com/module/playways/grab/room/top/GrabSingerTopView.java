package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.room.view.MoreOpView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GrabSingerTopView extends FrameLayout {
    public final static String TAG = "GrabSingerTopView";
    public static final int MSG_SHOW = 0;
    public static final int MSG_HIDE = 1;
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

    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW:
                    setVisibility(VISIBLE);
                    break;
                case MSG_HIDE:
                    setVisibility(GONE);
                    break;
            }
        }
    };

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

                        @Override
                        public void onClickGameRule() {

                        }

                        @Override
                        public void onClickVoiceAudition() {

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
        int total = (songModel.getTotalMs()/1000);
        mCountDownTask = HandlerTaskTimer.newBuilder()
                .take(total)
                .interval(1000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int remainTime = (total - integer) * 1000;
                        if (remainTime >= 0) {
                            mTvCountDown.setText(U.getDateTimeUtils().formatTimeStringForDate(remainTime, "mm:ss"));
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightOffEvent event) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            int num = (grabRoundInfoModel.getPlayUsers().size() - 1) - grabRoundInfoModel.getMLightInfos().size();
            mTvCurLight.setText(String.valueOf(num));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        playFlickerAnim();
    }

    private void playFlickerAnim() {
        stopFlickerAnim();
        mFlickerAnim = new AnimationDrawable();
        mFlickerAnim.setOneShot(true);
        Drawable drawable = null;
        drawable = U.getDrawable(R.drawable.liangdeng);
        mFlickerAnim.addFrame(drawable, 1800);
        drawable = U.getDrawable(R.drawable.liangdeng_shan);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.liangdeng);
        mFlickerAnim.addFrame(drawable, 300);
        drawable = U.getDrawable(R.drawable.liangdeng_shan);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.liangdeng);
        mFlickerAnim.addFrame(drawable, 300);
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
        mUiHandler.removeCallbacksAndMessages(null);
        super.setVisibility(visibility);
        if (visibility == GONE) {
            cancelCountDownTask();
            stopFlickerAnim();
        }
    }

    public void showWithDelay(long delay){
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(MSG_SHOW), delay);
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
        if(grabRoundInfoModel == null || grabRoundInfoModel.getPlayUsers() == null || grabRoundInfoModel.getPlayUsers().size() == 0){
            MyLog.d(TAG, "startSelfShow grabRoundInfoModel data error");
            return;
        }

        mTvTotalLight.setText("/" + (grabRoundInfoModel.getPlayUsers().size() - 1));
        int num = (grabRoundInfoModel.getPlayUsers().size() - 1)- grabRoundInfoModel.getMLightInfos().size() ;
        mTvCurLight.setText(String.valueOf(num));
        startSing(grabRoundInfoModel.getMusic());
    }
}

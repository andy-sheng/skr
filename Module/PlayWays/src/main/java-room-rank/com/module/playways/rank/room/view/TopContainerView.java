package com.module.playways.rank.room.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.score.bar.ScoreProgressBarWithSvga;
import com.module.rank.R;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.rank.room.score.bar.ScorePrograssBar2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TopContainerView extends RelativeLayout {
    public final static String TAG = "TopContainerView";
    ExImageView mMoreBtn;
    ScoreProgressBarWithSvga mScoreProgressBar;
    BaseImageView mAvatarIv;
    MoreOpView mMoreOpView;
    ExTextView mTvPassedTime;
    Listener mListener;
    RoomData mRoomData;

    HandlerTaskTimer mShowLastedTimeTask;

    public TopContainerView(Context context) {
        super(context);
        init();
    }

    public TopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.top_container_view_layout, this);
        mAvatarIv = this.findViewById(R.id.avatar_iv);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mScoreProgressBar = this.findViewById(R.id.score_progress_bar);
        mTvPassedTime =  this.findViewById(R.id.tv_passed_time);

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        initSparkPrograssBar();
        loadAvatar(AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                .setCircle(true)
                .build());
    }

    private void initSparkPrograssBar() {
        mScoreProgressBar.setProgress1(100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScoreProgressBar.setProgress1(90);
            }
        }, 4000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        cancelShowLastedTimeTask();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        if (event.show) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    public void loadAvatar(AvatarUtils.LoadParams params) {
        params.setCircle(true);
        AvatarUtils.loadAvatarByUrl(mAvatarIv, params);
    }

    public void setScoreProgress(int progress) {
        mScoreProgressBar.setProgress1(progress);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    public void startPlayLeftTime(long wholeTile) {
        if (mShowLastedTimeTask != null) {
            mShowLastedTimeTask.dispose();
        }

        long lastedTime = wholeTile / 1000;

        MyLog.d(TAG, "showLastedTime" + " lastedTime=" + lastedTime);

        mShowLastedTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take((int) lastedTime + 1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        long lastTime = lastedTime + 1 - integer;
                        if (lastTime < 0) {
                            cancelShowLastedTimeTask();
                            mTvPassedTime.setText("");
                            mScoreProgressBar.setProgress2(0);
                            return;
                        }
                        int p = (int) ((lastedTime+1 - integer) * 100 / lastedTime);
                        mScoreProgressBar.setProgress2(p);
                        mTvPassedTime.setText(U.getDateTimeUtils().formatTimeStringForDate(lastTime * 1000, "mm:ss"));
                    }
                });
    }

    public void cancelShowLastedTimeTask() {
        mTvPassedTime.setText("");
        if (mShowLastedTimeTask != null) {
            mShowLastedTimeTask.dispose();
            mShowLastedTimeTask = null;
        }
    }

    public interface Listener {
        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);
    }
}

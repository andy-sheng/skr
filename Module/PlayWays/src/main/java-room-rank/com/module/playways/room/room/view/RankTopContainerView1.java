package com.module.playways.room.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.BaseRoomData;
import com.module.playways.room.room.score.bar.ScoreProgressBarWithSvga;
import com.module.playways.room.room.score.bar.ScoreTipsView;
import com.module.rank.R;
import com.module.playways.room.room.event.InputBoardEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class RankTopContainerView1 extends RelativeLayout {
    public final static String TAG = "TopContainerView";
    ExImageView mMoreBtn;
    ScoreProgressBarWithSvga mScoreProgressBar;
    //    BaseImageView mAvatarIv;
    MoreOpView mMoreOpView;
    ExTextView mTvPassedTime;

    Listener mListener;
    BaseRoomData mRoomData;

    HandlerTaskTimer mShowLastedTimeTask;

    ScoreTipsView.Item mLastItem;

    public RankTopContainerView1(Context context) {
        super(context);
        init();
    }

    public RankTopContainerView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.top_container_view_layout, this);
//        mAvatarIv = this.findViewById(R.id.avatar_iv);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mScoreProgressBar = this.findViewById(R.id.score_progress_bar);
        mTvPassedTime = this.findViewById(R.id.tv_passed_time);

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

                        @Override
                        public void onClickGameRule() {

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
//        setScoreProgress(100);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setScoreProgress(90);
//            }
//        }, 4000);
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
//        if (event.show) {
//            setVisibility(GONE);
//        } else {
//            setVisibility(VISIBLE);
//        }
    }

    public void loadAvatar(AvatarUtils.LoadParams params) {
//        params.setCircle(true);
//        AvatarUtils.loadAvatarByUrl(mAvatarIv, params);
    }

    public void setScoreProgress(int progress) {
        for (int i = 0; i < 1; i++) {
            progress = (int) (Math.sqrt(progress) * 10);
        }

        mScoreProgressBar.setProgress1(progress);
        ScoreTipsView.Item item = new ScoreTipsView.Item();
        if (progress >= 95) {
            item.setLevel(ScoreTipsView.Level.Perfect);
        } else if (progress >= 90) {
            item.setLevel(ScoreTipsView.Level.Good);
        } else if (progress >= 70) {
            item.setLevel(ScoreTipsView.Level.Ok);
        } else if (progress < 20) {
            item.setLevel(ScoreTipsView.Level.Bad);
        }
        if (item.getLevel() != null) {
            if (mLastItem != null && item.getLevel() == mLastItem.getLevel()) {
                item.setNum(mLastItem.getNum() + 1);
            }
            mLastItem = item;
            ScoreTipsView.play(this, item);
        }
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

    public void startPlayLeftTime(long wholeTile) {
        if (mShowLastedTimeTask != null) {
            mShowLastedTimeTask.dispose();
        }

        long lastedTime = wholeTile / 1000;

        MyLog.d(TAG, "showLastedTime" + " lastedTime=" + lastedTime);
        mTvPassedTime.setVisibility(VISIBLE);
        mShowLastedTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take((int) lastedTime)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        long lastTime = lastedTime - integer;
                        int p = (int) ((lastedTime - integer) * 100 / lastedTime);
                        mScoreProgressBar.setProgress2(p);
                        mTvPassedTime.setText(lastTime + "s");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        reset("onComplete");
                    }
                });
    }

    void reset(String from) {
        MyLog.d(TAG, "reset" + " from=" + from);
        mTvPassedTime.setText("");
        mScoreProgressBar.setProgress1(0);
        mScoreProgressBar.setProgress2(0);
        mLastItem = null;
    }

    public void onGameFinish() {
        if (mMoreOpView != null) {
            mMoreOpView.dismiss();
        }
    }

    public void cancelShowLastedTimeTask() {
        reset("cancelShowLastedTimeTask");
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

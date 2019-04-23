package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.component.busilib.view.BitmapTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.pk.view.PKSingCardView;
import com.module.rank.R;
import com.zq.live.proto.Room.EQRoundStatus;

import java.util.List;


/**
 * 别人唱歌PK时，自己看到的板子
 */
public class PKOthersSingCardView extends RelativeLayout {
    public final static String TAG = "PKOthersSingCardView";

    final static int MSG_ENSURE_PLAY = 1;

    final static int COUNT_DOWN_STATUS_INIT = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_INIT;

    PKSingCardView mPkCardView;
    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    boolean mHasPlayFullAnimation = false;
    boolean mCanStartFlag = false;

    HandlerTaskTimer mCounDownTask;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ENSURE_PLAY) {
                tryStartCountDown();
            }
        }
    };

    public PKOthersSingCardView(Context context) {
        super(context);
        init();
    }

    public PKOthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKOthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_other_sing_card_layout, this);
        mPkCardView = (PKSingCardView) findViewById(R.id.pk_card_view);
        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) findViewById(R.id.count_down_tv);
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
        mPkCardView.setRoomData(roomData);
    }

    public void bindData() {
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mGrabRoomData.getUserInfo(list.get(0).getUserID());
            mRightUserInfoModel = mGrabRoomData.getUserInfo(list.get(1).getUserID());
        }
        if (mLeftUserInfoModel != null && mRightUserInfoModel != null) {
            mHasPlayFullAnimation = false;
            mUiHandler.removeCallbacksAndMessages(null);
            setVisibility(VISIBLE);
            // 绑定数据
            mPkCardView.bindData();
            if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                // pk第一个人唱
                animationGo();
            } else if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                if (mRightUserInfoModel != null) {
                    playAnimation(mRightUserInfoModel.getUserId());
                }
            }
        }
    }

    private void playAnimation(int userId) {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        mCountDownTv.setText(totalMs + "");

        mPkCardView.playScaleAnimation(userId, true, new PKSingCardView.AnimationListerner() {
            @Override
            public void onNoSVGAAnimationEnd() {
                // 开始倒计时
                mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
                mCircleCountDownView.setProgress(0);

                GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
                if (grabRoundInfoModel == null) {
                    return;
                }

                if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.isEnterInSingStatus()) {
                    countDown("中途进来");
                } else {
                    mCircleCountDownView.cancelAnim();
                    mCircleCountDownView.setMax(360);
                    mCircleCountDownView.setProgress(0);
                    mUiHandler.removeMessages(MSG_ENSURE_PLAY);
                    mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 3000);
                }
            }
        });
    }

    // pk 他人的为什么有动画
    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        mEnterTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO: 2019/4/23 先播放左边的动画，后面都是一体的
                if (mLeftUserInfoModel != null) {
                    playAnimation(mLeftUserInfoModel.getUserId());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    destoryAnimation();
                    stopCounDown();
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            destoryAnimation();
            stopCounDown();
            clearAnimation();
            setVisibility(GONE);
        }
    }

    public void tryStartCountDown() {
        MyLog.d(TAG, "tryStartCountDown");
        mCanStartFlag = true;
        mUiHandler.removeMessages(MSG_ENSURE_PLAY);
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDownAfterAnimation("tryStartCountDown");
        }
    }

    //给所有倒计时加上一个前面到满的动画
    private void countDownAfterAnimation(String from) {
        MyLog.d(TAG, "countDownAfterAnimation from=" + from);
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }

        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
            countDown("中途进来");
        } else {
            countDown("else full Animation");
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            MyLog.d(TAG, "countDown mCountDownStatus == COUNT_DOWN_STATUS_WAIT");
            if (mCanStartFlag) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            } else {
                // 不需要播放countdown
//                mCountDownProcess.startCountDown(0, totalMs);
                mCircleCountDownView.go(0, totalMs);
                startCountDownText(totalMs / 1000);
                return;
            }
        }

        int progress;  //当前进度条
        int leaveTime; //剩余时间
        MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant() + " enterStatus=" + infoModel.getEnterStatus());
        if (!infoModel.isParticipant() && infoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = infoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - infoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
//        mCountDownProcess.startCountDown(progress, leaveTime);
        mCircleCountDownView.go(progress, leaveTime);
        startCountDownText(leaveTime / 1000);
    }

    private void startCountDownText(int counDown) {
        mCounDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(counDown)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mCountDownTv.setText((counDown - integer) + "");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        stopCounDown();
                    }
                });
    }

    private void stopCounDown() {
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destoryAnimation();
        stopCounDown();
    }

    private void destoryAnimation() {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
    }
}

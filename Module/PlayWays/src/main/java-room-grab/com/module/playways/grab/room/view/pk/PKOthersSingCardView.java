package com.module.playways.grab.room.view.pk;


import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.common.view.ExViewStub;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.pk.view.PKSingCardView;
import com.module.playways.R;
import com.component.live.proto.Room.EQRoundStatus;

import java.util.List;


/**
 * 别人唱歌PK时，自己看到的板子
 */
public class PKOthersSingCardView extends ExViewStub {
    public final String TAG = "PKOthersSingCardView";

    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_WAIT;

    PKSingCardView mPkCardView;
    SingCountDownView2 mSingCountDownView;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    boolean mHasPlayFullAnimation = false;

    HandlerTaskTimer mCounDownTask;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    public PKOthersSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mGrabRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mPkCardView = (PKSingCardView) mParentView.findViewById(R.id.pk_card_view);
        mPkCardView.setRoomData(mGrabRoomData);
        mSingCountDownView = mParentView.findViewById(R.id.sing_count_down_view);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_pk_other_sing_card_stub_layout;
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        destoryAnimation();
    }

    public void bindData() {
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        tryInflate();
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mGrabRoomData.getUserInfo(list.get(0).getUserID());
            mRightUserInfoModel = mGrabRoomData.getUserInfo(list.get(1).getUserID());
        }
        mHasPlayFullAnimation = false;
        mUiHandler.removeCallbacksAndMessages(null);
        mParentView.setVisibility(View.VISIBLE);
        // 绑定数据
        mPkCardView.bindData();
        if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            // pk第一个人唱
            playCardEnterAniamtion();
        } else if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            if (mRightUserInfoModel != null) {
                playIndicateAnimation(mRightUserInfoModel.getUserId());
            }
        }
    }

    private void playIndicateAnimation(int userId) {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        mSingCountDownView.startPlay(0, totalMs, false);

        if (!infoModel.isParticipant() && infoModel.isEnterInSingStatus()) {
            // 开始倒计时
            // 直接播放svga 保证声纹动画
            mPkCardView.playScaleWithoutAnimation(userId);
            mPkCardView.playSingAnimation(userId);
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDown("中途进来");
        } else {
            mPkCardView.playScaleAnimation(userId, true, new PKSingCardView.AnimationListerner() {
                @Override
                public void onAnimationEndExcludeSvga() {
                    // 开始倒计时
                    mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
                    countDown("动画播放完毕");
                }

                @Override
                public void onAnimationEndWithDraw() {
                    // TODO: 2019/4/26 不会调用
                }
            });
        }
    }

    // pk 他人的为什么有动画
    private void playCardEnterAniamtion() {
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
                    playIndicateAnimation(mLeftUserInfoModel.getUserId());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (mParentView != null) {
            mParentView.startAnimation(mEnterTranslateAnimation);
        }
    }

    /**
     * 离场动画
     */
    public void hide() {
        if(mParentView!=null){
            if (mParentView.getVisibility() == View.VISIBLE) {
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
                        mSingCountDownView.reset();
                        mParentView.clearAnimation();
                        mParentView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mParentView.startAnimation(mLeaveTranslateAnimation);
            } else {
                destoryAnimation();
                mSingCountDownView.reset();
                mParentView.clearAnimation();
                mParentView.setVisibility(View.GONE);
            }
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        int progress;  //当前进度条
        int leaveTime; //剩余时间
        MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant() + " enterStatus=" + infoModel.getEnterStatus());
        if (!infoModel.isParticipant() && infoModel.getStatus() == infoModel.getEnterStatus()) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = infoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - infoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
        mSingCountDownView.startPlay(progress, leaveTime, true);
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

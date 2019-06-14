package com.module.playways.grab.room.view.pk;

import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.grab.room.view.pk.view.PKSingCardView;
import com.zq.live.proto.Room.EQRoundStatus;

import java.util.List;

public class PKSelfSingCardView extends ExViewStub {

    public final static String TAG = "PKSelfSingCardView";

    SelfSingLyricView mPkSelfSingLyricView;
    PKSingCardView mPkSingCardView;
    SingCountDownView2 mSingCountDownView;

    GrabRoomData mRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
//    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    public PKSelfSingCardView(ViewStub viewStub,GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mPkSelfSingLyricView =  mParentView.findViewById(R.id.pk_self_sing_lyric_view);
        mPkSingCardView =  mParentView.findViewById(R.id.pk_sing_card_view);
        mSingCountDownView =  mParentView.findViewById(R.id.sing_count_down_view);
    }

    public void playLyric() {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        tryInflate();
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mRoomData.getUserInfo(list.get(0).getUserID());
            mRightUserInfoModel = mRoomData.getUserInfo(list.get(1).getUserID());
        }
        setVisibility(View.VISIBLE);
        // 绑定数据
        mPkSingCardView.bindData();
        if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            // pk第一个人唱
            mPkSingCardView.setVisibility(View.VISIBLE);
            playCardEnterAnimation();
        } else if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            mPkSingCardView.setVisibility(View.VISIBLE);
            if (mRightUserInfoModel != null) {
                playIndicateAnimation(mRightUserInfoModel.getUserId());
            }
        }
    }

    /**
     * 播放指示谁唱的animation
     *
     * @param userId
     */
    private void playIndicateAnimation(int userId) {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        mSingCountDownView.startPlay(0, totalMs, false);

        mPkSingCardView.playScaleAnimation(userId, false, new PKSingCardView.AnimationListerner() {
            @Override
            public void onAnimationEndExcludeSvga() {
                mPkSingCardView.playWithDraw();
                playRealLyric();
            }

            @Override
            public void onAnimationEndWithDraw() {

            }
        });

    }

    private void playRealLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }
        mPkSelfSingLyricView.initLyric();
        int totalTs = infoModel.getSingTotalMs();
        mPkSelfSingLyricView.playWithAcc(infoModel, totalTs);
        mSingCountDownView.startPlay(0, totalTs, true);
    }

    /**
     * 入场动画
     */
    private void playCardEnterAnimation() {
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
                if (mLeftUserInfoModel != null) {
                    playIndicateAnimation(mLeftUserInfoModel.getUserId());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mParentView.startAnimation(mEnterTranslateAnimation);
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
        if (mPkSelfSingLyricView != null) {
            mPkSelfSingLyricView.setRoomData(roomData);
        }
        if (mPkSingCardView != null) {
            mPkSingCardView.setRoomData(roomData);
        }
    }
//
//    /**
//     * 离场动画，整个pk结束才执行
//     */
//    public void hide() {
//        if (this != null && this.getVisibility() == VISIBLE) {
//            if (mLeaveTranslateAnimation == null) {
//                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
//                mLeaveTranslateAnimation.setDuration(200);
//            }
//            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    mSingCountDownView.reset();
//                    clearAnimation();
//                    setVisibility(GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            this.startAnimation(mLeaveTranslateAnimation);
//        } else {
//            mSingCountDownView.reset();
//            clearAnimation();
//            setVisibility(GONE);
//        }
//        mPkSelfSingLyricView.reset();
//    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            mSingCountDownView.reset();
            mPkSelfSingLyricView.reset();
            mPkSingCardView.reset();
        }
    }

    public void destroy() {
        mPkSelfSingLyricView.destroy();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }

//        if (mLeaveTranslateAnimation != null) {
//            mLeaveTranslateAnimation.setAnimationListener(null);
//            mLeaveTranslateAnimation.cancel();
//        }
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
        if (mSingCountDownView != null) {
            mSingCountDownView.setListener(mListener);
        }
    }

}

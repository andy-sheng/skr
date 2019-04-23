package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.view.common.SingCountDownView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView;
import com.module.playways.grab.room.view.pk.view.PKSingCardView;
import com.zq.live.proto.Room.EQRoundStatus;

import java.util.List;

public class PKSelfSingCardView extends RelativeLayout {

    public final static String TAG = "PKSelfSingCardView";

    SelfSingLyricView mPkSelfSingLyricView;
    PKSingCardView mPkSingCardView;
    SingCountDownView mSingCountDownView;

    GrabRoomData mRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    public PKSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_self_sing_card_layout, this);
        mPkSelfSingLyricView = (SelfSingLyricView) findViewById(R.id.pk_self_sing_lyric_view);
        mPkSingCardView = (PKSingCardView) findViewById(R.id.pk_sing_card_view);
        mSingCountDownView = (SingCountDownView) findViewById(R.id.sing_count_down_view);
    }

    public void playLyric() {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mRoomData.getUserInfo(list.get(0).getUserID());
            mRightUserInfoModel = mRoomData.getUserInfo(list.get(1).getUserID());
        }
        if (mLeftUserInfoModel != null && mRightUserInfoModel != null) {
            setVisibility(VISIBLE);
            // 绑定数据
            mPkSingCardView.bindData();
            if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                // pk第一个人唱
                mPkSingCardView.setVisibility(VISIBLE);
                playCardEnterAnimation();
            } else if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                if (mRightUserInfoModel != null) {
                    mPkSingCardView.setVisibility(VISIBLE);
                    playIndicateAnimation(mRightUserInfoModel.getUserId());
                }
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

        mPkSingCardView.playScaleAnimation(userId, true, new PKSingCardView.AnimationListerner() {
            @Override
            public void onAnimationEndExcludeSvga() {
                mPkSingCardView.setVisibility(GONE);
                playRealLyric();
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
        if (infoModel.getMusic() == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        boolean withAcc = false;
        if (RoomDataUtils.isPKRound(mRoomData)) {
            // pk模式
            withAcc = true;
        }

        if (!withAcc) {
            mSingCountDownView.setTagImgRes(R.drawable.ycdd_daojishi_qingchang);
            mPkSelfSingLyricView.playWithNoAcc(infoModel.getMusic());
        } else {
            mSingCountDownView.setTagImgRes(R.drawable.ycdd_daojishi_banzou);
            mPkSelfSingLyricView.playWithAcc(infoModel, totalTs);
        }
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
        this.startAnimation(mEnterTranslateAnimation);
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

    /**
     * 离场动画，整个pk结束才执行
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
                    mSingCountDownView.reset();
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            mSingCountDownView.reset();
            clearAnimation();
            setVisibility(GONE);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }

        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
        if (mSingCountDownView != null) {
            mSingCountDownView.setListener(mListener);
        }
    }
}

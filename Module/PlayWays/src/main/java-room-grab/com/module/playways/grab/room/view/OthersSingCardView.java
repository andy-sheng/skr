package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.countdown.CircleCountDownView;
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.fresco.animation.drawable.AnimationListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.room.view.ArcProgressBar;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {
    public final static String TAG = "OthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;

    final static int COUNT_DOWN_STATUS_INIT = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 2;
    final static int COUNT_DOWN_STATUS_PLAYING = 3;

    int mCountDownStatus = COUNT_DOWN_STATUS_INIT;

    int mUseId;   // 当前唱歌人的id

    BaseImageView mGrabStageView;
    BaseImageView mSingAvatarView;
    CircleCountDownView mCircleCountDownView;

    AlphaAnimation mEnterAlphaAnimation;                // 进场动画
    TranslateAnimation mLeaveTranslateAnimation;   // 出场动画

    GrabRoomData mGrabRoomData;

    Handler mUiHandler = new Handler();

    boolean mHasPlayFullAnimation = false;
    boolean mCanStartFlag = false;

    public OthersSingCardView(Context context) {
        super(context);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_others_sing_card_layout, this);
        mGrabStageView = (BaseImageView) findViewById(R.id.grab_stage_view);
        mSingAvatarView = (BaseImageView) findViewById(R.id.sing_avatar_view);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);

        mSingAvatarView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mUseId != 0) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mUseId));
                }
            }
        });
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void bindData(UserInfoModel userInfoModel) {
        mUiHandler.removeCallbacksAndMessages(null);
        mHasPlayFullAnimation = false;
        setVisibility(VISIBLE);
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.getUserId();
            AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                    AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                            .setCircle(true)
                            .build());
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败");
        }
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        mCircleCountDownView.setProgress(0);
        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = new AlphaAnimation(0f, 1f);
            mEnterAlphaAnimation.setDuration(1000);
        }
        this.startAnimation(mEnterAlphaAnimation);
        FrescoWorker.loadImage(mGrabStageView, ImageFactory.newHttpImage(BaseRoomData.PK_MAIN_STAGE_WEBP)
                .setCallBack(new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info, Animatable animatable) {
                        if (animatable != null && animatable instanceof AnimatedDrawable2) {
                            ((AnimatedDrawable2) animatable).setAnimationListener(new AnimationListener() {

                                @Override
                                public void onAnimationStart(AnimatedDrawable2 drawable) {
                                    MyLog.d(TAG, "onAnimationStart" + " drawable=" + drawable);
                                }

                                @Override
                                public void onAnimationStop(AnimatedDrawable2 drawable) {
                                    MyLog.d(TAG, "onAnimationStop" + " drawable=" + drawable);
                                }

                                @Override
                                public void onAnimationReset(AnimatedDrawable2 drawable) {
                                }

                                @Override
                                public void onAnimationRepeat(AnimatedDrawable2 drawable) {

                                }

                                @Override
                                public void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber) {
                                }
                            });
                            animatable.start();
                        }
                    }

                    @Override
                    public void processWithFailure() {
                        MyLog.d(TAG, "processWithFailure");
                    }
                })
                .build()
        );
        countDownAfterAnimation("bindData");
    }

    public void tryStartCountDown() {
        MyLog.d(TAG, "tryStartCountDown");
        mCanStartFlag = true;
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

        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            countDown("中途进来");
        } else {
            countDown("else full Animation");
        }
    }

    private void countDown(String from) {
        MyLog.d(TAG, "countDown" + " from=" + from);
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            return;
        }
        int totalMs = grabRoundInfoModel.getSingEndMs() - grabRoundInfoModel.getSingBeginMs();
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            MyLog.d(TAG, "countDown mCountDownStatus == COUNT_DOWN_STATUS_WAIT");
            if (mCanStartFlag) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            } else {
                // 不需要播放countdown
//                mCountDownProcess.startCountDown(0, totalMs);
                mCircleCountDownView.go(0, totalMs);
                return;
            }
        }

        int progress;  //当前进度条
        int leaveTime; //剩余时间
        MyLog.d(TAG, "countDown isParticipant:" + grabRoundInfoModel.isParticipant() + " enterStatus=" + grabRoundInfoModel.getEnterStatus());
        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = grabRoundInfoModel.getElapsedTimeMs() * 100 / totalMs;
            leaveTime = totalMs - grabRoundInfoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = totalMs;
        }
//        mCountDownProcess.startCountDown(progress, leaveTime);
        mCircleCountDownView.go(progress, leaveTime);
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            this.startAnimation(mLeaveTranslateAnimation);
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mCountDownStatus = COUNT_DOWN_STATUS_INIT;
            mCanStartFlag = false;
            mHasPlayFullAnimation = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterAlphaAnimation != null) {
            mEnterAlphaAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.cancel();
        }
    }
}

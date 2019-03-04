package com.module.playways.grab.room.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.fresco.animation.drawable.AnimationListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.room.view.ArcProgressBar;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {
    public final static String TAG = "OthersSingCardView";
    final static int MSG_ENSURE_PLAY = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 1;
    final static int COUNT_DOWN_STATUS_PLAYING = 2;

    int useId;   // 当前唱歌人的id

    BaseImageView mGrabStageView;
    BaseImageView mSingAvatarView;
    ArcProgressBar mCountDownProcess;

    ObjectAnimator mEnterAnimation;   // 进场动画
    ObjectAnimator mLeaveAnimation;   // 出场动画

    GrabRoomData mGrabRoomData;

    int mCountDownStatus = COUNT_DOWN_STATUS_WAIT;

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
        mCountDownProcess = (ArcProgressBar) findViewById(R.id.count_down_process);

        RxView.clicks(mSingAvatarView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        if (useId != 0) {
                            EventBus.getDefault().post(new ShowPersonCardEvent(useId));
                        }
                    }
                });
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.useId = userInfoModel.getUserId();
        setVisibility(VISIBLE);
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        mCountDownProcess.setProgress(0);
        // 淡出效果
        if (mEnterAnimation == null) {
            mEnterAnimation = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f);
            mEnterAnimation.setDuration(1000);
        }
        mEnterAnimation.start();

        AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                        .setCircle(true)
                        .build());

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

        countDown();
    }

    public void tryStartCountDown() {
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDown();
        }
    }

    private void countDown() {
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        SongModel songModel = grabRoundInfoModel.getMusic();
        if (songModel == null) {
            return;
        }
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            // 不需要播放countdown
            mCountDownProcess.startCountDown(0, songModel.getStandLrcEndT() - songModel.getStandLrcBeginT());
            return;
        }

        int progress;  //当前进度条
        int leaveTime; //剩余时间
        if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
            progress = grabRoundInfoModel.getElapsedTimeMs() * 100 / (songModel.getStandLrcEndT() - songModel.getStandLrcBeginT());
            leaveTime = songModel.getStandLrcEndT() - songModel.getStandLrcBeginT() + 1000 - grabRoundInfoModel.getElapsedTimeMs();
        } else {
            progress = 1;
            leaveTime = songModel.getStandLrcEndT() - songModel.getStandLrcBeginT() + 1000;
        }
        mCountDownProcess.startCountDown(progress, leaveTime);
    }


    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveAnimation == null) {
                mLeaveAnimation = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0F, U.getDisplayUtils().getScreenWidth());
                mLeaveAnimation.setDuration(200);
            }
            mLeaveAnimation.start();

            mLeaveAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                    onAnimationEnd(animator);
                }
            });
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterAnimation != null) {
            mEnterAnimation.cancel();
        }
        if (mLeaveAnimation != null) {
            mLeaveAnimation.cancel();
        }
    }
}

package com.module.playways.grab.room.view.pk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.top.CircleAnimationView;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
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

    SVGAImageView mLeftSingSvga;
    SVGAImageView mRightSingSvga;

    LinearLayout mPkOtherArea;
    SimpleDraweeView mLeftIv;
    ExTextView mLeftName;
    CircleAnimationView mLeftCircleAnimationView;
    CircleAnimationView mRightCircleAnimationView;
    SimpleDraweeView mRightIv;
    ExTextView mRightName;
    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画
    ScaleAnimation mScaleAnimation;      // 头像放大动画
    ValueAnimator mValueAnimator;       // 画圆圈的属性动画

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    boolean mLeftAnimationFlag = false;    //左边动画是否在播标记（不包括SVGA）
    boolean mRightAnimationFlag = false;   //右边动画是否在播标记（不包括SVGA）
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
        mLeftSingSvga = (SVGAImageView) findViewById(R.id.left_sing_svga);
        mRightSingSvga = (SVGAImageView) findViewById(R.id.right_sing_svga);
        mPkOtherArea = (LinearLayout) findViewById(R.id.pk_other_area);
        mLeftIv = (SimpleDraweeView) findViewById(R.id.left_iv);
        mLeftName = (ExTextView) findViewById(R.id.left_name);
        mLeftCircleAnimationView = (CircleAnimationView) findViewById(R.id.left_circle_animation_view);
        mRightIv = (SimpleDraweeView) findViewById(R.id.right_iv);
        mRightName = (ExTextView) findViewById(R.id.right_name);
        mRightCircleAnimationView = (CircleAnimationView) findViewById(R.id.right_circle_animation_view);

        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) findViewById(R.id.count_down_tv);

        int offsetX = (U.getDisplayUtils().getScreenWidth() / 2 - U.getDisplayUtils().dip2px(16)) / 2;
        mLeftSingSvga.setTranslationX(-offsetX);
        mRightSingSvga.setTranslationX(offsetX);
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
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
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNickname());
            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNickname());
            if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                // pk第一个人唱
                animationGo();
            } else if (grabRoundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                playScaleAnimation(mRightUserInfoModel.getUserId());
            }
        }
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
                    playScaleAnimation(mLeftUserInfoModel.getUserId());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(mEnterTranslateAnimation);
    }

    private void playScaleAnimation(int uid) {
        // TODO: 2019/4/23 恢复成初始状态
        destoryAnimation();
        mLeftIv.clearAnimation();
        mRightIv.clearAnimation();
        mLeftCircleAnimationView.setVisibility(GONE);
        mRightCircleAnimationView.setVisibility(GONE);
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        int totalMs = infoModel.getSingTotalMs();
        mCountDownTv.setText(totalMs + "");

        // TODO: 2019/4/23 开始播放动画
        if (mScaleAnimation == null) {
            mScaleAnimation = new ScaleAnimation(1.0f, 1.35f, 1f, 1.35f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mScaleAnimation.setInterpolator(new OvershootInterpolator());
            mScaleAnimation.setFillAfter(true);
            mScaleAnimation.setDuration(500);
        } else {
            mScaleAnimation.setAnimationListener(null);
            mScaleAnimation.cancel();
        }
        mScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playCircleAnimation(uid);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
            if (!mLeftAnimationFlag && !mLeftSingSvga.isAnimating()) {
                // TODO: 2019/4/23 防止多次播放 
                mLeftAnimationFlag = true;
                mLeftIv.startAnimation(mScaleAnimation);
            } else {
                MyLog.w(TAG, "playScaleAnimation 动画已经在播放了" + " uid=" + uid);
            }
        } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
            if (!mRightAnimationFlag && !mRightSingSvga.isAnimating()) {
                // TODO: 2019/4/23 防止多次播放
                mRightAnimationFlag = true;
                mRightIv.startAnimation(mScaleAnimation);
            } else {
                MyLog.w(TAG, "playScaleAnimation 动画已经在播放了" + " uid=" + uid);
            }
        }
    }


    private void playCircleAnimation(int uid) {
        if (mValueAnimator != null) {
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
        }
        if (mValueAnimator == null) {
            mValueAnimator = new ValueAnimator();
            mValueAnimator.setIntValues(0, 100);
            mValueAnimator.setDuration(495);
        }
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int p = (int) animation.getAnimatedValue();
                if (uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setProgress(p);
                } else if (uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setProgress(p);
                }

            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setVisibility(VISIBLE);
                } else if (uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setVisibility(GONE);
                } else if (uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (uid == mLeftUserInfoModel.getUserId()) {
                    mLeftAnimationFlag = false;
                    mLeftCircleAnimationView.setVisibility(GONE);
                } else if (uid == mRightUserInfoModel.getUserId()) {
                    mRightAnimationFlag = false;
                    mRightCircleAnimationView.setVisibility(GONE);
                }
                playSingAnimation(uid);
            }
        });
        mValueAnimator.start();
    }

    // TODO: 2019/4/23 播放声纹动画，同时倒计时开始计时
    private void playSingAnimation(int uid) {
        if (uid == mLeftUserInfoModel.getUserId()) {
            playSingAnimation(mLeftSingSvga);
        } else if (uid == mRightUserInfoModel.getUserId()) {
            playSingAnimation(mRightSingSvga);
        }

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

    // 播放声纹动画
    private void playSingAnimation(SVGAImageView svgaImageView) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        if (svgaImageView != null && svgaImageView.getVisibility() == VISIBLE) {
            // 正在播放
            return;
        }

        svgaImageView.setVisibility(View.VISIBLE);
        svgaImageView.setLoops(-1);

        SvgaParserAdapter.parse("grab_main_stage.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                svgaImageView.setImageDrawable(drawable);
                svgaImageView.startAnimation();
            }

            @Override
            public void onError() {

            }
        });
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
        if (mScaleAnimation != null) {
            mScaleAnimation.setAnimationListener(null);
            mScaleAnimation.cancel();
        }
        if (mValueAnimator != null) {
            mValueAnimator.removeAllListeners();
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
        }
        if (mLeftSingSvga != null) {
            mLeftSingSvga.setCallback(null);
            mLeftSingSvga.stopAnimation(true);
        }
        if (mRightSingSvga != null) {
            mRightSingSvga.setCallback(null);
            mRightSingSvga.stopAnimation(true);
        }
        mLeftAnimationFlag = false;
        mRightAnimationFlag = false;
    }
}

package com.module.playways.grab.room.view.pk.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.top.CircleAnimationView;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundOverReason;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * PKOthersSingCardView 和 PKSelfSingCardView 的公用部分
 * 主要保护放大，画圈和声纹动画或过场动画衔接
 */
public class PKSingCardView extends RelativeLayout {

    public final static String TAG = "PKSingCardView";

    SVGAImageView mLeftSingSvga;
    SVGAImageView mRightSingSvga;
    LinearLayout mPkArea;

    RelativeLayout mLeftPkArea;
    RelativeLayout mLeftArea;
    SimpleDraweeView mLeftIv;
    ExRelativeLayout mLeftStatusArea;
    ExTextView mLeftName;
    ExTextView mLeftStatus;
    CircleAnimationView mLeftCircleAnimationView;

    RelativeLayout mRightPkArea;
    RelativeLayout mRightArea;
    SimpleDraweeView mRightIv;
    ExRelativeLayout mRightStatusArea;
    ExTextView mRightName;
    ExTextView mRightStatus;
    CircleAnimationView mRightCircleAnimationView;

    ScaleAnimation mScaleAnimation;        // 头像放大动画
    ValueAnimator mValueAnimator;          // 画圆圈的属性动画
    AnimatorSet mAnimatorSet;              // 左右拉开动画
    boolean mLeftAnimationFlag = false;    //左边动画是否在播标记（不包括SVGA）
    boolean mRightAnimationFlag = false;   //右边动画是否在播标记（不包括SVGA）
    boolean mIsPlaySVGA;                   // 是否播放SVGA

    int mLeftOverReason;
    int mRightOverReason;

    GrabRoomData mGrabRoomData;
    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    AnimationListerner mAnimationListerner;

    public PKSingCardView(Context context) {
        super(context);
        init();
    }

    public PKSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_sing_card_layout, this);

        mLeftSingSvga = (SVGAImageView) findViewById(R.id.left_sing_svga);
        mRightSingSvga = (SVGAImageView) findViewById(R.id.right_sing_svga);
        mPkArea = (LinearLayout) findViewById(R.id.pk_area);

        mLeftPkArea = (RelativeLayout) findViewById(R.id.left_pk_area);
        mLeftArea = (RelativeLayout) findViewById(R.id.left_area);
        mLeftIv = (SimpleDraweeView) findViewById(R.id.left_iv);
        mLeftStatusArea = (ExRelativeLayout) findViewById(R.id.left_status_area);
        mLeftName = (ExTextView) findViewById(R.id.left_name);
        mLeftStatus = (ExTextView) findViewById(R.id.left_status);
        mLeftCircleAnimationView = (CircleAnimationView) findViewById(R.id.left_circle_animation_view);

        mRightPkArea = (RelativeLayout) findViewById(R.id.right_pk_area);
        mRightArea = (RelativeLayout) findViewById(R.id.right_area);
        mRightIv = (SimpleDraweeView) findViewById(R.id.right_iv);
        mRightStatusArea = (ExRelativeLayout) findViewById(R.id.right_status_area);
        mRightName = (ExTextView) findViewById(R.id.right_name);
        mRightStatus = (ExTextView) findViewById(R.id.right_status);
        mRightCircleAnimationView = (CircleAnimationView) findViewById(R.id.right_circle_animation_view);

        mLeftIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mLeftUserInfoModel != null) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mLeftUserInfoModel.getUserId()));
                }
            }
        });

        mRightIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mRightUserInfoModel != null) {
                    EventBus.getDefault().post(new ShowPersonCardEvent(mRightUserInfoModel.getUserId()));
                }
            }
        });
    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void bindData() {
        if (mGrabRoomData == null) {
            return;
        }
        GrabRoundInfoModel grabRoundInfoModel = mGrabRoomData.getRealRoundInfo();
        if (grabRoundInfoModel == null) {
            MyLog.w(TAG, "setRoomData" + " grabRoundInfoModel=" + grabRoundInfoModel);
            return;
        }

        reset();

        List<SPkRoundInfoModel> list = grabRoundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mGrabRoomData.getUserInfo(list.get(0).getUserID());
            mLeftOverReason = list.get(0).getOverReason();

            mRightUserInfoModel = mGrabRoomData.getUserInfo(list.get(1).getUserID());
            mRightOverReason = list.get(1).getOverReason();
        }
        setVisibility(VISIBLE);
        if (mLeftUserInfoModel != null) {
            if (mLeftOverReason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                mLeftStatusArea.setVisibility(VISIBLE);
                mLeftStatus.setVisibility(VISIBLE);
                mLeftStatus.setText("不唱了");
            } else if (mLeftOverReason == EQRoundOverReason.ROR_MULTI_NO_PASS.getValue()) {
                mLeftStatusArea.setVisibility(VISIBLE);
                mLeftStatus.setVisibility(VISIBLE);
                mLeftStatus.setText("被灭灯");
            } else if (mLeftOverReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()) {
                mLeftStatusArea.setVisibility(VISIBLE);
                mLeftStatus.setVisibility(VISIBLE);
                mLeftStatus.setText("退出了");
            } else {
                mLeftStatusArea.setVisibility(GONE);
                mLeftStatus.setVisibility(GONE);
            }
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNicknameRemark());
        }
        if (mRightUserInfoModel != null) {
            if (mRightOverReason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                mRightStatusArea.setVisibility(VISIBLE);
                mRightStatus.setVisibility(VISIBLE);
                mRightStatus.setText("不唱了");
            } else if (mRightOverReason == EQRoundOverReason.ROR_MULTI_NO_PASS.getValue()) {
                mRightStatusArea.setVisibility(VISIBLE);
                mRightStatus.setVisibility(VISIBLE);
                mRightStatus.setText("被灭灯");
            } else if (mRightOverReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()) {
                mRightStatusArea.setVisibility(VISIBLE);
                mRightStatus.setVisibility(VISIBLE);
                mRightStatus.setText("退出了");
            } else {
                mRightStatusArea.setVisibility(GONE);
                mRightStatus.setVisibility(GONE);
            }
            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNicknameRemark());
        }
    }

    public void reset() {
        mLeftIv.clearAnimation();
        mRightIv.clearAnimation();
        mLeftCircleAnimationView.setVisibility(GONE);
        mRightCircleAnimationView.setVisibility(GONE);
        mLeftStatusArea.setVisibility(GONE);
        mLeftStatusArea.setVisibility(GONE);
        mLeftStatus.setVisibility(GONE);
        mRightStatus.setVisibility(GONE);

        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        mLeftAnimationFlag = false;
        mLeftAnimationFlag = false;
        mLeftOverReason = 0;
        mRightOverReason = 0;

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
        if (mAnimationListerner != null) {
            mAnimationListerner = null;
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
    }

    public void playScaleWithoutAnimation(int userId) {
        if (mLeftUserInfoModel != null && userId == mLeftUserInfoModel.getUserId()) {
            if (!mLeftAnimationFlag && !mLeftSingSvga.isAnimating()) {
                mLeftArea.setScaleX(1.35f);
                mLeftArea.setScaleY(1.35f);
            } else {
            }
        } else if (mRightUserInfoModel != null && userId == mRightUserInfoModel.getUserId()) {
            if (!mRightAnimationFlag && !mRightSingSvga.isAnimating()) {
                mRightArea.setScaleX(1.35f);
                mRightArea.setScaleY(1.35f);
            } else {
            }
        }
    }
    /**
     * @param uid        播放谁的动画
     * @param isPlaySVGA 是否播放声纹SVGA
     */
    public void playScaleAnimation(int uid, boolean isPlaySVGA, AnimationListerner animationListerner) {

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
                this.mIsPlaySVGA = isPlaySVGA;
                this.mAnimationListerner = animationListerner;
                mLeftAnimationFlag = true;
                mLeftArea.startAnimation(mScaleAnimation);
            } else {
                MyLog.w(TAG, "playScaleAnimation 动画已经在播放了" + " uid=" + uid);
            }
        } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
            if (!mRightAnimationFlag && !mRightSingSvga.isAnimating()) {
                // TODO: 2019/4/23 防止多次播放
                this.mIsPlaySVGA = isPlaySVGA;
                this.mAnimationListerner = animationListerner;
                mRightAnimationFlag = true;
                mRightArea.startAnimation(mScaleAnimation);
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
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setProgress(p);
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setProgress(p);
                }

            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setVisibility(VISIBLE);
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
                    mLeftCircleAnimationView.setVisibility(GONE);
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
                    mRightCircleAnimationView.setVisibility(GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
                    mLeftAnimationFlag = false;
                    mLeftCircleAnimationView.setVisibility(GONE);
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
                    mRightAnimationFlag = false;
                    mRightCircleAnimationView.setVisibility(GONE);
                }

                if (mAnimationListerner != null) {
                    mAnimationListerner.onAnimationEndExcludeSvga();
                }

                if (mIsPlaySVGA) {
                    playSingAnimation(uid);
                } else {
                    mLeftSingSvga.setVisibility(GONE);
                    mRightSingSvga.setVisibility(GONE);
                }
            }
        });
        mValueAnimator.start();
    }

    /**
     * 大幕拉开
     */
    public void playWithDraw() {
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            ObjectAnimator left = ObjectAnimator.ofFloat(mLeftPkArea, TRANSLATION_X, 0, -U.getDisplayUtils().getScreenWidth() / 2);
            left.setDuration(500);

            ObjectAnimator right = ObjectAnimator.ofFloat(mRightPkArea, TRANSLATION_X, 0, U.getDisplayUtils().getScreenWidth() / 2);
            right.setDuration(500);

            mAnimatorSet.playTogether(left, right);
        }

        setVisibility(VISIBLE);
        mAnimatorSet.removeAllListeners();
        mAnimatorSet.cancel();

        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                mLeftPkArea.setTranslationX(0);
                mRightPkArea.setTranslationX(0);
                if (mAnimationListerner != null) {
                    mAnimationListerner.onAnimationEndWithDraw();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimatorSet.start();
    }

    // TODO: 2019/4/23 播放声纹动画，同时倒计时开始计时
    public void playSingAnimation(int uid) {
        if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel.getUserId()) {
            playSingAnimation(mLeftSingSvga);
        } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel.getUserId()) {
            playSingAnimation(mRightSingSvga);
        }
    }

    // 播放声纹动画
    private void playSingAnimation(SVGAImageView svgaImageView) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        if (svgaImageView != null && svgaImageView.isAnimating()) {
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    public interface AnimationListerner {
        /**
         * 动画播放完毕，不包括svga, 不包括拉开动画
         */
        void onAnimationEndExcludeSvga();

        /**
         * 拉开动画播放完毕
         */
        void onAnimationEndWithDraw();
    }
}

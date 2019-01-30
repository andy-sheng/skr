package com.module.playways.rank.room.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.rank.room.presenter.EndGamePresenter;
import com.module.playways.rank.room.view.IVoteView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;

import java.util.concurrent.TimeUnit;

// 游戏结束页
public class EvaluationFragment extends BaseFragment implements IVoteView {

    public final static String TAG = "EvaluationFragment";

    RoomData mRoomData;

    RelativeLayout mMainActContainer;

    // 左边视图
    RelativeLayout mRlLeftArea;
    SimpleDraweeView mVoteLeftIv;
    SVGAImageView mStartLeftSvga;
    ExTextView mVoteLeftNameTv;
    ExTextView mVoteLeftSongTv;
    ExImageView mVoteLeftShadowIv;
    ExImageView mVoteLeftMie;

    // 右边视图
    RelativeLayout mRlRightArea;
    SimpleDraweeView mVoteRightIv;
    SVGAImageView mStartRightSvga;
    ExTextView mVoteRigntNameTv;
    ExTextView mVoteRightSongTv;
    ExImageView mVoteRightShadowIv;
    ExImageView mVoteRightMie;

    ExTextView mVoteDownTv;

    EndGamePresenter mPresenter;

    PlayerInfoModel left;
    PlayerInfoModel right;

    ExRelativeLayout mRlLeft;
    ExRelativeLayout mRlRight;
    ImageView mIvBottom;

    HandlerTaskTimer mVoteTimeTask;

    AnimatorSet mLeftVoteAnimationSet;
    AnimatorSet mRightVoteAnimationSet;

    volatile boolean isJumpToRecord = false;

//    RelativeLayout mRlCountDownContainer;

    static final int MSG_GO_RECORD_FRAGMENT = 1;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_GO_RECORD_FRAGMENT) {
                showRecordView(null);
            }
        }
    };

    @Override
    public int initView() {
        return R.layout.ranking_room_evaluation_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mVoteDownTv = (ExTextView) mRootView.findViewById(R.id.vote_down_tv);

        mRlLeftArea = (RelativeLayout) mRootView.findViewById(R.id.rl_left_area);
        mVoteLeftIv = (SimpleDraweeView) mRootView.findViewById(R.id.vote_left_iv);
        mStartLeftSvga = (SVGAImageView) mRootView.findViewById(R.id.start_left_svga);
        mVoteLeftNameTv = (ExTextView) mRootView.findViewById(R.id.vote_left_name_tv);
        mVoteLeftSongTv = (ExTextView) mRootView.findViewById(R.id.vote_left_song_tv);
        mVoteLeftShadowIv = (ExImageView) mRootView.findViewById(R.id.vote_left_shadow_iv);

        mRlRightArea = (RelativeLayout) mRootView.findViewById(R.id.rl_right_area);
        mVoteRightIv = (SimpleDraweeView) mRootView.findViewById(R.id.vote_right_iv);
        mStartRightSvga = (SVGAImageView) mRootView.findViewById(R.id.start_right_svga);
        mVoteRigntNameTv = (ExTextView) mRootView.findViewById(R.id.vote_rignt_name_tv);
        mVoteRightSongTv = (ExTextView) mRootView.findViewById(R.id.vote_right_song_tv);
        mVoteRightShadowIv = (ExImageView) mRootView.findViewById(R.id.vote_right_shadow_iv);

        mVoteLeftMie = (ExImageView) mRootView.findViewById(R.id.vote_left_mie);
        mVoteRightMie = (ExImageView) mRootView.findViewById(R.id.vote_right_mie);

        mRlLeft = (ExRelativeLayout) mRootView.findViewById(R.id.rl_left);
        mRlRight = (ExRelativeLayout) mRootView.findViewById(R.id.rl_right);
        mIvBottom = (ImageView) mRootView.findViewById(R.id.iv_bottom);

//        mRlCountDownContainer = (RelativeLayout) mRootView.findViewById(R.id.rl_count_down_container);


        if (left != null) {
            AvatarUtils.loadAvatarByUrl(mVoteLeftIv, AvatarUtils.newParamsBuilder(left.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(3))
                    .setBorderColorBySex(left.getUserInfo().getIsMale())
                    .build());
            mVoteLeftNameTv.setText(left.getUserInfo().getNickname());
            mVoteLeftSongTv.setText(left.getSongList().get(0).getItemName());
        }

        if (right != null) {
            AvatarUtils.loadAvatarByUrl(mVoteRightIv, AvatarUtils.newParamsBuilder(right.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(3))
                    .setBorderColorBySex(right.getUserInfo().getIsMale())
                    .build());
            mVoteRigntNameTv.setText(right.getUserInfo().getNickname());
            mVoteRightSongTv.setText(right.getSongList().get(0).getItemName());

        }

        mPresenter = new EndGamePresenter(this);
        addPresent(mPresenter);

        RxView.clicks(mVoteLeftMie)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mVoteLeftMie.setClickable(false);
                    mVoteRightMie.setClickable(false);
                    mPresenter.vote(mRoomData.getGameId(), left.getUserInfo().getUserId());
                });

        RxView.clicks(mVoteRightMie)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mVoteLeftMie.setClickable(false);
                    mVoteRightMie.setClickable(false);
                    mPresenter.vote(mRoomData.getGameId(), right.getUserInfo().getUserId());
                });

        startTimeTask();

        mRootView.setVisibility(View.INVISIBLE);
        HandlerTaskTimer.newBuilder().delay(500).start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                mRootView.setVisibility(View.VISIBLE);
                U.getSoundUtils().play(TAG, R.raw.select_animation);
                animationGo();
            }
        });
        U.getSoundUtils().preLoad(TAG, R.raw.select_animation, R.raw.select_dislikebutton, R.raw.general_countdown);

    }

    private void animationGo() {
        // 平移动动画
        TranslateAnimation animationLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        TranslateAnimation animationRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        TranslateAnimation animationBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        animationLeft.setDuration(300);
        animationLeft.setRepeatMode(Animation.REVERSE);
        animationLeft.setInterpolator(new AccelerateInterpolator());
        animationLeft.setFillAfter(true);

        animationRight.setDuration(300);
        animationRight.setRepeatMode(Animation.REVERSE);
        animationRight.setInterpolator(new AccelerateInterpolator());
        animationRight.setFillAfter(true);

        animationBottom.setDuration(300);
        animationBottom.setRepeatMode(Animation.REVERSE);
        animationBottom.setInterpolator(new AccelerateInterpolator());
        animationBottom.setFillAfter(true);

        mRlLeft.startAnimation(animationLeft);
        mRlRight.startAnimation(animationRight);
        mIvBottom.startAnimation(animationBottom);


        // 抖动动画
        ObjectAnimator animatorLeft = ObjectAnimator.ofFloat(mRlLeftArea, "translationX", 0, U.getDisplayUtils().dip2px(20), 0);
        ObjectAnimator animatorRight = ObjectAnimator.ofFloat(mRlRightArea, "translationX", 0, -U.getDisplayUtils().dip2px(20), 0);

        animatorLeft.setDuration(300);
        animatorLeft.setStartDelay(300);
        animatorLeft.setRepeatMode(ValueAnimator.REVERSE);
        animatorLeft.setInterpolator(new OvershootInterpolator());
        animatorLeft.start();

        animatorRight.setDuration(300);
        animatorRight.setStartDelay(300);
        animatorRight.setRepeatMode(ValueAnimator.REVERSE);
        animatorRight.setInterpolator(new OvershootInterpolator());
        animatorRight.start();

        // 灭灯按钮动画
        ScaleAnimation scaleAnimationL = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.match_sucess_title_anim);
        ScaleAnimation scaleAnimationR = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.match_sucess_title_anim);
        scaleAnimationL.setDuration(250);
        scaleAnimationR.setDuration(250);
        scaleAnimationL.setStartOffset(50);
        scaleAnimationR.setStartOffset(50);
        scaleAnimationL.setInterpolator(new OvershootInterpolator(3));
        scaleAnimationR.setInterpolator(new OvershootInterpolator(3));
        mVoteLeftMie.startAnimation(scaleAnimationL);
        mVoteRightMie.startAnimation(scaleAnimationR);

        HandlerTaskTimer.newBuilder().delay(500)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mLeftVoteAnimationSet = new AnimatorSet();//组合动画

                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mVoteLeftMie, "scaleX", 1.0f, 1.1f, 1.0f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mVoteLeftMie, "scaleY", 1.0f, 1.1f, 1.0f);

                        scaleX.setRepeatCount(ValueAnimator.INFINITE);
                        scaleY.setRepeatCount(ValueAnimator.INFINITE);

                        scaleX.setDuration(500);
                        scaleY.setDuration(500);

                        mLeftVoteAnimationSet.play(scaleX).with(scaleY);
                        mLeftVoteAnimationSet.start();
                    }
                });

        HandlerTaskTimer.newBuilder().delay(750)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mRightVoteAnimationSet = new AnimatorSet();//组合动画

                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mVoteRightMie, "scaleX", 1.0f, 1.1f, 1.0f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mVoteRightMie, "scaleY", 1.0f, 1.1f, 1.0f);

                        scaleX.setRepeatCount(ValueAnimator.INFINITE);
                        scaleY.setRepeatCount(ValueAnimator.INFINITE);

                        scaleX.setDuration(500);
                        scaleY.setDuration(500);

                        mRightVoteAnimationSet.play(scaleX).with(scaleY);
                        mRightVoteAnimationSet.start();
                    }
                });

//        ObjectAnimator countDownT = ObjectAnimator.ofFloat(mRlCountDownContainer, "translationY", -200, 0.0f);
//        ObjectAnimator countDownA = ObjectAnimator.ofFloat(mRlCountDownContainer, "alpha", 0.5f, 1.0f);
//        countDownT.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                mRlCountDownContainer.setVisibility(View.VISIBLE);
//            }
//        });

//        countDownT.setStartDelay(300);
//        countDownA.setStartDelay(300);
//        countDownT.setDuration(200);
//        countDownA.setDuration(200);
//
//        AnimatorSet countDownSet = new AnimatorSet();
//        countDownSet.play(countDownT).with(countDownA);
//        countDownSet.start();
    }

    /**
     * 更新评分倒计时
     */
    public void startTimeTask() {
        mVoteTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(12)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        U.getSoundUtils().play(TAG, R.raw.general_countdown);
                        mVoteDownTv.setText(String.format(U.app().getString(R.string.evaluation_time_info), 12 - integer));
                    }

                    @Override
                    public void onComplete() {
                        stopTimeTask();
                        if (!isJumpToRecord) {
                            isJumpToRecord = true;
                            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), RankRecordFragment.class)
                                    .setAddToBackStack(true)
                                    .addDataBeforeAdd(1, mRoomData)
                                    .build()
                            );
                        }
                    }
                });
    }

    public void stopTimeTask() {
        if (mVoteTimeTask != null) {
            mVoteTimeTask.dispose();
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (RoomData) data;
            if (mRoomData.getPlayerInfoList() != null && mRoomData.getPlayerInfoList().size() > 0) {
                for (PlayerInfoModel playerInfo : mRoomData.getPlayerInfoList()) {
                    if (left != null && playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                        right = playerInfo;
                    } else if (playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                        left = playerInfo;
                    }
                }
            }
        }
    }

    @Override
    public void voteSucess(long votedUserId) {
        if (mLeftVoteAnimationSet != null) {
            mLeftVoteAnimationSet.cancel();
        }

        U.getSoundUtils().play(TAG, R.raw.select_dislikebutton, 500);
        HandlerTaskTimer.newBuilder().delay(250).start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                if (mRightVoteAnimationSet != null) {
                    mRightVoteAnimationSet.cancel();
                }
            }
        });

        if (right.getUserInfo().getUserId() == votedUserId) {
            mVoteRightMie.setSelected(true);
            mVoteLeftMie.setClickable(false);
            mVoteRightMie.setClickable(false);
//            HandlerTaskTimer.newBuilder()
//                    .delay(200)
//                    .interval(50)
//                    .take(5)
//                    .start(new HandlerTaskTimer.ObserverW() {
//                        @Override
//                        public void onNext(Integer integer) {
//                            mVoteLeftShadowIv.setVisibility(integer % 2 == 1 ? View.VISIBLE : View.GONE);
//                        }
//                    });

            // 右边冒星星
            mStartRightSvga.setVisibility(View.VISIBLE);
            mStartRightSvga.startAnimation();
            mStartRightSvga.setCallback(new SVGACallback() {
                @Override
                public void onPause() {

                }

                @Override
                public void onFinished() {
                    mStartRightSvga.stopAnimation(true);
                    mStartRightSvga.setVisibility(View.GONE);
                }

                @Override
                public void onRepeat() {
                    if (mStartRightSvga.isAnimating()) {
                        mStartRightSvga.stopAnimation(false);
                    }
                }

                @Override
                public void onStep(int i, double v) {

                }
            });
        } else if (left.getUserInfo().getUserId() == votedUserId) {
            mVoteLeftMie.setSelected(true);
            mVoteLeftMie.setClickable(false);
            mVoteRightMie.setClickable(false);
//            HandlerTaskTimer.newBuilder()
//                    .delay(200)
//                    .interval(50)
//                    .take(5)
//                    .start(new HandlerTaskTimer.ObserverW() {
//                        @Override
//                        public void onNext(Integer integer) {
//                            mVoteRightShadowIv.setVisibility(integer % 2 == 1 ? View.VISIBLE : View.GONE);
//                        }
//                    });

            // 左边冒星星
            mStartLeftSvga.setVisibility(View.VISIBLE);
            mStartLeftSvga.startAnimation();
            mStartLeftSvga.setCallback(new SVGACallback() {
                @Override
                public void onPause() {

                }

                @Override
                public void onFinished() {
                    mStartLeftSvga.stopAnimation(true);
                    mStartLeftSvga.setVisibility(View.GONE);
                }

                @Override
                public void onRepeat() {
                    if (mStartLeftSvga.isAnimating()) {
                        mStartLeftSvga.stopAnimation(false);
                    }

                }

                @Override
                public void onStep(int i, double v) {

                }
            });
        }
    }

    @Override
    public void voteFailed() {
        mVoteLeftMie.setClickable(true);
        mVoteRightMie.setClickable(true);
    }

    @Override
    public void showRecordView(RecordData recordData) {
        stopTimeTask();
        mUiHanlder.removeMessages(MSG_GO_RECORD_FRAGMENT);

        if (!isJumpToRecord) {
            isJumpToRecord = true;
            if (recordData != null) {
                mRoomData.setRecordData(recordData);
            }
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), RankRecordFragment.class)
                    .setAddToBackStack(true)
                    .addDataBeforeAdd(1, mRoomData)
                    .build()
            );
        }

    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimeTask();
        if (mLeftVoteAnimationSet != null) {
            mLeftVoteAnimationSet.cancel();
        }
        if (mRightVoteAnimationSet != null) {
            mRightVoteAnimationSet.cancel();
        }

        if (mStartLeftSvga != null) {
            mStartLeftSvga.stopAnimation(true);
        }

        if (mStartRightSvga != null) {
            mStartRightSvga.stopAnimation(true);
        }
        if (mUiHanlder != null) {
            mUiHanlder.removeCallbacksAndMessages(null);
        }
        U.getSoundUtils().release(TAG);
    }

    @Override
    protected boolean onBackPressed() {
        stopTimeTask();
        getActivity().finish();
        return true;
    }
}

package com.module.playways.rank.prepare.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.manager.BgMusicManager;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.model.GameReadyModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.model.ReadyInfoModel;
import com.module.playways.rank.prepare.presenter.MatchSucessPresenter;
import com.module.playways.rank.prepare.view.IMatchSucessView;
import com.module.playways.rank.prepare.view.MatchSucessLeftView;
import com.module.playways.rank.prepare.view.MatchSucessRightView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGAImageView;
import com.zq.live.proto.Common.ESex;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchSuccessFragment extends BaseFragment implements IMatchSucessView {

    public final static String TAG = "MatchSuccessFragment";

    ExTextView mTvReadyTime;
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvIcon2;
    SimpleDraweeView mSdvIcon3;

    SVGAImageView mVsSvga;
    ExImageView mIvPrepare;

    View mBgLeftView;
    View mBgRightView;

    RelativeLayout mBottomContainer;

    MatchSucessPresenter mMatchSucessPresenter;

    volatile boolean isPrepared = false;

    PrepareData mPrepareData;

    PlayerInfoModel mLeftPlayer;

    PlayerInfoModel mRightPlayer;

    HandlerTaskTimer mReadyTimeTask;

    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.match_success_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvReadyTime = (ExTextView) mRootView.findViewById(R.id.tv_ready_time);
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);

        mVsSvga = (SVGAImageView) mRootView.findViewById(R.id.vs_svga);
        mIvPrepare = (ExImageView) mRootView.findViewById(R.id.iv_prepare);
        mBgLeftView = (MatchSucessLeftView) mRootView.findViewById(R.id.bg_left_view);
        mBgRightView = (MatchSucessRightView) mRootView.findViewById(R.id.bg_right_view);
        mBottomContainer = (RelativeLayout) mRootView.findViewById(R.id.bottom_container);

        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }

        if (mPrepareData.getPlayerInfoList() != null && mPrepareData.getPlayerInfoList().size() > 0) {
            for (PlayerInfoModel playerInfo : mPrepareData.getPlayerInfoList()) {
                if (mLeftPlayer != null && playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                    mRightPlayer = playerInfo;
                } else if (playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                    mLeftPlayer = playerInfo;
                }
            }
        }

        RxView.clicks(mIvPrepare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getSoundUtils().play(TAG, R.raw.pregame_ready);
                    mIvPrepare.setBackground(getResources().getDrawable(R.drawable.btn_pipeichenggong_pressed));
                    mIvPrepare.setClickable(false);
                    mMatchSucessPresenter.prepare(!isPrepared);
                });

        initAvatar(true);

        mMatchSucessPresenter = new MatchSucessPresenter(this, mPrepareData.getGameId(), mPrepareData);
        addPresent(mMatchSucessPresenter);

        U.getSoundUtils().preLoad(TAG, R.raw.pregame_animation, R.raw.pregame_ready, R.raw.general_countdown);


        startTimeTask();
        animationGo();
    }

    private void loadIcon(SimpleDraweeView simpleDraweeView) {
        AvatarUtils.loadAvatarByUrl(simpleDraweeView,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());
    }

    private void animationGo() {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getSoundUtils().play(TAG, R.raw.pregame_animation);
            }
        }, 500);

        //三块颜色背景
        TranslateAnimation animationLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0.0f);

        TranslateAnimation animationRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0.0f);

        TranslateAnimation animationBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);

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

        mBgLeftView.startAnimation(animationLeft);
        mBgRightView.startAnimation(animationRight);
        mBottomContainer.startAnimation(animationBottom);

        //头部文字
//        AnimatorSet animatorSet = new AnimatorSet();//组合动画
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mIvTop, "scaleX", 3.0f, 0.9f, 1.05f, 0.95f, 1.02f, 0.98f, 1.0f);
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mIvTop, "scaleY", 3.0f, 0.9f, 1.05f, 0.95f, 1.02f, 0.98f, 1.0f);
//        scaleX.setInterpolator(new DecelerateInterpolator(1));
//        scaleY.setInterpolator(new DecelerateInterpolator(1));
//        scaleX.setDuration(3000);
//        scaleY.setDuration(3000);
//        ObjectAnimator alpha = ObjectAnimator.ofFloat(mIvTop, "alpha", 0.4f, 1.0f);
//        alpha.setDuration(1200);
//        animatorSet.play(scaleX).with(scaleY).with(alpha);
//        animatorSet.start();

        //三个头像
        TranslateAnimation animationIconOne = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth() / 2, 0, -U.getDisplayUtils().getScreenHeight() / 2, 0);

        TranslateAnimation animationIconSecond = new TranslateAnimation(U.getDisplayUtils().getScreenWidth(), 0, -U.getDisplayUtils().getScreenHeight() / 2, 0);

        TranslateAnimation animationIconThird = new TranslateAnimation(0, 0, U.getDisplayUtils().getScreenHeight() / 2, 0);


        animationIconOne.setDuration(300);
        animationIconOne.setStartOffset(300);
        animationIconOne.setRepeatMode(Animation.REVERSE);
        animationIconOne.setInterpolator(new OvershootInterpolator());
        animationIconOne.setFillAfter(true);

        animationIconSecond.setDuration(300);
        animationIconSecond.setStartOffset(300);
        animationIconSecond.setRepeatMode(Animation.REVERSE);
        animationIconSecond.setInterpolator(new OvershootInterpolator());
        animationIconSecond.setFillAfter(true);

        animationIconThird.setDuration(300);
        animationIconThird.setStartOffset(300);

        animationIconThird.setRepeatMode(Animation.REVERSE);
        animationIconThird.setInterpolator(new OvershootInterpolator());
        animationIconThird.setFillAfter(true);

        mSdvIcon1.startAnimation(animationIconOne);
        mSdvIcon2.startAnimation(animationIconSecond);
        mSdvIcon3.startAnimation(animationIconThird);


        //3个头像的抖动动画
        ObjectAnimator animatorOne = ObjectAnimator.ofFloat(mSdvIcon1, "translationX", 0, U.getDisplayUtils().dip2px(20), 0);
        ObjectAnimator animatorSecond = ObjectAnimator.ofFloat(mSdvIcon2, "translationX", 0, -U.getDisplayUtils().dip2px(20), 0);
        ObjectAnimator animatorThird = ObjectAnimator.ofFloat(mSdvIcon3, "translationY", 0, -U.getDisplayUtils().dip2px(20), 0);

        animatorOne.setDuration(300);
        animatorOne.setStartDelay(750);
        animatorOne.setRepeatMode(ValueAnimator.REVERSE);
        animatorOne.setInterpolator(new OvershootInterpolator());
        animatorOne.start();

        animatorSecond.setDuration(300);
        animatorSecond.setStartDelay(750);
        animatorSecond.setRepeatMode(ValueAnimator.REVERSE);
        animatorSecond.setInterpolator(new OvershootInterpolator());
        animatorSecond.start();

        animatorThird.setDuration(300);
        animatorThird.setStartDelay(750);
        animatorThird.setRepeatMode(ValueAnimator.REVERSE);
        animatorThird.setInterpolator(new OvershootInterpolator());
        animatorThird.start();
    }


    /**
     * 更新准备时间倒计时
     */
    public void startTimeTask() {
        mReadyTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(10)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        U.getSoundUtils().play(TAG, R.raw.general_countdown);
                        if(10 - integer < 0){
                            return;
                        }
                        mTvReadyTime.setText(String.format(U.app().getString(R.string.ready_time_info), 10 - integer));
                    }
                });
    }

    public void stopTimeTask() {
        if (mReadyTimeTask != null) {
            mReadyTimeTask.dispose();
        }
    }


    private void initAvatar(boolean isGray) {
        AvatarUtils.loadAvatarByUrl(mSdvIcon1,
                AvatarUtils.newParamsBuilder(mLeftPlayer.getUserInfo().getAvatar())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColorBySex(mLeftPlayer.getUserInfo().getIsMale())
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                AvatarUtils.newParamsBuilder(mRightPlayer.getUserInfo().getAvatar())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColorBySex(mRightPlayer.getUserInfo().getIsMale())
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon3,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue())
                        .build());
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mPrepareData = (PrepareData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void ready(boolean isPrepareState) {
        MyLog.d(TAG, "ready" + " isPrepareState=" + isPrepareState);
        isPrepared = isPrepareState;
        if (isPrepared) {
//            stopTimeTask();
            U.getToastUtil().showShort("已准备");
            mIvPrepare.setEnabled(false);
        }
    }

    @Override
    public void readyList(List<ReadyInfoModel> readyInfos) {
        if (readyInfos == null || readyInfos.size() == 0) {
            return;
        }

        for (ReadyInfoModel jsonReadyInfo : readyInfos) {
            if (jsonReadyInfo.getUserID() == mLeftPlayer.getUserInfo().getUserId()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon1,
                        AvatarUtils.newParamsBuilder(mLeftPlayer.getUserInfo().getAvatar())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColorBySex(mLeftPlayer.getUserInfo().getIsMale())
                                .build());
            }

            if (jsonReadyInfo.getUserID() == mRightPlayer.getUserInfo().getUserId()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                        AvatarUtils.newParamsBuilder(mRightPlayer.getUserInfo().getAvatar())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColorBySex(mRightPlayer.getUserInfo().getIsMale())
                                .build());
            }

            if (jsonReadyInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon3,
                        AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue())
                                .build());
            }
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {
        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis() - jsonGameReadyInfo.getGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getGameStartInfo().getStartTimeMs()));

        initAvatar(false);

        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANK_ROOM)
                .withSerializable("prepare_data", mPrepareData)
                .navigation();

        //直接到首页，不是选歌界面
        getActivity().finish();
    }

    @Override
    public void needReMatch(boolean otherEr) {
        MyLog.d(TAG, "needReMatch 有人没准备，需要重新匹配");
        mMatchSucessPresenter.exitGame();
        goMatch(otherEr);
        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }

    @Override
    public boolean isReady() {
        return isPrepared;
    }

    void goMatch(boolean otherEr) {
        // 如果已经准备了就从新开始匹配，没有准备就直接跳转到选择歌曲界面
        // 如果rematch的时候是因为别人退出房间的原因导致rematch直接跳转到match界面
        if (isPrepared || otherEr) {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), MatchFragment.class)
                    .setNotifyHideFragment(MatchSuccessFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, mPrepareData)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                        }
                    })
                    .build());

            U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                    .setActivity(getActivity())
                    .setPopFragment(MatchSuccessFragment.this)
                    .setPopAbove(false)
                    .setHasAnimation(true)
                    .build());
        } else {
            U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                    .setActivity(getActivity())
                    .setPopFragment(MatchSuccessFragment.this)
                    .setPopAbove(false)
                    .setHasAnimation(true)
                    .setNotifyShowFragment(PrepareResFragment.class)
                    .build());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMatchSucessPresenter.destroy();
        stopTimeTask();
    }

    @Override
    protected boolean onBackPressed() {
        //主动触发回退直接到PrepareResFragment界面
        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                .setActivity(getActivity())
                .setPopFragment(MatchSuccessFragment.this)
                .setPopAbove(false)
                .setHasAnimation(true)
                .setNotifyShowFragment(PrepareResFragment.class)
                .build());
        mMatchSucessPresenter.exitGame();
        return true;
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyToHide() {
        MyLog.d(TAG, "pushIntoStash");
        mRootView.setVisibility(View.GONE);
//        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
//                .setPopFragment(this)
//                .setPopAbove(false)
//                .build()
//        );
    }
}

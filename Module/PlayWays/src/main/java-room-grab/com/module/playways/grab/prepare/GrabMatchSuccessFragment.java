package com.module.playways.grab.prepare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
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
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrabMatchSuccessFragment extends BaseFragment implements IMatchSucessView {

    public final static String TAG = "MatchSuccessFragment";

    ExTextView mTvReadyTime;
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvIcon2;
    SimpleDraweeView mSdvIcon3;
    SimpleDraweeView mSdvIcon4;
    SimpleDraweeView mSdvIcon5;

    ExRelativeLayout mRlIcon1Root;
    ExRelativeLayout mRlIcon2Root;
    ExRelativeLayout mRlIcon3Root;
    ExRelativeLayout mRlIcon4Root;
    ExRelativeLayout mRlIcon5Root;

    SVGAImageView mVsSvga;
    ExImageView mIvPrepare;

    View mBgLeftView;
    View mBgRightView;

    RelativeLayout mBottomContainer;

    MatchSucessPresenter mMatchSucessPresenter;

    volatile boolean isPrepared = false;

    PrepareData mPrepareData;

    HandlerTaskTimer mReadyTimeTask;

//    SVGAImageView mSvgaMatchSuccessBg;

    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.grab_match_success_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvReadyTime = (ExTextView) mRootView.findViewById(R.id.tv_ready_time);
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);
        mSdvIcon4 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon4);
        mSdvIcon5 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon5);

        mVsSvga = (SVGAImageView) mRootView.findViewById(R.id.vs_svga);
        mIvPrepare = (ExImageView) mRootView.findViewById(R.id.iv_prepare);
        mBgLeftView = (MatchSucessLeftView) mRootView.findViewById(R.id.bg_left_view);
        mBgRightView = (MatchSucessRightView) mRootView.findViewById(R.id.bg_right_view);
        mBottomContainer = (RelativeLayout) mRootView.findViewById(R.id.bottom_container);
//        mSvgaMatchSuccessBg = (SVGAImageView)mRootView.findViewById(R.id.svga_match_success_bg);

        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }

//        if (mPrepareData.getPlayerInfoList() != null && mPrepareData.getPlayerInfoList().size() > 0) {
//            for (PlayerInfoModel playerInfo : mPrepareData.getPlayerInfoList()) {
//                if (mLeftPlayer != null && playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
//                    mRightPlayer = playerInfo;
//                } else if (playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
//                    mLeftPlayer = playerInfo;
//                }
//            }
//        }

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

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVsSvga.setVisibility(View.VISIBLE);
                mVsSvga.startAnimation();
            }
        }, 1200);
    }

    private void loadIcon(SimpleDraweeView simpleDraweeView, boolean isGray, String avatar) {
        AvatarUtils.loadAvatarByUrl(simpleDraweeView,
                AvatarUtils.newParamsBuilder(avatar)
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(6))
                        .setBorderColor(U.getColor(R.color.white))
                        .build());
    }

    private void animationGo() {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getSoundUtils().play(TAG, R.raw.pregame_animation);
            }
        }, 500);

        playScaleAnim(mRlIcon1Root, 0);
        playScaleAnim(mRlIcon2Root, 50);
        playScaleAnim(mRlIcon3Root, 100);
        playScaleAnim(mRlIcon4Root, 150);
        playScaleAnim(mRlIcon5Root, 200);

    }

    private void playScaleAnim(View simpleDraweeView, long delay){
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleXPre = ObjectAnimator.ofFloat(simpleDraweeView, "scaleX", 1.0f, 0.5f);
        ObjectAnimator scaleYPre = ObjectAnimator.ofFloat(simpleDraweeView, "scaleY", 1.0f, 0.5f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(simpleDraweeView, "scaleX", 0.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(simpleDraweeView, "scaleY", 0.5f, 1.0f);
        scaleX.setInterpolator(new OvershootInterpolator(2));
        scaleY.setInterpolator(new OvershootInterpolator(2));
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setStartDelay(delay);
        scaleY.setStartDelay(delay);
        scaleXPre.setDuration(0);
        scaleYPre.setDuration(0);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(simpleDraweeView, "alpha", 0.5f, 1.0f);
        alpha.setDuration(500);
        alpha.setStartDelay(delay);
        animatorSet.play(scaleX).with(scaleY).with(alpha).with(scaleXPre).with(scaleYPre);
        animatorSet.start();
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
        mPrepareData.getPlayerInfoList().get(0);
        loadIcon(mSdvIcon1, isGray, mPrepareData.getPlayerInfoList().get(0).getUserInfo().getAvatar());
        loadIcon(mSdvIcon2, isGray, mPrepareData.getPlayerInfoList().get(1).getUserInfo().getAvatar());
        loadIcon(mSdvIcon3, isGray, mPrepareData.getPlayerInfoList().get(2).getUserInfo().getAvatar());
        loadIcon(mSdvIcon4, isGray, mPrepareData.getPlayerInfoList().get(3).getUserInfo().getAvatar());
        loadIcon(mSdvIcon5, isGray, mPrepareData.getPlayerInfoList().get(4).getUserInfo().getAvatar());

        mSdvIcon1.setTag("sdv" + mPrepareData.getPlayerInfoList().get(0).getUserInfo().getUserId());
        mSdvIcon2.setTag("sdv" + mPrepareData.getPlayerInfoList().get(1).getUserInfo().getUserId());
        mSdvIcon3.setTag("sdv" + mPrepareData.getPlayerInfoList().get(2).getUserInfo().getUserId());
        mSdvIcon4.setTag("sdv" + mPrepareData.getPlayerInfoList().get(3).getUserInfo().getUserId());
        mSdvIcon5.setTag("sdv" + mPrepareData.getPlayerInfoList().get(4).getUserInfo().getUserId());

        mRlIcon1Root = (ExRelativeLayout)mRootView.findViewById(R.id.rl_icon1_root);
        mRlIcon2Root = (ExRelativeLayout)mRootView.findViewById(R.id.rl_icon2_root);
        mRlIcon3Root = (ExRelativeLayout)mRootView.findViewById(R.id.rl_icon3_root);
        mRlIcon4Root = (ExRelativeLayout)mRootView.findViewById(R.id.rl_icon4_root);
        mRlIcon5Root = (ExRelativeLayout)mRootView.findViewById(R.id.rl_icon5_root);

        setIconStroke(mRlIcon1Root, mPrepareData.getPlayerInfoList().get(0).getUserInfo().getIsMale());
        setIconStroke(mRlIcon2Root, mPrepareData.getPlayerInfoList().get(1).getUserInfo().getIsMale());
        setIconStroke(mRlIcon3Root, mPrepareData.getPlayerInfoList().get(2).getUserInfo().getIsMale());
        setIconStroke(mRlIcon4Root, mPrepareData.getPlayerInfoList().get(3).getUserInfo().getIsMale());
        setIconStroke(mRlIcon5Root, mPrepareData.getPlayerInfoList().get(4).getUserInfo().getIsMale());

        showBackground();
    }

    public void showBackground() {
//        mSvgaMatchSuccessBg.setVisibility(View.VISIBLE);
//        mSvgaMatchSuccessBg.setLoops(1);
//
//        SVGAParser parser = new SVGAParser(getContext());
//        try {
//            parser.parse("grab_match_success.svga", new SVGAParser.ParseCompletion() {
//                @Override
//                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
//                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(MyUserInfoManager.getInstance().getAvatar()));
//                    mSvgaMatchSuccessBg.setLoops(-1);
//                    mSvgaMatchSuccessBg.setImageDrawable(drawable);
//                    mSvgaMatchSuccessBg.startAnimation();
//                }
//
//                @Override
//                public void onError() {
//
//                }
//            });
//        } catch (Exception e) {
//            System.out.print(true);
//        }
    }

    private SVGADynamicEntity requestDynamicItem(String avatar) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(avatar)) {
            dynamicEntity.setDynamicImage(avatar, "img_10");
            dynamicEntity.setDynamicImage(avatar, "img_11");
            dynamicEntity.setDynamicImage(avatar, "img_12");
            dynamicEntity.setDynamicImage(avatar, "img_13");
            dynamicEntity.setDynamicImage(avatar, "img_9");
        }
        return dynamicEntity;
    }

    private void setIconStroke(ExRelativeLayout exRelativeLayout, boolean isMale){
        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setStrokeWidth(U.getDisplayUtils().dip2px(3))
                .setStrokeColor(isMale ? U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color))
                .setSolidColor(isMale ? U.getColor(R.color.color_man_stroke_color_trans_20) : U.getColor(R.color.color_woman_stroke_color_trans_20))
                .build();

        exRelativeLayout.setBackground(drawable);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
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
            SimpleDraweeView mSdvIcon = mRootView.findViewWithTag("sdv" + jsonReadyInfo.getUserID());
            for(PlayerInfoModel playerInfoModel : mPrepareData.getPlayerInfoList()){
                if(playerInfoModel.getUserInfo().getUserId() == jsonReadyInfo.getUserID()){
                    loadIcon(mSdvIcon, false, playerInfoModel.getUserInfo().getAvatar());
                }
            }
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {
        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis() - jsonGameReadyInfo.getJsonGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs()));

        initAvatar(false);

        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
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
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabMatchFragment.class)
                    .setNotifyHideFragment(GrabMatchSuccessFragment.class)
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
                    .setPopFragment(GrabMatchSuccessFragment.this)
                    .setPopAbove(false)
                    .setHasAnimation(true)
                    .build());
        } else {
            getActivity().finish();
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
        mMatchSucessPresenter.exitGame();
        getActivity().finish();
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

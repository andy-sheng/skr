package com.module.rankingmode.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

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
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.GameReadyModel;
import com.module.rankingmode.prepare.model.JsonReadyInfo;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.MatchSucessPresenter;
import com.module.rankingmode.prepare.view.IMatchSucessView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchSuccessFragment extends BaseFragment implements IMatchSucessView {
    ExImageView mIvTop;
    ExTextView mTvReadyTime;
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvIcon2;
    SimpleDraweeView mSdvIcon3;
    ImageView mIvVs;
    ExImageView mIvPrepare;

    MatchSucessPresenter mMatchSucessPresenter;

    volatile boolean isPrepared = false;

    PrepareData mPrepareData;

    PlayerInfo leftPlayer;
    PlayerInfo rightPlayer;

    HandlerTaskTimer mReadyTimeTask;

    @Override
    public int initView() {
        return R.layout.match_success_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvTop = (ExImageView) mRootView.findViewById(R.id.iv_top);
        mTvReadyTime = (ExTextView) mRootView.findViewById(R.id.tv_ready_time);
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);
        mIvVs = (ImageView) mRootView.findViewById(R.id.iv_vs);
        mIvPrepare = (ExImageView) mRootView.findViewById(R.id.iv_prepare);

        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }

        if (mPrepareData.getPlayerInfoList() != null && mPrepareData.getPlayerInfoList().size() > 0) {
            for (PlayerInfo playerInfo : mPrepareData.getPlayerInfoList()) {
                if (leftPlayer != null && playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                    rightPlayer = playerInfo;
                } else if (playerInfo.getUserInfo().getUserId() != MyUserInfoManager.getInstance().getUid()) {
                    leftPlayer = playerInfo;
                }
            }
        }

        RxView.clicks(mIvPrepare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mIvPrepare.setBackground(getResources().getDrawable(R.drawable.btn_pipeichenggong_pressed));
                    mIvPrepare.setClickable(false);
                    mMatchSucessPresenter.prepare(!isPrepared);
                });

        initAvatar(true);

        mMatchSucessPresenter = new MatchSucessPresenter(this, mPrepareData.getGameId(), mPrepareData);
        addPresent(mMatchSucessPresenter);

        startTimeTask();
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
                AvatarUtils.newParamsBuilder(leftPlayer.getSongList().get(0).getCover())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                AvatarUtils.newParamsBuilder(rightPlayer.getSongList().get(0).getCover())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon3,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setGray(isGray)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());
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
            stopTimeTask();
            U.getToastUtil().showShort("已准备");
            mIvPrepare.setEnabled(false);
        }
    }

    @Override
    public void readyList(List<JsonReadyInfo> readyInfos) {
        if (readyInfos == null || readyInfos.size() == 0) {
            return;
        }

        for (JsonReadyInfo jsonReadyInfo : readyInfos) {
            if (jsonReadyInfo.getUserID() == leftPlayer.getUserInfo().getUserId()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon1,
                        AvatarUtils.newParamsBuilder(leftPlayer.getSongList().get(0).getCover())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColor(Color.WHITE)
                                .build());
            }

            if (jsonReadyInfo.getUserID() == rightPlayer.getUserInfo().getUserId()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                        AvatarUtils.newParamsBuilder(rightPlayer.getSongList().get(0).getCover())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColor(Color.WHITE)
                                .build());
            }

            if (jsonReadyInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                AvatarUtils.loadAvatarByUrl(mSdvIcon3,
                        AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                                .setCircle(true)
                                .setGray(false)
                                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                                .setBorderColor(Color.WHITE)
                                .build());
            }
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {
        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis() - jsonGameReadyInfo.getJsonGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs()));

        initAvatar(false);

        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                .withSerializable("prepare_data", mPrepareData)
                .navigation();

        //直接到首页，不是选歌界面
        getActivity().finish();
    }

    @Override
    public void needReMatch() {
        MyLog.d(TAG, "needReMatch 有人没准备，需要重新匹配");
        mMatchSucessPresenter.exitGame();
        goMatch();
        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }


    void goMatch() {
        // 这个activity直接到匹配页面,可是这时匹配中页面已经销毁了
        // 如果已经准备了就从新开始匹配，没有准备就直接跳转到选择歌曲界面
        if (isPrepared) {
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
//            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), SongSelectFragment.class)
//                    .setNotifyHideFragment(MatchSuccessFragment.class)
//                    .setAddToBackStack(false)
//                    .setHasAnimation(false)
//                    .addDataBeforeAdd(0, mPrepareData)
//                    .setFragmentDataListener(new FragmentDataListener() {
//                        @Override
//                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
//
//                        }
//                    })
//                    .build());

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
    public void onDestroyView() {
        super.onDestroyView();
        mMatchSucessPresenter.destroy();
        stopTimeTask();
    }

    @Override
    protected boolean onBackPressed() {
        goMatch();
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

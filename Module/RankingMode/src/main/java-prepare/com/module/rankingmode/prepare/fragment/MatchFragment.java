package com.module.rankingmode.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.MatchPresenter;
import com.module.rankingmode.prepare.view.IMatchingView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;

//这个是匹配界面，之前的FastMatchingSence
public class MatchFragment extends BaseFragment implements IMatchingView {
    ExImageView mIvBack;
    ExImageView mIvTop;
    ExTextView mTvMatchedTime;
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvIcon3;
    SimpleDraweeView mSdvIcon2;
    ExTextView mTvTip;
    ExImageView mIvCancelMatch;

    MatchPresenter mMatchPresenter;
    PrepareData mPrepareData;

    HandlerTaskTimer mMatchTimeTask;

    @Override
    public int initView() {
        return R.layout.match_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mIvTop = (ExImageView) mRootView.findViewById(R.id.iv_top);
        mTvMatchedTime = (ExTextView) mRootView.findViewById(R.id.tv_matched_time);
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mTvTip = (ExTextView) mRootView.findViewById(R.id.tv_tip);
        mIvCancelMatch = (ExImageView) mRootView.findViewById(R.id.iv_cancel_match);

        RxView.clicks(mIvCancelMatch)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    goBack();
                });

        RxView.clicks(mIvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    goBack();
                });

        AvatarUtils.loadAvatarByUrl(mSdvIcon1,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon2,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        AvatarUtils.loadAvatarByUrl(mSdvIcon3,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        mMatchPresenter = new MatchPresenter(this);
        mMatchPresenter.startLoopMatchTask(mPrepareData.getSongModel().getItemID());

        startTimeTask();
    }


    /**
     * 更新已匹配时间
     */
    public void startTimeTask() {
        mMatchTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mTvMatchedTime.setText(String.format(U.app().getString(R.string.match_time_info), integer));
                    }
                });
    }

    public void stopTimeTask() {
        if (mMatchTimeTask != null) {
            mMatchTimeTask.dispose();
        }
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mPrepareData = (PrepareData) data;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMatchPresenter.destroy();
    }

    void goBack() {
        mMatchPresenter.cancelMatch();
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip("马上要为你匹配到对手了\n还要退出吗？")
                .setConfirmTip("退出")
                .setCancelTip("继续匹配")
                .build();

        DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == R.id.confirm_tv) {
                                dialog.dismiss();
                                mMatchPresenter.cancelMatch();
                                stopTimeTask();
                                U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                        .setPopFragment(MatchFragment.this)
                                        .setPopAbove(false)
                                        .setNotifyShowFragment(PrepareResFragment.class)
                                        .build());
                            }

                            if (view.getId() == R.id.cancel_tv) {
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .create().show();

    }

    @Override
    public void matchSucess(int gameId, long gameCreatMs, List<PlayerInfo> playerInfoList) {
        MyLog.d(TAG, "matchSucess" + " gameId=" + gameId + " gameCreatMs=" + gameCreatMs + " playerInfoList=" + playerInfoList);
        mPrepareData.setGameId(gameId);
        mPrepareData.setGameCreatMs(gameCreatMs);
        mPrepareData.setPlayerInfoList(playerInfoList);

        stopTimeTask();
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), MatchSuccessFragment.class)
                .setAddToBackStack(false)
                .setNotifyHideFragment(MatchFragment.class)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mPrepareData)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                    }
                })
                .build());
    }

    @Override
    public void showUserIconList() {

    }

    @Override
    protected boolean onBackPressed() {
        goBack();
        return true;
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        mRootView.setVisibility(View.VISIBLE);
    }

    /**
     * MatchSuccessFragment add后，动画播放完再remove掉匹配中页面
     */
    @Override
    public void notifyToHide() {
        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                .setPopFragment(this)
                .setPopAbove(false)
                .build()
        );
    }
}

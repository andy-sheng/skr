package com.module.rankingmode.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.GameReadyModel;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.MatchSucessPresenter;
import com.module.rankingmode.prepare.view.IMatchSucessView;
import com.module.rankingmode.song.event.SongSelectEventClass;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

public class MatchSuccessFragment extends BaseFragment implements IMatchSucessView {
    ExImageView mIvTop;
    SimpleDraweeView mSdvIcon1;
    SimpleDraweeView mSdvIcon2;
    SimpleDraweeView mSdvIcon3;
    ImageView mIvVs;
    ExImageView mIvPrepare;

    MatchSucessPresenter mMatchSucessPresenter;

    volatile boolean isPrepared = false;

    PrepareData mPrepareData;

    @Override
    public int initView() {
        return R.layout.match_success_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvTop = (ExImageView)mRootView.findViewById(R.id.iv_top);
        mSdvIcon1 = (SimpleDraweeView)mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon2 = (SimpleDraweeView)mRootView.findViewById(R.id.sdv_icon2);
        mSdvIcon3 = (SimpleDraweeView)mRootView.findViewById(R.id.sdv_icon3);
        mIvVs = (ImageView)mRootView.findViewById(R.id.iv_vs);
        mIvPrepare = (ExImageView)mRootView.findViewById(R.id.iv_prepare);

        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }

        RxView.clicks(mIvPrepare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {

                    mMatchSucessPresenter.prepare(!isPrepared);
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

        mMatchSucessPresenter = new MatchSucessPresenter(this, mPrepareData.getGameId(), mPrepareData);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if(type == 0){
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

        if(isPrepared){
            U.getToastUtil().showShort("已准备");
            mIvPrepare.setEnabled(false);
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {

        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis()-jsonGameReadyInfo.getJsonGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs()));
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                .withSerializable("prepare_data", mPrepareData)
                .greenChannel().navigation();

        EventBus.getDefault().post(new SongSelectEventClass.PopSelectSongFragment());

        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopAbove(false)
                .setPopFragment(MatchSuccessFragment.this).build());
    }

    @Override
    public void needReMatch() {
        MyLog.d(TAG, "needReMatch");

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((FragmentActivity) MatchSuccessFragment.this.getContext(), MatchFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, mPrepareData.getSongModel())
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                    }
                })
                .build());

        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopAbove(false)
                .setPopFragment(MatchSuccessFragment.this).build());

        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }

    @Override
    protected boolean onBackPressed() {
        if(isPrepared){
            U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                    .setPopAbove(false)
                    .setPopFragment(MatchSuccessFragment.this).build());

            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((FragmentActivity) MatchSuccessFragment.this.getContext(), MatchFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, mPrepareData)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                        }
                    })
                    .build());
        } else {
            U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                    .setPopAbove(false)
                    .setShowFragment(PrepareResFragment.class)
                    .setPopFragment(MatchSuccessFragment.this).build());
        }


        return true;
    }

    @Override
    public void toStaskTop() {
        MyLog.d(TAG, "toStaskTop" );
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void pushIntoStash() {
        MyLog.d(TAG, "pushIntoStash" );
        mRootView.setVisibility(View.GONE);
    }
}

package com.module.rankingmode.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
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
import com.module.rankingmode.song.fragment.SongSelectFragment;

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
        mIvTop = (ExImageView) mRootView.findViewById(R.id.iv_top);
        mSdvIcon1 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon1);
        mSdvIcon2 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon2);
        mSdvIcon3 = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon3);
        mIvVs = (ImageView) mRootView.findViewById(R.id.iv_vs);
        mIvPrepare = (ExImageView) mRootView.findViewById(R.id.iv_prepare);

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
            U.getToastUtil().showShort("已准备");
            mIvPrepare.setEnabled(false);
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {
        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis() - jsonGameReadyInfo.getJsonGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs()));

        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                .withSerializable("prepare_data", mPrepareData)
                .navigation();

        // 这个activity直接到选歌页面,没问题
        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                .setActivity(getActivity())
                .setNotifyShowFragment(SongSelectFragment.class)
                .setBackToFragment(SongSelectFragment.class)
                .build());
    }

    @Override
    public void needReMatch() {
        MyLog.d(TAG, "needReMatch 有人没准备，需要重新匹配");
        goMatch();
        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }


    void goMatch() {
        // 这个activity直接到匹配页面,可是这时匹配中页面已经销毁了
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMatchSucessPresenter.destroy();
    }

    @Override
    protected boolean onBackPressed() {
        if (isPrepared) {
            //已经准备，说明用户想玩，直接到匹配中页面
            goMatch();
        } else {
            //还没准备就退出，说明用户不想玩了，到开始匹配页面
            U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                    .setActivity(getActivity())
                    .setBackToFragment(PrepareResFragment.class)
                    .setNotifyShowFragment(PrepareResFragment.class)
                    .build());
        }
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
        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                .setPopFragment(this)
                .setPopAbove(false)
                .build()
        );
    }
}

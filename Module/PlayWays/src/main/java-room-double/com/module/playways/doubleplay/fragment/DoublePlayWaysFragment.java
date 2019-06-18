package com.module.playways.doubleplay.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleCorePresenter;
import com.module.playways.doubleplay.DoubleRoundInfo;
import com.module.playways.doubleplay.inter.IDoublePlayView;

public class DoublePlayWaysFragment extends BaseFragment implements IDoublePlayView {

    TextView mReportTv;
    ImageView mExitIv;
    SimpleDraweeView mLeftAvatarSdv;
    ImageView mLeftLockIcon;
    ExTextView mLeftNameTv;
    SimpleDraweeView mRightAvatarSdv;   //右边固定是自己
    ImageView mRightLockIcon;
    ExTextView mRightNameTv;
    ExImageView mMicIv;
    ImageView mPickIv;
    ImageView mSelectIv;
    DoubleCorePresenter mDoubleCorePresenter;

    @Override
    public int initView() {
        return R.layout.double_play_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mReportTv = (TextView) mRootView.findViewById(R.id.report_tv);
        mExitIv = (ImageView) mRootView.findViewById(R.id.exit_iv);
        mLeftAvatarSdv = (SimpleDraweeView) mRootView.findViewById(R.id.left_avatar_sdv);
        mLeftLockIcon = (ImageView) mRootView.findViewById(R.id.left_lock_icon);
        mLeftNameTv = (ExTextView) mRootView.findViewById(R.id.left_name_tv);
        mRightAvatarSdv = (SimpleDraweeView) mRootView.findViewById(R.id.right_avatar_sdv);
        mRightLockIcon = (ImageView) mRootView.findViewById(R.id.right_lock_icon);
        mRightNameTv = (ExTextView) mRootView.findViewById(R.id.right_name_tv);
        mMicIv = (ExImageView) mRootView.findViewById(R.id.mic_iv);
        mPickIv = (ImageView) mRootView.findViewById(R.id.pick_iv);
        mSelectIv = (ImageView) mRootView.findViewById(R.id.select_iv);

        mReportTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 举报
            }
        });

        mExitIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 退出
            }
        });

        mRightNameTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 解锁资料
            }
        });

        mMicIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 开关麦克
            }
        });

        mPickIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // pick
            }
        });

        mSelectIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 点歌
            }
        });

        mDoubleCorePresenter = new DoubleCorePresenter(this);
        addPresent(mDoubleCorePresenter);
    }

    @Override
    public void changeRound(DoubleRoundInfo pre, DoubleRoundInfo mCur) {

    }

    @Override
    public void gameEnd(DoubleRoundInfo mCur) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}

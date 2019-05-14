package com.module.home.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.core.pay.EPayPlatform;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExLinearLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.home.R;
import com.module.home.adapter.HalfRechargeAdapter;
import com.module.home.presenter.BallencePresenter;
import com.respicker.view.GridSpacingItemDecoration;

public class HalfRechargeFragment extends BallanceFragment {
    ExLinearLayout mLlContent;

    Drawable mNormalBg = new DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(8))
            .setSolidColor(U.getColor(R.color.black_trans_20))
            .build();

    Drawable mSelectedBg = U.getDrawable(R.drawable.chongzhijiemian_dianjiuxuanzhongtai);

    Handler mUiHanlder = new Handler();

    @Override
    public int initView() {
        return R.layout.half_recharge_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mPlatformContainer = (LinearLayout) mRootView.findViewById(R.id.platform_container);
        mBtbWeixin = (ExTextView) mRootView.findViewById(R.id.btb_weixin);
        mIvWeixinFlag = (ExImageView) mRootView.findViewById(R.id.iv_weixin_flag);
        mBtbZhifubao = (ExTextView) mRootView.findViewById(R.id.btb_zhifubao);
        mZhifubaoFlag = (ExImageView) mRootView.findViewById(R.id.zhifubao_flag);
        mWithdrawTv = (TextView) mRootView.findViewById(R.id.withdraw_tv);
        mMainActContainer = (FrameLayout) mRootView.findViewById(R.id.main_act_container);
        mLlContent = (ExLinearLayout) mRootView.findViewById(R.id.ll_content);

        mRechargeAdapter = new HalfRechargeAdapter();

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, U.getDisplayUtils().dip2px(10), false));
        mRecyclerView.setAdapter(mRechargeAdapter);

        mEPayPlatform = EPayPlatform.WX_PAY;
        updatePlatformBg();

        mBtbWeixin.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEPayPlatform = EPayPlatform.WX_PAY;
                updatePlatformBg();
            }
        });

//        mBtbZhifubao.setOnClickListener(new DebounceViewClickListener() {
//            @Override
//            public void clickValid(View v) {
//                mEPayPlatform = EPayPlatform.ALI_PAY;
//                updatePlatformBg();
//            }
//        });

        mWithdrawTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mRechargeAdapter.getSelectedItem() == null) {
                    U.getToastUtil().showShort("请选择");
                    return;
                }

                if (mEPayPlatform == EPayPlatform.WX_PAY) {
                    if (U.getCommonUtils().hasInstallApp("com.tencent.mm")) {
                        mBallencePresenter.rechargeWxPay(mRechargeAdapter.getSelectedItem().getGoodsID());
                    } else {
                        U.getToastUtil().showShort("未安装微信");
                    }
                } else {
                    mBallencePresenter.rechargeAliPay(mRechargeAdapter.getSelectedItem().getGoodsID());
                }
            }
        });

        mMainActContainer.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mLlContent.setOnClickListener(v -> {
        });

        mBallencePresenter = new BallencePresenter(getActivity(), this);
        addPresent(mBallencePresenter);
        mBallencePresenter.getGoodsList();
    }

    private void updatePlatformBg() {
        if (mEPayPlatform == EPayPlatform.WX_PAY) {
            mIvWeixinFlag.setBackground(mSelectedBg);
            mZhifubaoFlag.setBackground(mNormalBg);
        } else {
            mIvWeixinFlag.setBackground(mNormalBg);
            mZhifubaoFlag.setBackground(mSelectedBg);
        }
    }

    @Override
    public void rechargeSuccess() {
        U.getToastUtil().showShort("充值成功");

        if (mWaitingDialogPlus != null) {
            mWaitingDialogPlus.dismiss();
        }

        if (mFragmentDataListener != null) {
            mFragmentDataListener.onFragmentResult(100, 0, null, null);
        }

        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }
}

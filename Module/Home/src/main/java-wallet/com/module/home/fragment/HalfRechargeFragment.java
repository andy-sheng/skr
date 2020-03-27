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
import com.common.miLianYun.MiLianYunManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExLinearLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.home.R;
import com.module.home.adapter.HalfRechargeAdapter;
import com.module.home.presenter.BallancePresenter;
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
        mRecyclerView = (RecyclerView) getRootView().findViewById(R.id.recycler_view);
        mPlatformContainer = (LinearLayout) getRootView().findViewById(R.id.platform_container);
        mBtbWeixin = (ExTextView) getRootView().findViewById(R.id.btb_weixin);
        mIvWeixinFlag = (ExImageView) getRootView().findViewById(R.id.iv_weixin_flag);
        mBtbZhifubao = (ExTextView) getRootView().findViewById(R.id.btb_zhifubao);
        mZhifubaoFlag = (ExImageView) getRootView().findViewById(R.id.zhifubao_flag);
        mWithdrawTv = (TextView) getRootView().findViewById(R.id.withdraw_tv);
        mMainActContainer = (FrameLayout) getRootView().findViewById(R.id.main_act_container);
        mLlContent = (ExLinearLayout) getRootView().findViewById(R.id.ll_content);

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

        mBtbZhifubao.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEPayPlatform = EPayPlatform.ALI_PAY;
                updatePlatformBg();
            }
        });

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
                } else if (mEPayPlatform == EPayPlatform.MI_PAY) {
                    mBallencePresenter.rechargeMiPay(mRechargeAdapter.getSelectedItem().getGoodsID());
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

        mBallencePresenter = new BallancePresenter(getActivity(), this);
        addPresent(mBallencePresenter);
        mBallencePresenter.getGoodsList();


        mWeixinRechargeArea = (FrameLayout) getRootView().findViewById(R.id.weixin_recharge_area);
        mXiaomiRechargeArea = (FrameLayout) getRootView().findViewById(R.id.xiaomi_recharge_area);
        mBtbXiaomi = (ExTextView) getRootView().findViewById(R.id.btb_xiaomi);
        mIvXiaomiFlag = (ExImageView) getRootView().findViewById(R.id.iv_xiaomi_flag);

        if (MiLianYunManager.INSTANCE.lianYunOpen()) {
            MiLianYunManager.INSTANCE.loginAuto();
            mWeixinRechargeArea.setVisibility(View.GONE);
            mXiaomiRechargeArea.setVisibility(View.VISIBLE);
            mEPayPlatform = EPayPlatform.MI_PAY;
            mIvWeixinFlag.setVisibility(View.GONE);
            mIvXiaomiFlag.setVisibility(View.VISIBLE);
        } else {
            mWeixinRechargeArea.setVisibility(View.VISIBLE);
            mXiaomiRechargeArea.setVisibility(View.GONE);
            mEPayPlatform = EPayPlatform.WX_PAY;
            mIvWeixinFlag.setVisibility(View.VISIBLE);
            mIvXiaomiFlag.setVisibility(View.GONE);
        }

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


        if (getFragmentDataListener() != null) {
            getFragmentDataListener().onFragmentResult(100, 0, null, null);
        }

        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

}

package com.module.home.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.pay.EPayPlatform;
import com.common.core.pay.PayBaseReq;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.adapter.RechargeAdapter;
import com.module.home.inter.IBallanceView;
import com.module.home.model.RechargeItemModel;
import com.module.home.presenter.BallencePresenter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.respicker.view.GridSpacingItemDecoration;

import java.util.List;

public class BallanceFragment extends BaseFragment implements IBallanceView {

    LinearLayout mMainActContainer;
    CommonTitleBar mTitlebar;

    BallencePresenter mBallencePresenter;

    EPayPlatform mEPayPlatform;
    RechargeAdapter mRechargeAdapter;

    TextView mWithdrawTv;
    ExTextView mDiaomendLast;
    ExTextView mDiaomentNum;
    RecyclerView mRecyclerView;
    LinearLayout mPlatformContainer;
    ExTextView mBtbWeixin;
    ExImageView mIvWeixinFlag;
    ExTextView mBtbZhifubao;
    ExImageView mZhifubaoFlag;
    LinearLayout mProtocolContainer;
    CheckBox mCheckbox;
    TextView mTvProtocal;

    DialogPlus mWaitingDialogPlus;

    @Override
    public int initView() {
        return R.layout.ballance_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mWithdrawTv = (TextView) mRootView.findViewById(R.id.withdraw_tv);

        mDiaomendLast = (ExTextView) mRootView.findViewById(R.id.diaomend_last);
        mDiaomentNum = (ExTextView) mRootView.findViewById(R.id.diaoment_num);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mPlatformContainer = (LinearLayout) mRootView.findViewById(R.id.platform_container);
        mBtbWeixin = (ExTextView) mRootView.findViewById(R.id.btb_weixin);
        mIvWeixinFlag = (ExImageView) mRootView.findViewById(R.id.iv_weixin_flag);
        mBtbZhifubao = (ExTextView) mRootView.findViewById(R.id.btb_zhifubao);
        mZhifubaoFlag = (ExImageView) mRootView.findViewById(R.id.zhifubao_flag);
        mProtocolContainer = (LinearLayout) mRootView.findViewById(R.id.protocol_container);
        mCheckbox = (CheckBox) mRootView.findViewById(R.id.checkbox);
        mTvProtocal = (TextView) mRootView.findViewById(R.id.tv_protocal);

        mRechargeAdapter = new RechargeAdapter();

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, U.getDisplayUtils().dip2px(10), false));
        mRecyclerView.setAdapter(mRechargeAdapter);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mWithdrawTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (!mCheckbox.isChecked()) {
                    U.getToastUtil().showShort("请同意协议");
                    return;
                }

                if (mRechargeAdapter.getSelectedItem() == null) {
                    U.getToastUtil().showShort("请选择");
                    return;
                }

                if (mEPayPlatform == EPayPlatform.WX_PAY) {
                    mBallencePresenter.rechargeWxPay(mRechargeAdapter.getSelectedItem().getGoodsID());
                } else {
                    mBallencePresenter.rechargeAliPay(mRechargeAdapter.getSelectedItem().getGoodsID());
                }
            }
        });

        mTvProtocal.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", "https://api.inframe.mobi/user-agreement.html")
                        .greenChannel().navigation();
            }
        });

        mEPayPlatform = EPayPlatform.WX_PAY;
        mIvWeixinFlag.setVisibility(View.VISIBLE);
        mBtbWeixin.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEPayPlatform = EPayPlatform.WX_PAY;
                mIvWeixinFlag.setVisibility(View.VISIBLE);
                mZhifubaoFlag.setVisibility(View.GONE);
            }
        });

        mBtbZhifubao.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEPayPlatform = EPayPlatform.ALI_PAY;
                mIvWeixinFlag.setVisibility(View.GONE);
                mZhifubaoFlag.setVisibility(View.VISIBLE);
            }
        });

        mBallencePresenter = new BallencePresenter(getActivity(), this);
        addPresent(mBallencePresenter);
        mBallencePresenter.getGoodsList();
        mBallencePresenter.getZSBalance();
    }

    @Override
    public void showRechargeList(List<RechargeItemModel> list) {
        mRechargeAdapter.setDataList(list);
    }

    @Override
    public void showBalance(String diamond) {
        mDiaomentNum.setText(diamond);
    }

    @Override
    public void rechargeFailed(String errorMsg) {
        U.getToastUtil().showShort(errorMsg);
        if (mWaitingDialogPlus != null) {
            mWaitingDialogPlus.dismiss();
        }
    }

    @Override
    public void rechargeSuccess() {
        U.getToastUtil().showShort("充值成功");
        if (mWaitingDialogPlus != null) {
            mWaitingDialogPlus.dismiss();
        }
    }

    @Override
    public void sendOrder(PayBaseReq payBaseResp) {
        if (mWaitingDialogPlus == null) {
            mWaitingDialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(new ProgressBar(getContext())))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_50)
                    .setExpanded(false)
                    .setCancelable(false)
                    .setGravity(Gravity.CENTER)
                    .create();
        }

        mWaitingDialogPlus.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}

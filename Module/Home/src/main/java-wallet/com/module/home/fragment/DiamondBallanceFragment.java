package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.module.home.R;
import com.module.home.WalletServerApi;
import com.module.home.event.RechargeSuccessEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DiamondBallanceFragment extends BaseFragment {
    public final static String TAG = "DiamondBallanceFragment";
    LinearLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    ImageView mIvDiamondIcon;
    ExTextView mTvDiamondText;
    ExTextView mTvDiamondBalance;
    StrokeTextView mRechargeBtn;

    WalletServerApi mWalletServerApi;

    @Override
    public int initView() {
        return R.layout.diamond_ballance_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
        mMainActContainer = (LinearLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mIvDiamondIcon = (ImageView) mRootView.findViewById(R.id.iv_diamond_icon);
        mTvDiamondText = (ExTextView) mRootView.findViewById(R.id.tv_diamond_text);
        mTvDiamondBalance = (ExTextView) mRootView.findViewById(R.id.tv_diamond_balance);
        mRechargeBtn = (StrokeTextView) mRootView.findViewById(R.id.recharge_btn);
        getZSBalance();

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), RechargeRecordlFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mRechargeBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), BallanceFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(RechargeSuccessEvent giftPresentEvent) {
        // 收到一条礼物消息,进入生产者队列
        getZSBalance();
    }

    public void getZSBalance() {
        ApiMethods.subscribe(mWalletServerApi.getZSBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                MyLog.w(TAG, "getZSBalance process" + " obj=" + obj);
                if (obj.getErrno() == 0) {
                    String amount = obj.getData().getString("totalAmountStr");
                    mTvDiamondBalance.setText(amount);
                }
            }
        }, this);
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
        return true;
    }
}

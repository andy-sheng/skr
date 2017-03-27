package com.wali.live.common.pay.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.dialog.MyProgressDialogEx;
import com.base.fragment.MyRxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.wali.live.common.pay.adapter.RechargeRecyclerViewAdapter;
import com.wali.live.common.pay.assist.IPayActivityFlag;
import com.wali.live.common.pay.constant.PayConstant;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.common.pay.constant.RechargeConfig;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.presenter.RechargePresenter;
import com.wali.live.common.pay.utils.PayStatisticUtils;
import com.wali.live.common.pay.view.IRechargeView;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.VISIT;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * 充值界面<br/>
 *
 * @module 充值
 * Created by rongzhisheng
 */
public class RechargeFragment extends MyRxFragment implements IRechargeView {
    private static final String TAG = RechargeFragment.class.getSimpleName();
    public static final int REQUEST_CODE = 0;

    /**
     * 用户当前选择的支付方式
     */
    private static PayWay sPayWay = PayWay.WEIXIN;

    public static PayWay getCurrentPayWay() {
        return sPayWay;
    }

    public static void setCurrentPayWay(PayWay payWay) {
        sPayWay = payWay;
    }

    /**
     * 非静态成员变量
     */
    private RechargeRecyclerViewAdapter mRechargeAdapter;

    /**
     * 是否为首次充值
     */
    private boolean mIsFirstRecharge = RechargeConfig.getIsFirstRecharge();

    /**
     * 是否只是需要获取可兑换的钻石数,结合{@link #mIsFirstRecharge}使用
     */
    private boolean mOnlyGetExchangeableDiamond = true;

    /**
     * 根据语言和渠道设置为国内支付列表或国际支付列表
     */
    private List<PayWay> mPayWayList;

    private RechargePresenter mRechargePresenter;

    public RechargePresenter getRechargePresenter() {
        return mRechargePresenter;
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_recharge, container, false);
    }

    @Override
    protected void bindView() {
        mRechargePresenter = new RechargePresenter(this, (BaseActivity) getActivity());
        mPayWayList = RechargeConfig.getNativePayWayList();
        sPayWay = getInitialPayWay();// 设置支付手段的首选项
        // 清空缓存的价格列表，消除缓存在切换支付方式时可能出现问题的隐患
        mRechargePresenter.clearPriceListCache();

        BackTitleBar titleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        titleBar.setTitle(R.string.diamond_outcome_hint);
        titleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.getTitleTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.getRightTextBtn().setText(R.string.record_recharge_title);
        RxView.clicks(titleBar.getRightTextBtn()).throttleFirst(3, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                {
                    if (!Network.hasNetwork(GlobalData.app())) {
                        ToastUtils.showToast(GlobalData.app(), R.string.network_unavailable);
                        return;
                    } else {
                        FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RechargeRecordFragment.class, null, true, false, true);
                    }
                }
            }
        });

        mRechargeAdapter = new RechargeRecyclerViewAdapter(getActivity());
        if (!mIsFirstRecharge) {
            mRechargeAdapter.setLastRechargeListType(RechargeConfig.getRechargeListType(sPayWay));
        }
        mRechargeAdapter.setIsFirstRecharge(mIsFirstRecharge);
        mRechargeAdapter.setPayWayList(mPayWayList);
        mRechargeAdapter.setRechargePresenter(mRechargePresenter);

        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mRechargeAdapter);
        recyclerView.setHasFixedSize(true);

        initPullPriceList();
        // 进入钻石充值页面打一次
        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, PayStatisticUtils.getRechargeTemplate(VISIT, getCurrentPayWay()), TIMES, "1");
    }

    /**
     * 在{@link #mPayWayList}初始化后调用
     */
    private PayWay getInitialPayWay() {
        PayWay defaultPayWay = mPayWayList.get(0);
        PayWay payWay = defaultPayWay;
        String lastPayWayName = RechargeConfig.getLastPaywayName();
        if (!TextUtils.isEmpty(lastPayWayName)) {
            try {
                payWay = PayWay.valueOf(lastPayWayName.toUpperCase());
            } catch (Exception e) {
                MyLog.e(TAG, "unexpected saved pay way:" + lastPayWayName);
            }
        }
        // 版本替换可能造成程序保存的用户上次使用的支付方式不适合当前版本
        // 想象这样的情况：小米钱包不能用于国际支付，GoogleWallet和PayPal不能用于国内支付
        if (!mPayWayList.contains(payWay)) {
            payWay = defaultPayWay;
        }
        return payWay;
    }

    private void initPullPriceList() {
        mRechargePresenter.pullPriceListAsync();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 先隐藏弹窗，避免弹窗移到屏幕左上角被用户看到
        mRechargeAdapter.hidePopupWindow();
    }

    /**
     * 是否可能去微信充值了
     */
    private boolean mMayRechargeFromOutSide = false;

    @Override
    public void onResume() {
        MyLog.d(TAG, "onResume");
        super.onResume();
        KeyboardUtils.hideKeyboard(getActivity());
        if (mMayRechargeFromOutSide) {
            mRechargePresenter.syncBalance();
        }
    }

    @Override
    public void onPause() {
        MyLog.d(TAG, "onPause");
        mMayRechargeFromOutSide = true;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRechargePresenter != null) {
            mRechargePresenter.destroy();
            mRechargePresenter = null;
        }
        mRechargeAdapter = null;
    }


    /**
     * 是否重载状态栏，如果不重载，则用该Fragment所附着的Activity的设置
     *
     * @return
     */
    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (mRechargeAdapter != null) {
            mRechargeAdapter.hidePopupWindow();
        }
        if (mIsFirstRecharge && mRechargeAdapter != null && mRechargeAdapter.getStep() > PayConstant.RECHARGE_STEP_FIRST) {
            mRechargeAdapter.setStep(mRechargeAdapter.getStep() - 1);
            mRechargeAdapter.notifyDataSetChanged();
            return true;
        }
        if (getActivity() != null && !isDetached()) {
            if (getActivity() instanceof IPayActivityFlag) {
                getActivity().finish();
            } else {
                FragmentNaviUtils.popFragment(getActivity());
            }
        }
        return true;
    }

    private MyProgressDialogEx mProgressDialog;

    @Override
    public void showProcessDialog(long most) {
        if (getActivity() != null && !isDetached()) {
            if (mProgressDialog == null) {
                //创建ProgressDialog对象
                mProgressDialog = MyProgressDialogEx.createProgressDialog(getActivity());
            }
            mProgressDialog.show(most);
        }

    }

    @Override
    public void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

    @Override
    public void showToast(@NonNull String msg) {
        ToastUtils.showToast(getActivity(), msg);
    }

    @Override
    public String getPackageName0() {
        return getActivity().getPackageName();
    }

    @Override
    public void setBalanceText(int balance, int vBalance) {
        mRechargeAdapter.setBalance(balance, vBalance, true);
    }

    @Override
    public void setRecyclerViewAdapterDataSourceAndNotify(List<Diamond> diamonds) {
        mRechargeAdapter.setRechargeList(diamonds);
    }

    @Override
    public void setRecyclerViewLoadingStatusAndNotify() {
        mRechargeAdapter.setLoadingStatus();
    }

    @Override
    public void updateExchangeableAndWillExpireDiamond(int exchangeableDiamondCnt
            , int willExpireDiamondCnt, int willExpireGiftCardCnt) {
        mRechargeAdapter.updateExchangeableAndWillExpireDiamond(exchangeableDiamondCnt,
                willExpireDiamondCnt, willExpireGiftCardCnt, true);
    }

    @Override
    public boolean isFirstRecharge() {
        return mIsFirstRecharge;
    }

    @Override
    public void showPopupWindow() {
        if (mRechargeAdapter.getStep() == PayConstant.RECHARGE_STEP_SECOND) {
            mRechargeAdapter.clickGridViewItem();
        }
    }

    @Override
    public boolean isNotPullRechargeList() {
        if (mIsFirstRecharge) {
            if (mOnlyGetExchangeableDiamond) {
                mOnlyGetExchangeableDiamond = false;
                return true;
            }
        }
        return false;
    }
}

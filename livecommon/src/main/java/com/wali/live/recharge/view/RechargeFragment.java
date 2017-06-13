package com.wali.live.recharge.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.dialog.MyProgressDialogEx;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.wali.live.pay.activity.RechargeActivity;
import com.wali.live.pay.fragment.RechargeRecordFragment;
import com.wali.live.pay.model.Diamond;
import com.wali.live.pay.view.IRechargeView;
import com.wali.live.recharge.adapter.RechargeRecyclerViewAdapter;
import com.wali.live.recharge.config.RechargeConfig;
import com.wali.live.recharge.presenter.RechargePresenter;
import com.wali.live.statistics.StatisticsKeyUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.pay.constant.PayConstant.RECHARGE_STEP_FIRST;
import static com.wali.live.pay.constant.PayConstant.RECHARGE_STEP_SECOND;

/**
 * 充值界面<br/>
 *
 * @module 充值
 * Created by rongzhisheng
 */
public class RechargeFragment extends BaseEventBusFragment implements IRechargeView, FragmentDataListener {
    private static final String TAG = RechargeFragment.class.getSimpleName();
    public static final int REQUEST_CODE = com.base.global.GlobalData.getRequestCode();

    public static RechargeFragment openFragment(@NonNull FragmentActivity fragmentActivity, @IdRes int containerId, Bundle bundle, boolean hasAnimation) {
        return (RechargeFragment) FragmentNaviUtils.addFragment(fragmentActivity, containerId, RechargeFragment.class, bundle, true, hasAnimation, true);
    }

    //////////////////////////////////////////////////////
    /////////////////////非静态成员变量/////////////////////
    //////////////////////////////////////////////////////

    private RechargeRecyclerViewAdapter mRechargeAdapter;

    //Inject
    private RechargePresenter mRechargePresenter = RechargePresenter.newInstance();

    //////////////////////////////////////////////////////
    /////////////////////////方法//////////////////////////
    //////////////////////////////////////////////////////

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.recharge_fragment, container, false);
    }

    @Override
    protected void bindView() {
        StatisticsKeyUtils.RechargeScribeParam.param = getArguments();
        mRechargePresenter.setView(this);

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
        RxView.clicks(titleBar.getRightTextBtn()).throttleFirst(3, TimeUnit.SECONDS).subscribe(
                new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (!Network.hasNetwork(com.base.global.GlobalData.app())) {
                            ToastUtils.showToast(com.base.global.GlobalData.app(), R.string.network_unavailable);
                            return;
                        } else {
                            FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RechargeRecordFragment.class, null, true, false, true);
                        }
                    }
                }
        );

        mRechargeAdapter = new RechargeRecyclerViewAdapter(getActivity());
        mRechargeAdapter.setPayWayList(RechargeConfig.getPayWayList());
        if (!RechargePresenter.isFirstRecharge() || RechargeConfig.getPayWaysSize() == 1) {
            mRechargeAdapter.setLastRechargeListType(RechargePresenter.getCurPayWay().getRechargeListType());
        }
        mRechargeAdapter.setRechargePresenter(mRechargePresenter);

        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mRechargeAdapter);
        recyclerView.setHasFixedSize(true);


        mRechargePresenter.loadDataAndUpdateView();
        // 进入钻石充值页面打一次
//        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY, getRechargeTemplate(VISIT, RechargePresenter.getCurPayWay()), TIMES, "1");
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
            mRechargePresenter.loadDataAndUpdateView();
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
        if (RechargePresenter.isFirstRecharge() && RechargePresenter.getStep() > RECHARGE_STEP_FIRST) {
            RechargePresenter.decrStep();
            if (mRechargeAdapter != null) {
                mRechargeAdapter.notifyDataSetChanged();
            }
            return true;
        }
        if (getActivity() != null && !isDetached()) {
            if (getActivity() instanceof RechargeActivity) {// 如果RechargeFragment是进入RechargeActivity的第一个界面，则加这个判断
                getActivity().finish();
            } else {
                FragmentNaviUtils.popFragment(getActivity());
            }
        }
        return true;
    }

    private MyProgressDialogEx mProgressDialog;

    @Override
    public void showProcessDialog(long most, @StringRes int strId) {
        if (getActivity() != null && !isDetached()) {
            if (!(FragmentNaviUtils.getTopFragment(getActivity()) instanceof RechargeFragment)) {
                return;
            }
            if (mProgressDialog == null) {
                mProgressDialog = MyProgressDialogEx.createProgressDialog(this.getActivity());
            }
            mProgressDialog.setMessage(getString(strId));
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
    public void setBalanceText(int balance, int vBalance) {
        mRechargeAdapter.setBalance(balance, vBalance, true);
    }

    @Override
    public void setRecyclerViewAdapterDataSourceAndNotify(List<Diamond> diamonds) {
        if (mRechargeAdapter == null) {
            return;
        }
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
        return true;
    }

    @Override
    public void showPopupWindow() {
        if (RechargePresenter.getStep() == RECHARGE_STEP_SECOND) {
            mRechargeAdapter.clickGridViewItem();
        }
    }

    @Override
    public void updateBalanceAreaData() {
        mRechargeAdapter.updateBalanceAreaData(true);
    }

    public boolean popAfterBackPress() {
        //需要pop的情况有两种，1、首次充值的第一步2、非首次充值的第二步
        return RechargePresenter.isFirstStep()//只有首次充值时才可能到达第一步
                || !RechargePresenter.isFirstRecharge();//非首次充值的第二步
    }

    @MainThread
    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle bundle) {
        MyLog.w(TAG, "onFragmentResult " + requestCode + " , resultCode=" + resultCode + " , bundle =" + bundle);
        Intent data = new Intent();
        if (bundle != null) {
            data.putExtras(bundle);
        }

        MyLog.e(TAG, "no match handle fragment result");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        if (event == null || event.eventType != KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN) {
            return;
        }
        if (mRechargeAdapter != null) {
            mRechargeAdapter.clickGridViewItem();
        }
    }
}

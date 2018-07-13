package com.wali.live.recharge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.dialog.MyProgressDialogEx;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.wali.live.pay.model.Diamond;
import com.wali.live.pay.view.IRechargeView;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.presenter.RechargePresenter;

import java.util.List;

import rx.Observable;

/**
 * Created by zhujianning on 18-7-13.
 */

public class RechargeDirectPayFragment extends BaseFragment implements IRechargeView {
    private static final String TAG = "RechargeDirectPayFragment";
    private static String EXTRA_GOOD_ID = "extra_good_id";
    private static String EXTRA_GEM_COUNT = "extra_gem_count";
    private static String EXTRA_GOOD_PRICE = "extra_good_price";
    private static String EXTRA_GIVE_GEM_CNT = "extra_give_gem_cnt";
    private static String EXTRA_TIMES = "extra_times";
    private static String EXTRA_PAY_TYPE = "extra_pay_type";
    private static String EXTRA_PAY_CHANNEL = "extra_pay_channel";

    private RechargePresenter mRechargePresenter = RechargePresenter.newInstance();
    private MyProgressDialogEx mProgressDialog;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.act_recharge_direct_pay, container, false);
    }

    @Override
    protected void bindView() {
        Bundle arguments = getArguments();
        Diamond goods = new Diamond();

        goods.setId(arguments.getInt(EXTRA_GOOD_ID, 0));
        goods.setCount(arguments.getInt(EXTRA_GEM_COUNT, 0));
        goods.setExtraGive(arguments.getInt(EXTRA_GIVE_GEM_CNT, 0));
        goods.setPrice(arguments.getInt(EXTRA_GOOD_PRICE, 0));

        mRechargePresenter.setView(this);
        mRechargePresenter.recharge(goods
                , RechargePresenter.getCurPayWay()
                , PayProto.RChannel.valueOf(arguments.getInt(EXTRA_PAY_CHANNEL, -1))
                , PayProto.PayType.valueOf(arguments.getInt(EXTRA_PAY_TYPE, 0)));
    }

    @Override
    public void showProcessDialog(long most, int strId) {
        if (getActivity() != null && !isDetached()) {
            if (!(FragmentNaviUtils.getTopFragment(getActivity()) instanceof RechargeDirectPayFragment)) {
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
        MyLog.d(TAG, " hideProcessDialog");
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }

        onBackPressed();
    }

    @Override
    public void setBalanceText(int balance, int vBalance) {

    }

    @Override
    public void setRecyclerViewAdapterDataSourceAndNotify(List<Diamond> diamonds) {

    }

    @Override
    public void setRecyclerViewLoadingStatusAndNotify() {

    }

    @Override
    public void updateExchangeableAndWillExpireDiamond(int exchangeableDiamondCnt, int willExpireDiamondCnt, int willExpireGiftCardCnt) {

    }

    @Override
    public boolean isFirstRecharge() {
        return false;
    }

    @Override
    public void showPopupWindow() {

    }

    @Override
    public void updateBalanceAreaData() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @NonNull
    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRechargePresenter != null) {
            mRechargePresenter.destroy();
            mRechargePresenter = null;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() != null && !isDetached()) {
            FragmentNaviUtils.popFragment(getActivity());
        }
        return true;
    }

    public static void openFragment(@NonNull FragmentActivity fragmentActivity, @IdRes int containerId, int goodId, int gemCnt, int giveGemCnt, int goodPrice, int payType, int channel) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_GOOD_ID, goodId);
        bundle.putInt(EXTRA_GEM_COUNT, gemCnt);
        bundle.putInt(EXTRA_GIVE_GEM_CNT, giveGemCnt);
        bundle.putInt(EXTRA_GOOD_PRICE, goodPrice);
        bundle.putInt(EXTRA_PAY_TYPE, payType);
        bundle.putInt(EXTRA_PAY_CHANNEL, channel);
        FragmentNaviUtils.addFragment(fragmentActivity, containerId, RechargeDirectPayFragment.class, bundle, true, false, true);
    }
}

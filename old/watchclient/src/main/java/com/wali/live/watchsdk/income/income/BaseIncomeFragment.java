package com.wali.live.watchsdk.income.income;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.dialog.MyProgressDialogEx;
import com.base.fragment.BaseEventBusFragment;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.income.WithdrawManager;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.proto.PayProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.UserProfit;
import com.wali.live.watchsdk.income.exchange.ExchangeGemActivity;
import com.wali.live.watchsdk.income.records.EarningsRecordsFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.base.utils.CommonUtils.getHumanReadableMoney;
import static com.wali.live.watchsdk.income.UserProfit.SIGN_STATUS_SIGNED;

/**
 * 基础提现Fragment
 * Created by rongzhisheng on 16-11-30.
 */

public abstract class BaseIncomeFragment extends BaseEventBusFragment implements View.OnClickListener {
    public static final int PROGRESS_SHOW_TIME_MOST = 5000;
    public static final int PROGRESS_SHOW_TIME_LEAST = 1000;

    protected static final int CLICK_INTERVAL_SECOND = 2;

    private final int ACTION_INCOME_TITLEBAR_BACKBTN = 100;
    private final int ACTION_INCOME_TITLEBAR_RIGHTBTN = 102;
    protected final int ACTION_INCOME_EXCHANGE_GEM = 103;
    protected final int ACTION_INCOME_EXCHANGE_HINT = 105;

    protected final int ACTION_INCOME_GET_ALI_MONEY = 201;
    protected final int ACTION_INCOME_GET_WX_MONEY = 202;
    protected final int ACTION_INCOME_GET_PAYPAL_MONEY = 203;

    protected BackTitleBar mTitleBar;
    /**
     * 可用星票数量
     */
//    protected TextView mUserTicketTotalTv;

//    protected TextView mUseableShowTv;
    protected ViewStub mNormalStub;
    protected ViewStub mSignedStub;
    protected TextView mSignedSeeDetailTv;
    //可兑换金额
    protected TextView mUnsignedExchangeableMoneyTv;
    protected ViewStub mVsGuide; //引导气泡 viewstub
    protected RelativeLayout mRlGuide;
    protected TextView mTvTipSeeRecords;
    protected TextView mTvTipSeeWebRecords;

    protected boolean isFirstShowTip1;
    protected boolean isFirstShowTip2;


    protected static final int CURRENCY_TYPE_CNY = 0;
    protected static final int CURRENCY_TYPE_USD = 1;
    /**
     * 可用星票提示
     */
//    protected TextView mAvailableTicketTip;
    protected int mCurrencyType = CURRENCY_TYPE_CNY;

    protected UserProfit mProfitData;
    protected boolean isLoading;

    protected boolean first = true;


    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected void bindView() {

        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(R.string.my_live_income);
        mTitleBar.getBackBtn().setOnClickListener(this);
        mTitleBar.getBackBtn().setTag(ACTION_INCOME_TITLEBAR_BACKBTN);
        TextView mRightView = mTitleBar.getRightTextBtn();
        mRightView.setText(R.string.cash_records_tip);
        mRightView.setOnClickListener(this);
        mRightView.setTag(ACTION_INCOME_TITLEBAR_RIGHTBTN);
        initCommonView();

        bindOwnView();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() == null) {
            return;
        }
        int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
        switch (viewAction) {
            case ACTION_INCOME_TITLEBAR_BACKBTN:
                getActivity().finish();
                break;
            case ACTION_INCOME_TITLEBAR_RIGHTBTN:
             //   WithdrawRecordsActivity.openActivity(getActivity());
                EarningsRecordsFragment.openFragment((BaseActivity) getActivity());
                break;
            case ACTION_INCOME_EXCHANGE_GEM:
                if (null != mProfitData) {
                    ExchangeGemActivity.handleJumpToExchangePage(getActivity(), StatisticsKey.ExchangeGem.FROM_MY_INCOME);
                }
                break;
        }
    }

    protected void loadDataFromServer() {
        isLoading = true;
        showProcessDialog(PROGRESS_SHOW_TIME_MOST, R.string.loading);
        Observable.just(null)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
                            return Observable.error(new Throwable("no net"));
                        }
                        return Observable.just(null);
                    }
                })
                .map(new Func1<Object, PayProto.QueryProfitResponse>() {
                    @Override
                    public PayProto.QueryProfitResponse call(Object o) {
                        return WithdrawManager.pullUserProfitSync();
                    }
                })
                .flatMap(new Func1<PayProto.QueryProfitResponse, Observable<PayProto.QueryProfitResponse>>() {
                    @Override
                    public Observable<PayProto.QueryProfitResponse> call(PayProto.QueryProfitResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Throwable("QueryProfitResponse is null"));
                        }
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new ExceptionWithCode(rsp.getRetCode()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .compose(this.<PayProto.QueryProfitResponse>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PayProto.QueryProfitResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        isLoading = false;
                        MyLog.e(TAG, "getUserProfit fail", e);
                        // 强制退出
                        ToastUtils.showToast(R.string.no_net);
                        if (!isDetached() && getActivity() != null) {
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onNext(PayProto.QueryProfitResponse rsp) {
                        hideProcessDialog(PROGRESS_SHOW_TIME_LEAST);
                        isLoading = false;
                        MyLog.e(TAG, "getUserProfit success ");
                        mProfitData = new UserProfit(rsp);
                        updateProfitView(mProfitData);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.WithdrawEvent event) {
        if (null != event &&
                (event.eventType == EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE
                        || event.eventType == EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE)) {// 首次绑定时会提取一元，导致星票变化
            MyLog.w(TAG, "receive event type=" + event.eventType);
            if (!isLoading) {
                loadDataFromServer();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Fragment> fragmentList = getFragmentManager().getFragments();
        if (fragmentList != null && !fragmentList.isEmpty()) {
            Fragment fragment = fragmentList.get(getFragmentManager().getBackStackEntryCount() - 1);
            if (fragment instanceof BaseIncomeFragment) {
                loadDataFromServer(); // 判断在栈顶
            }
        }
    }

    protected abstract void bindOwnView();

    protected void initCommonView() {
        mNormalStub = $(R.id.normal_income_layout);
        mSignedStub = $(R.id.signed_income_layout);
    }

    protected void initSignedView() {
        mSignedSeeDetailTv = $(R.id.signed_see_detail);
    }

    protected void initView() {
        mUnsignedExchangeableMoneyTv = $(R.id.unsigned_exchangeable_money);
    }


    @MainThread
    protected void updateProfitView(UserProfit profit) {
        isFirstShowTip1 = PreferenceUtils.getSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP1,true);
        isFirstShowTip2 = PreferenceUtils.getSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP2,true);

        if (profit.getSignStatus() == SIGN_STATUS_SIGNED) {
            if (first) {
                first = false;
                mSignedStub.inflate();
                initSignedView();
                if (isFirstShowTip1 || isFirstShowTip2){
                    inflateGuideLayout();
                }
            }
            if (isFirstShowTip1){
                mTvTipSeeRecords.setVisibility(View.VISIBLE);
                mRlGuide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isFirstShowTip1) {
                            mTvTipSeeRecords.setVisibility(View.GONE);
                            mTvTipSeeWebRecords.setVisibility(View.VISIBLE);
                            isFirstShowTip1 = false;
                            PreferenceUtils.setSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP1,false);
                        }else if (isFirstShowTip2){
                            mTvTipSeeWebRecords.setVisibility(View.GONE);
                            isFirstShowTip2 = false;
                            PreferenceUtils.setSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP2,false);
                            mRlGuide.setVisibility(View.GONE);
                        }
                    }
                });
            }else if (isFirstShowTip2){
                mTvTipSeeWebRecords.setVisibility(View.VISIBLE);
                mRlGuide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideGuideLayout();
                        isFirstShowTip2 = false;
                        PreferenceUtils.setSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP2,false);
                    }
                });
            }
        } else {
            if (first) {
                first = false;
                mNormalStub.inflate();
                initView();
                if (isFirstShowTip1 || isFirstShowTip2){
                    inflateGuideLayout();
                }
            }
            mUnsignedExchangeableMoneyTv.setText(getMoney(profit, UserProfit.TYPE_CNT));
            if (isFirstShowTip1){
                mTvTipSeeRecords.setVisibility(View.VISIBLE);
                mRlGuide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideGuideLayout();
                        isFirstShowTip1 = false;
                        PreferenceUtils.setSettingBoolean(getActivity(),PreferenceUtils.PREF_KEY_FIRST_POP_INCOME_TIP1,false);
                    }
                });
            }
        }

    }

    public void inflateGuideLayout(){
        mVsGuide = $(R.id.vs_guide_layout);
        if(mVsGuide == null){
            return;
        }
        mVsGuide.inflate();
        mRlGuide = $(R.id.guide_layout);
        mTvTipSeeRecords = $(R.id.tv_tip_see_all_records);
        mTvTipSeeWebRecords = $(R.id.tv_tip_see_web_records);
        if (mSignedSeeDetailTv != null) {
            mSignedSeeDetailTv.post(new Runnable() {
                @Override
                public void run() {
                    int[] location = new int[2];
                    mSignedSeeDetailTv.getLocationInWindow(location);
                    MyLog.d(TAG,location[1] + "y");
                    RelativeLayout.LayoutParams tipParams = (RelativeLayout.LayoutParams) mTvTipSeeWebRecords.getLayoutParams();
                    tipParams.topMargin = location[1] - DisplayUtils.dip2px(50);
                    mTvTipSeeWebRecords.setLayoutParams(tipParams);
                }
            });
        }
    }

    public void hideGuideLayout(){
        mRlGuide.setVisibility(View.GONE);
    }

    protected String getMoney(UserProfit profit, int type) {
        Pair<String, Integer> exchangeableMoneyInfo = getExchangeableMoneyInfo(profit, type);
//        return getString(R.string.exchangeable_money_amount_and_unit,
//                getHumanReadableMoney(exchangeableMoneyInfo.second), exchangeableMoneyInfo.first);
        //返回可提现金额（不带单位）
        return getHumanReadableMoney(exchangeableMoneyInfo.second);
    }

    protected Pair<String, Integer> getExchangeableMoneyInfo(@NonNull UserProfit profit, int type) {
        String currencyUnit;
        int moneyAmount = 0;
        if (CommonUtils.isChinese()){
            mCurrencyType = CURRENCY_TYPE_CNY;
        }else {
            mCurrencyType = CURRENCY_TYPE_USD;
        }
        switch (mCurrencyType) {
            case CURRENCY_TYPE_USD:
                currencyUnit = getString(R.string.usd_unit);
                switch (type) {
                    case UserProfit.TYPE_CNT:
                        moneyAmount = profit.getExchangeUsdcash();
                        break;
                    case UserProfit.TYPE_SHOW:
                        moneyAmount = profit.getShowUsdMoney();
                        break;
                    case UserProfit.TYPE_GAME:
                        moneyAmount = profit.getUsdGameExchangeCount();
                        break;
                }
                break;
            default:
                currencyUnit = getString(R.string.rmb_unit);
                switch (type) {
                    case UserProfit.TYPE_CNT:
                        moneyAmount = profit.getCurExchangeCount();
                        break;
                    case UserProfit.TYPE_SHOW:
                        moneyAmount = profit.getShowMoney();
                        break;
                    case UserProfit.TYPE_GAME:
                        moneyAmount = profit.getCurGameExchangeCount();
                        break;
                }
                break;
        }
        return Pair.create(currencyUnit, moneyAmount);
    }

    protected MyProgressDialogEx mProgressDialog;

    protected void showProcessDialog(long most, @StringRes int strId) {
        if (!isDetached()) {
            if (mProgressDialog == null) {
                mProgressDialog = MyProgressDialogEx.createProgressDialog(getActivity());
            }
            mProgressDialog.setMessage(getString(strId));
            mProgressDialog.show(most);
        }
    }

    protected void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

}

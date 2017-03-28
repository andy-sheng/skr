package com.wali.live.common.pay.presenter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.android.vending.billing.IInAppBillingService;
import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.network.NetworkUtils;
import com.base.utils.rx.RefuseRetryExeption;
import com.base.utils.rx.RxRetryAssist;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.XiaoMiOAuth;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.common.pay.constant.PayConstant;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.common.pay.constant.RechargeConfig;
import com.wali.live.common.pay.manager.PayManager;
import com.wali.live.common.pay.model.BalanceDetail;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.view.IRechargeView;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;
import com.xiaomi.game.plugin.stat.MiGamePluginStat;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.common.pay.fragment.RechargeFragment.getCurrentPayWay;
import static com.wali.live.common.pay.utils.PayStatisticUtils.getRechargeTemplate;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.CANCEL;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * @module 充值
 * Created by rongzhisheng on 16-7-1.
 */
public class RechargePresenter extends RxLifeCyclePresenter implements PayManager.PullRechargeListIface {
    private static final String TAG = RechargePresenter.class.getSimpleName();

    private IRechargeView mRechargeView;
    private IInAppBillingService mService;
    /**
     * 上次拉取哪种支付方式的价格列表
     */
    private PayWay mLastPullRechargeListPayWay;
    /**
     * MiLink请求序列
     */
    private int mPullRechargeListRequestId;
    /**
     * MiLink响应序列
     */
    private int mPullRechargeListResponseId;

    private BaseActivity mActivity;

    private Handler mHandle = new Handler();

    public RechargePresenter() {
        EventBus.getDefault().register(this);
        PayManager.setRechargePresenter(this);
        // 加上这个，支付宝SDK和小米钱包SDK就不会被限定一定要在Application的onCreate里调用了
        MiGamePluginStat.setCheckInitEnv(false);
    }

    public RechargePresenter(IRechargeView view, BaseActivity activity) {
        this();
        setRechargeView(view);
        mActivity = activity;
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
        mActivity = null;
        mHandle.removeCallbacksAndMessages(null);
    }

    public void setRechargeView(IRechargeView view) {
        mRechargeView = view;
    }

    public void setInAppBillingService(IInAppBillingService service) {
        mService = service;
    }

    private String getString(@StringRes int stringResId, Object... formatArgs) {
        return GlobalData.app().getString(stringResId, formatArgs);
    }

    public void clearPriceListCache() {
        PayManager.getDiamondListCache().clear();
    }

    @Override
    public boolean isNotPullRechargeList() {
        return mRechargeView.isNotPullRechargeList();
    }

    public void pullPriceListAsync() {
        if (mRechargeView == null) {
            MyLog.e(TAG, "mRechargeView is null in pullPriceListAsync");
            return;
        }
        mPullRechargeListRequestId++;
        // 使充值列表在切换国际支付方式的时候快速失效
        clearPriceListCache();
        mRechargeView.setRecyclerViewLoadingStatusAndNotify();
        mLastPullRechargeListPayWay = getCurrentPayWay();
        PayManager.pullPriceListAsync();
    }

    private List<Diamond> getDiamondListCache() {
        return PayManager.getDiamondListCache();
    }

    private void setHasReadRedPoint() {
        PayManager.setHasReadRedPoint();
    }

    public int getExchangeableDiamondCnt() {
        return PayManager.getExchangeableDiamondCnt();
    }

    public int getWillExpireDiamondCnt() {
        return PayManager.getWillExpireDiamondCnt();
    }

    public int getWillExpireGiftCardCnt() {
        return PayManager.getWillExpireGiftCardCnt();
    }

    private PayProto.CreateOrderResponse createOrderSync(Diamond goods, PayProto.PayType type) {
        return PayManager.createOrderSync(goods, type);
    }

    private PayProto.CheckOrderResponse checkOrderSync(String orderId, String payId, String receipt, String transactionId) {
        return PayManager.checkOrderSync(orderId, payId, receipt, transactionId);
    }

    /**
     * 充值
     *
     * @param goods
     * @param payWay
     */
    public void recharge(final Diamond goods, final PayWay payWay) {
        MyLog.w(TAG, "start buy diamond");
        Observable.just(0)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        //可能需要创建进度条
                        mRechargeView.showProcessDialog(5000);
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Integer, Observable<PayProto.PayType>>() {
                    @Override
                    public Observable<PayProto.PayType> call(Integer integer) {
                        PayProto.PayType type = null;
                        //创建订单
                        switch (payWay) {
                            case WEIXIN:
                                type = PayProto.PayType.WEIXIN;
                                return Observable.just(type);
                            case ZHIFUBAO:
                                type = PayProto.PayType.ALIPAY;
                                return Observable.just(type);
                            case MIBI:
                                type = PayProto.PayType.MIPAY;
                                return Observable.just(type);
                            case MIWALLET:
                                type = PayProto.PayType.MIWALLET;
                                return Observable.just(type);
                        }
                        return Observable.error(new RefuseRetryExeption(getString(R.string.pay_way_not_selected)));
                    }
                })
                .flatMap(new Func1<PayProto.PayType, Observable<PayProto.PayType>>() {
                    @Override
                    public Observable<PayProto.PayType> call(PayProto.PayType type) {
                        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
                            return Observable.error(new RefuseRetryExeption(getString(R.string.network_unavailable)));
                        }
                        return Observable.just(type);
                    }
                })
                .map(new Func1<PayProto.PayType, PayProto.CreateOrderResponse>() {
                    @Override
                    public PayProto.CreateOrderResponse call(PayProto.PayType type) {
                        return createOrderSync(goods, type);
                    }
                })
                .flatMap(new Func1<PayProto.CreateOrderResponse, Observable<PayProto.CreateOrderResponse>>() {
                    @Override
                    public Observable<PayProto.CreateOrderResponse> call(PayProto.CreateOrderResponse createOrderResponse) {
                        if (createOrderResponse == null) {
                            return Observable.error(new RefuseRetryExeption(getString(R.string.create_order_fail_server_timeout)));
                        } else if (createOrderResponse.getRetCode() != 0) {
                            if (createOrderResponse.getRetCode() == PayManager.GOODS_NOT_EXIST) {
                                pullPriceListAsync();
                                return Observable.error(new Exception(getString(R.string.create_order_goods_not_exist)));
                            }
                            return Observable.error(new RefuseRetryExeption(getString(R.string.create_order_fail_return_code, createOrderResponse.getRetCode())));
                        }
                        return Observable.just(createOrderResponse);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRechargeView.<PayProto.CreateOrderResponse>bindUntilEvent())
                .subscribe(new Observer<PayProto.CreateOrderResponse>() {
                    @Override
                    public void onCompleted() {
                        if (payWay != PayWay.MIBI) {
                            mRechargeView.hideProcessDialog(1000);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "create order fail", e);
                        String msg = e.getMessage();
                        if (!TextUtils.isEmpty(msg)) {
                            mRechargeView.showToast(msg);
                        }
                        mRechargeView.hideProcessDialog(1000);
                    }

                    @Override
                    public void onNext(PayProto.CreateOrderResponse response) {
                        String orderId = response.getOrderId();
                        int price = goods.getPrice();
                        String cpUserInfo = response.getUserInfo();
                        MyLog.w(TAG, "create order success [orderId:" + orderId + ",price:" + price
                                + ",count:" + goods.getCount() + ",payWay:" + payWay + "]");
                        switch (payWay) {
                            case MIBI:
                                //todo
                                payByMibi(orderId, price, cpUserInfo);
                                break;
                            default:
                                MyLog.e(TAG, "unknown payWay:" + payWay);
                                break;
                        }
                    }
                });
    }

//    @NonNull
//    private String getErrorMsgByErrorCode(int errorCode) {
//        switch (errorCode) {
//            case GET_SESSION_ERROR:
//                return getString(R.string.pay_error_code_get_session_fail);
//            case TOAST_GETSESSION_ERROR:
//                return getString(R.string.pay_error_code_get_session_error);
//            case CREATE_UNDEFINEORDER_ERROR:
//                return getString(R.string.pay_error_code_order_create_fail);
//            case WXPAY_ERROR:
//            case SCANPAY_ERROR:
//            case TOAST_PAY_FAIL:
//                return getString(R.string.pay_error_code_fail);
//            case WXPAY_CANCEL:
//            case SCANPAY_CANCEL:
//            case TOAST_PAY_CANCEL:
//                return getString(R.string.pay_error_code_cancel);
//            case TOAST_CLIENT_NOT_INSTALL:
//                return getString(R.string.pay_error_code_client_not_install);
//            case TOAST_NETWORK_ERROR:
//            case NET_ERROR:
//                return getString(R.string.pay_error_code_network_error);
//            case TOAST_CREATE_UNDEFINE_ORDER_ERROR:
//                return getString(R.string.pay_error_code_get_order_error);
//            case GET_PAYINTO_ERROR:
//                return getString(R.string.pay_error_code_pay_info_error);
//            default:
//                return getString(R.string.pay_error_code_fail);
//        }
//    }

    /**
     * 在每次用户支付成功时调用，保存用户的支付方式
     */
    public void saveUserSelectedPayWay() {
        final PayWay currentPayWay = getCurrentPayWay();
        Observable.just(currentPayWay)
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<PayWay>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "saved user selected payWay:" + currentPayWay);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "failed to save user selected payWay:" + currentPayWay);
                    }

                    @Override
                    public void onNext(PayWay payWay) {
                        PreferenceUtils.setSettingString(GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                                RechargeConfig.getLastPaywayKey(), payWay.name().toUpperCase());
                    }
                });
    }

    /**
     * 充值成功后调用此方法，记录用户是否为首次充值
     */
    private void saveRechargedInfo() {
        if (mRechargeView != null && mRechargeView.isFirstRecharge()) {
            Observable.just(false)
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                            MyLog.w(TAG, "save user recharged info ok");
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Boolean b) {
                            PreferenceUtils.setSettingBoolean(
                                    GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                                    RechargeConfig.getIsFirstRechargeKey(),
                                    false);
                        }
                    });
        }
        saveUserSelectedPayWay();
    }

    private void payByMibi(final String orderId, final int price, final String userInfo) {

        mRechargeView.setMibiRechargeLoginStatus(true);

        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId(String.valueOf(XiaoMiOAuth.APP_ID_PAY));
        appInfo.setAppKey(XiaoMiOAuth.APP_KEY_PAY);
        MiCommplatform.Init(GlobalData.app(), appInfo);

        MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int code, MiAccountInfo miAccountInfo) {
                MyLog.w(TAG,"MiCommplatform.milogin retCode:"+code);
                mRechargeView.hideProcessDialog(1000);
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        // 登陆成功
                        //获取用户的登陆后的UID（即用户唯一标识）
                        long uid = miAccountInfo.getUid();
                        String session = miAccountInfo.getSessionId();
                        MiBuyInfo miBuyInfo = new MiBuyInfo();
                        miBuyInfo.setCpOrderId(orderId);//订单号唯一（不为空）
                        miBuyInfo.setCpUserInfo(userInfo); //此参数在用户支付成功后会透传给CP的服务器
                        miBuyInfo.setAmount(price / 100);

                        MiCommplatform.getInstance().miUniPay(mActivity, miBuyInfo, new OnPayProcessListener() {
                            @Override
                            public void finishPayProcess(int code) {
                                MyLog.w(TAG,"MiCommplatform.miUniPay retCode:"+code);
                                mRechargeView.setMibiRechargeLoginStatus(false);
                                switch (code) {
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                                        //购买成功
                                        showToast(getString(R.string.mibi_pay_success_sync_order));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(SUCCESS, PayWay.MIBI), TIMES, "1");
                                        checkOrder(orderId, userInfo, null, null, true);
                                        break;
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                                        //购买失败
//                                        showToast(getErrorMsgByErrorCode(code));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(CANCEL, PayWay.MIBI), TIMES, "1");
                                        break;
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                                        //操作正在进行中
                                        break;
                                    default:
                                        //购买失败
//                                        showToast(getErrorMsgByErrorCode(code));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(CANCEL, PayWay.MIBI), TIMES, "1");
                                        break;
                                }
                            }
                        });
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                    default:
                        mRechargeView.setMibiRechargeLoginStatus(false);
                        //购买失败
//                        showToast(getErrorMsgByErrorCode(code));
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.MIBI), TIMES, "1");
                        break;
                }
            }
        });
    }

    private void showToast(final String msg) {
        if (mRechargeView != null) {
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    mRechargeView.showToast(msg);
                }
            });
        }
    }

    /**
     * 订单检查，加钻石
     *
     * @param orderId 内部订单号
     * @param payId
     * @param receipt
     */
    private void checkOrder(final String orderId, final String payId, final String receipt, final String transactionId, final boolean showTip) {
        MyLog.w(TAG, String.format("start check order, orderId:%s, payId:%s, receipt:%s", orderId, payId, receipt));
        Observable
                .just(null)
                .observeOn(Schedulers.io())
                .map(new Func1<Object, PayProto.CheckOrderResponse>() {
                    @Override
                    public PayProto.CheckOrderResponse call(Object o) {
                        return checkOrderSync(orderId, payId, receipt, transactionId);
                    }
                })
                .flatMap(new Func1<PayProto.CheckOrderResponse, Observable<PayProto.CheckOrderResponse>>() {
                    @Override
                    public Observable<PayProto.CheckOrderResponse> call(PayProto.CheckOrderResponse checkOrderResponse) {
                        if (checkOrderResponse == null) {
                            return Observable.error(new Exception(getString(R.string.query_order_fail_server_timeout)));
                        } else if (checkOrderResponse.getRetCode() != 0) {
                            MyLog.e(TAG, "check order retCode:" + checkOrderResponse.getRetCode());
                            return Observable.error(new Exception(getString(R.string.query_order_fail_return_code, checkOrderResponse.getRetCode())));
                        }
                        return Observable.just(checkOrderResponse);
                    }
                })
                .retryWhen(new RxRetryAssist(5, 6, false))// 每次5s,持续6次。
                .compose(mRechargeView.<PayProto.CheckOrderResponse>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PayProto.CheckOrderResponse>() {
                    @Override
                    public void onCompleted() {
                        if (showTip) {
                            mRechargeView.showToast(getString(R.string.recharge_success));
                        }
                        // 更新列表
//                         pullPriceListAsync();
                        MyLog.w(TAG, "add diamond success");
                        saveRechargedInfo();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "add diamond fail", e);
                        if (showTip) {
                            mRechargeView.showToast(e.getMessage());
                        }
                        MyUserInfoManager.getInstance().syncSelfDetailInfo();
                    }

                    @Override
                    public void onNext(PayProto.CheckOrderResponse checkOrderResponse) {
                        int diamondBalance = checkOrderResponse.getUsableGemCnt();
                        MyLog.w(TAG, "diamondBalance:" + diamondBalance);
                        // 会引起pullPriceListAsync
                        MyUserInfoManager.getInstance().setDiamondNum(diamondBalance);
                    }
                });
    }

    private Subscription mSyncBalanceSubscription;

    public void syncBalance() {
        if (mSyncBalanceSubscription != null && !mSyncBalanceSubscription.isUnsubscribed()) {
            // 仍在运行
            return;
        }
        mSyncBalanceSubscription = Observable.just(0)
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                        MyLog.w(TAG, "syncBalance");
                        MyUserInfoManager.getInstance().syncSelfDetailInfo();
                    }
                });
    }

    private Subscription mGetBalanceDetailSub;

    public void getBalance() {
        if (mGetBalanceDetailSub != null && !mGetBalanceDetailSub.isUnsubscribed()) {
            mGetBalanceDetailSub.unsubscribe();
        }
        mGetBalanceDetailSub = PayManager.getBalanceDetailRsp()
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<PayProto.QueryBalanceDetailResponse, Observable<BalanceDetail>>() {
                    @Override
                    public Observable<BalanceDetail> call(PayProto.QueryBalanceDetailResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse is null"));
                        } else if (rsp.getRetCode() != 0) {
                            return Observable.error(new Exception("QueryBalanceDetailResponse.retCode:" + rsp.getRetCode()));
                        }
                        return Observable.just(BalanceDetail.parseFrom(rsp));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BalanceDetail>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onNext(BalanceDetail balanceDetail) {
                        //TODO 余额界面
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(BalanceFragment.BUNDLE_KEY_BALANCE_DETAIL, balanceDetail);
//                BalanceFragment.openFragment((BaseAppActivity) mRechargeView.getActivity(), bundle, new WeakReference<>(this));
                    }
                });
    }

    private void loadPriceListFromCache() {
        Observable.just(getDiamondListCache())
                .flatMap(new Func1<List<Diamond>, Observable<List<Diamond>>>() {
                    @Override
                    public Observable<List<Diamond>> call(List<Diamond> diamonds) {
                        if (diamonds == null || diamonds.isEmpty()) {
                            return Observable.error(new Exception());
                        }
                        return Observable.just(diamonds);
                    }
                })
                .retryWhen(new RxRetryAssist(1, getString(R.string.pull_diamond_price_list_failed)))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRechargeView.<List<Diamond>>bindUntilEvent())
                .subscribe(new Observer<List<Diamond>>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "load price list completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        String msg = e.getMessage();
                        MyLog.e(TAG, "loadPriceListFromCache error, msg:" + msg);
                        if (!TextUtils.isEmpty(msg)) {
                            mRechargeView.showToast(msg);
                        }
                        clearPriceListCache();
                        mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(getDiamondListCache());
                    }

                    @Override
                    public void onNext(List<Diamond> diamonds) {
                        mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(diamonds);
                        setHasReadRedPoint();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.PayEvent event) {
        mPullRechargeListResponseId++;
        switch (event.eventType) {
            case EventClass.PayEvent.EVENT_TYPE_PAY_DIAMOND_CACHE_CHANGE: {
                MyLog.w(TAG, "recharge list updated, size:" + getDiamondListCache().size()
                        + ", requestId:" + mPullRechargeListRequestId
                        + ", responseId:" + mPullRechargeListResponseId);
                // TODO 需要rsp中包含信息，以便决定要不要使用此rsp
                //if (mPullRechargeListRequestId == mPullRechargeListResponseId) {
                loadPriceListFromCache();
                //} else {
                //    MyLog.w(TAG, "requestId:" + mPullRechargeListRequestId + ", responseId:" + mPullRechargeListResponseId);
                //}
                mRechargeView.updateExchangeableAndWillExpireDiamond(getExchangeableDiamondCnt()
                        , getWillExpireDiamondCnt(), getWillExpireGiftCardCnt());
            }
            break;
            case EventClass.PayEvent.EVENT_TYPE_PAY_EXCHANGEABLE_DIAMOND_CHANGE: {
                MyLog.w(TAG, "only update exchangeable diamond count:" + getExchangeableDiamondCnt());
                mRechargeView.updateExchangeableAndWillExpireDiamond(getExchangeableDiamondCnt()
                        , getWillExpireDiamondCnt(), getWillExpireGiftCardCnt());
            }
            break;
        }
    }

    /**
     * 用户信息变化事件，这里只关心钻石数和虚拟钻石数
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        MyLog.w(TAG, String.format("user diamond changed, diamond:%d, virtual diamond:%d",
                MyUserInfoManager.getInstance().getUser().getDiamondNum(), MyUserInfoManager.getInstance().getUser().getVirtualDiamondNum()));
        mRechargeView.setBalanceText(MyUserInfoManager.getInstance().getUser().getDiamondNum(), MyUserInfoManager.getInstance().getUser().getVirtualDiamondNum());
        //在星票变化时需要拉取可兑换钻石数目  可兑换钻石数目是在拉取充值列表时带回来的
        pullPriceListAsync();
    }

    /**
     * 可用钻石数变化的push
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.PayPush event) {
        if (event == null
                || event.payPush == null
                || event.payPush.getRetCode() != ErrorCode.CODE_SUCCESS) {
            return;
        }
        PayProto.PayPush payPush = event.payPush;
        MyLog.w(TAG, "payPush:" + payPush);
        MyUserInfoManager.getInstance().setDiamondNum(payPush.getUsableGemCnt());
    }
}


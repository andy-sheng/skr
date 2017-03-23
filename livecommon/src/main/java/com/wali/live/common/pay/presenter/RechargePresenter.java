package com.wali.live.common.pay.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.SparseArray;

import com.android.vending.billing.IInAppBillingService;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.network.NetworkUtils;
import com.base.utils.rx.RefuseRetryExeption;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.XiaoMiOAuth;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.common.pay.constant.PayConstant;
import com.wali.live.common.pay.constant.PayWay;
import com.wali.live.common.pay.manager.PayManager;
import com.wali.live.common.pay.model.BalanceDetail;
import com.wali.live.common.pay.model.Diamond;
import com.wali.live.common.pay.model.SkuDetail;
import com.wali.live.common.pay.model.UnconsumedProduct;
import com.wali.live.common.pay.utils.PayCommonUtils;
import com.wali.live.common.pay.view.IRechargeView;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;
import com.xiaomi.game.plugin.stat.MiGamePluginStat;
import com.xiaomi.gamecenter.alipay.HyAliPay;
import com.xiaomi.gamecenter.ucashier.HyUcashierPay;
import com.xiaomi.gamecenter.ucashier.PayResultCallback;
import com.xiaomi.gamecenter.ucashier.purchase.FeePurchase;
import com.xiaomi.gamecenter.wxwap.HyWxWapPay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

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
import static com.wali.live.statistics.StatisticsKey.Recharge.PAY_ERROR_CODE;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.ALIPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.ALIPAY_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.CREATE_UNDEFINEORDER_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.GET_PAYINTO_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.GET_SESSION_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.NET_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.SCANPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.SCANPAY_ERROR;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.WXPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.WXPAY_ERROR;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_CLIENT_NOT_INSTALL;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_CREATE_UNDEFINE_ORDER_ERROR;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_GETSESSION_ERROR;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_NETWORK_ERROR;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_PAY_CANCEL;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_PAY_FAIL;

/**
 * @module 充值
 * Created by rongzhisheng on 16-7-1.
 */
public class RechargePresenter extends RxLifeCyclePresenter implements PayManager.PullRechargeListIface {
    private static final String TAG = RechargePresenter.class.getSimpleName();

    // Google Play 相关常量
    public static final int GOOGLE_PLAY_PUCHASE_REQUEST_CODE = 1001;
    public static final int PAYPAL_PAY_REQUEST_CODE = 1002;

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

    public RechargePresenter() {
        EventBus.getDefault().register(this);
        PayManager.setRechargePresenter(this);
        // 加上这个，支付宝SDK和小米钱包SDK就不会被限定一定要在Application的onCreate里调用了
        MiGamePluginStat.setCheckInitEnv(false);
    }

    public RechargePresenter(IRechargeView view) {
        this();
        setRechargeView(view);
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
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

    public void showPopup() {
        mRechargeView.showPopupWindow();
    }

    //TODO GooglePlay相关 Begin >>>>>

    /**
     * 消费掉用户在GooglePlay上购买的商品，用于应对当时本该消费掉但失败的情况，在进入充值界面时调用
     */
    public void consumeGooglePlayProduct() {
        getUnconsumedProductFromGooglePlay()
                .flatMap(new Func1<List<UnconsumedProduct>, Observable<UnconsumedProduct>>() {
                    @Override
                    public Observable<UnconsumedProduct> call(List<UnconsumedProduct> unconsumedProductList) {
                        return Observable.from(unconsumedProductList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<UnconsumedProduct>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "consume previous GooglePlay Product completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(e);
                        MyLog.w(TAG, "consume previous GooglePlay Product failed");
                    }

                    @Override
                    public void onNext(UnconsumedProduct unconsumedProduct) {
                        try {
                            JSONObject receipt = new JSONObject(unconsumedProduct.getPurchaseData());
                            checkOrder(receipt.getString(PayConstant.GooglePlay.DEVELOPER_PAYLOAD), unconsumedProduct.getSignature(),
                                    unconsumedProduct.getPurchaseData(), null, false);
                        } catch (JSONException e) {
                            MyLog.e(TAG, String.format("parse purchase data to json fail, data:%s, msg:%s",
                                    unconsumedProduct.getPurchaseData(), e.getMessage()));
                        }
                    }
                });
    }

    /**
     * 获取用户有哪些在GooglePlay购买了，但是没有消费掉的商品
     *
     * @return
     */
    @CheckResult
    private Observable<List<UnconsumedProduct>> getUnconsumedProductFromGooglePlay() {
        return Observable.create(new Observable.OnSubscribe<List<UnconsumedProduct>>() {
            @Override
            public void call(Subscriber<? super List<UnconsumedProduct>> subscriber) {
                MyLog.w(TAG, "start query unconsumed GooglePlay products");
                if (mService == null) {
                    MyLog.e(TAG, "mService is null");
                    subscriber.onError(new Exception("mService is null"));
                    return;
                }
                String continuationToken = null;
                int times = 0;
                List<UnconsumedProduct> unconsumedProductList = new ArrayList<>();
                do {
                    try {
                        Bundle ownedItems = mService.getPurchases(PayConstant.GooglePlay.IN_APP_BILLING_VERSION, mRechargeView.getPackageName0(), PayConstant.GooglePlay.PRODUCT_TYPE_INAPP, continuationToken);
                        if (ownedItems == null) {
                            MyLog.e(TAG, "query unconsumed products form google play fail, ownedItems info is null");
                            subscriber.onError(new Exception("ownedItems info is null"));
                            break;
                        }
                        times++;
                        int rspCode = ownedItems.getInt(PayConstant.GooglePlay.RESPONSE_CODE);
                        if (rspCode == GooglePlayRspCode.OK.getValue()) {
                            ArrayList<String> purchaseDataList =
                                    ownedItems.getStringArrayList(PayConstant.GooglePlay.INAPP_PURCHASE_DATA_LIST);
                            ArrayList<String> signatureList =
                                    ownedItems.getStringArrayList(PayConstant.GooglePlay.INAPP_DATA_SIGNATURE_LIST);
                            continuationToken = ownedItems.getString(PayConstant.GooglePlay.INAPP_CONTINUATION_TOKEN);
                            MyLog.w(TAG, "times:" + times + ", purchaseDataList:" + purchaseDataList +
                                    ", signatureList:" + signatureList + ", continuationToken:" + continuationToken);
                            if (purchaseDataList.isEmpty() && times == 1) {
                                MyLog.i(TAG, "congratulations, all of product that user purchased from google play was converted to diamond");
                                subscriber.onCompleted();
                                return;
                            }
                            for (int i = 0; i < purchaseDataList.size(); i++) {
                                String purchaseData = purchaseDataList.get(i);
                                String signature = signatureList.get(i);
                                unconsumedProductList.add(new UnconsumedProduct(purchaseData, signature));
                            }
                        } else {
                            GooglePlayRspCode googlePlayRspCode = GooglePlayRspCode.valueOf(rspCode, GooglePlayRspCode.UNKNOWN);
                            MyLog.e(TAG, "query unconsumed products form google play fail, code:" +
                                    googlePlayRspCode);
                            subscriber.onError(new Exception(googlePlayRspCode.toString()));
                        }
                    } catch (RemoteException e) {
                        MyLog.e(TAG, "query unconsumed products form google play fail", e);
                        subscriber.onError(e);
                        break;
                    } catch (Exception e) {
                        MyLog.e(TAG, "query unconsumed products form google play fail", e);
                        subscriber.onError(e);
                        break;
                    }
                } while (continuationToken != null);
                MyLog.w(TAG, "user has " + unconsumedProductList.size() + " unconsumed GooglePlay product");
                subscriber.onNext(unconsumedProductList);
                subscriber.onCompleted();
            }
        });
    }

    /**
     * 根据订单信息消费用户在GooglePlay上购买的商品
     *
     * @param receipt
     */
    private void consumeGooglePlayProduct(@NonNull JSONObject receipt) {
        MyLog.w(TAG, "start to consume google play product, receipt:" + receipt);
        try {
            String purchaseToken = receipt.getString(PayConstant.GooglePlay.PURCHASE_TOKEN);
            if (!TextUtils.isEmpty(purchaseToken)) {
                int rspCode = mService.consumePurchase(PayConstant.GooglePlay.IN_APP_BILLING_VERSION, mRechargeView.getPackageName0(), purchaseToken);
                if (rspCode == GooglePlayRspCode.OK.getValue()) {
                    MyLog.i(TAG, "consume product success, receipt:" + receipt.toString());
                    saveRechargedInfo();
                    return;
                } else {
                    MyLog.e(TAG, "consume product fail, rspCode:" + GooglePlayRspCode.valueOf(rspCode, GooglePlayRspCode.UNKNOWN));
                }
            } else {
                MyLog.e(TAG, "consume product fail, purchaseToken is empty");
            }
        } catch (JSONException e) {
            MyLog.e(TAG, "consume product fail, json error, msg:" + e.getMessage() + ",receipt:" + receipt);
        } catch (RemoteException e) {
            MyLog.e(TAG, "consume product fail, call google play fail, msg:" + e.getMessage() + ",receipt:" + receipt);
        } catch (Exception e) {
            MyLog.e(TAG, "consume product fail, unexpected error, msg:" + e.getMessage() + ",receipt:" + receipt);
        }
    }

    private void payByGooglePlay(String orderId, Diamond diamond) {
        // 1、去GooglePlay买一个商品
        MyLog.i(TAG, "start purchase product from google play, orderId:" + orderId);
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(PayConstant.GooglePlay.IN_APP_BILLING_VERSION, mRechargeView.getPackageName0(),
                    diamond.getSkuDetail().getProductId(), PayConstant.GooglePlay.PRODUCT_TYPE_INAPP, orderId);
            if (buyIntentBundle != null) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable(PayConstant.GooglePlay.BUY_INTENT);
                if (pendingIntent != null) {
                    mRechargeView.getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(), GOOGLE_PLAY_PUCHASE_REQUEST_CODE,
                            new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                    return;
                } else {
                    MyLog.e(TAG, "pendingIntent is null");
                }
            } else {
                MyLog.e(TAG, "buyIntentBundle is null");
            }
        } catch (RemoteException e) {
            MyLog.e(TAG, "get buy intent fail", e);
        } catch (IntentSender.SendIntentException e) {
            MyLog.e(TAG, "send intent fail", e);
        } catch (Exception e) {
            MyLog.e(TAG, "unexpected error", e);
            MyLog.e(TAG, e);
        }
        String errMsg = getString(R.string.google_play_buy_fail_msg);
        MyLog.e(TAG, errMsg);
        mRechargeView.showToast(errMsg);
        //ToastUtils.showWithDrawToast(GlobalData.app(), R.string.google_play_buy_fail_msg, Toast.LENGTH_SHORT);
    }

    /**
     * Google Play支付完成后或取消支付等
     */
//    public void handleGooglePay(int resultCode, Intent data) {
//        int rspCode = data.getIntExtra(PayConstant.GooglePlay.RESPONSE_CODE, 0);
//        MyLog.w(TAG, "after purchase product from google play, rspCode:" + GooglePlayRspCode.valueOf(rspCode, GooglePlayRspCode.UNKNOWN)
//                + ", resultCode:" + resultCode);
//        if (resultCode != RESULT_OK) {
//            MyLog.e(TAG, "after purchase product from google play, rspCode:" + GooglePlayRspCode.valueOf(rspCode, GooglePlayRspCode.UNKNOWN)
//                    + ", resultCode:" + resultCode);
//            ToastUtils.showToast(R.string.pay_error_code_fail);
//        } else {
//            if (rspCode == GooglePlayRspCode.OK.getValue()) {
//                String purchaseData = data.getStringExtra(PayConstant.GooglePlay.INAPP_PURCHASE_DATA);
//                String dataSignature = data.getStringExtra(PayConstant.GooglePlay.INAPP_DATA_SIGNATURE);
//                MyLog.w(TAG, String.format("purchaseData:%s, dataSignature:%s", purchaseData, dataSignature));
//                if (TextUtils.isEmpty(purchaseData) || TextUtils.isEmpty(dataSignature)) {
//                    MyLog.e(TAG, String.format("purchaseData:%s, dataSignature:%s", purchaseData, dataSignature));
//                    return;
//                }
//                try {
//                    JSONObject receipt = new JSONObject(purchaseData);
//                    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                            PayStatisticUtils.getRechargeTemplate(SUCCESS, PayWay.GOOGLEWALLET), TIMES, "1");
//                    // 2、去服务器请求加钻
//                    checkOrder(receipt.getString(PayConstant.GooglePlay.DEVELOPER_PAYLOAD), dataSignature, purchaseData, null, true);
//                } catch (JSONException e) {
//                    MyLog.e(TAG, String.format("parse purchase data to json fail, data:%s, msg:%s", purchaseData, e.getMessage()));
//                }
//            } else {
//                MyLog.e(TAG, "purchase product in google play fail, google rspCode:"
//                        + GooglePlayRspCode.valueOf(rspCode, GooglePlayRspCode.UNKNOWN) + ", resultCode:" + resultCode);
//                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                        PayStatisticUtils.getRechargeTemplate(PAY_ERROR_CODE, PayWay.GOOGLEWALLET, rspCode), TIMES, "1");
//
//                if (rspCode == GooglePlayRspCode.USER_CANCELED.getValue()) {
//                    mRechargeView.showToast(getString(R.string.pay_error_code_cancel));
//                    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                            PayStatisticUtils.getRechargeTemplate(CANCEL, PayWay.GOOGLEWALLET), TIMES, "1");
//                } else {
//                    ToastUtils.showToast(R.string.pay_error_code_fail);
//                }
//            }
//        }
//    }

    /**
     * 根据productId列表批量查询SkuInfo
     *
     * @param skuList
     * @return 当请求失败或发生异常时返回null
     */
    @Nullable
    @CheckResult
    public Map<String, SkuDetail> querySkuInfo(ArrayList<String> skuList, boolean isPayByGoogleWallet) {
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(PayConstant.GooglePlay.ITEM_ID_LIST, skuList);
        try {
            Bundle skuDetails = getSkuInfo(querySkus);
            if (skuDetails == null) {
//                if (canShowErrorToast() && getCurrentPayWay() == PayWay.GOOGLEWALLET) {
//                    mRechargeView.showToast(getString(isPayByGoogleWallet ? R.string.connect_google_play_fail_toast : R.string.pull_diamond_price_list_failed));
//                }
                return null;
            }
            int responseCode = skuDetails.getInt(PayConstant.GooglePlay.RESPONSE_CODE);
            if (responseCode == GooglePlayRspCode.OK.getValue()) {
                ArrayList<String> responseList = skuDetails.getStringArrayList(PayConstant.GooglePlay.DETAILS_LIST);
                Map<String, SkuDetail> productIdSkuDetailMap = new HashMap<>();
                for (String response : responseList) {
                    SkuDetail skuDetail = toSkuDetail(response);
                    if (skuDetail != null) {
                        productIdSkuDetailMap.put(skuDetail.getProductId(), skuDetail);
                    }
                }
                return productIdSkuDetailMap;
            } else {
                MyLog.e(TAG, "get sku info fail, rsp code:" + GooglePlayRspCode.valueOf(responseCode, GooglePlayRspCode.UNKNOWN));
//                if (canShowErrorToast() && getCurrentPayWay() == PayWay.GOOGLEWALLET) {
//                    mRechargeView.showToast(getString(isPayByGoogleWallet ? R.string.connect_google_play_fail_toast : R.string.pull_diamond_price_list_failed));
//                }
                return null;
            }

        } catch (RemoteException e) {
            MyLog.e(TAG, "get sku info fail, msg:" + e.getMessage());
            return null;
        } catch (Exception e) {
            MyLog.e(TAG, "get sku info fail, msg:" + e.getMessage());
            return null;
        }
    }

    /**
     * 把jsonStr转换成SkuDetail
     *
     * @param jsonStr
     * @return 当尝jsonStr转换为JSONObject失败时以及缺少字段时，返回null
     */
    @Nullable
    @CheckResult
    private static SkuDetail toSkuDetail(String jsonStr) {
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            SkuDetail skuDetail = new SkuDetail();
            skuDetail.setProductId(json.getString("productId"));
            skuDetail.setType(json.getString("type"));
            skuDetail.setPrice(json.getString("price"));
            skuDetail.setPriceAmountMicros(json.getLong("price_amount_micros"));
            skuDetail.setPriceCurrencyCode(json.getString("price_currency_code"));
            skuDetail.setTitle(json.optString("title", null));
            skuDetail.setDescription(json.optString("description", null));
            return skuDetail;
        } catch (JSONException e) {
            MyLog.e(TAG, "parse sku detail json fail, msg:" + e.getMessage());
            return null;
        }
    }

    /**
     * 从GooglePlay查询给定的sku信息
     */
    @Nullable
    private Bundle getSkuInfo(final Bundle querySkus) throws RemoteException {
        if (mService == null) {
            return null;
        }
        // 从GooglePlay查询可能花费大量时间,会阻塞对后续包的处理,所以此处用超时机制
        FutureTask<Bundle> querySkuInfoTask = new FutureTask<Bundle>(new Callable<Bundle>() {
            @Override
            public Bundle call() throws Exception {
                return mService.getSkuDetails(PayConstant.GooglePlay.IN_APP_BILLING_VERSION, mRechargeView.getPackageName0(),
                        PayConstant.GooglePlay.PRODUCT_TYPE_INAPP, querySkus);
            }
        });
        new Thread(querySkuInfoTask).start();
        Bundle skuInfo = null;
        try {
            skuInfo = querySkuInfoTask.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            MyLog.e(TAG, "query sku info from GooglePlay fail", e);
            if (!querySkuInfoTask.isCancelled() && !querySkuInfoTask.isDone()) {
                querySkuInfoTask.cancel(true);
                MyLog.e(TAG, "cancel querySkuInfoTask");
            }
        }
        return skuInfo;
    }

    enum GooglePlayRspCode {
        UNKNOWN(-1),// 我们自定义的错误
        OK(0),
        USER_CANCELED(1),
        SERVICE_UNAVAILABLE(2),
        BILLING_UNAVAILABLE(3),
        ITEM_UNAVAILABLE(4),
        DEVELOPER_ERROR(5),
        ERROR(6),
        ITEM_ALREADY_OWNED(7),
        ITEM_NOT_OWNED(8);

        private int value;

        public int getValue() {
            return value;
        }

        GooglePlayRspCode(int value) {
            this.value = value;
        }

        private static SparseArray<GooglePlayRspCode> valueCodeMap = init();

        private static SparseArray<GooglePlayRspCode> init() {
            SparseArray<GooglePlayRspCode> map = new SparseArray<>();
            for (GooglePlayRspCode code : GooglePlayRspCode.values()) {
                map.put(code.value, code);
            }
            return map;
        }

        public static GooglePlayRspCode valueOf(int value, GooglePlayRspCode defaultValue) {
            GooglePlayRspCode code = valueCodeMap.get(value);
            return code != null ? code : defaultValue;
        }
    }

    //TODO GooglePlay相关 End <<<<<

    public boolean canShowErrorToast() {
        return mLastPullRechargeListPayWay == getCurrentPayWay()
                //&& (!mRechargeView.isFirstRecharge() || (mRechargeView.isFirstRecharge() && mRechargeView.existExpandedPayWay()))
                ;
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
        //if (mRechargeView.isFirstRecharge()) {
        //    mRechargeView.setExpandableListLoadingStatusAndNotify();
        //} else {
        mRechargeView.setRecyclerViewLoadingStatusAndNotify();
        //}
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
//                            case GOOGLEWALLET:
//                                type = PayProto.PayType.GO_PLAY;
//                                return Observable.just(type);
//                            case PAYPAL:
//                                type = PayProto.PayType.PAYPAL;
//                                return Observable.just(type);
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
                        mRechargeView.hideProcessDialog(1000);
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
                            case WEIXIN:
                                payByWeixin(orderId, price, cpUserInfo);
                                break;
                            case ZHIFUBAO:
                                payByAlipay(orderId, price, cpUserInfo);
                                break;
                            case MIBI:
                                break;
                            case MIWALLET:
                                payByMiWallet(orderId, price, cpUserInfo);
                                break;
//                            case GOOGLEWALLET:
//                                payByGooglePlay(orderId, goods);
//                                break;
//                            case PAYPAL:
//                                payByPayPal(orderId, goods);
//                                break;
                            default:
                                MyLog.e(TAG, "unknown payWay:" + payWay);
                                break;
                        }
                    }
                });
    }

    @NonNull
    private String getErrorMsgByErrorCode(int errorCode) {
        switch (errorCode) {
            case GET_SESSION_ERROR:
                return getString(R.string.pay_error_code_get_session_fail);
            case TOAST_GETSESSION_ERROR:
                return getString(R.string.pay_error_code_get_session_error);
            case CREATE_UNDEFINEORDER_ERROR:
                return getString(R.string.pay_error_code_order_create_fail);
            case WXPAY_ERROR:
            case SCANPAY_ERROR:
            case ALIPAY_ERROR:
            case TOAST_PAY_FAIL:
                return getString(R.string.pay_error_code_fail);
            case WXPAY_CANCEL:
            case SCANPAY_CANCEL:
            case ALIPAY_CANCEL:
            case TOAST_PAY_CANCEL:
                return getString(R.string.pay_error_code_cancel);
            case TOAST_CLIENT_NOT_INSTALL:
                return getString(R.string.pay_error_code_client_not_install);
            case TOAST_NETWORK_ERROR:
            case NET_ERROR:
                return getString(R.string.pay_error_code_network_error);
            case TOAST_CREATE_UNDEFINE_ORDER_ERROR:
                return getString(R.string.pay_error_code_get_order_error);
            case GET_PAYINTO_ERROR:
                return getString(R.string.pay_error_code_pay_info_error);
        }
        return "";
    }

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
                                PayConstant.SP_KEY_LAST_PAY_WAY, payWay.name().toUpperCase());
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
                                    PayConstant.SP_KEY_IS_FIRST_RECHARGE,
                                    false);
                        }
                    });
        }
        saveUserSelectedPayWay();
    }

    private void payByWeixin(String orderId, int price, final String userInfo) {
        com.xiaomi.gamecenter.wxwap.purchase.FeePurchase purchase = new com.xiaomi.gamecenter.wxwap.purchase.FeePurchase();
        purchase.setCpOrderId(orderId);
        purchase.setFeeValue(String.valueOf(price));
        purchase.setCpUserInfo(userInfo);
        HyWxWapPay hyWxPay = null;
        try {
            hyWxPay = HyWxWapPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init HyWxWapPay sdk fail, init here", e);
            HyWxWapPay.init(GlobalData.app(), String.valueOf(XiaoMiOAuth.APP_ID_PAY), XiaoMiOAuth.APP_KEY_PAY);
            hyWxPay = HyWxWapPay.getInstance();
        }
        hyWxPay.pay(mRechargeView.getActivity(), purchase, new com.xiaomi.gamecenter.wxwap.PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s, errorCode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                if (mRechargeView != null) {
                    ToastUtils.showToast(getErrorMsgByErrorCode(errorCode));
                }
                switch (errorCode) {
                    case WXPAY_CANCEL:
                    case SCANPAY_CANCEL://取消扫码支付
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.WEIXIN), TIMES, "1");
                        break;
                    //case CODE_WX_NOT_INSTALL:
                    //    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    //            getRechargeTemplate(APP_NOT_INSTALL, PayWay.WEIXIN), TIMES, "1");
                    //    break;
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(PAY_ERROR_CODE, PayWay.WEIXIN, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("weixin pay ok, orderId:%s, uid:%s", orderId, userInfo));
                if (mRechargeView != null) {
                    ToastUtils.showToast(R.string.weixin_pay_success_sync_order);
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(SUCCESS, PayWay.WEIXIN), TIMES, "1");
                checkOrder(orderId, userInfo, null, null, true);
            }
        });
    }

    private void payByAlipay(String orderId, int price, final String userInfo) {
        com.xiaomi.gamecenter.alipay.purchase.FeePurchase purchase = new com.xiaomi.gamecenter.alipay.purchase.FeePurchase();
        purchase.setCpOrderId(orderId);
        purchase.setFeeValue(String.valueOf(price));
        purchase.setCpUserInfo(userInfo);
        HyAliPay hyAliPay = null;
        try {
            hyAliPay = HyAliPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init alipay sdk fail, init here", e);
            HyAliPay.init(GlobalData.app(), String.valueOf(XiaoMiOAuth.APP_ID_PAY), XiaoMiOAuth.APP_KEY_PAY);
            hyAliPay = HyAliPay.getInstance();
        }

        hyAliPay.aliPay(mRechargeView.getActivity(), purchase, new com.xiaomi.gamecenter.alipay.PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s, errorCode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                if (mRechargeView != null) {
                    mRechargeView.showToast(getErrorMsgByErrorCode(errorCode));
                }
                switch (errorCode) {
                    case WXPAY_CANCEL:
                    case SCANPAY_CANCEL:
                    case ALIPAY_CANCEL:
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.ZHIFUBAO), TIMES, "1");
                        break;
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(PAY_ERROR_CODE, PayWay.ZHIFUBAO, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("alipay pay ok, orderId:%s, userInfo:%s", orderId, userInfo));
                if (mRechargeView != null) {
                    mRechargeView.showToast(getString(R.string.alipay_pay_success_sync_order));
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(SUCCESS, PayWay.ZHIFUBAO), TIMES, "1");
                checkOrder(orderId, userInfo, null, null, true);
            }
        });
    }

    private void payByMiWallet(final String orderId, final int price, final String userInfo) {
        // 初始化小米钱包
        int miWalletLoginAccountType = PayCommonUtils.getMiWalletLoginAccountType();
        MyLog.w(TAG, "mi wallet login account type from pref:" + miWalletLoginAccountType);
        switch (miWalletLoginAccountType) {
            case PayCommonUtils.LOGIN_ACCOUNT_TYPE_SYSTEM:
                payByMiWallet0(orderId, price, userInfo, true);
                break;
            case PayCommonUtils.LOGIN_ACCOUNT_TYPE_OTHER:
                payByMiWallet0(orderId, price, userInfo, false);
                break;
            case PayCommonUtils.LOGIN_ACCOUNT_TYPE_NONE:
                // 检测是否在系统上登录了小米账号
                Account systemAccount = null;
                AccountManager am = null;
                am = AccountManager.get(GlobalData.app().getApplicationContext());

                Account[] accounts = am.getAccountsByType("com.xiaomi");
                if (accounts.length > 0) {
                    systemAccount = accounts[0];
                } else {
                    // 非MIUI手机或没有登录系统账号
                    // 为了防止MIUI用户在没有登录系统账号时使用过小米钱包，然后登录系统账号，清除数据，再使用小米钱包时弹出对话框，这里记录为没有选择使用系统账号
                    MyLog.w(TAG, "system mi account not detected");
                    PayCommonUtils.setMiWalletLoginAccountType(PayCommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                    payByMiWallet0(orderId, price, userInfo, false);
                    return;
                }
                final String name = systemAccount.name;
                // 这里只是检测系统账号是否可用，所以写成固定的值也是可以的
                String tokenType = "weblogin:" + "https://account.xiaomi.com/pass/serviceLogin?sid=cashpay-wap" +
                        "&callback=https://m.pay.xiaomi.com/sts?sign=e%2BWpFF%2Br3039Tt5%2BK1jF401f8Ug%3D" +
                        "&followup=https%3A%2F%2Fm.pay.xiaomi.com%2FpayFunc%3Fsafe%3Dc74da2a26aaf4b6b992e3f06488f9aa6%26" +
                        "outOrderId%3D20161474188675162725%26sellerId%3D10000112&bal=";
                am.getAuthToken(systemAccount, tokenType, null, mRechargeView.getActivity(), new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                            MyLog.d(TAG, "KEY_AUTHTOKEN:" + authToken);
                            if (authToken != null && !TextUtils.isEmpty(name) && TextUtils.isDigitsOnly(name)) {
                                // 提示用户检测到了系统账号，询问用户是否用系统账号支付
                                final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(mRechargeView.getActivity());
                                builder.setTitle(R.string.use_mi_account_login_title);
                                builder.setMessage(getString(R.string.use_mi_account_login_miwallet_msg, name));
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PayCommonUtils.setMiWalletLoginAccountType(PayCommonUtils.LOGIN_ACCOUNT_TYPE_SYSTEM);
                                        payByMiWallet0(orderId, price, userInfo, true);
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PayCommonUtils.setMiWalletLoginAccountType(PayCommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                                        payByMiWallet0(orderId, price, userInfo, false);
                                        dialog.dismiss();
                                    }
                                });
                                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        if (mRechargeView != null) {
                                            mRechargeView.showToast(getErrorMsgByErrorCode(TOAST_PAY_CANCEL));
                                        }
                                    }
                                });
                                int color = GlobalData.app().getResources().getColor(R.color.color_e5aa1e);
                                builder.setPositiveButtonTextColor(color);
                                builder.setAutoDismiss(false).show();
                            } else {
                                // 获取authToken为空
                                MyLog.e(TAG, "authToken:" + authToken + ", name:" + name);
                                PayCommonUtils.setMiWalletLoginAccountType(PayCommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                                payByMiWallet0(orderId, price, userInfo, false);
                            }
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            MyLog.e(TAG, "get authToken fail", e);
                            // 没有权限获取到authToken或其他问题
                            PayCommonUtils.setMiWalletLoginAccountType(PayCommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                            payByMiWallet0(orderId, price, userInfo, false);
                        }
                    }
                }, null);
                break;
            default:
                // 配置文件被人改了……
                MyLog.e(TAG, "unexpected mi wallet login account type:" + miWalletLoginAccountType);
                break;
        }// end of switch
    }

    private void payByMiWallet0(String orderId, int price, final String userInfo, boolean useSystemAccount) {
        FeePurchase feePurchase = new FeePurchase();
        feePurchase.setCpOrderId(orderId);
        feePurchase.setCpUserInfo(userInfo);
        feePurchase.setFeeValue(String.valueOf(price));//单位分
        HyUcashierPay hyUcashierPay = null;
        try {
            hyUcashierPay = HyUcashierPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init miwallet sdk fail, init here", e);
            HyUcashierPay.init(GlobalData.app(), String.valueOf(XiaoMiOAuth.APP_ID_PAY), XiaoMiOAuth.APP_KEY_PAY);
            hyUcashierPay = HyUcashierPay.getInstance();
        }
        hyUcashierPay.ucashierPay(mRechargeView.getActivity(), useSystemAccount, feePurchase, new PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s,errorcode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                if (mRechargeView != null) {
                    mRechargeView.showToast(getErrorMsgByErrorCode(errorCode));
                }
                switch (errorCode) {
                    case TOAST_PAY_CANCEL:
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.MIWALLET), TIMES, "1");
                        break;
                }

                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(PAY_ERROR_CODE, PayWay.MIWALLET, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("weixin pay ok, orderId:%s, userInfo:%s", orderId, userInfo));
                if (mRechargeView != null) {
                    mRechargeView.showToast(getString(R.string.miwallet_pay_success_sync_order));
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(SUCCESS, PayWay.MIWALLET), TIMES, "1");
                checkOrder(orderId, null, null, null, true);
            }
        });
    }

    private String payPalOrderId;

//    private void payByPayPal(String orderId, Diamond goods) {
//        payPalOrderId = orderId;
//        SkuDetail skuDetail = goods.getSkuDetail();
//        if (skuDetail == null) {
//            MyLog.e(TAG, "missing skuDetail when processing PayPal payment");
//            return;
//        }
//        PayPalPayment payment = new PayPalPayment(new BigDecimal(skuDetail.getPriceAmountMicros()).divide(SkuDetail.DIVISOR),
//                skuDetail.getPriceCurrencyCode(), skuDetail.getTitle(), PayPalPayment.PAYMENT_INTENT_SALE);
//        payment.invoiceNumber(orderId).custom(skuDetail.getProductId());
//
//        Intent intent = new Intent(mRechargeView.getActivity(), PaymentActivity.class);
//        // send the same configuration for restart resiliency
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, getPayPalConfig());
//        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
//        mRechargeView.getActivity().startActivityForResult(intent, PAYPAL_PAY_REQUEST_CODE);
//    }
//
//    //private static PayPalConfiguration payPalConfig;
//    private static PayPalConfiguration getPayPalConfig() {
//        PayPalConfiguration payPalConfig = new PayPalConfiguration();
//        if (!Constants.isTestBuild) {
//            payPalConfig.environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION).clientId(PayConstant.PayPal.PAYPAL_CLIENT_ID_PRODUCTION);
//        } else {
//            payPalConfig.environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PayConstant.PayPal.PAYPAL_CLIENT_ID_SANDBOX);
//        }
//        String languageCode4PayPal = PayCommonUtils.getLanguageCode4PayPal();
//        MyLog.w(TAG, "languageCode4PayPal:" + languageCode4PayPal);
//        payPalConfig.languageOrLocale(languageCode4PayPal);
//        payPalConfig.acceptCreditCards(false);
//        return payPalConfig;
//    }

//    public void handlePayPal(int resultCode, Intent data) {
//        MyLog.w(TAG, "handlePayPal, resultCode:" + resultCode);
//        if (resultCode == RESULT_OK) {
//            MyLog.w(TAG, "PayPal buy success, will handle data, orderId:" + payPalOrderId);
//            if (TextUtils.isEmpty(payPalOrderId)) {
//                MyLog.e(TAG, "orderId is empty");
//                return;
//            }
//            handlePayPalOk(data);
//            return;
//        }
//        // 失败情况
//        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                PayStatisticUtils.getRechargeTemplate(PAY_ERROR_CODE, PayWay.PAYPAL, resultCode), TIMES, "1");
//
//        switch (resultCode) {
//            case Activity.RESULT_CANCELED: {
//                MyLog.w(TAG, "The user canceled PayPal payment.");
//                mRechargeView.showToast(getString(R.string.pay_error_code_cancel));
//                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                        PayStatisticUtils.getRechargeTemplate(CANCEL, PayWay.PAYPAL), TIMES, "1");
//            }
//            break;
//            case PaymentActivity.RESULT_EXTRAS_INVALID: {
//                MyLog.w(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
//                mRechargeView.showToast(getString(R.string.pay_error_code_fail));
//            }
//            break;
//            default: {
//                MyLog.e(TAG, "resultCode:" + resultCode);
//                mRechargeView.showToast(getString(R.string.pay_error_code_fail));
//            }
//            break;
//        }
//    }
//
//    private void handlePayPalOk(Intent data) {
//        PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//        if (confirm != null) {
//            try {
//                JSONObject confirmJson = confirm.toJSONObject();
//                String confirmJsonString = confirmJson.toString();
//                MyLog.i(TAG, confirmJsonString);
//                JSONObject response = confirmJson.optJSONObject("response");
//                if (response == null) {
//                    MyLog.e(TAG, String.format("PayPal response data error, orderId:%s, comfirmJson:%s", payPalOrderId, confirmJsonString));
//                    mRechargeView.showToast(getString(R.string.paypal_response_data_error));
//                    return;
//                }
//                String id = response.optString("id");
//                if (TextUtils.isEmpty(id)) {
//                    MyLog.e(TAG, String.format("PayPal response data error, orderId:%s, comfirmJson:%s", payPalOrderId, confirmJsonString));
//                    mRechargeView.showToast(getString(R.string.paypal_response_data_error));
//                    return;
//                }
//                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                        PayStatisticUtils.getRechargeTemplate(SUCCESS, PayWay.PAYPAL), TIMES, "1");
//                checkOrder(payPalOrderId, null, null, id, true);
//            } catch (Exception e) {
//                MyLog.e(TAG, "an extremely unlikely failure occurred: ", e);
//                mRechargeView.showToast(getString(R.string.paypal_response_data_error));
//            }
//        } else {
//            MyLog.e(TAG, "PaymentConfirmation is null, orderId:" + payPalOrderId);
//            mRechargeView.showToast(getString(R.string.paypal_response_data_error));
//        }
//    }

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
//                            if (checkOrderResponse.getRetCode() == CODE_PAY_TICKET_ALREADY_USED && getCurrentPayWay() == PayWay.GOOGLEWALLET) {
//                                MyLog.w(TAG, "this order:"+ orderId + " has already added gem for google play, rsp:" + checkOrderResponse);
//                            } else {
                            return Observable.error(new Exception(getString(R.string.query_order_fail_return_code, checkOrderResponse.getRetCode())));
//                            }
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
//                        if (getCurrentPayWay() == PayWay.GOOGLEWALLET) {// 离GoogleWallet支付成功还差一步
//                            try {
//                                JSONObject receiptJson = new JSONObject(receipt);
//                                EventBus.getDefault().post(new EventClass.GooglePlayConsumeEvent(receiptJson));
//                            } catch (JSONException e) {
//                                MyLog.e(TAG, String.format("parse receipt to json fail, need to consider how to consume purchased products," +
//                                        " receipt:%s, msg:%s", receipt, e.getMessage()));
//                            }
//                        } else {
                        saveRechargedInfo();
//                        }
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
// onNext(BalanceDetail balance)

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
                            if (canShowErrorToast()) {
                                mRechargeView.showToast(msg);
                            }
                        }
                        clearPriceListCache();
                        //if (mRechargeView.isFirstRecharge()) {
                        //    mRechargeView.setExpandableListAdapterDataSourceAndNotify(getDiamondListCache());
                        //} else {
                        mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(getDiamondListCache());
                        //}
                        // 在这里调用pullPriceListAsync可能会造成死循环
                    }

                    @Override
                    public void onNext(List<Diamond> diamonds) {
                        //if (mRechargeView.isFirstRecharge()) {
                        //    mRechargeView.setExpandableListAdapterDataSourceAndNotify(diamonds);
                        //} else {
                        mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(diamonds);
                        //}
                        // 小红点去了
                        setHasReadRedPoint();
                    }
                });
    }

    //////////// EventBus Begin ////////////
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(EventClass.GooglePlayConsumeEvent event) {
        // 3、去GooglePlay消费掉这个商品
        consumeGooglePlayProduct(event.receipt);
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
        // 会通过EventBus发出UserInfoEvent
        MyUserInfoManager.getInstance().setDiamondNum(payPush.getUsableGemCnt());
    }

    /**
     * giftcard的push
     *
     * @param event
     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(EventClass.GiftCardPush event) {
//        if (event == null || event.obj1 == null) {
//            return;
//        }
//        PayProto.GiftCardPush giftCardPush = (PayProto.GiftCardPush) event.obj1;
//        MyLog.w(TAG, "giftCardPush:" + giftCardPush);
//        //MyUserInfoManager.getInstance().setDiamondNum(giftCardPush.getAndUsableGemCnt());
//        //MyUserInfoManager.getInstance().setVirtualDiamondNum(giftCardPush.getUsableVirtualGemCnt());
//        MyUserInfoManager.getInstance().setDiamonds(giftCardPush.getAndUsableGemCnt(), giftCardPush.getUsableVirtualGemCnt());
//    }
    //////////// EventBus End ////////////

}


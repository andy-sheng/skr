package com.wali.live.recharge.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.BaseActivity;
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
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.pay.constant.PayConstant;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.manager.PayManager;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.pay.model.Diamond;
import com.wali.live.pay.view.IRechargeView;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.config.RechargeConfig;
import com.wali.live.recharge.data.RechargeInfo;
import com.wali.live.recharge.net.CheckOrderRequest;
import com.wali.live.recharge.net.CreateOrderRequest;
import com.wali.live.recharge.net.GetGemPriceRequest;
import com.wali.live.recharge.payway.IPayWay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.mi.live.data.api.ErrorCode.CODE_SUCCESS;
import static com.wali.live.pay.constant.PayConstant.SP_FILENAME_RECHARGE_CONFIG;
import static com.wali.live.pay.constant.PayConstant.SP_KEY_IS_FIRST_RECHARGE;
import static com.wali.live.pay.constant.PayConstant.SP_KEY_LAST_PAY_WAY;

/**
 * 维护了充值时的一些状态<br>
 * 封装了充值需要的一些逻辑<br>
 * Created by rongzhisheng on 16-12-23.
 */
public class RechargePresenter extends RxLifeCyclePresenter implements IRechargePresenter {
    private static final String TAG = "RechargePresenter";

    private volatile IRechargeView mRechargeView;

    @MainThread
    public static RechargePresenter newInstance() {
        return new RechargePresenter();
    }

    private RechargePresenter() {
        EventBus.getDefault().register(this);
        initIsFirst();
    }

    @MainThread
    public void setView(@NonNull IRechargeView view) {
        mRechargeView = view;
        RechargeConfig.onEnterRecharge(mRechargeView.getActivity());
        isFirstRecharge = RechargeConfig.getPayWaysSize() == 1 ? false : isFirstRecharge;
        step = RechargeConfig.getPayWaysSize() == 1 || !isFirstRecharge ? PayConstant.RECHARGE_STEP_SECOND : PayConstant.RECHARGE_STEP_FIRST;
        setCurPayWay(RechargeConfig.getInitialPayWay());
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
        RechargeConfig.onExitRecharge(mRechargeView.getActivity());
        mRechargeView = null;
    }

    @MainThread
    @Override
    public void loadDataAndUpdateView() {
        if (mRechargeView == null) {
            return;
        }
        RechargeInfo.replaceGemCache(null);
        mRechargeView.setRecyclerViewLoadingStatusAndNotify();
        final PayWay curPayWay = getCurPayWay();
        final IPayWay payWayImpl = RechargeConfig.getPayWayImpl(curPayWay);
        Observable.just(null)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
                            return Observable.error(new RefuseRetryExeption(GlobalData.app().getString(R.string.network_unavailable).toString()));
                        }
                        return Observable.just(o);
                    }
                })
                .flatMap(new Func1<Object, Observable<PayProto.RChannel>>() {
                    @Override
                    public Observable<PayProto.RChannel> call(Object o) {
                        return Observable.just(curPayWay.getChannel());
                    }
                })
                .map(new Func1<PayProto.RChannel, PayProto.GetGemPriceResponse>() {
                    @Override
                    public PayProto.GetGemPriceResponse call(PayProto.RChannel channel) {
                        return new GetGemPriceRequest(channel).syncRsp();
                    }
                })
                .flatMap(new Func1<PayProto.GetGemPriceResponse, Observable<PayProto.GetGemPriceResponse>>() {
                    @Override
                    public Observable<PayProto.GetGemPriceResponse> call(PayProto.GetGemPriceResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new Throwable("GetGemPriceRsp is null"));
                        }
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new ExceptionWithCode(rsp.getRetCode()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .map(new Func1<PayProto.GetGemPriceResponse, PayProto.GetGemPriceResponse>() {
                    @Override
                    public PayProto.GetGemPriceResponse call(PayProto.GetGemPriceResponse rsp) {
                        RechargeInfo.setUsableGemCount(rsp.getUsableGemCnt());
                        RechargeInfo.setUsableVirtualGemCount(rsp.getUsableVirtualGemCnt());
                        RechargeInfo.setExchangeableGemCnt(rsp.getExchangeableGemCnt());
                        RechargeInfo.setWillExpireGemCnt(rsp.getExpireVirtualGemCnt());
                        RechargeInfo.setWillExpireGiftCardCnt(rsp.getExpireGiftCardCnt());
                        if (rsp.hasAmount()) {
                            PayProto.RechargeDayAmount amount = rsp.getAmount();
                            if (amount != null) {
                                RechargeInfo.setWeiXinTodayAmount(amount.getWxpayAmount());
                                RechargeInfo.setMiWalletTodayAmount(amount.getMiwalletAmount());
                            }
                        }
                        //if (!isNotPullRechargeList()) {
                        if (getStep() != PayConstant.RECHARGE_STEP_FIRST) {
                            if (payWayImpl != null) {
                                // 要在非UI线程parseGemPriceResponse
                                RechargeInfo.replaceGemCache(payWayImpl.parseGemPriceResponse(rsp));
                            } else {
                                MyLog.e(TAG, "unexpected pay way:" + curPayWay);
                            }
                        }
                        return rsp;
                    }
                })
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getStep() == PayConstant.RECHARGE_STEP_FIRST) {
                            mRechargeView.updateBalanceAreaData();
                        } else {
                            mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(RechargeInfo.getGemCache());
                        }
                        setHasReadRedPoint();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        if (getStep() == PayConstant.RECHARGE_STEP_FIRST) {
                            ToastUtils.showToast(R.string.no_net);
                        } else {
                            RechargeInfo.replaceGemCache(null);
                            if (mRechargeView != null) {
                                mRechargeView.setRecyclerViewAdapterDataSourceAndNotify(RechargeInfo.getGemCache());
                            }
                        }
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        MyLog.w(TAG, "GetGemPrice completed");
                    }
                });
    }


    /**
     * 充值
     *
     * @param goods
     * @param payWay
     */
    @MainThread
    public void recharge(final Diamond goods, @NonNull final PayWay payWay) {
        MyLog.w(TAG, "start buy diamond, payWay:" + payWay);
        Observable.just(0)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mRechargeView.showProcessDialog(5000, R.string.loading);
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
                            return Observable.error(new RefuseRetryExeption(GlobalData.app().
                                    getString(R.string.network_unavailable).toString()));
                        }
                        return Observable.just(o);
                    }
                })
                .map(new Func1<Object, PayProto.CreateOrderResponse>() {
                    @Override
                    public PayProto.CreateOrderResponse call(Object o) {
                        return new CreateOrderRequest(goods, payWay.getPayType(), payWay.getChannel()).syncRsp();
                    }
                })
                .flatMap(new Func1<PayProto.CreateOrderResponse, Observable<PayProto.CreateOrderResponse>>() {
                    @Override
                    public Observable<PayProto.CreateOrderResponse> call(PayProto.CreateOrderResponse rsp) {
                        if (rsp == null) {
                            return Observable.error(new RefuseRetryExeption(GlobalData.app().getString(R.string.create_order_fail_server_timeout).toString()));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            if (rsp.getRetCode() == PayManager.GOODS_NOT_EXIST) {
                                // TODO: 17-1-1 更新充值列表
                                return Observable.error(new Exception(GlobalData.app().getString(R.string.create_order_goods_not_exist).toString()));
                            }
                            return Observable.error(new RefuseRetryExeption(GlobalData.app().getString(R.string.create_order_fail_return_code, rsp.getRetCode()).toString()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if(o instanceof PayProto.CreateOrderResponse) {
                            PayProto.CreateOrderResponse response = (PayProto.CreateOrderResponse) o;
                            String orderId = response.getOrderId();
                            int price = goods.getPrice();
                            String cpUserInfo = response.getUserInfo();
                            MyLog.w(TAG, "create order success [orderId:" + orderId + ",price:" + price
                                    + ",count:" + goods.getCount() + ",payWay:" + payWay + "]");
                            final IPayWay payWayImpl = RechargeConfig.getPayWayImpl(payWay);
                            if (payWayImpl != null) {
                                payWayImpl.pay(mRechargeView.getActivity(), orderId, goods, cpUserInfo);
                            } else {
                                MyLog.e(TAG, "unexpected payWay:" + payWay);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "create order fail", throwable);
                        String msg = throwable.getMessage();
                        if (!TextUtils.isEmpty(msg)) {
                            ToastUtils.showToast(msg);
                        }
                        if (mRechargeView != null) {
                            mRechargeView.hideProcessDialog(1000);
                        }
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mRechargeView.hideProcessDialog(1000);
                    }
                });
    }

    /**
     * @param orderId
     * @param payId
     * @param receipt
     * @param transactionId
     * @param showTip       在{@link IPayWay#consumeHoldProduct()}方法里调用本方法时，通常会设置该参数为false
     *                      目前只在用GoogleWallet静默检查持有的未消耗商品的情况下设为false
     *                      以后如果有其他静默的情形，改为int类型
     */
    @AnyThread
    @Override
    public void checkOrder(@NonNull final PayWay payWay, final String orderId, final String payId, final String receipt, final String transactionId, final boolean showTip) {
        if (mRechargeView == null) {
            return;
        }
        MyLog.w(TAG, String.format("start check order, orderId:%s, payId:%s, receipt:%s", orderId, payId, receipt));
        getCheckOrderResponse(payWay, orderId, payId, receipt, transactionId)
                .retryWhen(new RxRetryAssist(2, 3, false))// 重试2次,每次3s
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if(o instanceof PayProto.CheckOrderResponse) {
                            PayProto.CheckOrderResponse rsp = (PayProto.CheckOrderResponse)o;
                            int retCode = rsp.getRetCode();
                            if (retCode == CODE_SUCCESS) {
                                int diamondBalance = rsp.getUsableGemCnt();
                                MyLog.w(TAG, "new diamondBalance:" + diamondBalance);
                                // 会发出UserInfoEvent
                                MyUserInfoManager.getInstance().setDiamondNum(diamondBalance);
                                if (showTip) {
                                    ToastUtils.showToast(R.string.recharge_success);
                                }
                            }

                            IPayWay payWayImpl = RechargeConfig.getPayWayImpl(payWay);
                            if (payWayImpl != null) {
                                if (payWay.canCheckOrder(retCode)) {
                                    if (!payWayImpl.postHandleAfterCheckOrder(receipt)) {
                                        saveUserRechargePreference();
                                    }
                                }
                            } else {
                                MyLog.e(TAG, "can not get " + payWay + " impl, receipt:" + receipt);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "add diamond fail", throwable);
                        if (showTip) {
                            ToastUtils.showToast(throwable.getMessage());
                        }
                        // 尝试从拉取个人基本信息的方式更新钻石
                        Observable.just(0)
                                .observeOn(Schedulers.io())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(Integer integer) {
                                        // 会发出UserInfoEvent
//                                        LiveSyncManager.getInstance().syncOwnUserInfo();
                                        MyUserInfoManager.getInstance().syncSelfDetailInfo();
                                    }
                                });
                    }
                }, new Action0() {
                    @Override
                    public void call() {

                    }
                });
    }

    public static Observable<PayProto.CheckOrderResponse> getCheckOrderResponse(@NonNull final PayWay payWay, final String orderId, final String payId, final String receipt, final String transactionId) {
        return Observable
                .just(null)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
                            return Observable.error(new RefuseRetryExeption(GlobalData.app().getString(R.string.network_unavailable).toString()));
                        }
                        return Observable.just(o);
                    }
                })
                .map(new Func1<Object, PayProto.CheckOrderResponse>() {
                    @Override
                    public PayProto.CheckOrderResponse call(Object o) {
                        return new CheckOrderRequest(orderId, payId, receipt, transactionId).syncRsp();
                    }
                })
                .flatMap(new Func1<PayProto.CheckOrderResponse, Observable<PayProto.CheckOrderResponse>>() {
                    @Override
                    public Observable<PayProto.CheckOrderResponse> call(PayProto.CheckOrderResponse checkOrderResponse) {
                        if (checkOrderResponse == null) {
                            return Observable.error(new Exception(GlobalData.app().getString(R.string.query_order_fail_server_timeout).toString()));
                        } else if (checkOrderResponse.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            MyLog.e(TAG, "check order retCode:" + checkOrderResponse.getRetCode());
                            if (payWay.canCheckOrder(checkOrderResponse.getRetCode())) {
                                return Observable.just(checkOrderResponse);
                            }
                            return Observable.error(new Exception(GlobalData.app().getString(R.string.query_order_fail_return_code, checkOrderResponse.getRetCode()).toString()));
                        }
                        return Observable.just(checkOrderResponse);
                    }
                });
    }

    @AnyThread
    public static void saveUserRechargePreference() {
        if (isFirstRecharge()) {
            Observable.just(false)
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                            MyLog.w(TAG, "saveTitle user recharged info ok");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MyLog.w(TAG, "saveTitle user recharged info fail");
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            if (isFirstRecharge()) {
                                PreferenceUtils.setSettingBoolean(
                                        GlobalData.app().getSharedPreferences(SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                                        SP_KEY_IS_FIRST_RECHARGE,
                                        false);
                                // 这里并不更新isFirstRecharge的值，等下次进入充值界面时更新
                            }
                        }
                    });
        }

        final PayWay payWay = getCurPayWay();
        Observable.just(payWay)
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<PayWay>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "saved user selected payWay:" + payWay);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "failed to saveTitle user selected payWay:" + payWay);
                    }

                    @Override
                    public void onNext(PayWay a) {
                        PreferenceUtils.setSettingString(GlobalData.app().getSharedPreferences(SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                                SP_KEY_LAST_PAY_WAY, payWay.name().toUpperCase());//TODO 注意这里的存储格式要和枚举的名字一致，因为以后要用valueOf恢复
                    }
                });

    }

    private static volatile PayWay curPayWay = PayWay.ZHIFUBAO;

    public static PayWay getCurPayWay() {
        return curPayWay;
    }

    public static void setCurPayWay(PayWay payWay) {
        curPayWay = payWay;
    }

    private static boolean isFirstRecharge = true;

    private synchronized static void initIsFirst() {
        isFirstRecharge = PreferenceUtils.getSettingBoolean(
                GlobalData.app().getSharedPreferences(SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                SP_KEY_IS_FIRST_RECHARGE,
                true);
        step = isFirstRecharge ? PayConstant.RECHARGE_STEP_FIRST : PayConstant.RECHARGE_STEP_SECOND;
    }

    public synchronized static boolean isFirstRecharge() {
        return isFirstRecharge;
    }

    private static int step = PayConstant.RECHARGE_STEP_FIRST;

    public synchronized static int getStep() {
        return step;
    }

    public synchronized static void setStep(int step) {
        RechargePresenter.step = step;
    }

    public synchronized static boolean isFirstStep() {
        return step == PayConstant.RECHARGE_STEP_FIRST;
    }

    public synchronized static void incrStep() {
        step++;
    }

    public synchronized static void decrStep() {
        step--;
    }

    ///**
    // * 首次充值的情况下，用户从step2返回step1时，只要让Adapter重新创建Item，不发起网络请求
    // */
    //private boolean mNotPullRechargeList = true;
    //
    ///**
    // * 只有非首次充值第一次进入step1时返回true
    // *
    // * @return
    // */
    //private boolean isNotPullRechargeList() {
    //    if (isFirstRecharge()) {
    //        if (mNotPullRechargeList) {
    //            mNotPullRechargeList = false;
    //            return true;
    //        }
    //    }
    //    return false;
    //}

    private void setHasReadRedPoint() {
        PayManager.setHasReadRedPoint();
    }

    /**
     * 礼物卡、虚拟钻变化的push
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.GiftCardPush event) {
        if (event == null || event.obj1 == null) {
            return;
        }
        PayProto.GiftCardPush giftCardPush = (PayProto.GiftCardPush) event.obj1;
        MyLog.w(TAG, "giftCardPush:" + giftCardPush);
        // 会通过EventBus发出UserInfoEvent
        MyUserInfoManager.getInstance().setDiamonds(giftCardPush.getAndUsableGemCnt(), giftCardPush.getUsableVirtualGemCnt());
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EventClass.RechargeCheckOrderEvent event) {
        MyLog.w(TAG, "received check order event");
        if (event == null || event.payWay == null) {
            return;
        }
        checkOrder(event.payWay, event.orderId, event.payId, event.receipt, event.transactionId, event.showTip);
    }


    private Subscription mGetBalanceDetailSub;

    @Override
    public void getBalance() {
        if (mGetBalanceDetailSub != null && !mGetBalanceDetailSub.isUnsubscribed()) {
            mGetBalanceDetailSub.unsubscribe();
        }
        if (null != mRechargeView) {
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
                    .compose(bindUntilEvent(PresenterEvent.DESTROY))
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            BalanceDetail balanceDetail = (BalanceDetail) o;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(BalanceFragment.BUNDLE_KEY_BALANCE_DETAIL, balanceDetail);
                            BalanceFragment.openFragment((BaseActivity) mRechargeView.getActivity(), bundle, new WeakReference<>((IRechargePresenter)RechargePresenter.this));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, throwable.getMessage());
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            MyLog.w(TAG, "get QueryBalanceDetailResponse success");
                        }
                    });
        }
    }

    @Override
    public void showPopup() {
        if (mRechargeView != null) {
            mRechargeView.showPopupWindow();
        }
    }
}

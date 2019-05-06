package com.module.playways.room.gift.presenter;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.module.playways.room.gift.GiftServerApi;
import com.module.playways.room.gift.event.UpdateCoinAndDiamondEvent;
import com.module.playways.room.gift.inter.IContinueSendView;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.scheduler.ContinueSendScheduler;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class BuyGiftPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "BuyGiftPresenter";
    public final static int ErrZSNotEnough = 8362101; //钻石余额不足，充值后就可以送礼啦
    public final static int ErrPresentObjLeave = 8362102; //送礼对象已离开，请重新选择
    public final static int ErrCoinNotEnough = 8362103; //金币余额不足，充值后就可以送礼啦
    public final static int ErrSystem = 104; //系统错误

    GiftServerApi mGiftServerApi;
    IContinueSendView mIContinueSendView;

    ContinueSendScheduler mContinueSendScheduler;

    ExecutorService mBuyGiftExecutor = Executors.newSingleThreadExecutor();

    Handler mHandler = new Handler(Looper.getMainLooper());

    public BuyGiftPresenter(IContinueSendView iContinueSendView) {
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi.class);
        mIContinueSendView = iContinueSendView;
        mContinueSendScheduler = new ContinueSendScheduler(3000);
        addToLifeCycle();
    }

    public void buyGift(BaseGift baseGift, long roomId, long userID) {
        MyLog.w(TAG, "buyGift" + " baseGift=" + baseGift + " roomId=" + roomId + " userID=" + userID);

        final int[] continueCount = new int[1];

        Observable.create(new ObservableOnSubscribe<RequestBody>() {
            @Override
            public void subscribe(ObservableEmitter<RequestBody> emitter) throws Exception {
                long ts = System.currentTimeMillis() / 1000;
                ContinueSendScheduler.BuyGiftParam buyGiftParam = null;
                if (baseGift.isCanContinue()) {
                    buyGiftParam = mContinueSendScheduler.sendParam(baseGift, userID);
                } else {
                    buyGiftParam = new ContinueSendScheduler.BuyGiftParam(System.currentTimeMillis(), 1);
                }
                continueCount[0] = buyGiftParam.getContinueCount();
                HashMap<String, Object> map = new HashMap<>();
                map.put("giftID", baseGift.getGiftID());
                map.put("continueCnt", buyGiftParam.getContinueCount());
                map.put("continueID", buyGiftParam.getContinueId());
                map.put("count", 1);
                map.put("receiveUserID", userID);
                map.put("roomID", roomId);
                map.put("timestamp", ts);

                HashMap<String, Object> signMap = new HashMap<>(map);
                signMap.put("appSecret", "64c5b47f618489dece9b2f95afb56654");
                map.put("signV2", U.getMD5Utils().signReq(signMap));

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                emitter.onNext(body);
                emitter.onComplete();
            }
        }).flatMap(new Function<RequestBody, ObservableSource<ApiResult>>() {
            @Override
            public ObservableSource<ApiResult> apply(RequestBody requestBody) throws Exception {
                return mGiftServerApi.buyGift(requestBody);
            }
        }).subscribeOn(Schedulers.from(mBuyGiftExecutor))
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<ApiResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResult result) {
                        MyLog.w(TAG, "buyGift process" + " result=" + result);
                        //{"coinBalance":207,"zuanBalance":14586340}
                        //还是在购买线程处理的

                        {
                            if (result.getErrno() == 0) {
                                {
                                    if (baseGift.isCanContinue()) {
                                        mContinueSendScheduler.sendGiftSuccess();
                                    }

                                    int coin = JSON.parseObject(result.getData().getString("coinBalance"), Integer.class);
                                    int diamond = JSON.parseObject(result.getData().getString("zuanBalance"), Integer.class);
                                    EventBus.getDefault().post(new UpdateCoinAndDiamondEvent(coin, diamond));
                                }
                            }
                        }

                        mHandler.post(() -> {
                            if (result.getErrno() == 0) {
                                mIContinueSendView.buySuccess(baseGift, continueCount[0]);
                            } else {
                                mIContinueSendView.buyFaild(result.getErrno(), result.getErrmsg());
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShort("购买礼物失败");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void endContinueSend() {
        mContinueSendScheduler.endContinueSend();
    }

    @Override
    public void destroy() {
        super.destroy();
        mBuyGiftExecutor.shutdown();
        mHandler.removeCallbacksAndMessages(null);
    }
}

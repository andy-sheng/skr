package com.module.playways.room.gift.presenter;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
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
import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.gift.scheduler.ContinueSendScheduler;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.msg.event.GiftPresentEvent;
import com.zq.live.proto.Common.GiftInfo;
import com.zq.live.proto.Room.GPrensentGiftMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

    public void buyGift(BaseGift baseGift, long roomId, UserInfoModel userInfoModel) {
        MyLog.w(TAG, "buyGift" + " giftId=" + baseGift.getGiftID() + " roomId=" + roomId + " userID=" + userInfoModel.getUserId());

        final int[] continueCount = new int[1];
        final long[] continueId = new long[1];

        Observable.create(new ObservableOnSubscribe<RequestBody>() {
            @Override
            public void subscribe(ObservableEmitter<RequestBody> emitter) throws Exception {
                long ts = System.currentTimeMillis() / 1000;
                ContinueSendScheduler.BuyGiftParam buyGiftParam = null;
                if (baseGift.isCanContinue()) {
                    buyGiftParam = mContinueSendScheduler.sendParam(baseGift, userInfoModel.getUserId());
                } else {
                    buyGiftParam = new ContinueSendScheduler.BuyGiftParam(System.currentTimeMillis(), 1);
                }

                long ctId = buyGiftParam.getContinueId();
                continueCount[0] = buyGiftParam.getContinueCount();
                continueId[0] = ctId;
                HashMap<String, Object> map = new HashMap<>();
                map.put("giftID", baseGift.getGiftID());
                map.put("continueCnt", buyGiftParam.getContinueCount());
                map.put("continueID", continueId[0]);
                map.put("count", 1);
                map.put("receiveUserID", userInfoModel.getUserId());
                map.put("roomID", roomId);
                map.put("timestamp", ts);

                HashMap<String, Object> signMap = new HashMap<>(map);
                signMap.put("appSecret", "64c5b47f618489dece9b2f95afb56654");
                map.put("signV2", U.getMD5Utils().signReq(signMap));

                MyLog.w(TAG, "buyGift param is "
                        + " giftID=" + baseGift.getGiftID()
                        + " continueCnt=" + continueCount[0]
                        + " continueID=" + buyGiftParam.getContinueId()
                        + " count=" + 1
                        + " receiveUserID=" + userInfoModel.getUserId()
                        + " roomID=" + roomId
                        + " timestamp=" + ts);

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                emitter.onNext(body);
                emitter.onComplete();
            }
        }).flatMap(new Function<RequestBody, ObservableSource<ApiResult>>() {
            @Override
            public ObservableSource<ApiResult> apply(RequestBody requestBody) throws Exception {
                return mGiftServerApi.buyGift(requestBody);
            }
        }).map(new Function<ApiResult, ApiResult>() {
            @Override
            public ApiResult apply(ApiResult result) throws Exception {
                //需要在购买礼物线程处理的问题
                if (result.getErrno() == 0) {
                    if (baseGift.isCanContinue()) {
                        mContinueSendScheduler.sendGiftSuccess();
                    }
                    int coin = result.getData().getIntValue("coinBalance");
                    int diamond = result.getData().getIntValue("zuanBalance");
                    EventBus.getDefault().post(new UpdateCoinAndDiamondEvent(coin, diamond));

                    UserInfoModel own = new UserInfoModel();
                    own.setUserId((int) MyUserInfoManager.getInstance().getUid());
                    own.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                    own.setNickname(MyUserInfoManager.getInstance().getNickName());
                    own.setSex(MyUserInfoManager.getInstance().getSex());

                    BasePushInfo basePushInfo = new BasePushInfo();
                    basePushInfo.setRoomID((int) roomId);

                    GPrensentGiftMsgModel gPrensentGiftMsgModel = new GPrensentGiftMsgModel();
                    gPrensentGiftMsgModel.setGiftInfo(baseGift);
                    gPrensentGiftMsgModel.setSendUserInfo(own);
                    gPrensentGiftMsgModel.setRoomID(roomId);
                    gPrensentGiftMsgModel.setCount(1);
                    gPrensentGiftMsgModel.setContinueID(continueId[0]);
                    gPrensentGiftMsgModel.setContinueCnt(continueCount[0]);
                    gPrensentGiftMsgModel.setReceiveUserInfo(userInfoModel);

                    EventBus.getDefault().post(new GiftPresentEvent(basePushInfo, gPrensentGiftMsgModel));
                }
                return result;
            }
        })
                .subscribeOn(Schedulers.from(mBuyGiftExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<ApiResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResult result) {
                        MyLog.w(TAG, "buyGift process" + " result=" + result);
                        if (result.getErrno() == 0) {
                            mIContinueSendView.buySuccess(baseGift, continueCount[0]);
                        } else {
                            mIContinueSendView.buyFaild(result.getErrno(), result.getErrmsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showShort("购买礼物失败");
                    }

                    @Override
                    public void onComplete() {
                        MyLog.d(TAG, "buyGift onComplete");
                    }
                });
    }

    @Override
    public void destroy() {
        super.destroy();
        mBuyGiftExecutor.shutdown();
        mHandler.removeCallbacksAndMessages(null);
    }
}

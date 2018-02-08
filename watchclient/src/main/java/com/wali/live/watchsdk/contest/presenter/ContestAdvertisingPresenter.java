package com.wali.live.watchsdk.contest.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.contest.model.AdvertisingItemInfo;
import com.wali.live.watchsdk.contest.request.AddRevivalCardActReq;
import com.wali.live.watchsdk.contest.request.GetRevivalActRequest;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wanglinzhang on 2018/1/31.
 */
public class ContestAdvertisingPresenter extends BaseRxPresenter<IContestAdvertisingView> {
    private Subscription mCetRevivalActSubscription;
    private Subscription mAddRevivalCardActSubscription;
    private List<AdvertisingItemInfo> mRevivalCardActInfo;

    public ContestAdvertisingPresenter(IContestAdvertisingView view) {
        super(view);
        mRevivalCardActInfo = new ArrayList<>();
    }

    public List<AdvertisingItemInfo> getRevivalCardActInfo() {
        return mRevivalCardActInfo;
    }

    public boolean hasCardAct() {
        return mRevivalCardActInfo.size() > 0;
    }

    public void addRevivalCardAct(final int type, final String contestId, final String pkgName) {
        if (mAddRevivalCardActSubscription != null && !mAddRevivalCardActSubscription.isUnsubscribed()) {
            return;
        }

        mAddRevivalCardActSubscription = Observable
                .create(new Observable.OnSubscribe<LiveSummitProto.AddRevivalCardActRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveSummitProto.AddRevivalCardActRsp> subscriber) {
                        LiveSummitProto.AddRevivalCardActRsp rsp = new AddRevivalCardActReq(type, contestId, pkgName).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("addRevivalAct rsp is null"));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<LiveSummitProto.AddRevivalCardActRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveSummitProto.AddRevivalCardActRsp>() {
                    @Override
                    public void call(LiveSummitProto.AddRevivalCardActRsp rsp) {
                        MyLog.w(TAG, "addRevivalAct onNext");
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            mView.addRevivalCardActFailed(rsp.getRetCode());
                        } else {
                            mView.addRevivalCardActSuccess(rsp.getRevivalNum());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "addRevivalAct onError=" + throwable.getMessage());
                    }
                });
    }

    public void getRevivalAct(final String contestID) {
        if (mCetRevivalActSubscription != null && !mCetRevivalActSubscription.isUnsubscribed()) {
            return;
        }

        mCetRevivalActSubscription = Observable
                .create(new Observable.OnSubscribe<LiveSummitProto.GetRevivalActivityRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveSummitProto.GetRevivalActivityRsp> subscriber) {
                        LiveSummitProto.GetRevivalActivityRsp rsp = new GetRevivalActRequest(contestID).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("getRevivalAct rsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("getRevivalAct retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<LiveSummitProto.GetRevivalActivityRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveSummitProto.GetRevivalActivityRsp>() {
                    @Override
                    public void call(LiveSummitProto.GetRevivalActivityRsp rsp) {
                        MyLog.w(TAG, "getRevivalAct onNext");
                        processActInfo(rsp);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.getRevivalActFailed();
                        MyLog.w(TAG, "getInviteCode onError=" + throwable.getMessage());
                    }
                });
    }

    private void processActInfo(LiveSummitProto.GetRevivalActivityRsp rsp) {
        if (rsp.hasRevivalCardAct()) {
            MyLog.w(TAG, "processActInfo:");
            List<LiveSummitProto.GameRevivalActInfo> gameList = rsp.getRevivalCardAct().getGameActList();
            for (int i = 0; i < gameList.size(); ++i) {
                LiveSummitProto.GameRevivalActInfo actInfo = gameList.get(i);
                //get adAct info
                AdvertisingItemInfo adItm = new AdvertisingItemInfo(actInfo);
                mRevivalCardActInfo.add(adItm);
                MyLog.w(TAG, adItm.toString());
            }
        }
        mView.getRevivalActSuccess();
    }
}

package com.wali.live.watchsdk.contest.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.request.GetContestInviteCodeRequest;
import com.wali.live.watchsdk.contest.request.SetContestInviteCodeRequest;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2018/1/12.
 */
public class ContestInvitePresenter extends BaseRxPresenter<IContestInviteView> {
    private Subscription mCetInviteCodeSubscription;
    private Subscription mSetInviteCodeSubscription;

    public ContestInvitePresenter(IContestInviteView view) {
        super(view);
    }

    public void getInviteCode() {
        if (!TextUtils.isEmpty(ContestGlobalCache.getRevivalCode())) {
            mView.getInviteCodeSuccess(ContestGlobalCache.getRevivalCode());
            return;
        }
        if (mCetInviteCodeSubscription != null && !mCetInviteCodeSubscription.isUnsubscribed()) {
            return;
        }
        mCetInviteCodeSubscription = Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        LiveSummitProto.GetContestInviteCodeRsp rsp = new GetContestInviteCodeRequest().syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("getInviteCode rsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("getInviteCode retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(rsp.getInviteCode());
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<String>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String code) {
                        MyLog.w(TAG, "getInviteCode onNext");
                        ContestGlobalCache.setRevivalCode(code);
                        mView.getInviteCodeSuccess(code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "getInviteCode onError=" + throwable.getMessage());
                    }
                });
    }

    public void setInviteCode(final String inviteCode) {
        if (mSetInviteCodeSubscription != null && !mSetInviteCodeSubscription.isUnsubscribed()) {
            return;
        }
        mSetInviteCodeSubscription = Observable
                .create(new Observable.OnSubscribe<LiveSummitProto.SetContestInviteCodeRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveSummitProto.SetContestInviteCodeRsp> subscriber) {
                        LiveSummitProto.SetContestInviteCodeRsp rsp = new SetContestInviteCodeRequest(inviteCode).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("setInviteCode rsp is null"));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<LiveSummitProto.SetContestInviteCodeRsp>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveSummitProto.SetContestInviteCodeRsp>() {
                    @Override
                    public void call(LiveSummitProto.SetContestInviteCodeRsp rsp) {
                        MyLog.w(TAG, "setInviteCode onNext");
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            mView.setInviteCodeFailure(rsp.getRetCode());
                        } else {
                            int revivalNum = rsp.getRevivalNum();
                            ContestGlobalCache.setRevivalNum(revivalNum);
                            mView.setInviteCodeSuccess(revivalNum);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "setInviteCode onError=" + throwable.getMessage());
                    }
                });
    }
}

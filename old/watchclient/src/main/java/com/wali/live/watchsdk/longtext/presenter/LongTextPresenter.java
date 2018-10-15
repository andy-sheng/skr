package com.wali.live.watchsdk.longtext.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.rx.RxRetryAssist;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.Feeds.GetFeedInfoResponse;
import com.wali.live.watchsdk.feeds.request.GetFeedsInfoRequest;
import com.wali.live.watchsdk.longtext.model.LongTextModel;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/9/19.
 */
public class LongTextPresenter extends BaseRxPresenter<ILongTextView> {
    private Subscription mSubscription;

    public LongTextPresenter(ILongTextView view) {
        super(view);
    }

    public void getFeedsInfo(String feedId, long ownerId) {
        getFeedsInfo(feedId, false, ownerId);
    }

    /**
     * 拉取一个feedsinfo
     *
     * @param isOnlyFocus true表示只拉取关注的人, false表示拉取全部
     */
    private void getFeedsInfo(final String feedId, final boolean isOnlyFocus, final long ownerId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable
                .create(new Observable.OnSubscribe<GetFeedInfoResponse>() {
                    @Override
                    public void call(Subscriber<? super GetFeedInfoResponse> subscriber) {
                        if (TextUtils.isEmpty(feedId)) {
                            subscriber.onError(new Throwable("getFeedsInfo feedId is empty"));
                            return;
                        }

                        GetFeedInfoResponse rsp = new GetFeedsInfoRequest(feedId, isOnlyFocus, ownerId).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Throwable("getFeedsInfo rsp is null"));
                            return;
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    }
                })
                .retryWhen(new RxRetryAssist(3, ""))
                .subscribeOn(Schedulers.io())
                .compose(mView.<GetFeedInfoResponse>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GetFeedInfoResponse>() {
                    @Override
                    public void call(GetFeedInfoResponse rsp) {
                        int code = rsp.getRet();
                        MyLog.d(TAG, "getFeedsInfo rsp code=" + code);
                        if (code == ErrorCode.CODE_SUCCESS) {
                            mView.getFeedInfoSuccess(new LongTextModel(rsp.getFeedInfo()));
                        } else if (code == ErrorCode.CODE_FEEDS_DELETED) {
                            mView.notifyFeedInfoDeleted();
                        } else {
                            mView.notifyFeedInfoFailure();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        mView.notifyFeedInfoFailure();
                    }
                });
    }
}

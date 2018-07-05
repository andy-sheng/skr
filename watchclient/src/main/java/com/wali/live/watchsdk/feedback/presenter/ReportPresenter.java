package com.wali.live.watchsdk.feedback.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.api.feedback.FeedBackApi;
import com.wali.live.watchsdk.feedback.contact.ReportContact;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-7-5.
 */

public class ReportPresenter extends RxLifeCyclePresenter implements ReportContact.IPresenter {
    private static final String TAG = "ReportPresenter";

    private ReportContact.IView mIview;
    private Subscription mSendReportSubscribe;

    public ReportPresenter(ReportContact.IView iView) {
        this.mIview = iView;
    }


    @Override
    public void sendReport(final long targetId
            , final int reportType
            , final String roomId
            , final String liveUrl
            , final String reprotPos
            , final String commentProof
            , final int contentType
            , final String otherReason) {

        mSendReportSubscribe = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                boolean b = FeedBackApi.sendReport(targetId, reportType, roomId, liveUrl, reprotPos, commentProof, contentType, otherReason);
                subscriber.onNext(b);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean res) {
                        MyLog.v(TAG, " sendReport res : " + res);
                        mIview.reportFeedBack(res);
                    }
                });
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mSendReportSubscribe != null && !mSendReportSubscribe.isUnsubscribed()) {
            mSendReportSubscribe.unsubscribe();
        }
    }
}

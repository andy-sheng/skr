package presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import model.ContestNoticeModel;
import request.GetContestNoticeRequest;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestPreparePresenter extends BaseRxPresenter<IContestPrepareView> {
    private Subscription mGetNoticeSubscription;
    private Subscription mIntervalUpdateSubscription;

    private long mZuid;

    public ContestPreparePresenter(IContestPrepareView view, long zuid) {
        super(view);
        mZuid = zuid;
    }

    public void getContestNotice() {
        MyLog.w(TAG, "getContestNotice");
        if (mGetNoticeSubscription != null && !mGetNoticeSubscription.isUnsubscribed()) {
            return;
        }
        mGetNoticeSubscription = Observable
                .create(new Observable.OnSubscribe<ContestNoticeModel>() {
                    @Override
                    public void call(Subscriber<? super ContestNoticeModel> subscriber) {
                        LiveSummitProto.GetContestNoticeRsp rsp = new GetContestNoticeRequest(mZuid).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("getContestNotice rsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("getContestNotice retCode = %d", rsp.getRetCode())));
                        } else {
                            ContestGlobalCache.setContestNotice(rsp.getNoticeInfo());
                            subscriber.onNext(new ContestNoticeModel(rsp.getNoticeInfo()));
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<ContestNoticeModel>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ContestNoticeModel>() {
                    @Override
                    public void call(ContestNoticeModel model) {
                        MyLog.w(TAG, "getContestNotice onNext");
                        mView.setContestNotice(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "getContestNotice onError=" + throwable.getMessage());
                    }
                });
    }

    public void startIntervalUpdate(long delayTime) {
        MyLog.w(TAG, "intervalUpdate delayTime=" + delayTime);
        if (mIntervalUpdateSubscription != null && !mIntervalUpdateSubscription.isUnsubscribed()) {
            return;
        }
        mIntervalUpdateSubscription = Observable.interval(0, 5, TimeUnit.SECONDS)
                .delay(delayTime, TimeUnit.MILLISECONDS)
                .take(1000)
                .compose(mView.<Long>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        MyLog.d(TAG, "intervalUpdate call");
                        getContestNotice();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "intervalUpdate failed", throwable);
                    }
                });
    }

    public void cancelIntervalUpdate() {
        MyLog.w(TAG, "cancelIntervalUpdate");
        if (mIntervalUpdateSubscription != null && !mIntervalUpdateSubscription.isUnsubscribed()) {
            mIntervalUpdateSubscription.unsubscribe();
        }
    }
}

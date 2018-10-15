package rank.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.LiveSummitProto;
import rank.model.ContestRankModel;
import rank.request.ContestAllRankRequest;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankPresenter extends BaseRxPresenter<IContestRankView> {
    private Subscription mGetRankSubscription;

    public ContestRankPresenter(IContestRankView view) {
        super(view);
    }

    public void getContestRank() {
        if (mGetRankSubscription != null && !mGetRankSubscription.isUnsubscribed()) {
            return;
        }
        mGetRankSubscription = Observable
                .create(new Observable.OnSubscribe<ContestRankModel>() {
                    @Override
                    public void call(Subscriber<? super ContestRankModel> subscriber) {
                        LiveSummitProto.GetContestAllRankRsp rsp = new ContestAllRankRequest().syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("getContestRank rsp is null"));
                        } else if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(String.format("getContestRank retCode = %d", rsp.getRetCode())));
                        } else {
                            subscriber.onNext(new ContestRankModel(rsp.getRankListList()));
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<ContestRankModel>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ContestRankModel>() {
                    @Override
                    public void call(ContestRankModel model) {
                        MyLog.w(TAG, "getContestRank onNext");
                        mView.setContestRank(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "getContestRank onError=" + throwable.getMessage());
                    }
                });
    }
}

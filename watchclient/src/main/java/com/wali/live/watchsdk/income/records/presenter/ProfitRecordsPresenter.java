package com.wali.live.watchsdk.income.records.presenter;

import com.base.presenter.Presenter;
import com.wali.live.watchsdk.income.records.model.ProfitMonthDetail;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhaomin on 17-6-29.
 */
public class ProfitRecordsPresenter implements Presenter {

    private IProfitRecordsView mIProfitRecordsView;
    private ProfitRecordsRepository mRepositroy;

    public ProfitRecordsPresenter(IProfitRecordsView mIProfitRecordsView, ProfitRecordsRepository mRepositroy) {
        this.mIProfitRecordsView = mIProfitRecordsView;
        this.mRepositroy = mRepositroy;
    }

    public void fetchRecords(final long uid, final int year, final int month) {

        Observable.create(new Observable.OnSubscribe<ProfitMonthDetail>() {
            @Override
            public void call(Subscriber<? super ProfitMonthDetail> subscriber) {

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(mIProfitRecordsView.<ProfitMonthDetail>bindUntilEvent())
                .subscribe(new Subscriber<ProfitMonthDetail>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mIProfitRecordsView.onProfitRecordGetFailed();
                    }

                    @Override
                    public void onNext(ProfitMonthDetail detail) {
                        mIProfitRecordsView.onProfitRecordsGetSuccess(detail);
                    }
                });
    }



    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

}

package com.wali.live.watchsdk.income.records.presenter;


import com.wali.live.watchsdk.income.records.model.ProfitMonthDetail;

import rx.Observable;

/**
 * Created by zhaomin on 17-6-29.
 */
public interface IProfitRecordsView {

    void onProfitRecordsGetSuccess(ProfitMonthDetail detail);

    void onProfitRecordGetFailed();

    <T> Observable.Transformer<T, T> bindUntilEvent();
}

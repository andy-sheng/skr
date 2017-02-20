package com.wali.live.common.watchhistory.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.repository.datasource.WatchHistoryInfoDaoAdapter;
import com.wali.live.common.watchhistory.view.IWatchHistoryView;
import com.wali.live.dao.WatchHistoryInfo;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2016/11/6.
 */
public class WatchHistoryInfoPresenter extends RxLifeCyclePresenter {
    private String TAG = "WatchHistoryInfoPresenter" + this.hashCode();

    public void recordWatchHistory(final WatchHistoryInfo watchHistoryInfo) {
        Observable.just(null)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        MyLog.d(TAG, "recordWatchHistory watchHistoryInfo=" + watchHistoryInfo.toString());
                        WatchHistoryInfoDaoAdapter.getInstance().insertOrReplaceWatchHistoryInfo(watchHistoryInfo);
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "recordWatchHistory failed, exception=" + throwable);
                    }
                });
    }

    public void getAllWatchHistory(final IWatchHistoryView watchHistoryView) {
        Observable.just(null)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        return WatchHistoryInfoDaoAdapter.getInstance().getWatchHistoryInfoList();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        watchHistoryView.notifyWatchHistoryView(o); //我的资料页刷新ui上的观看记录数
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getAllWatchHistory failed, exception=" + throwable);
                        watchHistoryView.notifyWatchHistoryView();
                    }
                });
    }

    //我的资料页
    public void getWatchHistoryCount(final IWatchHistoryView watchHistoryView) {
        Observable.just(null)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        long cnt = WatchHistoryInfoDaoAdapter.getInstance().getWatchHistoryInfoCount();
                        MyLog.w(TAG, "getWatchHistoryCount cnt =" + cnt);
                        return cnt;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        watchHistoryView.notifyWatchHistoryView(o); //我的资料页刷新ui上的观看记录数
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getWatchHistoryCount failed, exception=" + throwable);
                    }
                });
    }

    public void deleteAllWatchHistory(final IWatchHistoryView watchHistoryView) {
        Observable.just(null)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        WatchHistoryInfoDaoAdapter.getInstance().deleteAll();
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        List<WatchHistoryInfo> watchHistoryInfoList = new ArrayList<>();
                        watchHistoryView.notifyWatchHistoryView(watchHistoryInfoList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "deleteAllWatchHistory failed, exception=" + throwable);
                    }
                });
    }

    public void deleteRedundantWatchHistory() {
        Observable.just(null)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        WatchHistoryInfoDaoAdapter.getInstance().deleteRedundantWatchHistory();
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "deleteRedundantWatchHistory failed, exception=" + throwable);
                    }
                });
    }
}

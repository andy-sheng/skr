package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.request.CreateGroupRequest;
import com.wali.live.watchsdk.fans.request.GetGroupListRequest;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/7.
 */
public class FansGroupListPresenter extends BaseRxPresenter<IFansGroupListView> {
    private int mStart = 0;
    private boolean mHasMore = true;

    public FansGroupListPresenter(IFansGroupListView view) {
        super(view);
    }

    private void resetInnerStatus() {
        mStart = 0;
        mHasMore = true;
    }

    public void getFansGroupList(boolean isFirst) {
        // 如果首次拉取就重置状态，否则就从mStart处拉取
        if (isFirst) {
            resetInnerStatus();
        }
        // 如果已经没有更多，直接返回
        if (!mHasMore) {
            MyLog.d(TAG, "get fans group list no more");
            return;
        }
        Observable
                .create(new Observable.OnSubscribe<FansGroupListModel>() {
                    @Override
                    public void call(Subscriber<? super FansGroupListModel> subscriber) {
                        VFansProto.GetGroupListRsp rsp = new GetGroupListRequest(mStart).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("group list rsp is null"));
                            return;
                        }
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }
                        mStart = rsp.getNextStart();
                        mHasMore = rsp.getHasMore();

                        subscriber.onNext(new FansGroupListModel(rsp));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<FansGroupListModel>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupListModel>() {
                    @Override
                    public void call(FansGroupListModel model) {
                        MyLog.d(TAG, "get fans group success");
                        mView.setFansGroupList(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void createGroup(final String name) {
        Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        VFansProto.CreateGroupRsp rsp = new CreateGroupRequest(name).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("create group rsp is null"));
                            return;
                        }
                        subscriber.onNext(rsp.getErrCode() == ErrorCode.CODE_SUCCESS);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<Boolean>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        mView.notifyCreateGroupResult(result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}

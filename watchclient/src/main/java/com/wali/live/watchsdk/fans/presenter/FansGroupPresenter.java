package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.request.GetGroupListRequest;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/7.
 */
public class FansGroupPresenter extends BaseRxPresenter<IFansGroupView> {
    private int mStart = 0;
    private boolean mHasMore = true;

    public FansGroupPresenter(IFansGroupView view) {
        super(view);
    }

    public void getFansGroupList() {
        if (!mHasMore) {
            MyLog.d(TAG, "get fans group list no more");
            return;
        }
        Observable
                .create(new Observable.OnSubscribe<FansGroupListModel>() {
                    @Override
                    public void call(Subscriber<? super FansGroupListModel> subscriber) {
                        VFansProto.GetGroupListRsp rsp = new GetGroupListRequest(mStart).syncRsp();
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
                .compose(mView.<FansGroupListModel>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupListModel>() {
                    @Override
                    public void call(FansGroupListModel model) {
                        MyLog.d(TAG, "get fans group success");
                        mView.getFansGroupListSuccess(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}

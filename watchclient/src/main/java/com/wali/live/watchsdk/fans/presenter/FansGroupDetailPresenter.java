package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/9.
 */
public class FansGroupDetailPresenter extends BaseRxPresenter<IFansGroupDetailView> {
    public FansGroupDetailPresenter(IFansGroupDetailView view) {
        super(view);
    }

    public void getFansGroupDetail(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<FansGroupDetailModel>() {
                    @Override
                    public void call(Subscriber<? super FansGroupDetailModel> subscriber) {
                        VFansProto.GroupDetailRsp rsp = new GetGroupDetailRequest(zuid).syncRsp();
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }
                        subscriber.onNext(new FansGroupDetailModel(rsp));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .compose(mView.<FansGroupDetailModel>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupDetailModel>() {
                    @Override
                    public void call(FansGroupDetailModel model) {
                        mView.getFansGroupDetailSuccess(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}

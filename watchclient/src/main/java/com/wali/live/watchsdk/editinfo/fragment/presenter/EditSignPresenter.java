package com.wali.live.watchsdk.editinfo.fragment.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.UserProto.UploadUserPropertiesRsp;
import com.wali.live.watchsdk.editinfo.fragment.request.UploadInfoRequest;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/8/15.
 */
public class EditSignPresenter extends BaseRxPresenter<IEditSignView> {
    private Subscription mEditSubscription;

    public EditSignPresenter(IEditSignView view) {
        super(view);
    }

    public void uploadSign(final String sign) {
        if (mEditSubscription != null && !mEditSubscription.isUnsubscribed()) {
            mEditSubscription.unsubscribe();
        }
        mEditSubscription = Observable
                .create(new Observable.OnSubscribe<UploadUserPropertiesRsp>() {
                    @Override
                    public void call(Subscriber<? super UploadUserPropertiesRsp> subscriber) {
                        UploadUserPropertiesRsp rsp = new UploadInfoRequest().uploadSign(sign).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("UploadUserPropertiesRsp is null"));
                        } else {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<UploadUserPropertiesRsp>bindLifecycle())
                .subscribe(new Observer<UploadUserPropertiesRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "uploadSign onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "uploadSign onError=" + e.getMessage());
                    }

                    @Override
                    public void onNext(UploadUserPropertiesRsp rsp) {
                        MyLog.d(TAG, "uploadSign onNext");
                        if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mView.editSuccess(sign);
                        } else {
                            mView.editFailure(rsp.getRetCode());
                        }
                    }
                });
    }
}

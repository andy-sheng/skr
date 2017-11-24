package com.wali.live.watchsdk.fans.setting.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.setting.request.GetGroupMedalRequest;
import com.wali.live.watchsdk.fans.setting.request.SetGroupMedalRequest;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/6/17.
 */
public class FansMedalSettingPresenter extends BaseRxPresenter<IFansMedalSettingView> {
    private long mZuid;

    public FansMedalSettingPresenter(IFansMedalSettingView view, long zuid) {
        super(view);
        mZuid = zuid;
    }

    public void getGroupMedal() {
        Observable
                .create(new Observable.OnSubscribe<List<String>>() {
                    @Override
                    public void call(Subscriber<? super List<String>> subscriber) {
                        VFansProto.GetGroupMedalRsp rsp = new GetGroupMedalRequest(mZuid).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("get group medal rsp is null"));
                            return;
                        }
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }
                        subscriber.onNext(rsp.getMedalListList());
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<List<String>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> list) {
                        mView.setGroupMedal(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void setGroupMedal(final int level, final String medal) {
        Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        VFansProto.SetGroupMedalRsp rsp = new SetGroupMedalRequest(mZuid, level, medal).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("set group medal rsp is null"));
                            return;
                        }

                        subscriber.onNext(rsp.getErrCode() == ErrorCode.CODE_SUCCESS);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<Boolean>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.d(TAG, "setGroupMedal=" + result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });

    }
}

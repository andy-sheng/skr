package com.wali.live.watchsdk.fans.presenter.specific;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.request.ApplyJoinGroupRequest;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/6/18.
 */
public class ApplyJoinGroupPresenter extends BaseRxPresenter<IApplyJoinGroupView> {
    private long mZuid;
    private String mRoomId;

    private Subscription mSubscription;

    public ApplyJoinGroupPresenter(IApplyJoinGroupView view, long zuid, String roomId) {
        super(view);
        mZuid = zuid;
        mRoomId = roomId;
    }

    public void applyJoinGroup(final String applyMsg) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        VFansProto.ApplyJoinGroupRsp rsp = new ApplyJoinGroupRequest(mZuid, applyMsg, mRoomId).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("apply join rsp is null"));
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
                        if (result) {
                            ToastUtils.showToast(R.string.vfans_apply_group_success);
                        } else {
                            ToastUtils.showToast(R.string.vfans_apply_group_failed);
                        }
                        mView.setApplyJoinResult(result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}

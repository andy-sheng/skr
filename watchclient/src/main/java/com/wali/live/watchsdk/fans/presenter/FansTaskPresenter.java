package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.request.FinishJobRequest;
import com.wali.live.watchsdk.fans.request.GetGroupJobRequest;
import com.wali.live.watchsdk.fans.task.model.GroupJobListModel;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/13.
 */
public class FansTaskPresenter extends BaseRxPresenter<IFansTaskView> {
    public FansTaskPresenter(IFansTaskView view) {
        super(view);
    }

    public void getTaskList(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<GroupJobListModel>() {
                    @Override
                    public void call(Subscriber<? super GroupJobListModel> subscriber) {
                        VFansProto.GroupJobListRsp rsp = new GetGroupJobRequest(zuid).syncRsp();
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }

                        subscriber.onNext(new GroupJobListModel(rsp));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<GroupJobListModel>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GroupJobListModel>() {
                    @Override
                    public void call(GroupJobListModel model) {
                        MyLog.d(TAG, "get fans group success");
                        mView.setGroupTaskList(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void finishJob(final long zuid, final VFansCommonProto.GroupJobType jobType, final String roomId) {
        Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        VFansProto.GroupJobListRsp rsp = new FinishJobRequest(zuid, jobType, roomId).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("finish job rsp is null"));
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
                        if (result) {
                            mView.notifyFinishTaskSuccess();
                        } else {
                            ToastUtils.showToast(R.string.fans_task_get_reword_fail);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });
    }
}

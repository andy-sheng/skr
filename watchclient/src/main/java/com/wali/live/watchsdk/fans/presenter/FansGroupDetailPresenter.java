package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberListModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;
import com.wali.live.watchsdk.fans.request.QuitGroupRequest;

import java.util.List;

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
                        if (rsp == null) {
                            subscriber.onError(new Exception("get group detail rsp is null"));
                            return;
                        }
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
                        mView.setFansGroupDetail(model);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void getTopThreeMember(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<List<FansMemberModel>>() {
                    @Override
                    public void call(Subscriber<? super List<FansMemberModel>> subscriber) {
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(zuid, 0, 3).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("get top three rsp is null"));
                            return;
                        }
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }

                        FansMemberListModel model = new FansMemberListModel(rsp);
                        subscriber.onNext(model.getMemberList());
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<List<FansMemberModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FansMemberModel>>() {
                    @Override
                    public void call(List<FansMemberModel> list) {
                        mView.setTopThreeMember(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void quitFansGroup(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        VFansProto.QuitGroupRsp rsp = new QuitGroupRequest(zuid).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("quit group rsp is null"));
                            return;
                        }
                        subscriber.onNext(rsp.getErrCode() == ErrorCode.CODE_SUCCESS);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<Boolean>bindLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            mView.notifyQuitGroupSuccess();
                            ToastUtils.showToast(R.string.group_quiting_success);
                        } else {
                            ToastUtils.showToast(R.string.group_quiting_failed);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}

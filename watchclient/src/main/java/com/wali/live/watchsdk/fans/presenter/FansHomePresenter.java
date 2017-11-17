package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.member.FansMemberListModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/9.
 */
public class FansHomePresenter extends BaseRxPresenter<FansHomePresenter.IView> {
    public FansHomePresenter(IView view) {
        super(view);
    }

    public void getTopThreeMember(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<List<FansMemberModel>>() {
                    @Override
                    public void call(Subscriber<? super List<FansMemberModel>> subscriber) {
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(zuid, 0, 3).syncRsp();
                        if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
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

    public interface IView extends IRxView {
        void setTopThreeMember(List<FansMemberModel> list);
    }
}

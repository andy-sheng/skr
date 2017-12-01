package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.specific.RecentJobModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;
import com.wali.live.watchsdk.fans.request.GetRecentJobRequest;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/8.
 *
 * @module 粉丝团页面(FansPagerFragment)的presenter
 */
public class FansPagerPresenter extends BaseRxPresenter<FansPagerPresenter.IView> {
    public FansPagerPresenter(IView view) {
        super(view);
    }

    public void getGroupDetail(final long anchorId) {
        Observable.just(anchorId)
                .map(new Func1<Long, FansGroupDetailModel>() {
                    @Override
                    public FansGroupDetailModel call(Long anchorId) {
                        if (anchorId <= 0) {
                            MyLog.e(TAG, "getGroupDetail null anchorId = " + anchorId);
                            return null;
                        }
                        VFansProto.GroupDetailRsp rsp = new GetGroupDetailRequest(anchorId).syncRsp();
                        if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                            return new FansGroupDetailModel(rsp);
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<FansGroupDetailModel>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupDetailModel>() {
                    @Override
                    public void call(FansGroupDetailModel groupDetailModel) {
                        //TODO 这里需要加载每日任务 loadTask
                        if (groupDetailModel != null) {
                            mView.setGroupDetail(groupDetailModel);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void getRecentJob(final long zuid) {
        Observable
                .create(new Observable.OnSubscribe<List<RecentJobModel>>() {
                    @Override
                    public void call(Subscriber<? super List<RecentJobModel>> subscriber) {
                        VFansProto.GetRecentJobRsp rsp = new GetRecentJobRequest(zuid).syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("get recent job rsp is null"));
                            return;
                        }
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }

                        List<RecentJobModel> list = new ArrayList<>(rsp.getJobListCount());
                        for (VFansCommonProto.RecentJobInfo protoRecentJob : rsp.getJobListList()) {
                            list.add(new RecentJobModel(protoRecentJob));
                        }
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<List<RecentJobModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RecentJobModel>>() {
                    @Override
                    public void call(List<RecentJobModel> list) {
                        mView.setRecentJobList(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public interface IView extends IRxView {
        void setGroupDetail(FansGroupDetailModel groupDetailModel);

        void setRecentJobList(List<RecentJobModel> list);
    }
}

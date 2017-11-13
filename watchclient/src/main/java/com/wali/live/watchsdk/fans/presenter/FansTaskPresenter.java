package com.wali.live.watchsdk.fans.presenter;

import android.util.Pair;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.task.GroupJobModel;
import com.wali.live.watchsdk.fans.model.task.LimitGroupJobModel;
import com.wali.live.watchsdk.fans.request.GetGroupJobListRequest;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/13.
 *
 * @module 粉丝任务
 */

public class FansTaskPresenter extends BaseRxPresenter<FansTaskPresenter.IView> {
    public FansTaskPresenter(IView view) {
        super(view);
    }

    public void getTaskFromServer(long zuid) {
        Observable.just(zuid)
                .map(new Func1<Long, Pair>() {
                    @Override
                    public Pair call(Long zuid) {
                        VFansProto.GroupJobListRsp rsp = new GetGroupJobListRequest(zuid).syncRsp();
                        if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                            ArrayList<GroupJobModel> groupJobModels = new ArrayList<>();
                            for (VFansCommonProto.GroupJobInfo info : rsp.getJobListList()) {
                                groupJobModels.add(new GroupJobModel(info));
                            }
                            ArrayList<LimitGroupJobModel> limitGroupJobModels = new ArrayList<>();
                            for (VFansCommonProto.LimitedGroupJobInfo info : rsp.getLimitedJobListList()) {
                                limitGroupJobModels.add(new LimitGroupJobModel(info));
                            }
                            return new Pair<>(groupJobModels, limitGroupJobModels);
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
//                .compose(mView.<ArrayList<GroupJobModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair>() {
                    @Override
                    public void call(Pair pair) {
                        if (mView != null) {
                            mView.setDataList(pair);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getTaskFromServer failed=" + false);
                    }
                });
    }

    public interface IView extends IRxView {
        void setDataList(Pair pair);
    }
}

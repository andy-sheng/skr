package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.request.GetGroupDetailRequest;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
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

    public void getGroupDetailFromServer(final long anchorId) {
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
                        if (mView != null && groupDetailModel != null) {
                            mView.setGroupDetail(groupDetailModel);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getGroupDetailFromServer failed=" + throwable);
                    }
                });
    }

    public interface IView extends IRxView {
        void setGroupDetail(FansGroupDetailModel groupDetailModel);
    }
}

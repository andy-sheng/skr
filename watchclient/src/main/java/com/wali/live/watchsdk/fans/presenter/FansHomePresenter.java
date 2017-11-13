package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/9.
 */

public class FansHomePresenter extends BaseRxPresenter<FansHomePresenter.IView> {
    private FansGroupDetailModel mGroupDetailModel;

    @Override
    protected String getTAG() {
        return "FansHomePresenter";
    }

    public FansHomePresenter(IView view, FansGroupDetailModel groupDetailModel) {
        super(view);
        mGroupDetailModel = groupDetailModel;
    }

    public void getMemberListFromServer() {
        if (mGroupDetailModel == null) {
            MyLog.v(TAG, "getMemberListFromServer mGroupDetailModel is null");
            return;
        }
        MyLog.v(TAG, "getMemberListFromServer");
        Observable.just(0l)
                .map(new Func1<Long, List<FansMemberModel>>() {
                    @Override
                    public List<FansMemberModel> call(Long zuid) {
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(mGroupDetailModel.getZuid(), 0, 3,
                                VFansCommonProto.MemRankType.ORDER_BY_EXP,
                                VFansCommonProto.RankDateType.TOTAL_TYPE).syncRsp();
                        MyLog.w(TAG, "getMemberListFromServer rsp=" + rsp);
                        if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                            List<FansMemberModel> memberInfoList = new ArrayList<>(); //成员信息
                            for (VFansProto.MemberInfo member : rsp.getMemListList()) {
                                memberInfoList.add(new FansMemberModel(member));
                            }
                            return memberInfoList;
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
//                .compose(mView.<List<MemberInfoModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FansMemberModel>>() {
                    @Override
                    public void call(List<FansMemberModel> list) {
                        if (mView != null && list != null && list.size() > 0) {
                            mView.setTopThreeMember(list);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getMemberListFromServer failed=" + throwable);
                    }
                });
    }

    public interface IView extends IRxView {
        void setTopThreeMember(List<FansMemberModel> list);
    }
}

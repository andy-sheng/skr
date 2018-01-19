package com.wali.live.watchsdk.fans.rank.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.rank.model.RankListModel;
import com.wali.live.watchsdk.fans.request.GetGroupRankListRequest;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/29.
 */
public class FansRankPresenter extends BaseRxPresenter<IFansRankView> {
    private int mLoadStart = 0;
    private boolean mIsLoadingMore;
    private boolean mHasMoreData = true;
    private int mPageSize = 10;

    private VFansCommonProto.RankDateType mDateType;
    private long mZuid;
    private boolean mIsGroup = false;

    public FansRankPresenter(IFansRankView view, VFansCommonProto.RankDateType dataType, long zuid, boolean isGroup) {
        super(view);
        mDateType = dataType;
        mZuid = zuid;
        mIsGroup = isGroup;
    }

    public void reset() {
        mLoadStart = 0;
        mIsLoadingMore = false;
        mHasMoreData = true;
    }

    public void loadRankData() {
        if (mIsLoadingMore) {
            return;
        }
        if (!mHasMoreData) {
            return;
        }
        mIsLoadingMore = true;
        Observable
                .create(new Observable.OnSubscribe<RankListModel>() {
                    @Override
                    public void call(Subscriber<? super RankListModel> subscriber) {
                        if (!mIsGroup) {
                            VFansProto.MemberListRsp rsp = new GetMemberListRequest(mZuid, mLoadStart, mPageSize,
                                    VFansCommonProto.MemRankType.ORDER_BY_EXP, mDateType).syncRsp();
                            if (rsp == null) {
                                subscriber.onError(new Exception("get member list rsp is null"));
                                return;
                            }
                            if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                                subscriber.onError(new Exception("get member list error = " + rsp.getErrMsg() + " : " + rsp.getErrCode()));
                                return;
                            }
                            mLoadStart = rsp.getNextStart();
                            mHasMoreData = rsp.getHasMore();
                            subscriber.onNext(new RankListModel(rsp));
                            subscriber.onCompleted();
                        } else {
                            VFansProto.GroupRankListRsp rsp = new GetGroupRankListRequest(UserAccountManager.getInstance().getUuidAsLong(), mLoadStart, mPageSize,
                                    mDateType).syncRsp();
                            if (rsp == null) {
                                subscriber.onError(new Exception("get group rank list rsp is null"));
                                return;
                            }
                            if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                                subscriber.onError(new Exception("get group rank list error = " + rsp.getErrMsg() + " : " + rsp.getErrCode()));
                                return;
                            }
                            mLoadStart = rsp.getNextStart();
                            mHasMoreData = rsp.getHasMore();
                            subscriber.onNext(new RankListModel(rsp));
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<RankListModel>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RankListModel>() {
                    @Override
                    public void call(RankListModel rankListModel) {
                        mView.notifyGetRankListSuccess(rankListModel);
                        mIsLoadingMore = false;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        mIsLoadingMore = false;
                        mView.notifyGetRankListFailure();
                    }
                });
    }
}

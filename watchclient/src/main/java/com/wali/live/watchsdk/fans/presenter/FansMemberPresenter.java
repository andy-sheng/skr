package com.wali.live.watchsdk.fans.presenter;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.member.FansMemberListModel;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;
import com.wali.live.watchsdk.fans.view.FansMemberView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.proto.VFansCommonProto.MemRankType.ORDER_BY_MEMTYPE;
import static com.wali.live.proto.VFansCommonProto.RankDateType.TOTAL_TYPE;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员页表现
 */
public class FansMemberPresenter extends BaseSdkRxPresenter<FansMemberView.IView>
        implements FansMemberView.IPresenter {
    private static final String TAG = "FansMemberPresenter";

    private static final int PAGE_LIMIT = 10;

    private long mZuid;
    private int mLoadStart = 0;
    private boolean mIsLoadingMore;
    private boolean mHasMoreData = true;

    private Subscription mPullSubscription;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public FansMemberPresenter() {
        super(null);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        syncMemberData();
    }

    @Override
    public void syncMemberData() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        getMemberListFromServer();
    }

    private void getMemberListFromServer() {
        final long zuid = mZuid;
        final int start = mLoadStart;
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, FansMemberListModel>() {
                    @Override
                    public FansMemberListModel call(Integer integer) {
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(zuid, start,
                                PAGE_LIMIT, ORDER_BY_MEMTYPE, TOTAL_TYPE).syncRsp();
                        if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            throw new RuntimeException("GetMemberListRequest failed, errCode=" +
                                    (rsp != null ? rsp.getErrCode() : "null"));
                        }
                        return new FansMemberListModel(rsp);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<FansMemberListModel>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansMemberListModel>() {
                    @Override
                    public void call(FansMemberListModel model) {
                        if (mView == null) {
                            return;
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getMemberListFromServer failed, throwable=" + throwable);
                    }
                });
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}

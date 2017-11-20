package com.wali.live.watchsdk.fans.presenter;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.VFansProto;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.member.FansMemberListModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
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
import static com.wali.live.utils.relation.RelationUtils.FOLLOW_STATE_BLACK;
import static com.wali.live.utils.relation.RelationUtils.FOLLOW_STATE_BOTH_WAY;
import static com.wali.live.utils.relation.RelationUtils.FOLLOW_STATE_SUCCESS;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员页表现
 */
public class FansMemberPresenter extends BaseSdkRxPresenter<FansMemberView.IView>
        implements FansMemberView.IPresenter {

    private static final int PAGE_LIMIT = 10;

    private long mAnchorId;
    private int mLoadStart = 0;
    private boolean mHasMoreData = true;

    private Subscription mPullSubscription;

    @Override
    protected final String getTAG() {
        return "FansMemberPresenter";
    }

    public FansMemberPresenter(long anchorId) {
        super(null);
        mAnchorId = anchorId;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        syncMemberData();
    }

    @Override
    public final void syncMemberData() {
        getMemberListFromServer();
    }

    @Override
    public final void pullMore() {
        getMemberListFromServer();
    }

    @Override
    public void showPersonalInfo(long userId) {
        // TODO-YangLi 添加跳转到个人信息页
    }

    @Override
    public void fellowUser(final FansMemberModel item) {
        final long targetUid = item.getUuid();
        MyLog.w(TAG, "followUser targetUid=" + targetUid);
        Observable.just(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return RelationUtils.follow(UserAccountManager.getInstance().getUuidAsLong(), targetUid);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        if (mView == null) {
                            return;
                        }
                        if (result >= FOLLOW_STATE_SUCCESS) {
                            item.setIsFollow(true);
                            item.setIsBothWay(result == FOLLOW_STATE_BOTH_WAY);
                            mView.onFellowDone(item);
                        } else if (result == FOLLOW_STATE_BLACK) {
                            ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(
                                    R.string.setting_black_follow_hint));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "followUser failed, exception=" + throwable);
                    }
                });
    }

    private void getMemberListFromServer() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        mView.onLoadingStarted();
        if (!mHasMoreData) {
            mView.onLoadingDone(false);
            return;
        }
        final long zuid = mAnchorId;
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
                .compose(this.<FansMemberListModel>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansMemberListModel>() {
                    @Override
                    public void call(FansMemberListModel model) {
                        if (mView == null) {
                            return;
                        }
                        mLoadStart = model.getNextStart();
                        mHasMoreData = model.isHasMoreData();
                        mView.onNewDataSet(model.getMemberList());
                        mView.onLoadingDone(mHasMoreData);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getMemberListFromServer failed, throwable=" + throwable);
                        if (mView == null) {
                            return;
                        }
                        mView.onLoadingFailed();
                    }
                });
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}

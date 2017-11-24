package com.wali.live.watchsdk.fans.presenter;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.VFansProto;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter.MemberItem;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;
import com.wali.live.watchsdk.fans.view.FansMemberView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        syncMemberData();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, userId, null);
    }

    @Override
    public void fellowUser(final MemberItem item) {
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
                .map(new Func1<Integer, Object[]>() {
                    @Override
                    public Object[] call(Integer integer) {
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(zuid, start,
                                PAGE_LIMIT, ORDER_BY_MEMTYPE, TOTAL_TYPE).syncRsp();
                        if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            throw new RuntimeException("GetMemberListRequest failed, errCode=" +
                                    (rsp != null ? rsp.getErrCode() : "null"));
                        }

                        final Object[] result = new Object[3];
                        result[0] = Integer.valueOf(rsp.getNextStart());
                        result[1] = Boolean.valueOf(rsp.getHasMore());
                        final int cnt = rsp.getMemListCount();
                        if (cnt > 0) {
                            final ArrayList<MemberItem> memberItems = new ArrayList<>(cnt);
                            for (VFansProto.MemberInfo memProto : rsp.getMemListList()) {
                                memberItems.add(new MemberItem(memProto));
                            }
                            result[2] = memberItems;
                        }
                        return result;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<Object[]>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object[]>() {
                    @Override
                    public void call(Object[] result) {
                        if (mView == null) {
                            return;
                        }
                        mLoadStart = (int) result[0];
                        mHasMoreData = (boolean) result[1];
                        mView.onNewDataSet((List<MemberItem>) result[2]);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.UpdateMemberListEvent event) {
        if (event == null) {
            return;
        }
        mLoadStart = event.loadingStart;
        mView.onUpdateDataSet(event.memberItems);
    }
}

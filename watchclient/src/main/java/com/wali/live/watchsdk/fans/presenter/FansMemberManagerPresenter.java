package com.wali.live.watchsdk.fans.presenter;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansMemberManagerAdapter.MemberItem;
import com.wali.live.watchsdk.fans.request.GetMemberListRequest;
import com.wali.live.watchsdk.fans.request.UpdateMemberRequest;
import com.wali.live.watchsdk.fans.view.FansMemberManagerView;

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

/**
 * Created by yangli on 2017/11/16.
 *
 * @module 粉丝团成员管理页表现
 */
public class FansMemberManagerPresenter extends BaseSdkRxPresenter<FansMemberManagerView.IView>
        implements FansMemberManagerView.IPresenter {

    private static final int PAGE_LIMIT = 10;

    private final ArrayList<MemberItem> mDataSet = new ArrayList<>();

    private long mAnchorId;
    private volatile boolean mHasMoreData = true;

    private Subscription mPullSubscription;

    @Override
    protected final String getTAG() {
        return "FansMemberManagerPresenter";
    }

    public FansMemberManagerPresenter(long anchorId) {
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
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, List<MemberItem>>() {
                    @Override
                    public List<MemberItem> call(Integer integer) {
                        final int start = mDataSet.size();
                        VFansProto.MemberListRsp rsp = new GetMemberListRequest(zuid, start,
                                PAGE_LIMIT, ORDER_BY_MEMTYPE, TOTAL_TYPE).syncRsp();
                        if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            throw new RuntimeException("GetMemberListRequest failed, errCode=" +
                                    (rsp != null ? rsp.getErrCode() : "null"));
                        }
                        synchronized (mDataSet) {
                            mHasMoreData = rsp.getHasMore();
                            final int cnt = rsp.getMemListCount();
                            if (cnt > 0) {
                                mDataSet.ensureCapacity(mDataSet.size() + cnt);
                                for (VFansProto.MemberInfo memProto : rsp.getMemListList()) {
                                    mDataSet.add(new MemberItem(memProto));
                                }
                                return (List<MemberItem>) mDataSet.clone();
                            }
                            return null;
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<MemberItem>>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MemberItem>>() {
                    @Override
                    public void call(List<MemberItem> result) {
                        if (mView == null) {
                            return;
                        }
                        mView.onNewDataSet(result);
                        mView.onLoadingDone(mHasMoreData);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        if (mView == null) {
                            return;
                        }
                        mView.onLoadingFailed();
                    }
                });
    }

    private Observable updateManager(final MemberItem memberItem, final int updateType, final int memType) {
        final long zuid = mAnchorId;
        final long targetId = memberItem.getUuid();
        return Observable.just(0)
                .map(new Func1<Integer, Pair<Integer, List<MemberItem>>>() {
                    @Override
                    public Pair<Integer, List<MemberItem>> call(Integer integer) {
                        VFansProto.UpdateGroupMemRsp rsp = new UpdateMemberRequest(zuid, targetId,
                                updateType, memType).syncRsp();
                        if (rsp == null) {
                            throw new RuntimeException("updateManager failed, rsp is null");
                        }
                        final int errCode = rsp.getErrCode();
                        if (errCode == ErrorCode.CODE_SUCCESS) {
                            synchronized (mDataSet) {
                                if (mDataSet.remove(memberItem)) { // 确保设置管理期间 该成员未删除
                                    int i = 0;
                                    for (MemberItem elem : mDataSet) {
                                        if (memType <= elem.getMemType()) {
                                            break;
                                        }
                                        ++i;
                                    }
                                    memberItem.setMemType(memType);
                                    mDataSet.add(i, memberItem);
                                    return Pair.create(errCode, (List<MemberItem>) mDataSet.clone());
                                }
                            }
                        }
                        return Pair.create(errCode, null);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<Pair<Integer, List<MemberItem>>>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void setManager(final MemberItem memberItem, final boolean state) {
        final int updateType = state ? VFansProto.UpdateGroupMemType.SET_ADMIN_VALUE :
                VFansProto.UpdateGroupMemType.CANCEL_ADMIN_VALUE;
        final int memType = state ? VFansCommonProto.GroupMemType.ADMIN_VALUE :
                VFansCommonProto.GroupMemType.MASS_VALUE;
        updateManager(memberItem, updateType, memType)
                .subscribe(new Action1<Pair<Integer, List<MemberItem>>>() {
                    @Override
                    public void call(Pair<Integer, List<MemberItem>> result) {
                        if (mView == null) {
                            return;
                        }
                        final int errCode = result.first;
                        if (errCode == ErrorCode.CODE_SUCCESS) {
                            mView.onNewDataSet(result.second);
                        } else if (errCode == ErrorCode.CODE_VFANS_NO_GROUP_PRIVILEGE) {
                            ToastUtils.showToast(R.string.vfans_oprate_no_permisson);
                        } else if (errCode == ErrorCode.CODE_VFANS_ADMIN_REACH_LIMIT) {
                            ToastUtils.showToast(R.string.vfans_add_manager_faild_has_enough);
                        } else {
                            ToastUtils.showToast(state ? R.string.vfans_add_manager_faild :
                                    R.string.vfans_cancel_manager_faild);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    @Override
    public void setDeputyManager(MemberItem memberItem, final boolean state) {
        final int updateType = state ? VFansProto.UpdateGroupMemType.SET_ADMIN_VALUE :
                VFansProto.UpdateGroupMemType.CANCEL_ADMIN_VALUE;
        final int memType = state ? VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE :
                VFansCommonProto.GroupMemType.MASS_VALUE;
        updateManager(memberItem, updateType, memType)
                .subscribe(new Action1<Pair<Integer, List<MemberItem>>>() {
                    @Override
                    public void call(Pair<Integer, List<MemberItem>> result) {
                        if (mView == null) {
                            return;
                        }
                        final int errCode = result.first;
                        if (errCode == ErrorCode.CODE_SUCCESS) {
                            mView.onNewDataSet(result.second);
                        } else if (errCode == ErrorCode.CODE_VFANS_NO_GROUP_PRIVILEGE) {
                            ToastUtils.showToast(R.string.vfans_oprate_no_permisson);
                        } else if (errCode == ErrorCode.CODE_VFANS_DEPUTY_ADMIN_REACH_LIMIT) {
                            ToastUtils.showToast(R.string.vfans_add_deputy_manager_faild_has_enough);
                        } else {
                            ToastUtils.showToast(state ? R.string.vfans_add_deputy_manager_faild :
                                    R.string.vfans_cancel_deputy_manager_faild);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    @Override
    public void removeMember(@NonNull final List<MemberItem> memberItems) {
        final long zuid = mAnchorId;
        final int updateType = VFansProto.UpdateGroupMemType.REMOVE_MEMBER_VALUE;
        final int memType = VFansCommonProto.GroupMemType.MASS_VALUE;
        Observable.just(0)
                .map(new Func1<Integer, Pair<List<MemberItem>, List<MemberItem>>>() {
                    @Override
                    public Pair<List<MemberItem>, List<MemberItem>> call(Integer i) {
                        ArrayList<MemberItem> deletedSet = new ArrayList<>(memberItems.size());
                        for (MemberItem item : memberItems) {
                            final long targetId = item.getUuid();
                            VFansProto.UpdateGroupMemRsp rsp = new UpdateMemberRequest(zuid, targetId,
                                    updateType, memType).syncRsp();
                            if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                                continue;
                            }
                            deletedSet.add(item);
                        }
                        if (!deletedSet.isEmpty()) {
                            synchronized (mDataSet) {
                                mDataSet.removeAll(deletedSet);
                                return Pair.create((List<MemberItem>) mDataSet.clone(), (List<MemberItem>) deletedSet);
                            }
                        }
                        return Pair.create(null, null);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<Pair<List<MemberItem>, List<MemberItem>>>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<List<MemberItem>, List<MemberItem>>>() {
                    @Override
                    public void call(Pair<List<MemberItem>, List<MemberItem>> result) {
                        if (mView == null) {
                            return;
                        }
                        if (result.first == null || result.first.isEmpty()) {
                            ToastUtils.showToast(R.string.vfans_kick_faild);
                        } else {
                            ToastUtils.showToast(R.string.vfans_kick_success);
                            mView.onNewDataSet(result.first);
                            mView.onRemoveDone(result.second);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    @Override
    public final boolean onEvent(int event, IParams params) {
        return false;
    }

    public static class MemberPullerHelper {
    }
}

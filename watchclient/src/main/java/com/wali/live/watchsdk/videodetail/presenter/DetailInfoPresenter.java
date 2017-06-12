package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.event.BlockOrUnblockEvent;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.Feeds;
import com.wali.live.proto.LiveShowProto;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feeds.FeedsInfoUtils;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.view.DetailInfoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.mi.live.data.event.FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW;
import static com.wali.live.component.ComponentController.MSG_NEW_DETAIL_REPLAY;
import static com.wali.live.component.ComponentController.MSG_SHOW_PERSONAL_INFO;
import static com.wali.live.component.ComponentController.MSG_UPDATE_LIKE_STATUS;

/**
 * Created by yangli on 2017/06/01.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情信息表现
 */
public class DetailInfoPresenter extends ComponentPresenter<DetailInfoView.IView>
        implements DetailInfoView.IPresenter {
    private static final String TAG = "DetailInfoPresenter";

    private RoomBaseDataModel mMyRoomData;

    private Subscription mFeedsSubscription;

    public DetailInfoPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
        EventBus.getDefault().register(this);
        registerAction(MSG_NEW_DETAIL_REPLAY);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BlockOrUnblockEvent event) {
        if (mView != null && event != null && mMyRoomData.getUid() == event.uuid) {
            mView.onUnFollowed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (mView != null && event != null && mMyRoomData.getUid() == event.uuid) {
            if (event.eventType == EVENT_TYPE_FOLLOW) {
                mView.onFollowed(event.isBothFollow);
            } else {
                mView.onUnFollowed();
            }
        }
    }

    @Override
    public void syncUserInfo() {
        final long userId = mMyRoomData.getUid();
        MyLog.w(TAG, "syncUserInfo userId=" + userId);
        Observable.just(userId)
                .map(new Func1<Long, User>() {
                    @Override
                    public User call(Long userId) {
                        return UserInfoManager.getUserInfoByUuid(userId, true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<User>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (mView != null) {
                            mView.onUserInfo(user);
                        }
                        if (user != null) {
                            if (TextUtils.isEmpty(mMyRoomData.getNickName())) {
                                mMyRoomData.setNickname(user.getNickname());
                            }
                            mComponentController.onEvent(VideoDetailController.MSG_COMPLETE_USER_INFO);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncUserInfo failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void syncFeedsInfo() {
        if (mFeedsSubscription != null && !mFeedsSubscription.isUnsubscribed()) {
            mFeedsSubscription.unsubscribe();
        }
        final String feedId = mMyRoomData.getRoomId();
        final long ownerId = mMyRoomData.getUid();
        MyLog.w(TAG, "syncFeedsInfo feedId=" + feedId + ", ownerId=" + ownerId);
        mFeedsSubscription = Observable.just(0)
                .map(new Func1<Integer, FeedsInfo>() {
                    @Override
                    public FeedsInfo call(Integer integer) {
                        Feeds.GetFeedInfoResponse rsp = FeedsInfoUtils.fetchFeedsInfo(feedId, ownerId, false);
                        if (rsp == null || rsp.getRet() != ErrorCode.CODE_SUCCESS) {
                            return null;
                        }
                        FeedsInfo outInfo = new FeedsInfo();
                        try {
                            Feeds.FeedInfo feedInfo = rsp.getFeedInfo();
                            outInfo.timestamp = feedInfo.getFeedCteateTime();
                            outInfo.mySelfLike = feedInfo.getFeedLikeContent().getMyselfLike();
                            LiveShowProto.BackInfo backInfo = feedInfo.getFeedContent().getBackInfo();
                            outInfo.title = backInfo.getBaTitle();
                            outInfo.viewerCnt = backInfo.getViewerCnt();
                            outInfo.coverUrl = backInfo.getCoverUrl();
                        } catch (Exception e) {
                            MyLog.e(TAG, "syncFeedsInfo failed, exception=" + e);
                        }
                        return outInfo;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<FeedsInfo>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<FeedsInfo>() {
                    @Override
                    public void call(FeedsInfo outInfo) {
                        if (mView != null && outInfo != null) {
                            mComponentController.onEvent(MSG_UPDATE_LIKE_STATUS, new Params()
                                    .putItem(outInfo.mySelfLike));
                            mView.onFeedsInfo(mMyRoomData.getUid(), outInfo.title, outInfo.timestamp,
                                    outInfo.viewerCnt, outInfo.coverUrl);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncFeedsInfo failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void showPersonalInfo() {
        long uid = mMyRoomData.getUid();
        if (uid > 0) {
            mComponentController.onEvent(MSG_SHOW_PERSONAL_INFO, new Params().putItem(uid));
        }
    }

    @Override
    public void followUser() {
        final long targetUid = mMyRoomData.getUid();
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
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        if (mView != null && result == RelationUtils.FOLLOW_STATE_BLACK) {
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

    private void onNewVideo() {
        syncFeedsInfo();
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case MSG_NEW_DETAIL_REPLAY:
                    onNewVideo();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public static class FeedsInfo {
        String title = "";
        boolean mySelfLike = false;
        long timestamp = 0;
        int viewerCnt = 0;
        String coverUrl = "";
    }
}

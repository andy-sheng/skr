package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
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
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
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
import static com.wali.live.component.BaseSdkController.MSG_NEW_DETAIL_REPLAY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_FEEDS_DETAIL;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_PERSONAL_INFO;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_LIKE_STATUS;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_START_TIME;

/**
 * Created by yangli on 2017/06/01.
 *
 * @module 详情信息表现
 */
public class DetailInfoPresenter extends BaseSdkRxPresenter<DetailInfoView.IView>
        implements DetailInfoView.IPresenter {
    private static final String TAG = "DetailInfoPresenter";

    private RoomBaseDataModel mMyRoomData;

    private Subscription mFeedsSubscription;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public DetailInfoPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel roomData) {
        super(controller);
        mMyRoomData = roomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_NEW_DETAIL_REPLAY);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        syncUserInfo();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
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
                            mMyRoomData.getUser().setIsFocused(user.isFocused());
                            mMyRoomData.getUser().setAvatar(user.getAvatar());
                            mMyRoomData.setTicket(user.getLiveTicketNum());
                            mMyRoomData.getUser().setSign(user.getSign());
                            postEvent(VideoDetailController.MSG_COMPLETE_USER_INFO);
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
                            //feedinfo 的type 0 ：正常feed 1：推荐列表
                            Feeds.FeedInfo feedInfo = rsp.getFeedInfo();
                            outInfo.timestamp = feedInfo.getFeedCteateTime();
                            outInfo.mySelfLike = feedInfo.getFeedLikeContent().getMyselfLike();
                            //feedContent的feedType://Feed的Type类型.0 直播，1 图片，2. 小视频， 3 回放视频,
                            // 4, 直播结束，等待回放, 5,聚合回放, 6 作品
                            Feeds.FeedContent content = feedInfo.getFeedContent();
                            switch (content.getFeedType()) {
                                case FeedsInfo.TYPE_LIVE:
                                    LiveShowProto.LiveShow liveShow = content.getLiveShow();
                                    outInfo.title = liveShow.getLiTitle();
                                    outInfo.viewerCnt = liveShow.getViewerCnt();
                                    outInfo.coverUrl = liveShow.getCoverUrl();
                                    outInfo.url = liveShow.getUrl();
                                    outInfo.shareUrl = liveShow.getShareUrl();
                                    break;
                                case FeedsInfo.TYPE_BACK:
                                    LiveShowProto.BackInfo backInfo = content.getBackInfo();
                                    outInfo.title = backInfo.getBaTitle();
                                    outInfo.viewerCnt = backInfo.getViewerCnt();
                                    outInfo.coverUrl = backInfo.getCoverUrl();
                                    outInfo.url = backInfo.getUrl();
                                    outInfo.shareUrl = backInfo.getShareUrl();
                                    break;
                                case FeedsInfo.TYPE_VIDEO:
                                default:
                                    Feeds.UGCFeed ugcFeed = content.getUgcFeed();
                                    if (ugcFeed != null) {
                                        outInfo.title = ugcFeed.getTiltle();
                                        outInfo.description = ugcFeed.getDesc();
                                        outInfo.viewerCnt = ugcFeed.getViewCount();
                                        outInfo.coverUrl = ugcFeed.getCoverPage();
                                        outInfo.url = ugcFeed.getUrl();
                                        outInfo.shareUrl = ugcFeed.getShareUrl();
                                        outInfo.isReplay = false;
                                    }
                                    break;
                            }
                            //分享相关需要的信息
                            mMyRoomData.setShareUrl(outInfo.shareUrl);
                            mMyRoomData.setCoverUrl(outInfo.coverUrl);
                            mMyRoomData.setLiveTitle(outInfo.title);
                            mMyRoomData.setViewerCnt(outInfo.viewerCnt);
                        } catch (Exception e) {
                            MyLog.e(TAG, "parseFeedsInfo failed, exception=" + e);
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
                        if (mView == null) {
                            return;
                        }
                        if (outInfo != null) {
                            //詳情頁需要再刷一下ui
                            postEvent(MSG_PLAYER_FEEDS_DETAIL, new Params().putItem(outInfo));
                            postEvent(MSG_UPDATE_LIKE_STATUS, new Params().putItem(outInfo.mySelfLike));
                            postEvent(MSG_UPDATE_START_TIME, new Params().putItem(outInfo.timestamp));
                            if (TextUtils.isEmpty(mMyRoomData.getVideoUrl()) && !TextUtils.isEmpty(outInfo.url)) {
                                mMyRoomData.setVideoUrl(outInfo.url);
                                postEvent(MSG_PLAYER_START);
                            }
                            mView.onFeedsInfo(mMyRoomData.getUid(), outInfo.title, outInfo.timestamp,
                                    outInfo.viewerCnt, outInfo.coverUrl);
                        } else {
                            MyLog.d(TAG, "feedsInfo failed");
                            postEvent(MSG_UPDATE_START_TIME, new Params().putItem(0l));
                            postEvent(MSG_PLAYER_FEEDS_DETAIL, new Params().putItem(new FeedsInfo())); // 在刷新底部回放、评论
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
            postEvent(MSG_SHOW_PERSONAL_INFO, new Params().putItem(uid));
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

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_NEW_DETAIL_REPLAY:
                syncFeedsInfo();
                break;
            default:
                break;
        }
        return false;
    }

    public static class FeedsInfo {
        //Feed的Type类型.0 直播，1 图片，2. 小视频， 3 回放视频, 4, 直播结束，等待回放, 5,聚合回放, 6 作品
        public static final int TYPE_LIVE = 0;
        public static final int TYPE_IMG = 1;
        public static final int TYPE_VIDEO = 2;
        public static final int TYPE_BACK = 3;
        boolean isReplay = true;
        String title = "";
        String description = "";
        boolean mySelfLike = false;
        long timestamp = 0;
        int viewerCnt = 0;
        String coverUrl = "";
        String url = "";
        String shareUrl = "";
    }
}

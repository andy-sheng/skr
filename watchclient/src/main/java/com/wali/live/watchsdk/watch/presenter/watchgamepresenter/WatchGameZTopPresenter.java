package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.text.TextUtils;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.feedback.FeedBackApi;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.RelationProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.feedback.ReportFragment;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.download.GameDownloadOptControl;
import com.wali.live.watchsdk.watch.model.WatchGameInfoConfig;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameZTopView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_GAME_BARRAGE;
import static com.wali.live.component.BaseSdkController.MSG_NEW_GAME_WATCH_EXIST_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SOUND_OFF;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SOUND_ON;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_BARRAGE;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_TOUCH_VIEW;
import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_VOLUME;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_LAND_DOWNLOAD_CLICK;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_TYPE_CLICK;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.STATTUS_BEGIN_DOWNLOAD;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.STATTUS_CONTINUE_DOWNLOAD;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.STATTUS_INSTALL;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.STATTUS_LAUNCH;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.STATTUS_PAUSE_DOWNLOAD;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.RequestGameDownloadByMiLiveEvent.SUCCESS;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameZTopPresenter extends BaseSdkRxPresenter<WatchGameZTopView.IView>
        implements WatchGameZTopView.IPresenter {
    private static final String TAG = "WatchGameZTopPresenter";

    private RoomBaseDataModel mMyRoomData;

    private Subscription mFollowSubscription;

    private boolean mIsLandscape;

    private boolean mIsDownloadByGc;

    private CustomDownloadManager.ApkStatusEvent mLastStatusEvent;//存储最后一个有用的状态

    public WatchGameZTopPresenter(IEventController controller) {
        super(controller);
        if (controller != null && controller instanceof WatchComponentController) {
            mMyRoomData = ((WatchComponentController) mController).getRoomBaseDataModel();
        }
    }


    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_PLAYER_START);
        registerAction(MSG_PLAYER_SOUND_OFF);
        registerAction(MSG_PLAYER_SOUND_ON);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mView != null) {
            mView.stopView();
        }
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        if (mView == null) {
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                mView.reOrient(false);
                mIsLandscape = false;
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                mView.reOrient(true);
                mIsLandscape = true;
                return true;
            case MSG_ON_BACK_PRESSED:
                if (mIsLandscape) {
                    forceRotate();
                    return true;
                }
                return false;
            //其他地方调用暂停时候手动的去更新下ui
            case MSG_PLAYER_PAUSE:
                mView.updatePauseEvent(true);
                break;
            case MSG_PLAYER_START:
                mView.updatePauseEvent(false);
                break;
            case MSG_PLAYER_SOUND_OFF:
                mView.updateMuteEvent(true);
                break;
            case MSG_PLAYER_SOUND_ON:
                mView.updateMuteEvent(false);
                break;
        }
        return false;
    }

    @Override
    public void forceRotate() {
        postEvent(MSG_FORCE_ROTATE_SCREEN);
    }

    @Override
    public void exitRoom() {
        postEvent(MSG_NEW_GAME_WATCH_EXIST_CLICK);
    }

    @Override
    public void syncAnchorInfo() {
        if (mMyRoomData == null) {
            return;
        }

        boolean isFollowed = mMyRoomData.isFocused() || mMyRoomData.getUid() == UserAccountManager.getInstance().getUuidAsLong();
        mView.updateAnchorInfo(mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                mMyRoomData.getNickName(), isFollowed);
    }

    @Override
    public void showAnchorInfo() {
        if (mMyRoomData == null) {
            return;
        }
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mMyRoomData.getUid(), null);
    }

    @Override
    public void followAnchor() {
        if (mFollowSubscription != null &&
                !mFollowSubscription.isUnsubscribed() ||
                !AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            return;
        }
        mFollowSubscription = RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(),
                mMyRoomData.getUid(), mMyRoomData.getRoomId())
                .subscribeOn(Schedulers.io())
                .compose(this.<RelationProto.FollowResponse>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RelationProto.FollowResponse>() {
                    @Override
                    public void call(RelationProto.FollowResponse followResponse) {
                        MyLog.d(TAG, "followResultCode = " + followResponse.getCode());
                        mView.onFollowResult(followResponse.getCode());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mView.onFollowResult(-1);
                    }
                });
    }

    @Override
    public void showGiftView() {
        MyLog.d(TAG, "showGiftView");
        EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(
                GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST));
    }

    @Override
    public void optDisLike() {
        if (mMyRoomData == null) {
            MyLog.w(TAG, "MyRoomData is null");
            return;
        }

        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean b = FeedBackApi.sendDisLikeLiveFeedBack(System.currentTimeMillis(), mMyRoomData.getUid(), mMyRoomData.getRoomId());
                subscriber.onNext(b);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        ToastUtils.showToast(aBoolean ? R.string.dislike_feedback_success : R.string.dislike_feedback_fail);
                    }
                });
    }

    @Override
    public void optReprot() {
        if (mMyRoomData == null) {
            MyLog.w(TAG, "MyRoomData is null");
            return;
        }

        ReportFragment.openFragment((BaseActivity) mView.getRealView().getContext()
                , mMyRoomData.getUid()
                , mMyRoomData.getRoomId()
                , mMyRoomData.getVideoUrl()
                , ReportFragment.LOCATION_ROOM, ReportFragment.EXT_ANCHOR);
    }

    @Override
    public WatchComponentController getController() {
        return (WatchComponentController) mController;
    }

    @Override
    public void videoPause() {
        MyLog.d(TAG, "videoPause");
        postEvent(MSG_PLAYER_PAUSE);
    }

    @Override
    public void videoRestart() {
        MyLog.d(TAG, "videoRestart");
        postEvent(MSG_PLAYER_START);
    }

    @Override
    public void vodeoReFresh() {
        MyLog.d(TAG, "vodeoReFresh");
        postEvent(MSG_PLAYER_RECONNECT);
    }

    @Override
    public void optBarrageControl(boolean needHide) {
        postEvent(needHide ? MSG_HIDE_GAME_BARRAGE : MSG_SHOW_GAME_BARRAGE);
    }

    @Override
    public void videoMute(boolean isMute) {
        MyLog.d(TAG, "videoMute isMute = " + isMute);
        if (isMute) {
            postEvent(MSG_PLAYER_SOUND_OFF);
        } else {
            postEvent(MSG_PLAYER_SOUND_ON);
        }
    }

    @Override
    public void syncGameInfo() {
        GameDownloadOptControl.tryQueryGameDownStatus(mMyRoomData.getGameInfoModel());
    }

    @Override
    public void openFullScreenMoreLiveView() {
        postEvent(MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW);
    }

    @Override
    public void videoTouchViewClick() {
        postEvent(MSG_VIDEO_TOUCH_VIEW);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.WatchGameControllChangeEvent event) {
        if (event == null) {
            return;
        }
        if (event.type == WATCH_GAME_CONTROLL_VOLUME && event.percent != 0) {
            videoMute(false);
        }
    }

    @Override
    public void tryUpdateDownloadStatus() {
        if(mLastStatusEvent != null
                && mMyRoomData.getGameInfoModel() != null
                && mLastStatusEvent.gameId == mMyRoomData.getGameInfoModel().getGameId()) {

            if(mIsDownloadByGc) {
                mLastStatusEvent.isByQuery = true;
                onEventMainThread(mLastStatusEvent);
            } else {
                onEventMainThread(new EventClass.UpdateGameInfoStatus(mMyRoomData.getGameInfoModel()));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.RequestGameDownloadByMiLiveEvent event) {
        if (event.mGameInfoModel == null) {
            return;
        }

        if (event.mGameInfoModel.getGameId() == mMyRoomData.getGameInfoModel().getGameId()) {
            if (event.type == STATTUS_INSTALL) {
                if (event.status == SUCCESS) {
                    postEvent(MSG_PLAYER_PAUSE);
                }
            } else if (event.type == STATTUS_LAUNCH) {
                if (event.status == SUCCESS) {
                    postEvent(MSG_PLAYER_PAUSE);
                }
            } else if (event.type == STATTUS_BEGIN_DOWNLOAD
                    || event.type == STATTUS_CONTINUE_DOWNLOAD) {
                CustomDownloadManager.Item item = new CustomDownloadManager.Item(mMyRoomData.getGameInfoModel().getPackageUrl(), mMyRoomData.getGameInfoModel().getGameName());
                CustomDownloadManager.getInstance().beginDownload(item, mView.getRealView().getContext());
            } else if (event.type == STATTUS_PAUSE_DOWNLOAD) {
                CustomDownloadManager.getInstance().pauseDownload(mMyRoomData.getGameInfoModel().getPackageUrl());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (null != event && mMyRoomData != null && mMyRoomData.getUser() != null && mMyRoomData.getUser().getUid() == event.uuid) {
            final User user = mMyRoomData.getUser();
            if (user != null && user.getUid() == event.uuid) {
                boolean needUpdateDb = false;

                if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                    user.setIsFocused(true);
                    mView.showFollowBtn(false, true);
                    needUpdateDb = true;
                } else if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                    user.setIsFocused(false);
                    mView.showFollowBtn(true, true);
                    needUpdateDb = true;
                } else {
                    MyLog.e(TAG, "type error");
                }
                MyLog.d(TAG, "needUpdateDb=" + needUpdateDb);
                if (needUpdateDb) {
                    // 其后台线程
                    Observable.just(null)
                            .map(new Func1<Object, Object>() {
                                @Override
                                public Object call(Object o) {
                                    return RelationDaoAdapter.getInstance().insertRelation(user.getRelation());
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.ApkStatusEvent event) {
        if (event == null) {
            return;
        }

        if (event.isByGame || mIsDownloadByGc) {
            if (event.gameId == mMyRoomData.getGameInfoModel().getGameId()
                    && event.packageName.equals(mMyRoomData.getGameInfoModel().getPackageName())) {
                mLastStatusEvent = event;
                mIsDownloadByGc = true;
                switch (event.status) {
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING:
                    case CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD:
                    case CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD:
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED:
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_FAILED:
                        if (event.isByQuery) {
                            mView.updateGamePopView(mMyRoomData.getGameInfoModel(), event.status, true, true);
                        }
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH_SUCEESS:
                        postEvent(MSG_PLAYER_PAUSE);
                        break;
                }
            }
        } else {
            if(mMyRoomData != null
                    && mMyRoomData.getGameInfoModel() != null
                    && mMyRoomData.getGameInfoModel().getGameId() == event.gameId) {
                mIsDownloadByGc = false;
                mLastStatusEvent = event;//
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        if (mView == null) {
            return;
        }
        MyLog.d(TAG, "KeyboardEvent");
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                mView.keyBoardEvent(false);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                mView.keyBoardEvent(true);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                mMyRoomData = event.source;
                if (mView != null) {
                    mView.updateGameInfo(event.source);
                }
            }
            break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.UpdateGameInfoStatus event) {
        if (event == null
                || mView == null) {
            return;
        }

        if (mMyRoomData != null
                && mMyRoomData.getGameInfoModel() != null && event.mGameInfoModel != null) {
            GameInfoModel gameInfoModel = mMyRoomData.getGameInfoModel();
            if (TextUtils.isEmpty(gameInfoModel.getPackageName()) || TextUtils.isEmpty(event.mGameInfoModel.getPackageName())) {
                // 包名无效
                return;
            }

            if (gameInfoModel.getGameId() == event.mGameInfoModel.getGameId()
                    && gameInfoModel.getPackageName().equals(event.mGameInfoModel.getPackageName())) {
                // 是当前需要处理的本地游戏
                if (PackageUtils.isInstallPackage(gameInfoModel.getPackageName())) {
                    // 已经安装 隐藏
                    mView.updateGamePopView(gameInfoModel, CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH, false, false);
                } else {
                    // 未安装 显示挂件
                    String apkPath = CustomDownloadManager.getInstance().getDownloadPath(gameInfoModel.getPackageUrl());
                    if (PackageUtils.isCompletedPackage(apkPath, gameInfoModel.getPackageName())) {
                        // 存在包
                        mView.updateGamePopView(gameInfoModel, CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED, true, false);
                    } else {
                        // 下载不完全
                        mView.updateGamePopView(gameInfoModel, CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD, true, false);
                    }
                }
            }
        }
    }

    private void clickDownloadStatistic(String url) {
        WatchGameInfoConfig.InfoItem infoItem = WatchGameInfoConfig.sGameInfoMap.get(url);
        if (infoItem != null) {
            MilinkStatistics.getInstance().statisticGameWatchDownload(GAME_WATCH_TYPE_CLICK,
                    GAME_WATCH_BIZTYPE_LAND_DOWNLOAD_CLICK, infoItem.anchorId, infoItem.channelId, infoItem.packageName);
        }
    }
}

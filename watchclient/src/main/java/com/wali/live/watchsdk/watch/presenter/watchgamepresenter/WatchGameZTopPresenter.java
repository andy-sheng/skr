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
import com.wali.live.watchsdk.feedback.ReportFragment;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
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
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_BARRAGE;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameZTopPresenter extends BaseSdkRxPresenter<WatchGameZTopView.IView>
        implements WatchGameZTopView.IPresenter {
    private static final String TAG = "WatchGameZTopPresenter";

    private RoomBaseDataModel mMyRoomData;

    private Subscription mFollowSubscription;

    private boolean mIsLandscape;

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
                if(mIsLandscape) {
                    forceRotate();
                    return true;
                }
                return false;
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
    public void clickDownLoad(int flag) {
        if (mMyRoomData.getGameInfoModel() != null) {
            if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD) {
                CustomDownloadManager.Item item = new CustomDownloadManager.Item(mMyRoomData.getGameInfoModel().getPackageUrl(), mMyRoomData.getGameInfoModel().getGameName());
                CustomDownloadManager.getInstance().beginDownload(item, mView.getRealView().getContext());
            } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING) {
                ToastUtils.showToast("暂停下载");
                CustomDownloadManager.getInstance().pauseDownload(mMyRoomData.getGameInfoModel().getPackageUrl());
            } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {
                ToastUtils.showToast("继续下载");
                CustomDownloadManager.Item item = new CustomDownloadManager.Item(mMyRoomData.getGameInfoModel().getPackageUrl(), mMyRoomData.getGameInfoModel().getGameName());
                CustomDownloadManager.getInstance().beginDownload(item, mView.getRealView().getContext());
            } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED) {
                String apkPath = CustomDownloadManager.getInstance().getDownloadPath(mMyRoomData.getGameInfoModel().getPackageUrl());
                PackageUtils.tryInstall(apkPath);
            } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
                PackageUtils.tryLaunch(mMyRoomData.getGameInfoModel().getPackageName());
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
        String key = MD5.MD5_32(mMyRoomData.getGameInfoModel().getPackageUrl());
        if (!TextUtils.isEmpty(event.downloadKey)) {
            if (event.downloadKey.equals(key)) {
                mView.updateInstallStatus(event.status, event.progress, event.reason);
            }
        } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
            // 安装完成
        } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE) {
            // 卸载完成
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        if(mView == null) {
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
}

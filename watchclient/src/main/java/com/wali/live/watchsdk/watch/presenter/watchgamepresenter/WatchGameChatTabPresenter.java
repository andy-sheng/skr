package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.system.PackageUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
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
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameChatTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameChatTabPresenter extends BaseSdkRxPresenter<WatchGameChatTabView.IView>
        implements WatchGameChatTabView.IPresenter {
    private static final String TAG = "WatchGameBottomEditPresenter";

    private RoomBaseDataModel mMyRoomData;
    private Subscription mFollowSubscription;

    public WatchGameChatTabPresenter(@NonNull WatchComponentController controller) {
        super(controller);
        mMyRoomData = controller.getRoomBaseDataModel();
    }

    @Override
    public void updateUi() {
        MyLog.d(TAG, "syncData");
        mView.updateAnchorInfo(mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                mMyRoomData.getCertificationType(), mMyRoomData.getLevel(), mMyRoomData.getNickName());
        mView.updateViewerNum(mMyRoomData.getViewerCnt());
        mView.showFollowBtn(mMyRoomData.isEnableRelationChain(), mMyRoomData.isFocused());
    }

    @Override
    public void pauseVideo() {
        postEvent(MSG_PLAYER_PAUSE);
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }

    @CallSuper
    public void startPresenter() {
        super.startPresenter();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (mMyRoomData != null) {
            mView.updateGameInfo(mMyRoomData);
        }
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                updateUi();
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET: {
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {
                if (mView != null) {
                    mView.updateViewerNum(event.source.getViewerCnt());
                }
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                if (mView != null) {
                    mView.updateGameInfo(event.source);
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void getAnchorInfo() {
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mMyRoomData.getUid(), null);
    }

    @Override
    public void followAnchor() {
        MyLog.d(TAG, "follow anchor");
        if (mFollowSubscription != null &&
                !mFollowSubscription.isUnsubscribed() ||
                !AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            return;
        }
        mFollowSubscription = RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(),
                mMyRoomData.getUid(), mMyRoomData.getRoomId())
                .subscribeOn(Schedulers.io())
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (null != event && mMyRoomData != null && mMyRoomData.getUser() != null && mMyRoomData.getUser().getUid() == event.uuid) {
            final User user = mMyRoomData.getUser();
            if (user != null && user.getUid() == event.uuid) {
                boolean needUpdateDb = false;

                if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                    user.setIsFocused(true);
                    mView.showFollowBtn(mMyRoomData.isEnableRelationChain(), true);
                    needUpdateDb = true;
                } else if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                    user.setIsFocused(false);
                    mView.showFollowBtn(mMyRoomData.isEnableRelationChain(), false);
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
    public void onEventMainThread(EventClass.UpdateGameInfoStatus event) {
        if (event == null) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.ApkStatusEvent event) {
        if (event == null) {
            return;
        }

        MyLog.d(TAG, "ApkStatusEvent" + event.status + "event.isByGc" + event.isByQuery);
        if (mMyRoomData != null
                && mMyRoomData.getGameInfoModel().getPackageName().equals(event.packageName)) {
            MyLog.d(TAG, "ApkStatusEvent" + event.status + "event.isByGc" + event.isByQuery);
            if (event.isByQuery) {
                mView.updateGamePopView(mMyRoomData.getGameInfoModel(), event.status, true, true);
            }
        }

    }
}

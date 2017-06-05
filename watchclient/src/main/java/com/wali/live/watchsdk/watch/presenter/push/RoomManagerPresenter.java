package com.wali.live.watchsdk.watch.presenter.push;

import android.os.Handler;
import android.os.Looper;

import com.base.activity.RxActivity;
import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.watchsdk.R;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * for watch/repaly/live
 * Created by chengsimin on 16/7/4.
 */
public class RoomManagerPresenter extends RxLifeCyclePresenter implements IPushMsgProcessor {

    private static final String TAG = "RoomManagePresenter";

    private LiveRoomChatMsgManager mRoomChatMsgManager;
    private boolean mIsWatchRoom = false;
    private long mManagerUpdateTime;
    private MyAlertDialog mDialog; //多个presenter之间可能会有多个dialog，先不管
    private RxActivity mRxActivity;

    protected RoomBaseDataModel mMyRoomData;

    public RoomManagerPresenter(
            RxActivity rxActivity,
            LiveRoomChatMsgManager roomChatMsgManager,
            boolean isWatchRoom,
            RoomBaseDataModel myRoomData) {
        mRoomChatMsgManager = roomChatMsgManager;
        mIsWatchRoom = isWatchRoom;
        mRxActivity = rxActivity;
        mMyRoomData = myRoomData;
    }

    private Handler mHandler;

    @Override
    public void process(final BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_FORBIDDEN) {
            BarrageMsg.ForbiddenMsgExt ext = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "forbiddenUserId:" + ext.forbiddenUserId);
            if (mIsWatchRoom) {
                processForbiddenForWatchRoom(ext.forbiddenUserId, roomBaseDataModel);
            } else {
                processForbiddenForLiveRoom(ext.forbiddenUserId);
            }
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN) {
            BarrageMsg.ForbiddenMsgExt ext = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "cancelforbiddenUserId:" + ext.forbiddenUserId);
            if (mIsWatchRoom) {
                processCancelForbiddenForWatchRoom(ext.forbiddenUserId, roomBaseDataModel);
            } else {
                processCancelForbiddenForLiveRoom(ext.forbiddenUserId);
            }
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_SET_MANAGER) {
            if (!mIsWatchRoom || MiLinkClientAdapter.getsInstance().isTouristMode()) {
                return;
            }
            if (msg.getSentTime() > mManagerUpdateTime) {
                mManagerUpdateTime = msg.getSentTime();
                LiveRoomManagerModel manager = new LiveRoomManagerModel(UserAccountManager.getInstance().getUuidAsLong());
                User mine = MyUserInfoManager.getInstance().getUser();
                manager.level = mine.getLevel();
                manager.avatar = mine.getAvatar();
                manager.certificationType = mine.getCertificationType();
                WatchRoomCharactorManager.getInstance().setManager(manager);
                WatchRoomCharactorManager.initBanSpeakerList(UserAccountManager.getInstance().getUuidAsLong(), roomBaseDataModel.getUid(), roomBaseDataModel.getRoomId());
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = DialogUtils.showAlertDialog(mRxActivity, GlobalData.app().getResources().getString(R.string.setting_dialog_black_title), msg.getBody(), GlobalData.app().getString(R.string.ok));
                    }
                });
            }
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER) {
            if (!mIsWatchRoom) {
                return;
            }
            if (msg.getSentTime() > mManagerUpdateTime) {
                mManagerUpdateTime = msg.getSentTime();
                WatchRoomCharactorManager.getInstance().setManager(null);
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mDialog = DialogUtils.showAlertDialog(mRxActivity, GlobalData.app().getString(R.string.setting_dialog_black_title), msg.getBody(), GlobalData.app().getString(R.string.ok));
                    }
                });
                mRoomChatMsgManager.addChatMsg(msg, true);
            }
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN,
                BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER,
                BarrageMsgType.B_MSG_TYPE_SET_MANAGER
        };
    }

    private void processForbiddenForWatchRoom(long forbiddenUserId, RoomBaseDataModel roomBaseDataModel) {
        if (forbiddenUserId == com.mi.live.data.account.UserAccountManager.getInstance().getUuidAsLong()) {
            roomBaseDataModel.setCanSpeak(false);
        }
        if (WatchRoomCharactorManager.getInstance().hasManagerPower(roomBaseDataModel.getUid())) {
            WatchRoomCharactorManager.getInstance().banSpeaker(forbiddenUserId, true);
        }
    }

    private void processCancelForbiddenForWatchRoom(long forbiddenUserId, RoomBaseDataModel roomBaseDataModel) {
        if (forbiddenUserId == UserAccountManager.getInstance().getUuidAsLong()) {
            roomBaseDataModel.setCanSpeak(true);
        }
        if (WatchRoomCharactorManager.getInstance().hasManagerPower(roomBaseDataModel.getUid())) {
            WatchRoomCharactorManager.getInstance().banSpeaker(forbiddenUserId, false);
        }
    }

    private void processForbiddenForLiveRoom(final long forbiddenUserId) {
        Observable.just(0)
                .map(new Func1<Integer, User>() {
                    @Override
                    public User call(Integer integer) {
                        return UserInfoManager.getUserInfoByUuid(forbiddenUserId, false);
                    }
                })
                .compose(mRxActivity.<User>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(Schedulers.io())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        LiveRoomCharacterManager.getInstance().banSpeaker(user, true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        MyLog.e(TAG, "get userinfo error,uuid:" + forbiddenUserId, throwable);
                    }
                });
    }

    private void processCancelForbiddenForLiveRoom(long forbiddenUserId) {
        User user = new User();
        user.setUid(forbiddenUserId);
        LiveRoomCharacterManager.getInstance().banSpeaker(user, false);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void syncOwnerInfo(final long uid, final boolean needPullLiveInfo) {
        Observable.just(0)
                .map(new Func1<Integer, User>() {
                    @Override
                    public User call(Integer integer) {
                        return UserInfoManager.getUserInfoByUuid(uid, needPullLiveInfo);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<User>bindUntilEvent(RxLifeCyclePresenter.PresenterEvent.DESTROY))
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (user == null || mMyRoomData == null || mMyRoomData.getUid() != uid) {
                            return;
                        }
                        if (user.isManager(MyUserInfoManager.getInstance().getUuid())) {
                            LiveRoomManagerModel manager = new LiveRoomManagerModel(
                                    UserAccountManager.getInstance().getUuidAsLong());
                            User mine = MyUserInfoManager.getInstance().getUser();
                            manager.level = mine.getLevel();
                            manager.avatar = mine.getAvatar();
                            manager.certificationType = mine.getCertificationType();
                            WatchRoomCharactorManager.getInstance().setManager(manager);
                            WatchRoomCharactorManager.initBanSpeakerList(
                                    UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getUid(), mMyRoomData.getRoomId());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncOwnerInfo failed, exception=" + throwable);
                    }
                });

    }
}

package com.wali.live.watchsdk.watch.presenter;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.manager.WatchRoomCharactorManager;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/14.
 */
public class UserInfoPresenter {
    private static final String TAG = UserInfoPresenter.class.getSimpleName();

    private RxActivity mRxActivity;
    private RoomBaseDataModel mMyRoomData;

    private boolean mHasUpdateOwnerInfo = false;

    // 如果从登录过来，也需要更新，因为可能主播是自己关注过的
    private boolean mHasUpdateFromLogin = false;

    private Subscription mFirstSubscription;
    private Subscription mSecondSubscription;

    public UserInfoPresenter(RxActivity mRxActivity, RoomBaseDataModel mMyRoomData) {
        this.mRxActivity = mRxActivity;
        this.mMyRoomData = mMyRoomData;
    }

    // 更新主播信息
    public void updateOwnerInfo() {
        // 第一次默认拉名片HomePage，第二次登录拉info更新关注状态，因为接口更快
        if (!mHasUpdateOwnerInfo) {
            updateHomePageFromServer();
        } else if (!mHasUpdateFromLogin) {
            updateOwnerInfoFromServer();
        }
    }

    /**
     * 目前主要用来切换房间时，重置内部状态
     */
    public void reset() {
        if (mFirstSubscription != null && !mFirstSubscription.isUnsubscribed()) {
            mFirstSubscription.unsubscribe();
        }
        if (mSecondSubscription != null && !mSecondSubscription.isUnsubscribed()) {
            mSecondSubscription.unsubscribe();
        }
        clearLoginFlag();
    }

    public void clearLoginFlag() {
        MyLog.w(TAG, "User info clear flag");
        mHasUpdateFromLogin = false;
    }

    private void updateHomePageFromServer() {
        MyLog.w(TAG, "update home page");
        mFirstSubscription = Observable
                .create(new Observable.OnSubscribe<User>() {
                    @Override
                    public void call(Subscriber<? super User> subscriber) {
                        subscriber.onNext(UserInfoManager.getUserInfoByUuid(mMyRoomData.getUid(), false));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mRxActivity.<User>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(User user) {
                        if (user != null) {
                            mHasUpdateOwnerInfo = true;

                            mMyRoomData.setNickname(user.getNickname());
                            mMyRoomData.setTicket(user.getLiveTicketNum());
                            mMyRoomData.setUser(user);
                            if (mMyRoomData.getUser().isManager(UserAccountManager.getInstance().getUuidAsLong())) {
                                LiveRoomManagerModel manager = new LiveRoomManagerModel(UserAccountManager.getInstance().getUuidAsLong());
                                manager.level = MyUserInfoManager.getInstance().getUser().getLevel();
                                manager.avatar = MyUserInfoManager.getInstance().getAvatar();
                                manager.certificationType = MyUserInfoManager.getInstance().getUser().getCertificationType();
                                manager.isInRoom = true;
                                WatchRoomCharactorManager.getInstance().setManager(manager);
                            }
                            EventBus.getDefault().post(new RoomDataChangeEvent(mMyRoomData, RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE));
                        }
                    }
                });
    }

    private void updateOwnerInfoFromServer() {
        MyLog.w(TAG, "update owner info");
        mSecondSubscription = Observable
                .create(new Observable.OnSubscribe<User>() {
                    @Override
                    public void call(Subscriber<? super User> subscriber) {
                        subscriber.onNext(UserInfoManager.getUserInfoById(mMyRoomData.getUid()));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mRxActivity.<User>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(User user) {
                        if (user != null) {
                            mHasUpdateFromLogin = true;

                            mMyRoomData.setNickname(user.getNickname());
                            mMyRoomData.getUser().setIsFocused(user.isFocused());
                            mMyRoomData.getUser().setAvatar(user.getAvatar());
                            mMyRoomData.setTicket(user.getLiveTicketNum());
                            mMyRoomData.getUser().setSign(user.getSign());

                            EventBus.getDefault().post(new RoomDataChangeEvent(mMyRoomData, RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE));
                        }
                    }
                });
    }
}

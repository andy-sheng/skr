package com.wali.live.watchsdk.watch.presenter;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/14.
 */
public class UserInfoPresenter {
    private static final String TAG = UserInfoPresenter.class.getSimpleName();

    private RxActivity mRxActivity;
    private RoomBaseDataModel mMyRoomData;

    public UserInfoPresenter(RxActivity mRxActivity, RoomBaseDataModel mMyRoomData) {
        this.mRxActivity = mRxActivity;
        this.mMyRoomData = mMyRoomData;
    }

    private boolean mHasUpdateOwnerInfo = false;
    // 如果从登录过来，也需要更新，因为可能主播是自己关注过的
    private boolean mHasUpdateFromLogin = false;

    // 更新主播信息
    public void updateOwnerInfo() {
        if (!mHasUpdateOwnerInfo) {
            updateOwnerInfoFromServer(false);
        } else if (!mHasUpdateFromLogin) {
            updateOwnerInfoFromServer(true);
        }
    }

    public void clearLoginFlag() {
        MyLog.w(TAG, "User info clear flag");
        mHasUpdateFromLogin = false;
    }

    private void updateOwnerInfoFromServer(final boolean isFromLogin) {
        MyLog.w(TAG, "User info update from login=" + isFromLogin);
        Observable
                .create(new Observable.OnSubscribe<User>() {
                    @Override
                    public void call(Subscriber<? super User> subscriber) {
                        subscriber.onNext(UserInfoManager.getUserInfoById(mMyRoomData.getUid()));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                            if (isFromLogin) {
                                mHasUpdateFromLogin = true;
                            } else {
                                mHasUpdateOwnerInfo = true;
                            }

                            mMyRoomData.setNickname(user.getNickname());
                            mMyRoomData.getUser().setIsFocused(user.isFocused());
                            EventBus.getDefault().post(new RoomDataChangeEvent(mMyRoomData, RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE));
                        }
                    }
                });
    }
}

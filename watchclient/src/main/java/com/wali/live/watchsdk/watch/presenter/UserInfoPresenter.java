package com.wali.live.watchsdk.watch.presenter;

import com.base.activity.RxActivity;
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
    RxActivity mRxActivity;
    RoomBaseDataModel mMyRoomData;

    public UserInfoPresenter(RxActivity mRxActivity, RoomBaseDataModel mMyRoomData) {
        this.mRxActivity = mRxActivity;
        this.mMyRoomData = mMyRoomData;
    }

    boolean mHasUpdateOwnerInfo = false;

    // 更新主播信息
    public void updateOwnerInfo() {
        if(mHasUpdateOwnerInfo){
            return;
        }
        Observable.create(new Observable.OnSubscribe<User>() {
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
                            mHasUpdateOwnerInfo = true;
                            mMyRoomData.setNickname(user.getNickname());
                            mMyRoomData.getUser().setIsFocused(user.isFocused());
                            EventBus.getDefault().post(new RoomDataChangeEvent(mMyRoomData, RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE));
                        }
                    }
                });

    }
}

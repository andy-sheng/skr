package com.wali.live.watchsdk.endlive.presenter;

import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/07/18.
 *
 * @module 结束页的presenter
 */
public class UserEndLivePresenter extends RxLifeCyclePresenter {
    private static final String TAG = "UserEndLivePresenter";
    @Nullable
    protected User mUser;
    @Nullable
    protected IUserEndLiveView mView;
    protected Subscription mSubscription;

    public UserEndLivePresenter(User user, IUserEndLiveView view) {
        mUser = user;
        mView = view;
    }

    public void getUser() {
        Observable.just(0).map(new Func1<Integer, User>() {
            @Override
            public User call(Integer integer) {
                if (mUser.getUid() <= 0) {
                    return null;
                }
                return UserInfoManager.getUserInfoByUuid(mUser.getUid(), true);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<User>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (user != null) {
                            mUser = user;
                            if (mView != null) {
                                mView.onRefresh();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "getUser failed=" + throwable);
                    }
                });
    }

    public void follow(final String roomId) {
        Observable.just(null).map(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return RelationUtils.follow(UserAccountManager.getInstance().getUuidAsLong(),
                        mUser.getUid(), roomId) >= RelationUtils.FOLLOW_STATE_SUCCESS;
            }
        }).subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (result) {
                            mUser.setIsFocused(result);
                            ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.endlive_follow_success));
                        } else {
                            ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.follow_failed));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "follow failed=" + throwable);
                    }
                });
    }

    public void nextRoom(int time) {
        if (time < 0) {
            time = 0;
        }
        final int countTime = time;
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long t) {
                        return countTime - t.intValue();
                    }
                })
                .take(countTime + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer t) {
                        if (mView != null) {
                            mView.onCountDown(t);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "countDown failed=" + throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        //onComplete
                        if (mView != null) {
                            mView.onNextRoom();
                        }
                    }
                });
    }

    public interface IUserEndLiveView {
        void onCountDown(int time);

        void onNextRoom();

        void onRefresh();
    }
}

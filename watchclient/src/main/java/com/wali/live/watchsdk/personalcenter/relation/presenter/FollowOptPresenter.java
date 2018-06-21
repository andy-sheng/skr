package com.wali.live.watchsdk.personalcenter.relation.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowOptContact;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowOptPresenter extends RxLifeCyclePresenter implements FollowOptContact.Ipresenter {
    private static final String TAG = "FollowOptPresenter";
    private FollowOptContact.Iview mIview;
    private Subscription mFollowSubscribe;
    private Subscription mUnFollowSubscribe;

    public FollowOptPresenter(FollowOptContact.Iview iview) {
        mIview = iview;
    }


    @Override
    public void follow(final long targetUid) {
        if (mFollowSubscribe != null && !mFollowSubscribe.isUnsubscribed()) {
            return;
        }

        mFollowSubscribe = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                int retCode = RelationUtils.follow(UserAccountManager.getInstance().getUuidAsLong(), targetUid);
                if (retCode == RelationUtils.FOLLOW_STATE_SUCCESS
                        || retCode == RelationUtils.FOLLOW_STATE_BOTH_WAY) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onError(new Exception("follow opt fail"));
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
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
                        mIview.followSuccess(targetUid);
                    }
                });
    }

    @Override
    public void unFollow(final long targetUid) {
        if (mUnFollowSubscribe != null && !mUnFollowSubscribe.isUnsubscribed()) {
            return;
        }

        mUnFollowSubscribe = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean isSuccess = RelationUtils.unFollow(UserAccountManager.getInstance().getUuidAsLong(), targetUid);
                if (isSuccess) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onError(new Exception("unfollow opt fail"));
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(PresenterEvent.DESTROY))
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
                        mIview.unFollowSuccess();
                    }
                });
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mFollowSubscribe != null && !mFollowSubscribe.isUnsubscribed()) {
            mFollowSubscribe.unsubscribe();
        }

        if (mUnFollowSubscribe != null && !mUnFollowSubscribe.isUnsubscribed()) {
            mUnFollowSubscribe.unsubscribe();
        }
    }
}

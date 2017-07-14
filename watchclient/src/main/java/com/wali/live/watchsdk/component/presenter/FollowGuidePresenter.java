package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.RelationProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.FollowGuideView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.mi.live.data.event.FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW;

/**
 * Created by zyh on 2017/07/13.
 *
 * @module 游戏直播间内关注引导的presenter
 */
public class FollowGuidePresenter extends ComponentPresenter<FollowGuideView.IView>
        implements FollowGuideView.IPresenter {
    private static final String TAG = "FollowGuidePresenter";

    private Subscription mSubscription;
    private RoomBaseDataModel mMyRoomData;

    public FollowGuidePresenter(@NonNull IComponentController componentController, RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        startPresenter();
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        registerAction(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(WatchComponentController.MSG_ON_ORIENT_PORTRAIT);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        stopPresenter();
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (mView != null && event != null && mMyRoomData.getUid() == event.uuid) {
            if (event.eventType == EVENT_TYPE_FOLLOW) {
                mView.onFollowSuc();
            }
        }
    }

    @Override
    public void follow(long targetId, String roomId) {
        RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(),
                targetId, roomId)
                .subscribeOn(Schedulers.io())
                .compose(this.<RelationProto.FollowResponse>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RelationProto.FollowResponse>() {
                    @Override
                    public void call(RelationProto.FollowResponse followResponse) {
                        if (followResponse.getCode() == ErrorCode.CODE_RELATION_BLACK) {
                            ToastUtils.showToast(GlobalData.app().getString(R.string.setting_black_follow_hint));
                        } else if (followResponse.getCode() == 0) {
                            ToastUtils.showToast(GlobalData.app().getString(R.string.follow_success));
                            if (mView != null) {
                                mView.onFollowSuc();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ToastUtils.showToast(GlobalData.app().getString(R.string.follow_failed));
                    }
                });
    }

    /**
     * FollowView显示的倒计时
     */
    @Override
    public void countDownIn(int time) {
        if (time < 0) {
            time = 0;
        }
        final int countTime = time;
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
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
                            mView.updateCountDown(t);
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
                            mView.hideSelf();
                        }
                    }
                });
    }

    /**
     * 显示FollowView之前的倒计时
     */
    public void countDownOut(int guideFollowTs) {
        if (guideFollowTs < 0) {
            guideFollowTs = 0;
        }
        final int countTime = guideFollowTs;
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long ts) {
                        return countTime - ts.intValue();
                    }
                })
                .take(guideFollowTs + 1)
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer i) {
                        MyLog.w(TAG, "i=" + i);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "showGameGuideView failed=" + throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        //complete
                        mComponentController.onEvent(WatchComponentController.MSG_SHOW_FOLLOW_GUIDE);
                    }
                });
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onScreenChanged(true);
                    break;
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onScreenChanged(false);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}

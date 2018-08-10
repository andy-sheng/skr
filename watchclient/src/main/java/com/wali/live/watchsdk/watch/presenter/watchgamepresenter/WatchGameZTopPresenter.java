package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.RelationProto;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameZTopView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_NEW_GAME_WATCH_EXIST_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameZTopPresenter extends ComponentPresenter<WatchGameZTopView.IView>
        implements WatchGameZTopView.IPresenter {
    private RoomBaseDataModel mMyRoomData;

    private Subscription mFollowSubscription;

    public WatchGameZTopPresenter(IEventController controller) {
        super(controller);
        if (controller != null && controller instanceof WatchComponentController) {
            mMyRoomData = ((WatchComponentController) mController).getRoomBaseDataModel();
        }
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);

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
            mView.cancelAnimator();
        }
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                mView.reOrient(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                mView.reOrient(true);
                return true;
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

}

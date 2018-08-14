package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
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
}

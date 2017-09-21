package com.wali.live.watchsdk.component.presenter.panel;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.relation.RelationApi;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.RelationProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.panel.LinkInfoPanel;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by yangli on 2017/09/14.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module 主播-主播连麦信息面板表现
 */
public class LinkInfoPresenter extends BaseSdkRxPresenter<LinkInfoPanel.IView>
        implements LinkInfoPanel.IPresenter {
    private static final String TAG = "LinkInfoPresenter";

    private RoomBaseDataModel mMyRoomData;

    private String mUserLiveId;
    private long mUserId;
    private User mLinkUser;

    private Subscription mUserSub;
    private Subscription mFollowSub;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public boolean isShow() {
        return mView != null && mView.isShow();
    }

    public User getLinkUser() {
        return mLinkUser;
    }

    public LinkInfoPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
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
        MyLog.d(TAG, "stopPresenter");
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mView != null) {
            mView.hideSelf(true);
        }
    }

    private void syncUserInfo(long userId) {
        if (mLinkUser != null && mLinkUser.getUid() == userId) {
            String nickName = mLinkUser.getNickname();
            mView.updateLinkUserName(TextUtils.isEmpty(nickName) ? String.valueOf(userId) : nickName);
            mView.updateFellow(mLinkUser.isFocused());
            return;
        }
        if (mUserSub != null && !mUserSub.isUnsubscribed()) {
            mUserSub.unsubscribe();
            mUserSub = null;
        }
        mUserId = userId;
        mLinkUser = null;
        mUserSub = Observable.just(userId)
                .map(new Func1<Long, User>() {
                    @Override
                    public User call(Long userId) {
                        return UserInfoManager.getUserInfoById(userId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<User>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        if (mView != null && user != null) {
                            mLinkUser = user;
                            String nickName = mLinkUser.getNickname();
                            if (!TextUtils.isEmpty(nickName)) {
                                mView.updateLinkUserName(nickName);
                                mView.updateFellow(mLinkUser.isFocused());
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncUserInfo failed, exception=" + throwable);
                    }
                });
    }

    public void onLinkStart(long userId, String userLiveId, boolean isLandscape) {
        MyLog.w("onLinkStart");
        mView.showSelf(true, isLandscape);
        mUserLiveId = userLiveId;
        syncUserInfo(userId);
    }

    public void onLinkStop() {
        MyLog.w("onLinkStop");
        mView.hideSelf(true);
        stopPresenter();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (event != null && mUserId == event.uuid && mView != null) {
            mView.updateFellow(event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW);
        }
    }

    public void follow() {
        if (!AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            return;
        }
        if (mFollowSub != null && !mFollowSub.isUnsubscribed()) {
            return;
        }
        mFollowSub = RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(), mUserId, mUserLiveId)
                .subscribeOn(Schedulers.io())
                .compose(this.<RelationProto.FollowResponse>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RelationProto.FollowResponse>() {
                    @Override
                    public void call(RelationProto.FollowResponse rsp) {
                        if (mView == null) {
                            return;
                        }
                        Resources resources = mView.getRealView().getResources();
                        if (rsp != null && rsp.getCode() == ErrorCode.CODE_SUCCESS) {
                            ToastUtils.showToast(resources.getString(R.string.follow_success));
                            mView.updateFellow(true);
                        } else {
                            if (rsp != null && rsp.getCode() == ErrorCode.CODE_RELATION_BLACK) {
                                ToastUtils.showToast(resources.getString(R.string.setting_black_follow_hint));
                            } else {
                                ToastUtils.showToast(resources.getString(R.string.follow_failed));
                            }
                            mView.updateFellow(false);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "follow failed, exception=" + throwable);
                        if (mView != null) {
                            ToastUtils.showToast(mView.getRealView().getResources().getString(R.string.follow_failed));
                            mView.updateFellow(false);
                        }
                    }
                });
    }

    @Override
    public void enterRoom(Context context) {
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(mUserId, mUserLiveId, null)
                .setEnableShare(mMyRoomData.getEnableShare())
                .build();
        WatchSdkActivity.openActivity((Activity) context, roomInfo);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            default:
                break;
        }
        return false;
    }
}

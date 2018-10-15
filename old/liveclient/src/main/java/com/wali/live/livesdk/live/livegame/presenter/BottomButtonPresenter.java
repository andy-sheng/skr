package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.FansGroupListFragment;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.request.GetGroupListRequest;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_ON_ACTIVITY_RESUMED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SETTING_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends BaseSdkRxPresenter<LiveBottomButton.IView>
        implements LiveBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    @Nullable
    private GameLivePresenter mGameLivePresenter;
    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BottomButtonPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            @Nullable GameLivePresenter gameLivePresenter) {
        super(controller);
        mMyRoomData = myRoomData;
        mGameLivePresenter = gameLivePresenter;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_ACTIVITY_RESUMED);
        EventBus.getDefault().register(this);
        syncUnreadCount();
        checkFans();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showInputView() {
        postEvent(MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showSettingPanel() {
        postEvent(MSG_SHOW_SETTING_PANEL);
    }

    @Override
    public void muteAudio(boolean isMute) {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.muteMic(isMute);
        }
    }

    @Override
    public void showShareView() {
        postEvent(MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public boolean isEnableShare() {
        return mMyRoomData != null ? mMyRoomData.getEnableShare() : false;
    }

    @Override
    public void showMsgCtrlView() {
        postEvent(MSG_SHOW_MESSAGE_PANEL);
    }

    @Override
    public void showVipFansView() {
        FansGroupListFragment.open((BaseActivity) mView.getRealView().getContext());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConversationLocalStore.NotifyUnreadCountChangeEvent event) {
        if (event == null || mView == null) {
            return;
        }
        mView.onUpdateUnreadCount((int) event.unreadCount);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    // TODO-YangLi 相同代码，可以考虑抽取基类
    private Subscription mSubscription;
    private Subscription mFansSubscription;

    private void checkFans() {
        if (mFansSubscription != null && !mFansSubscription.isUnsubscribed()) {
            return;
        }
        mFansSubscription = Observable
                .create(new Observable.OnSubscribe<FansGroupListModel>() {
                    @Override
                    public void call(Subscriber<? super FansGroupListModel> subscriber) {
                        VFansProto.GetGroupListRsp rsp = new GetGroupListRequest().syncRsp();
                        if (rsp == null) {
                            subscriber.onError(new Exception("group list rsp is null"));
                            return;
                        }
                        if (rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            subscriber.onError(new Exception(rsp.getErrMsg() + " : " + rsp.getErrCode()));
                            return;
                        }
                        subscriber.onNext(new FansGroupListModel(rsp));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<FansGroupListModel>bindUntilEvent(PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FansGroupListModel>() {
                    @Override
                    public void call(FansGroupListModel model) {
                        MyLog.d(TAG, "get fans group success");
                        if (mView != null) {
                            MyLog.d(TAG, "has group info=" + model.hasGroup());
                            if (model.hasGroup()) {
                                mView.showFansIcon();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private void syncUnreadCount() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            return;
        }
        mSubscription = Observable.just(0)
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer i) {
                        return (int) ConversationLocalStore.getAllConversationUnReadCount();
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<Integer>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer unreadCount) {
                        if (mView != null) {
                            mView.onUpdateUnreadCount(unreadCount);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            case MSG_ON_ACTIVITY_RESUMED:
                if (mGameLivePresenter != null) {
                    mView.updateMuteBtn(mGameLivePresenter.isMuteMic());
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}

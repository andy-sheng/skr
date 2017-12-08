package com.wali.live.watchsdk.component.presenter.panel;

import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.component.view.panel.WatchMenuPanel;
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

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_ON_MENU_PANEL_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_FANS_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by zyh on 2017/12/07.
 *
 * @module 底部更多面板
 */
public class WatchMenuPresenter extends BaseSdkRxPresenter<WatchMenuPanel.IView>
        implements WatchMenuPanel.IPresenter {
    private static final String TAG = "WatchMenuPresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public WatchMenuPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        EventBus.getDefault().register(this);
        syncUnreadCount();
        checkFans();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        postEvent(MSG_ON_MENU_PANEL_HIDDEN);
        unregisterAllAction();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showShareView() {
        postEvent(MSG_HIDE_BOTTOM_PANEL);
        postEvent(MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public void showMsgCtrlView() {
        postEvent(MSG_HIDE_BOTTOM_PANEL);
        postEvent(MSG_SHOW_MESSAGE_PANEL);
    }

    @Override
    public void showVipFansView() {
        postEvent(MSG_HIDE_BOTTOM_PANEL);
        postEvent(MSG_SHOW_FANS_PANEL);
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
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<Integer>bindUntilEvent(PresenterEvent.STOP))
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
                        MyLog.w(TAG, throwable);
                    }
                });
    }

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
                break;
            default:
                break;
        }
        return false;
    }
}

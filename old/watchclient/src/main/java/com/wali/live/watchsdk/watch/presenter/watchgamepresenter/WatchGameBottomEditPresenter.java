package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.gift.exception.GiftErrorCode;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.dao.Gift;
import com.wali.live.proto.GiftProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.personalcenter.MyInfoHalfFragment;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameBottomEditView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_POP_INSUFFICIENT_TIPS;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameBottomEditPresenter extends BaseSdkRxPresenter<WatchGameBottomEditView.IView>
        implements WatchGameBottomEditView.IPresenter {
    private static final String TAG = "WatchGameBottomEditPresenter";

    private Subscription mSubscription;

    private RoomBaseDataModel mMyRoomData;
    private String mWidgetLinkUrl;
    private int mFastGiftId;
    private int mRequestTimes;
    private long mLastSendTs;
    private long mContinueId;

    public WatchGameBottomEditPresenter(WatchComponentController controller) {
        super(controller);
        mMyRoomData = controller.getRoomBaseDataModel();
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        MyLog.d(TAG, "startPresenter");
        EventBus.getDefault().register(this);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_ORIENT_PORTRAIT);

        if(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
            if(mView != null) {
                mView.hideGiftBtn();
                mView.hideFastGfitBtn();
            }
        }

        syncUnreadCount();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
        unregisterAllAction();
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
                })
                .subscribeOn(Schedulers.io())
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

    private void sendFastGift(final int giftId) {
        //保存此次请求发出时间
        //保存此次请求continuId
        long nowTs = System.currentTimeMillis();
        if (nowTs - mLastSendTs > 3000) {
            mRequestTimes = 1;
            mContinueId = nowTs;
        } else {
            mRequestTimes++;
        }
        mLastSendTs = nowTs;

        final Gift gift = GiftRepository.findGiftById(giftId);
        Observable.create(new Observable.OnSubscribe<GiftProto.BuyGiftRsp>() {
            @Override
            public void call(Subscriber<? super GiftProto.BuyGiftRsp> subscriber) {
                if (gift == null) {
                    subscriber.onError(new Exception("gift == null"));
                    subscriber.onCompleted();
                    return;
                }

                GiftProto.BuyGiftRsp rsp;
                rsp = GiftRepository.bugGiftSync(gift, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mRequestTimes, System.currentTimeMillis()
                        , mContinueId, null, mMyRoomData.getLiveType(), false, mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GiftProto.BuyGiftRsp>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .subscribe(new Observer<GiftProto.BuyGiftRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                        mRequestTimes = 0;
                        mLastSendTs = -1;
                    }

                    @Override
                    public void onNext(GiftProto.BuyGiftRsp rsp) {
                        if (rsp.getRetCode() == GiftErrorCode.SUCC) {

                            BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(giftId, gift.getName(), gift.getCatagory(),
                                    gift.getSendDescribe(), mRequestTimes, rsp.getReceiverTotalTickets(),
                                    rsp.getTicketUpdateTime(), mContinueId, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), rsp.getRedPacketId(),
                                    "", 0, false, 1, "");
                            BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);

                            mView.startFastGiftPBarAnim();

                            //扣钱
                            int deduct = rsp.getUsableGemCnt();
                            int virtualGemCnt = rsp.getUsableVirtualGemCnt();
                            MyUserInfoManager.getInstance().setDiamonds(deduct, virtualGemCnt);
                        } else if (rsp.getRetCode() == GiftErrorCode.GIFT_PAY_BARRAGE) {
                            postEvent(MSG_POP_INSUFFICIENT_TIPS);
                        } else {
                            ToastUtils.showToast(mView.getRealView().getContext(), GlobalData.app().getResources().getString(R.string.buy_gift_failed_with_err_code) + rsp.getRetCode());
                        }
                    }
                });
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }

        switch (event) {
            case MSG_ON_ORIENT_LANDSCAPE:
                if(mView.getRealView().getVisibility() != View.GONE) {
                    mView.getRealView().setVisibility(View.GONE);
                }
                break;
            case MSG_ON_ORIENT_PORTRAIT:
                if(mView.getRealView().getVisibility() != View.VISIBLE) {
                    mView.getRealView().setVisibility(View.VISIBLE);
                }
                break;
        }
        return false;
    }

    @Override
    public void showMyInfoPannel() {
        MyLog.d(TAG, "showMyInfoPannel");
        MyInfoHalfFragment.openFragment((BaseSdkActivity) mView.getRealView().getContext());
    }

    @Override
    public void onFastGiftClick() {
        MyLog.d(TAG, "onFastGiftClick");
        if (!TextUtils.isEmpty(mWidgetLinkUrl)) {
            SchemeSdkActivity.openActivity((Activity) mView.getRealView().getContext(), Uri.parse(mWidgetLinkUrl));
        } else {
            if (mFastGiftId > 0) {
                sendFastGift(mFastGiftId);
            }
        }
    }

    @Override
    public void showGiftView() {
        MyLog.d(TAG, "showGiftView");
        EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(
                GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST));
    }

    @Override
    public void showInputView() {
        MyLog.d(TAG, "showInputView");
        postEvent(MSG_SHOW_INPUT_VIEW);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MiLinkEvent.StatusLogined event) {
        syncUnreadCount();
        mView.tryBindAvatar();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.PersonalInfoChangeEvent event) {
        if (event == null) {
            return;
        }

        if (event.isAvatorChange && mView != null) {
            mView.tryBindAvatar();
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.UpdateFastGiftInfoEvent event) {
        MyLog.d(TAG, "UpdateFastGiftInfoEvent");
        if (event == null
                || mView == null
                || mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
            return;
        }

        this.mWidgetLinkUrl = event.linkUrl;
        this.mFastGiftId = event.giftId;
        if (!TextUtils.isEmpty(mWidgetLinkUrl)) {
            mView.setFastGift(event.widgetIcon, true);
        } else {
            GiftRepository.findGiftByIdAsync(this.mFastGiftId, new GiftRepository.FindGiftCallback() {
                @Override
                public void find(final Gift gift) {
                    Observable.create(new Observable.OnSubscribe<Object>() {
                        @Override
                        public void call(Subscriber<? super Object> subscriber) {
                            mView.setFastGift(gift != null ? gift.getPicture() : ""
                                    , gift != null);
                            subscriber.onCompleted();
                        }
                    })
                            .compose(bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe();

                }
            });

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(com.wali.live.event.EventClass.RechargeCheckOrderEvent event) {//支付成功需要刷新首充按钮和续费有礼
        MyLog.w(TAG, "onEvent RechargeCheckOrderEvent");
        if(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
            return;
        }

        if (!TextUtils.isEmpty(mWidgetLinkUrl)) {
            onEvent(new EventClass.UpdateFastGiftInfoEvent(mFastGiftId, null, null));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserInfoEvent event) {
        if(event == null) {
            return;
        }

        if(mView != null) {
            mView.tryBindAvatar();
        }
    }
}

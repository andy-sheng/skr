package com.wali.live.watchsdk.component.presenter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.event.TurnTableEvent;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.gift.exception.GiftErrorCode;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.dao.Gift;
import com.wali.live.proto.GiftProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.request.GetGroupListRequest;
import com.wali.live.watchsdk.personalcenter.MyInfoHalfFragment;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_BIG_TURN_TABLE_BTN;
import static com.wali.live.component.BaseSdkController.MSG_ON_MENU_PANEL_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_POP_INSUFFICIENT_TIPS;
import static com.wali.live.component.BaseSdkController.MSG_SHOE_GAME_ICON;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_BIG_TURN_TABLE_BTN;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_BIG_TURN_TABLE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_DOWNLOAD;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MENU_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends BaseSdkRxPresenter<WatchBottomButton.IView>
        implements WatchBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    private RoomBaseDataModel mMyRoomData;

    //快捷送礼物和快捷充值相关
    private String mWidgetLinkUrl;
    private int mFastGiftId;
    private int mRequestTimes;
    private long mRequestContinueId;
    private Subscription mClesrFastGiftFlagSubscriber;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public BottomButtonPresenter(
            @NonNull IEventController controller,
            RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_BOTTOM_POPUP_SHOWED);
        registerAction(MSG_BOTTOM_POPUP_HIDDEN);
        registerAction(MSG_SHOE_GAME_ICON);
        registerAction(MSG_ON_MENU_PANEL_HIDDEN);
        registerAction(MSG_SHOW_BIG_TURN_TABLE_BTN);
        registerAction(MSG_HIDE_BIG_TURN_TABLE_BTN);
        EventBus.getDefault().register(this);

        syncUnreadCount();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        mView.destroyView();
    }

    @Override
    public void showInputView() {
        postEvent(MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showGiftView() {
        if (AccountAuthManager.triggerActionNeedAccount(mView.getRealView().getContext())) {
            EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(
                    GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST));
        }
    }

    @Override
    public void rotateScreen() {
        // TODO 增加转屏处理 YangLi
    }

    @Override
    public void processMoreBtnShow() {
        // 判断是否支持分享和关系链
//        if (mMyRoomData.getEnableShare()) {
//            mView.showMoreBtn();
//        } else {
//            Observable.create(new Observable.OnSubscribe<FansGroupListModel>() {
//                @Override
//                public void call(Subscriber<? super FansGroupListModel> subscriber) {
//                    VFansProto.GetGroupListRsp rsp = new GetGroupListRequest().syncRsp();
//                    if (rsp != null) {
//                        subscriber.onNext(new FansGroupListModel(rsp));
////                        subscriber.onNext(null);
//                    } else {
//                        subscriber.onNext(null);
//                    }
//                    subscriber.onCompleted();
//                }
//            })
//                    .subscribeOn(Schedulers.io())
//                    .compose(this.<FansGroupListModel>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Action1<FansGroupListModel>() {
//                        @Override
//                        public void call(FansGroupListModel model) {
//                            MyLog.d(TAG, "get fans group success");
//                            if (mView != null) {
//                                MyLog.d(TAG, "has group info=" + model.hasGroup());
//                                if (model.hasGroup()) {
//                                    mView.showMoreBtn();
//                                    return;
//                                }
//                            }
//                        }
//                    }, new Action1<Throwable>() {
//                        @Override
//                        public void call(Throwable throwable) {
//                            MyLog.e(TAG, throwable);
//                        }
//                    });
//        }

        //Todo-暂时先改成下面这个样子
        mView.showMoreBtn();
    }

    @Override
    public void onFastGiftClick() {
        if(!TextUtils.isEmpty(mWidgetLinkUrl)) {
            SchemeSdkActivity.openActivity((Activity) mView.getRealView().getContext(), Uri.parse(mWidgetLinkUrl));
        } else {
            if(mFastGiftId > 0) {
                sendFastGift(mFastGiftId);
            }
        }
    }

    @Override
    public void onBigTurnTableClick() {
        postEvent(MSG_SHOW_BIG_TURN_TABLE_PANEL);
    }

    private void sendFastGift(final int giftId) {
        //保存此次请求发出时间
        //保存此次请求continuId
        if (mRequestContinueId == -1) {
            mRequestContinueId = System.currentTimeMillis();
        }
        mRequestTimes++;

        if(mClesrFastGiftFlagSubscriber != null) {
            mClesrFastGiftFlagSubscriber.unsubscribe();
        }

        final Gift gift = GiftRepository.findGiftById(giftId);
        Observable.create(new Observable.OnSubscribe<GiftProto.BuyGiftRsp>() {
            @Override
            public void call(Subscriber<? super GiftProto.BuyGiftRsp> subscriber) {
                if(gift == null) {
                    subscriber.onError(new Exception("gift == null"));
                    subscriber.onCompleted();
                    return;
                }

                GiftProto.BuyGiftRsp rsp;
                rsp = GiftRepository.bugGiftSync(gift, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mRequestTimes, System.currentTimeMillis()
                        , mRequestContinueId, null, mMyRoomData.getLiveType(), false, mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);

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
                        mRequestContinueId = -1;
                    }

                    @Override
                    public void onNext(GiftProto.BuyGiftRsp rsp) {
                        if(rsp.getRetCode() == GiftErrorCode.SUCC) {
                            BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(giftId, gift.getName(), gift.getCatagory(),
                                    gift.getSendDescribe(), mRequestTimes, rsp.getReceiverTotalTickets(),
                                    rsp.getTicketUpdateTime(), mRequestContinueId, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), rsp.getRedPacketId(),
                                    "", 0, false, 1, "");
                            BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);

                            mView.startFastGiftPBarAnim();
                            tryClearSendGiftFlag();
                        } else if(rsp.getRetCode() == GiftErrorCode.GIFT_PAY_BARRAGE) {
                            postEvent(MSG_POP_INSUFFICIENT_TIPS);
                        } else {
                            ToastUtils.showToast(mView.getRealView().getContext(), GlobalData.app().getResources().getString(R.string.buy_gift_failed_with_err_code) + rsp.getRetCode());
                        }
                    }
                });
    }

    private void tryClearSendGiftFlag() {
        mClesrFastGiftFlagSubscriber = Observable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        mRequestTimes = 0;
                        mRequestContinueId = -1;
                    }
                });
    }

    @Override
    public void showGameDownloadView() {
        postEvent(MSG_SHOW_GAME_DOWNLOAD);
    }

    @Override
    public void showWatchMenuPanel(int unReadCnt) {
        postEvent(MSG_SHOW_MENU_PANEL, new Params().putItem(unReadCnt));
    }

    @Override
    public void showMyInfoPannel() {
        MyInfoHalfFragment.openFragment((BaseSdkActivity) mView.getRealView().getContext());
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
        if (event == null || mView == null) {
            return;
        }

        this.mWidgetLinkUrl = event.linkUrl;
        this.mFastGiftId = event.giftId;
        if(!TextUtils.isEmpty(mWidgetLinkUrl)) {
            mView.setFastGift(event.widgetIcon, true);
        } else {
            Gift gift = GiftRepository.findGiftById(this.mFastGiftId);
            mView.setFastGift(gift != null ? gift.getPicture() : ""
                    , gift != null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(com.wali.live.event.EventClass.RechargeCheckOrderEvent event) {//支付成功需要刷新首充按钮和续费有礼
        MyLog.w(TAG, "onEvent RechargeCheckOrderEvent");
        if(!TextUtils.isEmpty(mWidgetLinkUrl)) {
            onEvent(new EventClass.UpdateFastGiftInfoEvent(mFastGiftId, null, null));
        }
    }

    //显示大转盘按钮event
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TurnTableEvent event) {
        if(event == null) {
            return;
        }

        MyLog.d(TAG, "TurnTableEvent");
        postEvent(MSG_SHOW_BIG_TURN_TABLE_BTN, new Params().putItem(event.getData()));
    }

    // TODO-YangLi 相同代码，可以考虑抽取基类
    private Subscription mSubscription;

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
            case MSG_BOTTOM_POPUP_SHOWED:
                mView.getRealView().setVisibility(View.GONE);
                return true;
            case MSG_BOTTOM_POPUP_HIDDEN:
                mView.getRealView().setVisibility(View.VISIBLE);
                return true;
            case MSG_SHOE_GAME_ICON:
                mView.showGameIcon((GameViewModel) params.getItem(0));
                return true;
            case MSG_ON_MENU_PANEL_HIDDEN:
                mView.updateMoreBtnStatus();
                break;
            case MSG_SHOW_BIG_TURN_TABLE_BTN:
                mView.showBigTurnTable();
                break;
            case MSG_HIDE_BIG_TURN_TABLE_BTN:
                mView.hideBigTurnTable();
                break;
            default:
                break;
        }
        return false;
    }
}

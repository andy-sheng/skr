package com.wali.live.common.gift.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import com.base.activity.BaseRotateSdkActivity;
import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.network.NetworkUtils;
import com.base.utils.rx.RefuseRetryExeption;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.BuyGiftType;
import com.mi.live.data.gift.model.GiftCard;
import com.mi.live.data.gift.model.GiftInfoForThisRoom;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.gift.model.giftEntity.PeckOfGift;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.gift.exception.GiftErrorCode;
import com.wali.live.common.gift.exception.GiftException;
import com.wali.live.common.gift.view.GiftDisPlayItemView;
import com.wali.live.common.gift.view.GiftMallView;
import com.wali.live.component.ComponentController;
import com.wali.live.dao.Gift;
import com.wali.live.proto.GiftProto;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zjn on 16-11-30.
 */
public class GiftMallPresenter implements IBindActivityLIfeCycle {
    public static final String TAG = "GiftMallPresenter";

    private RoomBaseDataModel mMyRoomData; // 主播id
    private Activity mActivity;
    @Nullable
    private ComponentController mComponentController;

    private ViewStub mGiftMallViewStub;
    private GiftMallView mGiftMallView;

    private boolean mIsLandscape = false;// 是否是横屏

    private GiftInfoForThisRoom mGiftInfoForThisRoom;
    private PayProto.GiftCardPush mGiftCardPush;

    private Context mContext;

    private boolean mHasLoadData = false;// 是否加载数据

    private Gift mRandomGift = null;

    private volatile long continueId = System.currentTimeMillis();

    private ContinueSendNumber mContinueSend = new ContinueSendNumber(); // 连送次数

    private ExecutorService singleThreadForBuyGift = Executors.newSingleThreadExecutor(); // 送礼的线程池

    private Subscription mSountDownSubscription; // 倒计时的订阅
    //房间花费星票数，结束页显示
    private int mSpendTicket = 0;

    public GiftMallPresenter(
            Activity activity,
            Context baseContext,
            RoomBaseDataModel myRoomData,
            @Nullable ComponentController componentController) {
        mMyRoomData = myRoomData;
        mActivity = activity;
        mContext = baseContext;
        mComponentController = componentController;
    }

    public int getSpendTicket() {
        return mSpendTicket;
    }

    public void setViewStub(ViewStub viewStub) {
        mGiftMallViewStub = viewStub;
    }

    public void showGiftMallView() {
        if (mGiftMallView == null) {
            inflateGiftMallView();
        } else {
            toShowGiftMallView();
        }
    }

    public void hideGiftMallView() {
        if (mGiftMallView == null) {
            return;
        }
        if (mGiftMallView.getAnimation() != null) {
            mGiftMallView.getAnimation().cancel();
        }

        MyLog.d(TAG, "hideGiftMallView");
        if (mGiftMallView.getVisibility() == View.VISIBLE) {
            mGiftMallView.setVisibility(View.GONE);
            if (mComponentController != null) {
                mComponentController.onEvent(ComponentController.MSG_BOTTOM_POPUP_HIDDEN);
            }
        }
    }

    public void selectGiftView(int id) {
        mGiftMallView.selectGiftViewById(id);
    }

    public boolean isGiftMallViewVisibility() {
        if (mGiftMallView != null && mGiftMallView.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public void setGiftInfoForEnterRoom(GiftInfoForThisRoom giftInfoForThisRoom) {
        mGiftInfoForThisRoom = giftInfoForThisRoom;
        if (isGiftMallViewVisibility()) {
            loadDataFromCache("setGiftInfoForEnterRoom");
        } else {
            // 不可见，只设立标记位，延迟加载
            mHasLoadData = false;
        }
    }

    public void resetRandomGift() {
        mRandomGift = null;
    }

    public void buyGift() {
        final long timestamp = System.currentTimeMillis();
        //
        final GiftMallPresenter.GiftWithCard buyGiftWithCard = mGiftMallView.getSelectedGift();
        final GiftDisPlayItemView giftDisPlayItemView = mGiftMallView.getSelectedView();
//TODO 一定记得去掉
//TODO test-only-begin
//        if (true && Constants.isDebugOrTestBuild) {
//            final Gift buyGift = buyGiftWithCard.gift;
//            Observable.just(buyGift)
//                    .observeOn(Schedulers.from(singleThreadForBuyGift))
//                    .flatMap(new Func1<Gift, Observable<?>>() {
//                        @Override
//                        public Observable<?> call(Gift gift) {
//                            {
//                                BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(buyGift.getGiftId(), buyGift.getName(), buyGift.getCatagory(),
//                                        "我送了" + buyGift.getName(), mContinueSend.get(), 0,
//                                        0, continueId, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), "111", "", 0, false);
//                                BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
//                            }
//                            mContinueSend.add();
//                            return Observable.just(null);
//                        }
//                    })
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .compose(getRxActivity().bindUntilEvent(ActivityEvent.DESTROY))
//                    .subscribe(new Action1<Object>() {
//                        @Override
//                        public void call(Object o) {
//                            if (mSountDownSubscription != null) {
//                                mSountDownSubscription.unsubscribe();
//                            }
//                            if (buyGift.getCanContinuous()) {
//                                mSountDownSubscription = mGiftMallView.countDown();
//                                giftDisPlayItemView.setContinueSendGiftNum(mContinueSend.get() - 1);
//                            }
//                            giftDisPlayItemView.setDataSource(buyGiftWithCard);
//                        }
//                    });
//            return;
//        }
        final Boolean[] useGiftCard = {false};
        final Gift[] sendGift = {null};
        //TODO test-only-end
        if (buyGiftWithCard == null) {
            return;
        }

        /**
         * 这个礼物送出的时间，根据送出的时间去判断要不要去改mContinueSend的num，
         * 因为可能第一个连送的最一个（或n个）还没有返回response的时候下一个连送已经开始
         * 这个时候会影响continueId和连送num的值
         */

        final Long[] requestTime = {System.currentTimeMillis()};
        /**
         * 记录这个连送的continueId,因为这个请求回来的时候全局的continueId可能已经改变了
         */
        final Long[] requestContinueId = new Long[1];

        Observable.just(buyGiftWithCard.gift)
                .flatMap(new Func1<Gift, Observable<Gift>>() {
                    @Override
                    public Observable<Gift> call(Gift gift) {
                        if (gift == null) {
                            return Observable.error(new GiftException(mContext.getString(R.string.no_gift_selected)));
                        }

                        if (buyGiftWithCard.card == null || buyGiftWithCard.card.getGiftCardCount() <= 0) {
                            if (gift.getCatagory() == GiftType.PRIVILEGE_GIFT && gift.getLowerLimitLevel() > MyUserInfoManager.getInstance().getUser().getLevel()) {
                                //特权礼物
                                return Observable.error(new GiftException(mContext.getResources().getQuantityString(R.plurals.verify_user_level_toast,
                                        gift.getLowerLimitLevel(), gift.getLowerLimitLevel())));
                            } else if ((gift.getCatagory() == GiftType.Mi_COIN_GIFT && (gift.getPrice() / 10) > getCurrentTotalBalance()) ||
                                    (gift.getCatagory() != GiftType.Mi_COIN_GIFT && gift.getPrice() > getCurrentTotalBalance())) {
                                return Observable.error(new GiftException(GiftErrorCode.GIFT_INSUFFICIENT_BALANCE, mContext.getString(R.string.insufficient_balance)));
                            }
                        }
                        switch (gift.getCatagory()) {
                            case GiftType.PECK_OF_GIFT: {
                                if (!mGiftMallView.getIsContinueSendFlag()) {
                                    mGiftMallView.setIsContinueSendFlag(true);
                                    sendGift[0] = getRandomPeckOfGift((PeckOfGift) gift);
                                    mRandomGift = sendGift[0];
                                } else {
                                    sendGift[0] = mRandomGift;
                                }
                                if (sendGift[0] == null) {
                                    mGiftMallView.setIsContinueSendFlag(false);
                                    return Observable.error(new GiftException(mContext.getString(R.string.old_gift_version)));
                                }
                                return Observable.just(sendGift[0]);
                            }
                            case GiftType.NORMAL_GIFT:
                            case GiftType.BARRAGE_GIFT:
                            case GiftType.RED_ENVELOPE_GIFT:
                            case GiftType.ROOM_BACKGROUND_GIFT:
                            case GiftType.LIGHT_UP_GIFT:
                            case GiftType.GLOBAL_GIFT:
                            case GiftType.NORMAL_EFFECTS_GIFT:
                            case GiftType.HIGH_VALUE_GIFT:
                            case GiftType.BIG_PACK_OF_GIFT:
                            case GiftType.Mi_COIN_GIFT:
                            case GiftType.PRIVILEGE_GIFT: {
                                return Observable.just(gift);
                            }
                            default: {
                                return Observable.error(new GiftException(mContext.getString(R.string.old_gift_version)));
                            }
                        }
                    }
                })
                .flatMap(new Func1<Gift, Observable<Gift>>() {
                    @Override
                    public Observable<Gift> call(Gift gift) {
                        if (!NetworkUtils.hasNetwork(mContext)) {
                            return Observable.error(new RefuseRetryExeption(mContext.getString(R.string.network_disable)));
                        }

                        return Observable.just(gift);
                    }
                })
                .map(new Func1<Gift, GiftProto.BuyGiftRsp>() {
                    @Override
                    public GiftProto.BuyGiftRsp call(Gift gift) {
                        if (mContinueSend.get() == 1) {
                            continueId = System.currentTimeMillis();
                            //设置最新continueId
                            mContinueSend.setContinueId(continueId);
                        }
                        useGiftCard[0] = buyGiftWithCard.canUseCard();
                        //保存此次请求发出时间
                        requestTime[0] = System.currentTimeMillis();
                        //保存此次请求continuId
                        requestContinueId[0] = continueId;

                        if (buyGiftWithCard.gift.getCatagory() == GiftType.Mi_COIN_GIFT || gift.getBuyType() == BuyGiftType.BUY_GAME_ROOM_GIFT) {
                            return GiftRepository.bugGiftSync(gift, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mContinueSend.get(requestContinueId[0]), timestamp, requestContinueId[0], null, mRoomType, useGiftCard[0], true);
                        } else {
                            return GiftRepository.bugGiftSync(gift, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mContinueSend.get(requestContinueId[0]), timestamp, requestContinueId[0], null, mRoomType, useGiftCard[0], false);
                        }
                    }
                })
                .flatMap(new Func1<GiftProto.BuyGiftRsp, Observable<GiftProto.BuyGiftRsp>>() {
                    @Override
                    public Observable<GiftProto.BuyGiftRsp> call(GiftProto.BuyGiftRsp buyGiftRsp) {
                        if (buyGiftRsp == null) {
                            return Observable.error(new GiftException(mContext.getString(R.string.send_gift_failed)));
                        }
                        int code = buyGiftRsp.getRetCode();
                        String retMsg = buyGiftRsp.getRetMsg();
                        if (code != GiftErrorCode.SUCC) {
                            if (!TextUtils.isEmpty(retMsg)) {
                                return Observable.error(new GiftException(retMsg));
                            } else {
                                switch (code) {
                                    case GiftErrorCode.GIFT_NOT_EXIST: {
                                        GiftRepository.syncGiftList();
                                        return Observable.error(new GiftException(GiftErrorCode.GIFT_NOT_EXIST, mContext.getString(R.string.gift_out_date)));
                                    }
                                    case GiftErrorCode.GIFT_CARD_INSUFFICIENT: {
                                        //礼物卡数量不足
                                        if (buyGiftWithCard.card != null) {
                                            buyGiftWithCard.card.setGiftCardCount(0);
                                        }
                                        return Observable.error(new GiftException(GiftErrorCode.GIFT_CARD_INSUFFICIENT, mContext.getString(R.string.gift_card_insufficient)));
                                    }
                                    case GiftErrorCode.CODE_MIBI_INSUFFICIENT: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_MIBI_INSUFFICIENT, mContext.getString(R.string.mi_coin_insufficient_balance)));
                                    }
                                    case GiftErrorCode.CODE_MIBI_CONSUME_TIMEOUT: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_MIBI_CONSUME_TIMEOUT, mContext.getString(R.string.mi_coin_consume_timeout)));
                                    }
                                    case GiftErrorCode.CODE_NOT_MIBI_USER: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_NOT_MIBI_USER, mContext.getString(R.string.not_mi_coin_user)));
                                    }
                                    case GiftErrorCode.CODE_RISK_CONTROL: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_RISK_CONTROL, mContext.getString(R.string.mi_coin_risk_control)));
                                    }
                                    case GiftErrorCode.CODE_MIBI_ACCOUNT_FROZEN: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_MIBI_ACCOUNT_FROZEN, mContext.getString(R.string.mi_coin_account_frozen)));
                                    }
                                    case GiftErrorCode.CODE_MIBI_CONSUME_ERROR: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_MIBI_CONSUME_ERROR, mContext.getString(R.string.mi_coin_other_consume_error)));
                                    }
                                    case GiftErrorCode.CODE_LOW_LEVEL_ERROR: {
                                        return Observable.error(new GiftException(GiftErrorCode.CODE_MIBI_CONSUME_ERROR, mContext.getString(R.string.user_level_too_low_cant_buy)));
                                    }
                                }
                                return Observable.error(new GiftException(mContext.getString(R.string.buy_gift_failed_with_err_code) + code));
                            }
                        }
                        Gift buyGift = buyGiftWithCard.gift;
                        if (buyGift.getCatagory() == GiftType.PECK_OF_GIFT) {
                            buyGift = sendGift[0];
                        }
                        BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(buyGift.getGiftId(), buyGift.getName(), buyGift.getCatagory(),
                                buyGift.getSendDescribe(), mContinueSend.get(requestContinueId[0]), buyGiftRsp.getReceiverTotalTickets(),
                                buyGiftRsp.getTicketUpdateTime(), requestContinueId[0], mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), buyGiftRsp.getRedPacketId(), "", 0, false);
                        BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
                        mContinueSend.add(requestTime[0], requestContinueId[0]);

                        // 更新card
                        List<GiftProto.VGiftCard> giftCardListList = buyGiftRsp.getGiftCardListList();
                        if (giftCardListList != null) {
                            for (GiftProto.VGiftCard card : giftCardListList) {
                                if (card.getGiftId() == buyGift.getGiftId()) {
                                    if (buyGiftWithCard.card != null) {
                                        buyGiftWithCard.card.setGiftCardCount(card.getGiftCardCnt());
                                    }
                                    break;
                                }
                            }
                        }
                        return Observable.just(buyGiftRsp);
                    }
                })
                .subscribeOn(Schedulers.from(singleThreadForBuyGift))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<GiftProto.BuyGiftRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<GiftProto.BuyGiftRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.v(TAG, "sendGift onCompleted " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof GiftException) {
                            String msg = e.getMessage();
                            MyLog.w(TAG, "buy gift error:" + msg);
                            if (!TextUtils.isEmpty(msg)) {
                                if (((GiftException) e).errCode == GiftErrorCode.GIFT_INSUFFICIENT_BALANCE) {
//                                    mGiftMallView.showInsufficientBalanceTips();
                                    EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
                                } else {
                                    ToastUtils.showToast(mContext, msg);
                                }
                            }
                            switch (((GiftException) e).errCode) {
                                case GiftErrorCode.GIFT_CARD_INSUFFICIENT: {
                                    giftDisPlayItemView.setDataSource(buyGiftWithCard);
                                }
                                break;
                            }
                        } else {
                            if (Constants.isDebugOrTestBuild) {
                                ToastUtils.showToast(mContext, e.getMessage());
                            }
                        }

                        if (giftDisPlayItemView.isContinueSendBtnShow()) {
                            if (mGiftMallView.getIsBigGiftBtnShowFlag()) {
                                return;
                            }
                            giftDisPlayItemView.hideContinueSendBtn();
                            giftDisPlayItemView.changeCornerStatus(buyGiftWithCard.gift.getIcon(), false);
                        }

                        mGiftMallView.setIsContinueSendFlag(false);
                    }

                    @Override
                    public void onNext(GiftProto.BuyGiftRsp buyGiftRsp) {
                        //TODO 主播的票数也从这获得
                        MyLog.v(TAG, "sendGift onNext " + Thread.currentThread().getName());
                        MyLog.w(TAG, "buyGiftRsp:" + buyGiftRsp.toString());
                        if (buyGiftWithCard.gift.getCatagory() == GiftType.Mi_COIN_GIFT
                                || buyGiftWithCard.gift.getBuyType() == BuyGiftType.BUY_GAME_ROOM_GIFT) {
                            mSpendTicket += (int) ((float) buyGiftWithCard.gift.getPrice() / 10f);
                        } else {
                            mSpendTicket += buyGiftWithCard.gift.getPrice();
                        }

                        //扣钱
                        int deduct = buyGiftRsp.getUsableGemCnt();
                        int virtualGemCnt = buyGiftRsp.getUsableVirtualGemCnt();
                        updateUserAsset(deduct, virtualGemCnt, null, buyGiftRsp.getUserAssetTimestamp());
                        unsubscribeSountDownSubscription();

                        if (mGiftMallView.getIsBuyGiftBySendBtn()) {
                            if (buyGiftWithCard.gift.getCanContinuous()) {
                                /**
                                 * 如果当前continueId不是请求时候的continueId，则不不去更新连送按钮的数据，
                                 * 只有最新的连送才需要更新连送数据
                                 */
                                if (requestContinueId[0] == mContinueSend.getCurrentContinueId()) {
                                    mGiftMallView.setContinueSendBtnNum(mContinueSend.get(requestContinueId[0]) - 1);
                                }
                                mSountDownSubscription = mGiftMallView.countDown();
                            } else {
                                giftDisPlayItemView.hideContinueSendBtn();
                                giftDisPlayItemView.changeCornerStatus(buyGiftWithCard.gift.getIcon(), false);
                                mGiftMallView.setIsBigGiftBtnShowFlag(false);
                            }
                        } else {
                            if (buyGiftWithCard.gift.getCanContinuous()) {
                                unsubscribeSountDownSubscription();

                                mSountDownSubscription = mGiftMallView.countDown();
                            } else {
                                giftDisPlayItemView.showContinueSendBtn(false);
                                giftDisPlayItemView.changeCornerStatus(buyGiftWithCard.gift.getIcon(), true);
                            }

                            /**
                             * 如果当前continueId不是请求时候的continueId，则不需要去更新橱窗的连送数据，
                             * 应该是最新的连送才会显示连送数据 （现在橱窗显示的x5,表示连送5个）
                             */
                            if (requestContinueId[0] == mContinueSend.getCurrentContinueId()) {
                                giftDisPlayItemView.setContinueSendGiftNum(mContinueSend.get(requestContinueId[0]) - 1);
                            }
                        }

                        if (useGiftCard[0]) {
                            //获取礼物特效时候加载数据
                            giftDisPlayItemView.setDataSource(buyGiftWithCard);
                        }
                    }
                });

    }

    public int getCurrentTotalBalance() {
        return (MyUserInfoManager.getInstance().getUser().getDiamondNum() + MyUserInfoManager.getInstance().getUser().getVirtualDiamondNum());
    }

    public void unsubscribeSountDownSubscription() {
        if (mSountDownSubscription != null) {
            mSountDownSubscription.unsubscribe();
        }
    }

    Subscription mLoadDataSubscription;

    public synchronized void loadDataFromCache(String from) {
        MyLog.d(TAG, "loadDataFromCache from:" + from);
        if (mLoadDataSubscription != null && !mLoadDataSubscription.isUnsubscribed()) {
            return;
        }
        if (mGiftMallView == null) {
            return;
        }
        if (!mIsLandscape) {
            final List<List<GiftMallPresenter.GiftWithCard>> dataSourceList = new ArrayList<>(); // 数据源
            mLoadDataSubscription = dataSource()
                    .buffer(8)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(getRxActivity().<List<GiftWithCard>>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new Observer<List<GiftWithCard>>() {
                        @Override
                        public void onCompleted() {
                            MyLog.d(TAG, "onCompleted:" + dataSourceList.size());
                            if (mIsLandscape) {
                                mLoadDataSubscription = null;
                                loadDataFromCache("onCompleted orient1");
                                return;
                            }
                            mGiftMallView.setGiftDisplayViewPagerAdapterDataSource(dataSourceList);
                            mGiftMallView.setGiftListErrorViewVisibility(true);
                            mHasLoadData = true;

                            if (mGiftMallView.getSelectGiftViewByGiftId() != null) {
                                mGiftMallView.getSelectGiftViewByGiftId().select();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mGiftMallView.setGiftListErrorViewVisibility(false);
                        }

                        @Override
                        public void onNext(List<GiftMallPresenter.GiftWithCard> giftInfos) {
                            MyLog.d(TAG, "onNext" + giftInfos.toString());
                            dataSourceList.add(giftInfos);
                        }
                    });
        } else {
            final List<GiftMallPresenter.GiftWithCard> dataList = new ArrayList<>();
            mLoadDataSubscription = dataSource()
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(getRxActivity().<GiftMallPresenter.GiftWithCard>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new Observer<GiftMallPresenter.GiftWithCard>() {
                        @Override
                        public void onCompleted() {
                            if (!mIsLandscape) {
                                mLoadDataSubscription = null;
                                loadDataFromCache("onCompleted orient2");
                                return;
                            }
                            if (mGiftMallView.setGiftDisplayRecycleViewAdapterDataSource(dataList)) {
                                mHasLoadData = true;

                                if (mGiftMallView.getSelectGiftViewByGiftId() != null) {
                                    mGiftMallView.getSelectGiftViewByGiftId().select();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(GiftMallPresenter.GiftWithCard gift) {
                            dataList.add(gift);
                        }
                    });
        }
    }

    public void resetContinueSend() {
        mContinueSend.reset();
    }

    private Observable<GiftWithCard> dataSource() {
//        final boolean[] isHasLoadingMiCoinFirstTime = {false};
        return Observable.just(GiftRepository.getGiftListCache())
                .flatMap(new Func1<List<Gift>, Observable<List<Gift>>>() {
                    @Override
                    public Observable<List<Gift>> call(List<Gift> giftInfos) {
                        if (giftInfos.isEmpty()) {
                            return Observable.error(new Exception(mContext.getString(R.string.get_gift_list_fail)));
                        }
                        return Observable.just(giftInfos);
                    }
                })
                .flatMap(new Func1<List<Gift>, Observable<Gift>>() {
                    @Override
                    public Observable<Gift> call(List<Gift> giftInfos) {
                        Collections.sort(giftInfos, new Comparator<Gift>() {
                            @Override
                            public int compare(Gift gift1, Gift gift2) {
                                return gift1.getSortId() - gift2.getSortId();
                            }
                        });
                        return Observable.from(giftInfos);
                    }
                })
                .filter(new Func1<Gift, Boolean>() {
                    @Override
                    public Boolean call(Gift gift) {
                        MyLog.d(TAG, "dataSourceGiftId:" + gift.toString());

                        if (mGiftInfoForThisRoom != null && mGiftInfoForThisRoom.enable()) {
                            return mGiftInfoForThisRoom.needShow(gift.getGiftId());
                        }
                        return gift.getCanSale();
                    }
                })
                //仅仅在watchsdkLite上去除红包入口
                .filter(new Func1<Gift, Boolean>() {
                    @Override
                    public Boolean call(Gift gift) {
//                        if (gift.getCatagory() == GiftType.RED_ENVELOPE_GIFT) {
//                            return false;
//                        }
                        return true;
                    }
                })
                .map(new Func1<Gift, GiftWithCard>() {
                    @Override
                    public GiftWithCard call(Gift gift) {
                        GiftWithCard giftWithCard = new GiftWithCard();
                        giftWithCard.gift = gift;

                        //判断当前是否有米币礼物
                        MyLog.d(TAG, "getMiCoinFlag:" + gift.getGiftId());

                        if (mGiftInfoForThisRoom != null) {
                            giftWithCard.card = mGiftInfoForThisRoom.getGiftCardById(gift.getGiftId());
                        }
                        return giftWithCard;
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    private long userAssetTs = 0;

    private void updateUserAsset(int deduct, int virtualGemCnt, List<GiftCard> giftCardList, long ts) {
        if (ts > userAssetTs) {
            userAssetTs = ts;
            MyUserInfoManager.getInstance().setDiamonds(deduct, virtualGemCnt);
            if (giftCardList != null) {
                if (mGiftInfoForThisRoom == null) {
                    mGiftInfoForThisRoom = new GiftInfoForThisRoom();
                }
                mGiftInfoForThisRoom.updateGiftCard(giftCardList, userAssetTs);
                if (isGiftMallViewVisibility()) {
                    loadDataFromCache("updateUserAsset");
                } else {
                    // 不可见，只设立标记位，延迟加载
                    mHasLoadData = false;
                }
            } else {
                MyLog.d(TAG, "updateUserAsset giftCardList==null");
            }
        } else {
            MyLog.d(TAG, "not larger");
        }
    }

    /**
     * 彩蛋礼物
     * 1.选取礼物-根据概率
     * 2.连击按钮时播放上次选取的礼物
     *
     * @param peckGift
     * @return
     */
    private Gift getRandomPeckOfGift(PeckOfGift peckGift) {
        List<PeckOfGift.PeckOfGiftInfo> peckOfGiftInfoList = peckGift.getPeckOfGiftInfoList();
        if (peckOfGiftInfoList == null || peckOfGiftInfoList.isEmpty()) {
            return null;
        }

        //服务器返回概率如:10 20等
        //Test
//        for(int i = 0; i < giftIdList.size(); i++) {
//            giftProbabilityMap.put(giftIdList.get(i), 30);
//        }

        int denominator = 0;
        for (int i = 0; i < peckOfGiftInfoList.size(); i++) {
            PeckOfGift.PeckOfGiftInfo peckOfGiftInfo = peckOfGiftInfoList.get(i);
            MyLog.d(TAG, "giftId:" + peckOfGiftInfo.getGiftId() + "giftProbability:" + peckOfGiftInfo.getProbability());
            denominator += peckOfGiftInfo.getProbability();
        }

        List<Integer> list = new ArrayList<>();
        int index;
        Gift randomGift;
        do {
            index = getRandomGiftIndex(denominator, peckOfGiftInfoList);
            if (list.contains(index)) {
                index = getRandomGiftIndex(denominator, peckOfGiftInfoList);
                randomGift = GiftRepository.findGiftById(peckOfGiftInfoList.get(index).getGiftId());
                if (randomGift != null && randomGift.getCatagory() == GiftType.NORMAL_GIFT) {
                    MyLog.d(TAG, "randomGift.toString():" + randomGift.toString());
                    return randomGift;
                }
                continue;
            }
            list.add(index);
            randomGift = GiftRepository.findGiftById(peckOfGiftInfoList.get(index).getGiftId());
            if (randomGift != null && randomGift.getCatagory() == GiftType.NORMAL_GIFT) {
                return randomGift;
            }
        }
        while (GiftRepository.checkOneAnimationRes(randomGift) == null && list.size() < peckOfGiftInfoList.size());
        if (list.size() >= peckOfGiftInfoList.size()) {
            return null;
        }
        return randomGift;
    }

    private int getRandomGiftIndex(int length, List<PeckOfGift.PeckOfGiftInfo> peckOfGiftInfoList) {
        double v = Math.random() * length;
        int index = (int) v;

        int currentDenominator = 0;
        int randomIndex = 0;
        for (int i = 0; i < peckOfGiftInfoList.size(); i++) {
            if (index <= (currentDenominator + peckOfGiftInfoList.get(i).getProbability())) {
                randomIndex = i;
                break;
            } else {
                currentDenominator += peckOfGiftInfoList.get(i).getProbability();
            }
        }

        return randomIndex;
    }

    private RxActivity getRxActivity() {
        return (RxActivity) mActivity;
    }

    private void inflateGiftMallView() {
        View root = mGiftMallViewStub.inflate();
        mGiftMallView = (GiftMallView) root.findViewById(R.id.gift_mall_view);
        toShowGiftMallView();

        mGiftMallView.firstInflateGiftMallView(this, mActivity, mMyRoomData, mIsLandscape);
        mGiftMallViewStub = null;
    }

    private void toShowGiftMallView() {
        if (mGiftMallView.getAnimation() != null) {
            mGiftMallView.getAnimation().cancel();
        }

        MyLog.d(TAG, "showGiftMallView");
        if (mGiftMallView.getVisibility() != View.VISIBLE) {
            mGiftMallView.setVisibility(View.VISIBLE);
            if (mComponentController != null) {
                mComponentController.onEvent(ComponentController.MSG_BOTTOM_POPUP_SHOWED);
            }
        }
    }

    private Subscription mSyncBalanceSubscription;

    private Subscription mSyncMiCoinSubscription;

    private void syncBalance() {

        if (mSyncBalanceSubscription != null && !mSyncBalanceSubscription.isUnsubscribed()) {
            // 仍在运行
            MyLog.d(TAG, "syncBalance cancel");
            return;
        }
        MyLog.d(TAG, "syncBalance...");
        mSyncBalanceSubscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                MyUserInfoManager.getInstance().syncSelfDetailInfo();
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
//                        if (!mIsLandscape) {
//                            mRefreshRechargeIv.stopRotate(1000);
//                        }
                    }

                    @Override
                    public void onError(Throwable e) {
//                        if (!mIsLandscape) {
//                            mRefreshRechargeIv.stopRotate(1000);
//                        }
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
    }

    /**
     * 目前主要用来切换房间时，重置内部状态
     */
    public void reset() {
        MyLog.w(TAG, "reset");
        mGiftInfoForThisRoom = null;
        mHasLoadData = false;
        if (mGiftMallView != null) {
            mGiftMallView.processSwitchAnchorEvent();
        }
    }

    /**
     * giftcard的push
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftCardPush event) {
        PayProto.GiftCardPush giftCardPush = (PayProto.GiftCardPush) event.obj1;
        mGiftCardPush = giftCardPush;
        MyLog.w(TAG, "giftCardPush:" + giftCardPush);
        updateUserAsset(giftCardPush.getAndUsableGemCnt(), giftCardPush.getUsableVirtualGemCnt(),
                GiftCard.convert(giftCardPush.getGiftCardsList()), giftCardPush.getUserAssetTimestamp());
    }
//
//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.SwitchAnchor event) {
//        mGiftInfoForThisRoom = null;
//        mHasLoadData = false;
//        MyLog.w(TAG, "SwitchAnchor");
//        if (mGiftMallView == null) {
//            return;
//        }
//        mGiftMallView.processSwitchAnchorEvent();
//    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftMallEvent event) {
        MyLog.d(TAG, "onEvent");
        switch (event.eventType) {
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_CACHE_CHANGE: {
                if (isGiftMallViewVisibility()) {
                    loadDataFromCache("EVENT_TYPE_GIFT_CACHE_CHANGE");
                }
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST: {
                if (mGiftMallView == null) {
                    return;
                }
                if (!mGiftMallView.getHasLoadViewFlag()) {
                    mGiftMallView.removeGiftMallView();
                    mGiftMallView.initGiftMallView(getRxActivity());
                }
                if (!mHasLoadData) {
                    loadDataFromCache("EVENT_TYPE_GIFT_SHOW_MALL_LIST");
                }
            }
            break;

            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST: {
                MyLog.d(TAG, "EVENT_TYPE_GIFT_HIDE_MALL_LIST");
                if (mGiftMallView == null) {
                    return;
                }

                //隐藏礼物橱窗时候清楚所有按钮状态
                mGiftMallView.resetGiftItemBtnInfo();
                mGiftMallView.hideInsufficientBalanceTips();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_CLICK_SELECT_GIFT: {
                if (mGiftMallView == null) {
                    return;
                }
                if (!mGiftMallView.getHasLoadViewFlag()) {
                    mGiftMallView.removeGiftMallView();
                    mGiftMallView.initGiftMallView(getRxActivity());
                }
                if (!mHasLoadData) {
                    loadDataFromCache("EVENT_TYPE_CLICK_SELECT_GIFT");
                }

            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        boolean isLandscape = false;
        if (event.orientation == BaseRotateSdkActivity.ORIENTATION_DEFAULT) {
            return;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED) {
            isLandscape = true;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_REVERSED) {
            isLandscape = false;
        }

        if (mIsLandscape != isLandscape) {
            mHasLoadData = false;

            mIsLandscape = isLandscape;
            if (mGiftMallView != null) {
                mGiftMallView.resetStatus();
                mGiftMallView.setOrientEventInfo(isLandscape);
            }
            if (isGiftMallViewVisibility()) {
                mGiftMallView.processOrientEvent(isLandscape);
                loadDataFromCache("OrientEvent");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        if (mGiftMallView != null) {
            MyLog.d(TAG, "onEventMainThread UserInfoEvent Change");
            mGiftMallView.setBalanceInfo();
        }
    }

    private int mRoomType = RoomBaseDataModel.SINGLE_MODEL;

//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.LiveStatusEvent event) {
//        switch (event.type) {
//            case EventClass.LiveStatusEvent.EVENT_TYPE_TO_SINGLE_STATUS: {
//                mRoomType = RoomBaseDataModel.SINGLE_MODEL;
//            }
//            break;
//            case EventClass.LiveStatusEvent.EVENT_TYPE_TO_PK_STATUS: {
//                mRoomType = RoomBaseDataModel.PK_MODEL;
//            }
//            break;
//        }
//    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);
        if (singleThreadForBuyGift != null) {
            singleThreadForBuyGift.shutdown();
        }

        if (mGiftMallView != null) {
            mGiftMallView.onActivityDestroy();
            mGiftMallView = null;
        }
    }

    @Override
    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    //支持上下滑動的時候重置接口需要調用
    public void resetTicket() {
        mSpendTicket = 0;
    }

    public void showSendEnvelopeView() {
        if (mComponentController != null) {
            mComponentController.onEvent(ComponentController.MSG_SHOW_SEND_ENVELOPE);
        }
    }

    public static class GiftWithCard {
        public Gift gift;


        public GiftCard card;
        public static HashSet<Integer> hashSet = new HashSet<>();

        @Override
        public String toString() {
            return "GiftWithCard{" +
                    "giftId=" + gift.getGiftId() + ",name" + gift.getName() +
                    ", card=" + card +
                    '}';
        }

        public boolean canUseCard() {
            return card != null && card.getGiftCardCount() > 0;
        }
    }


    static class ContinueSendNumber {
        HashMap<Long, Integer> mContinueMap = new HashMap();
        private volatile int num = 1;
        private long lastResetTime = System.currentTimeMillis();
        private long currentContinueId = 0l;

        //记录最后一个reset的时间，以防reset完之后有上一个reset的数据更改num
        public synchronized void reset() {
            //目前之保存最近一次连送的信息
            mContinueMap.clear();
            if (currentContinueId != 0) {
                /**
                 * 有可能上一个连送的最后一个（或最后n个）还没返回，但新的连送已经开始，
                 * 所以需要分开两个连送数据，保存上一个连送的数据
                 */
                mContinueMap.put(currentContinueId, num);
            }

            lastResetTime = System.currentTimeMillis();

            /**
             * 设置一个无意义数字，表示已经从新开始连送，但是还没有设置continueId，
             * 以防在礼物橱窗的错误的位置显示连送的标，因为这个很可能是上一个连送的数了
             */
            currentContinueId = -1;
            num = 1;
        }

        public synchronized void setContinueId(long continueId) {
            currentContinueId = continueId;
        }

        public synchronized long getCurrentContinueId() {
            return currentContinueId;
        }

        public synchronized void add(long requestTime, long continueId) {
            if (requestTime > lastResetTime) {
                num++;
            } else {
                mContinueMap.put(continueId, mContinueMap.get(continueId) + 1);
            }
        }

        public synchronized int get() {
            return num;
        }

        public synchronized int get(long continueId) {
            /**
             * 有时候虽然已经reset了，但是新的continueId还没有设置，这个continueId
             * 的值可能在map里面
             */
            if (mContinueMap.containsKey(continueId)) {
                return mContinueMap.get(continueId);
            }

            return num;

        }
    }

}

package com.wali.live.watchsdk.bigturntable.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.mi.live.data.repository.model.turntable.TurnTablePreConfigModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.dao.Gift;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.bigturntable.api.BigTurnTableApi;
import com.wali.live.watchsdk.bigturntable.view.WatchBigTurnTablePanelView;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-7-11.
 */

public class WatchBigTurnTablePanelPresenter extends RxLifeCyclePresenter {
    private static final String TAG = "WatchBigTurnTablePanelPresenter";

    private Context mContext;

    //data
    private boolean mIsLandscape;
    private TurnTableConfigModel mDataSource;
    private RoomBaseDataModel mMyRoomInfo;

    //ui
    private WatchBigTurnTablePanelView mBigTurnTablePanelView;
    private ViewStub mViewStub;

    public WatchBigTurnTablePanelPresenter(Context context, boolean isLandscape, RoomBaseDataModel myRoomInfo) {
        this.mContext = context;
        this.mIsLandscape = isLandscape;
        this.mMyRoomInfo = myRoomInfo;
    }

    public void setViewStub(ViewStub viewStub) {
        mViewStub = viewStub;
    }

    public void showPanel() {
        if (mBigTurnTablePanelView == null) {
            inflate();
        }
        if (mIsLandscape) {
            mBigTurnTablePanelView.switchOrient(mIsLandscape);
        }
        mBigTurnTablePanelView.setVisibility(View.VISIBLE);
        loadData();
    }

    private long getZuid() {
        if (null != mMyRoomInfo.getUser()) {
            return mMyRoomInfo.getUser().getUid();
        } else {
            return 0;
        }
    }

    private String getName() {
        if (null != mMyRoomInfo.getUser()) {
            return mMyRoomInfo.getUser().getNickname();
        } else {
            return "";
        }
    }

    private long getAvatar() {
        if (null != mMyRoomInfo.getUser()) {
            return mMyRoomInfo.getUser().getAvatar();
        } else {
            return 0;
        }
    }

    private void inflate() {
        View root = mViewStub.inflate();
        mBigTurnTablePanelView = (WatchBigTurnTablePanelView) root.findViewById(R.id.big_turn_table_panel_container);
        if (null != mMyRoomInfo.getUser()) {
            mBigTurnTablePanelView.setUser(getZuid(), getName(), getAvatar(), mDataSource.getType());
        }
        mBigTurnTablePanelView.setOnDrawTurnTableListener(new WatchBigTurnTablePanelView.OnDrawTurnTableListener() {
            @Override
            public void onDrawTurnTable() {
                drawTurnTableReq(MyUserInfoManager.getInstance().getUuid(), getZuid(), mMyRoomInfo.getRoomId(), mDataSource.getType());
            }

            @Override
            public void onRotateAnimatorFinish(PrizeItemModel data, String prizeKey) {
                MyLog.w(TAG, " onRotateAnimatorFinish lottery time:" + DateTimeUtils.formatFeedsJournalCreateData(System.currentTimeMillis(), System.currentTimeMillis())
                        + ", data.getGiftId():" + data.getGiftId() + ", prizeKey:" + prizeKey);
                rewardTurntableReq(MyUserInfoManager.getInstance().getUuid(), getZuid(), mMyRoomInfo.getRoomId(), prizeKey, data);
            }
        });
        mViewStub = null;
    }

    public void setTurnTableData(TurnTableConfigModel turnTableConfigModel) {
        boolean needRefresh = false;
        if (mDataSource != null) {
            needRefresh = true;
        }

        mDataSource = turnTableConfigModel;

        if (mBigTurnTablePanelView != null) {
            if (null != mMyRoomInfo && null != mMyRoomInfo.getUser()) {
                mBigTurnTablePanelView.setUser(getZuid(), getName(), getAvatar(), mDataSource.getType());
            }
        }

        if (needRefresh) {
            loadData();
        }
    }

    private void loadData() {
        Observable.create(new Observable.OnSubscribe<TurnTableConfigModel>() {
            @Override
            public void call(Subscriber<? super TurnTableConfigModel> subscriber) {
                if (mDataSource != null) {
                    TurnTablePreConfigModel turnTablePreConfigModel = mDataSource.getTurnTablePreConfigModel();
                    List<PrizeItemModel> prizeItems = turnTablePreConfigModel.getPrizeItems();
                    if (prizeItems != null && !prizeItems.isEmpty()) {
                        for (int i = 0; i < prizeItems.size(); i++) {
                            PrizeItemModel prizeItemModel = prizeItems.get(i);
                            if (!prizeItemModel.isCustom()) {
                                Gift giftById = GiftRepository.findGiftById(prizeItemModel.getGiftId());
                                if (giftById != null) {
                                    prizeItemModel.setGiftPic(giftById.getPicture());
                                }
                            } else {
                                if (TextUtils.isEmpty(prizeItemModel.getCustomDes())) {
                                    prizeItemModel.setCustomDes(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips));
                                }
                            }
                        }
                    }
                    subscriber.onNext(mDataSource);
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<TurnTableConfigModel>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<TurnTableConfigModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(TurnTableConfigModel turnTableConfigModel) {
                        mDataSource = turnTableConfigModel;
                        if (mBigTurnTablePanelView != null) {
                            mBigTurnTablePanelView.setDatas(mDataSource.getTurnTablePreConfigModel().getPrizeItems());
                        }
                    }
                });
    }

    private void drawTurnTableReq(long uid, long zuid, String liveId, BigTurnTableProto.TurntableType type) {
        MyLog.w(TAG, "draw trun table zuid:" + zuid + ", type:" + type);
        BigTurnTableApi.drawTurnTableReq(uid, zuid, liveId, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<BigTurnTableProto.DrawTurntableRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<BigTurnTableProto.DrawTurntableRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(BigTurnTableProto.DrawTurntableRsp rsp) {
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            MyLog.w(TAG, "lottery time:" + DateTimeUtils.formatFeedsJournalCreateData(System.currentTimeMillis(), System.currentTimeMillis())
                                    + ", rsp.getPrizeKey():" + rsp.getPrizeKey() + ", rsp.getPrizeIndex():" + rsp.getPrizeIndex());
                            mBigTurnTablePanelView.startRotate(rsp.getPrizeIndex(), rsp.getPrizeKey());
                        } else {
                            if (rsp != null) {

                                if (rsp.getRetCode() == ErrorCode.CODE_INSUFFICIENT_BALANCE) {
                                    ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.insufficient_balance_tips));
                                } else if (rsp.getRetCode() == ErrorCode.CODE_TURNTABLE_NOT_START) {
                                    ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.big_turn_table_close_tips));
                                } else if (rsp.getRetCode() == ErrorCode.CODE_CHARGE_ERROR) {
                                    ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.big_turn_table_charge_fail));
                                } else if (rsp.getRetCode() == ErrorCode.CODE_LOTTERY_ERROR) {
                                    ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.big_turn_table_lottery_fail));
                                } else {
                                    ToastUtils.showToast("fail:" + rsp.getRetCode());
                                }
                                MyLog.w(TAG, "Big turn table lottery fail" + rsp.getRetCode());
                            }
                        }
                    }
                });
    }

    private void rewardTurntableReq(long uid, long zuid, String liveId, String prizekey, final PrizeItemModel data) {
        MyLog.w(TAG, "rewardTurntableReq" + zuid);
        BigTurnTableApi.rewardTurntableReq(uid, zuid, liveId, prizekey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<BigTurnTableProto.RewardTurntableRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<BigTurnTableProto.RewardTurntableRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(BigTurnTableProto.RewardTurntableRsp rsp) {
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            if (data.getToWhom() == BigTurnTableProto.ToWhom.ANCHOR) {
                                //TODO-后面补上
//                                BigTurnTableProto.VGiftCard giftCard = rsp.getGiftCard();
//                                if (giftCard != null) {
//                                    MyLog.w(TAG, "rewardTurntableReq time:" + DateTimeUtils.formatFeedsJournalCreateData(System.currentTimeMillis(), System.currentTimeMillis())
//                                            + "get gift card gift id:" + giftCard.getGiftId() + ", gift card cnt:" + giftCard.getGiftCardCnt() + ", end time:" + giftCard.getEndTime());
//                                    GiftCard giftCard1 = new GiftCard();
//                                    giftCard1.setEndTime(giftCard.getEndTime());
//                                    giftCard1.setGiftId(giftCard.getGiftId());
//                                    giftCard1.setGiftCardCount(giftCard.getGiftCardCnt());
//                                    EventBus.getDefault().post(new EventClass.GiftCardChangeEvent(giftCard1, rsp.getUserAssetTimestamp()));
//                                }

                                if (data.getGiftType() == BigTurnTableProto.GiftType.VIRTUAL_GIFT_VALUE
                                        && data.getToWhom() == BigTurnTableProto.ToWhom.ANCHOR) {
                                    Gift giftById = GiftRepository.findGiftById(data.getGiftId());
                                    if (giftById != null) {
                                        String des = null;
                                        if (data.isCustom()) {
                                            des = String.format(GlobalData.app().getResources().getString(R.string.gift_tips1), giftById.getName());
                                        } else {
                                            des = String.format(GlobalData.app().getResources().getString(R.string.gift_tips), giftById.getName());
                                        }
                                        BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(giftById.getGiftId(), giftById.getName(), giftById.getCatagory(),
                                                des, 0, rsp.getReceiverTotalTickets(),
                                                System.currentTimeMillis(), 0, mMyRoomInfo.getRoomId(),
                                                String.valueOf(getZuid()), "", "",
                                                0, false, 0, getName());
                                        BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    public void orientationChange(boolean isLandscape) {
        MyLog.d(TAG, "zjnTest isLandscape:" + isLandscape);
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            if (mBigTurnTablePanelView != null) {
                mBigTurnTablePanelView.switchOrient(mIsLandscape);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mBigTurnTablePanelView != null) {
            mBigTurnTablePanelView.destory();
        }
    }
}

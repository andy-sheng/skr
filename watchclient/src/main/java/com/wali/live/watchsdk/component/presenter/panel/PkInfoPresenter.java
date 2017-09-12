package com.wali.live.watchsdk.component.presenter.panel;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.LivePKProto;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by yangli on 2017/09/11.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module PK信息面板表现
 */
public class PkInfoPresenter extends BaseSdkRxPresenter<PkInfoPanel.IView>
        implements PkInfoPanel.IPresenter {
    private static final String TAG = "PkInfoPresenter";

    private RoomBaseDataModel mMyRoomData;

    private Handler mUiHandler = new Handler();
    private Subscription mDownTimerSub;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PkInfoPresenter(
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
    }

    public void startPresenter(boolean isLandscape) {
        MyLog.d(TAG, "startPresenter");
        startPresenter();
    }

    @Override
    public void stopPresenter() {
        MyLog.d(TAG, "stopPresenter");
        super.stopPresenter();
        unregisterAllAction();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void startDownTimer(int time) {
        if (mDownTimerSub != null && !mDownTimerSub.isUnsubscribed()) {
            mDownTimerSub.unsubscribe();
        }
        final int totalTime = time;
        mDownTimerSub = Observable.interval(totalTime, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long cnt) {
                        if (mView != null) {
                            mView.onUpdateRemainTime(totalTime - cnt);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "startDownTimer failed, exception=" + throwable);
                    }
                });
    }

    private void updateTypeAndTime(int timeType, long startTs, long currServerTs) {
        int remainTime = 180;
        switch (timeType) {
            case 1:
                remainTime = 180;
                break;
            case 2:
                remainTime = 600;
                break;
            case 3:
                remainTime = 900;
                break;
            default:
                break;
        }
        if (currServerTs > startTs) {
            remainTime -= remainTime - (int) ((currServerTs - startTs) / 1000);
        }
        if (remainTime > 0) {
            startDownTimer(remainTime);
            mView.onUpdateRemainTime(remainTime);
        } else {
            mView.onUpdateRemainTime(0);
        }
    }

    public void onPkStart(final BarrageMsg.PKInfoMessageExt msgExt, final long currServerTs, final boolean isLandscape) {
        if (msgExt == null || msgExt.info == null) {
            return;
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showSelf(true, isLandscape);
                LivePKProto.NewPKInfo info = msgExt.info;
                updateTypeAndTime(info.getSetting().getDuration().getId(), info.getBeginTs(), currServerTs);
                LivePKProto.PKInfoItem infoItem1 = msgExt.info.getFirst(),
                        infoItem2 = msgExt.info.getSecond();
                String pkType = info.getSetting().getContent().getName();
                if (infoItem1.getUuid() == mMyRoomData.getUid()) {
                    mView.onPkStart(pkType, infoItem1.getUuid(), infoItem2.getUuid());
                    mView.onUpdateScoreInfo(infoItem1.getScore(), infoItem2.getScore());
                } else {
                    mView.onPkStart(pkType, infoItem2.getUuid(), infoItem1.getUuid());
                    mView.onUpdateScoreInfo(infoItem2.getScore(), infoItem1.getScore());
                }
            }
        });
    }

    public void onPkEnd(final BarrageMsg.PKEndInfoMessageExt msgExt) {
        if (msgExt == null || msgExt.info == null) {
            return;
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    LivePKProto.PKInfoItem infoItem1 = msgExt.info.getFirst(), infoItem2 = msgExt.info.getSecond();
                    if (msgExt.endType == 1) { // PK 提前结束
                        boolean ownerWin = mMyRoomData.getUid() != msgExt.uuid; // 不是房主提前结束PK
                        if (infoItem1.getUuid() == mMyRoomData.getUid()) {
                            mView.onPkEnd(ownerWin, infoItem1.getScore(), infoItem2.getScore());
                        } else {
                            mView.onPkEnd(ownerWin, infoItem1.getScore(), infoItem2.getScore());
                        }
                    } else {
                        if (infoItem1.getUuid() == mMyRoomData.getUid()) {
                            mView.onPkEnd(infoItem1.getScore(), infoItem2.getScore());
                        } else {
                            mView.onPkEnd(infoItem1.getScore(), infoItem2.getScore());
                        }
                    }
                }
            }
        });
    }

    public void onPkScore(final BarrageMsg.PKInfoMessageExt msgExt) {
        if (msgExt == null || msgExt.info == null) {
            return;
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    LivePKProto.PKInfoItem infoItem1 = msgExt.info.getFirst(), infoItem2 = msgExt.info.getSecond();
                    if (infoItem1.getUuid() == mMyRoomData.getUid()) {
                        mView.onUpdateScoreInfo(infoItem1.getScore(), infoItem2.getScore());
                    } else {
                        mView.onUpdateScoreInfo(infoItem2.getScore(), infoItem1.getScore());
                    }
                }
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
                return true;
            default:
                break;
        }
        return false;
    }
}

package com.wali.live.watchsdk.component.presenter.panel;

import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
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

    @Override
    public void stopPresenter() {
        MyLog.d(TAG, "stopPresenter");
        super.stopPresenter();
        unregisterAllAction();
    }

    private void startDownTimer(long time) {
        mView.onUpdateRemainTime(time);
        if (mDownTimerSub != null && !mDownTimerSub.isUnsubscribed()) {
            mDownTimerSub.unsubscribe();
            mDownTimerSub = null;
        }
        if (time == 0) {
            return;
        }
        final long totalTime = time;
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

    public void onPkStart(PkStartInfo info, boolean isLandscape) {
        if (mView == null || info == null) {
            return;
        }
        mView.showSelf(true, isLandscape);
        startDownTimer(info.remainTime);
        if (info.uuid1 == mMyRoomData.getUid()) {
            mView.onPkStart(info.pkType, info.uuid1, info.uuid2);
            mView.onUpdateScoreInfo(info.score1, info.score2);
        } else {
            mView.onPkStart(info.pkType, info.uuid2, info.uuid1);
            mView.onUpdateScoreInfo(info.score2, info.score1);
        }
    }

    public void onPkScore(PkScoreInfo info) {
        if (mView == null || info == null) {
            return;
        }
        if (info.uuid1 == mMyRoomData.getUid()) {
            mView.onUpdateScoreInfo(info.score1, info.score2);
        } else {
            mView.onUpdateScoreInfo(info.score2, info.score1);
        }
    }

    public void onPkEnd(PkEndInfo info) {
        if (mView == null || info == null) {
            return;
        }
        if (info.quitUuid != 0) { // PK 提前结束
            boolean ownerWin = mMyRoomData.getUid() != info.quitUuid; // 不是房主提前结束PK
            if (info.uuid1 == mMyRoomData.getUid()) {
                mView.onPkEnd(ownerWin, info.score1, info.score2);
            } else {
                mView.onPkEnd(ownerWin, info.score2, info.score1);
            }
        } else {
            if (info.uuid1 == mMyRoomData.getUid()) {
                mView.onPkEnd(info.score1, info.score2);
            } else {
                mView.onPkEnd(info.score2, info.score1);
            }
        }
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

    public static class PkScoreInfo {
        long uuid1;
        long uuid2;
        long score1;
        long score2;

        public PkScoreInfo(LivePKProto.NewPKInfo info) {
            LivePKProto.PKInfoItem item1 = info.getFirst(), item2 = info.getSecond();
            uuid1 = item1.getUuid();
            score1 = item1.getScore();
            uuid2 = item2.getUuid();
            score2 = item2.getScore();
        }
    }

    public static class PkStartInfo extends PkScoreInfo {
        long remainTime;
        String pkType;

        public PkStartInfo(LivePKProto.NewPKInfo info, long currServerTs) {
            super(info);
            pkType = info.getSetting().getContent().getName();
            parseRemainTime(info.getSetting().getDuration().getId(), info.getBeginTs(), currServerTs);
        }

        private void parseRemainTime(int timeType, long startTs, long currServerTs) {
            remainTime = 180;
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
                remainTime -= (int) ((currServerTs - startTs) / 1000);
            }
            remainTime = Math.max(remainTime, 0);
        }
    }

    public static class PkEndInfo extends PkScoreInfo {
        long quitUuid;

        public PkEndInfo(LivePKProto.NewPKInfo info, long quitUuid) {
            super(info);
            this.quitUuid = quitUuid;
        }
    }
}

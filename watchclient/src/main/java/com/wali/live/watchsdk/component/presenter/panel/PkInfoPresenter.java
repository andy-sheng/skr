package com.wali.live.watchsdk.component.presenter.panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.LivePKProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

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

    private final CompositeSubscription mDownTimerSub = new CompositeSubscription();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public boolean isShow() {
        return mView != null && mView.isShow();
    }

    public boolean isResulting() {
        return mView != null && mView.isResulting();
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
        if (mView != null) {
            mView.hideSelf(true);
        }
    }

    private void startDownTimer(final int totalStartTime, final int totalPkTime) {
        mDownTimerSub.clear();
        if (totalPkTime == 0) {
            return;
        }
        if (totalStartTime > 0) {
            mView.showStartTimer(true);
            mView.onUpdateStartTimer(totalStartTime);
            Subscription startTimer = Observable.interval(1, TimeUnit.SECONDS)
                    .onBackpressureDrop()
                    .take(totalStartTime)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                            if (mView != null) {
                                mView.showStartTimer(false);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            MyLog.e(TAG, "startTimer failed, exception=" + e);
                            if (mView != null) {
                                mView.showStartTimer(false);
                            }
                        }

                        @Override
                        public void onNext(Long cnt) {
                            if (mView != null) {
                                mView.onUpdateStartTimer((int) (totalStartTime - cnt - 1));
                            }
                        }
                    });
            mDownTimerSub.add(startTimer);
        } else {
            mView.showStartTimer(false);
        }
        mView.onUpdateProgressTimer(totalPkTime);
        Subscription pkTimer = Observable.interval(totalStartTime, 1, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .take(totalPkTime)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long cnt) {
                        if (mView != null) {
                            mView.onUpdateProgressTimer((int) (totalPkTime - cnt - 1));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "startDownTimer failed, exception=" + throwable);
                    }
                });
        mDownTimerSub.add(pkTimer);
    }

    public void onPkStart(PkStartInfo info, boolean isLandscape) {
        if (info == null) {
            return;
        }
        MyLog.w("onPkStart");
        mView.showSelf(true, isLandscape);
        startDownTimer(info.startRemainTime, info.pkRemainTime);
        if (info.uuid1 == mMyRoomData.getUid()) {
            mView.onPkStart(info.pkType, info.uuid1, info.uuid2);
            mView.onUpdateScoreInfo(info.score1, info.score2);
        } else {
            mView.onPkStart(info.pkType, info.uuid2, info.uuid1);
            mView.onUpdateScoreInfo(info.score2, info.score1);
        }
    }

    public void onPkScore(PkScoreInfo info) {
        if (info == null) {
            return;
        }
        MyLog.d("onPkScore");
        if (info.uuid1 == mMyRoomData.getUid()) {
            mView.onUpdateScoreInfo(info.score1, info.score2);
        } else {
            mView.onUpdateScoreInfo(info.score2, info.score1);
        }
    }

    public void onPkEnd(PkEndInfo info) {
        if (info == null) {
            return;
        }
        MyLog.w("onPkEnd");
        mDownTimerSub.clear();
        if (info.quitUuid != 0) { // PK 提前结束
            boolean ownerWin = mMyRoomData.getUid() != info.quitUuid; // 不是房主提前结束PK
            final Context context = mView.getRealView().getContext();
            if (info.uuid1 == mMyRoomData.getUid()) {
                ToastUtils.showLongToast(context, context.getString(R.string.pk_end_run,
                        info.nickName2, info.nickName1));
                mView.onPkEnd(ownerWin, info.score1, info.score2);
            } else {
                ToastUtils.showLongToast(context, context.getString(R.string.pk_end_run,
                        info.nickName1, info.nickName2));
                mView.onPkEnd(ownerWin, info.score2, info.score1);
            }
        } else {
            if (info.uuid1 == mMyRoomData.getUid()) {
                mView.onPkEnd(info.score1, info.score2);
            } else {
                mView.onPkEnd(info.score2, info.score1);
            }
            mView.onUpdateProgressTimer(0);
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

        // For Test
        public PkScoreInfo(long uuid1, long uuid2, long score1, long score2) {
            this.uuid1 = uuid1;
            this.uuid2 = uuid2;
            this.score1 = score1;
            this.score2 = score2;
        }

        public PkScoreInfo(LivePKProto.NewPKInfo info) {
            LivePKProto.PKInfoItem item1 = info.getFirst(), item2 = info.getSecond();
            uuid1 = item1.getUuid();
            score1 = item1.getScore();
            uuid2 = item2.getUuid();
            score2 = item2.getScore();
        }
    }

    public static class PkStartInfo extends PkScoreInfo {
        int startRemainTime;
        int pkRemainTime;
        String pkType;

        // For Test
        public PkStartInfo(long uuid1, long uuid2, long score1, long score2, String pkType, int remainTime) {
            super(uuid1, uuid2, score1, score2);
            this.pkType = pkType;
            this.pkRemainTime = remainTime;
        }

        public PkStartInfo(LivePKProto.NewPKInfo info, long currServerTs) {
            super(info);
            pkType = info.getSetting().getContent().getName();
            parseRemainTime(info.getSetting().getDuration().getId(), info.getBeginTs(), currServerTs);
        }

        private void parseRemainTime(int timeType, long startTs, long currServerTs) {
            pkRemainTime = 180;
            switch (timeType) {
                case 1:
                    pkRemainTime = 180;
                    break;
                case 2:
                    pkRemainTime = 600;
                    break;
                case 3:
                    pkRemainTime = 900;
                    break;
                default:
                    break;
            }
            if (currServerTs == 0) {
                startRemainTime = 10;
            } else if (currServerTs < startTs + 10000) { // 10s开始倒计时还未结束
                startRemainTime = (int) (10000 - currServerTs + startTs) / 1000;
            } else { // 10s开始倒计时已结束，计算PK进度剩余时间
                startRemainTime = 0;
                pkRemainTime -= (int) (currServerTs - startTs - 10000) / 1000;
                pkRemainTime = Math.max(pkRemainTime, 0);
            }
        }
    }

    public static class PkEndInfo extends PkScoreInfo {
        long quitUuid;
        String nickName1;
        String nickName2;

        // For Test
        public PkEndInfo(long uuid1, long uuid2, long score1, long score2, long quitUuid) {
            super(uuid1, uuid2, score1, score2);
            this.quitUuid = quitUuid;
            this.nickName1 = String.valueOf(uuid1);
            this.nickName2 = String.valueOf(uuid2);
        }

        public PkEndInfo(LivePKProto.NewPKInfo info, long quitUuid) {
            super(info);
            this.quitUuid = quitUuid;
        }

        public void setNickName(User user) {
            if (user == null || TextUtils.isEmpty(user.getNickname())) {
                return;
            }
            long uuid = user.getUid();
            if (uuid1 == uuid) {
                nickName1 = user.getNickname();
            } else if (uuid2 == uuid) {
                nickName2 = user.getNickname();
            }
        }
    }
}

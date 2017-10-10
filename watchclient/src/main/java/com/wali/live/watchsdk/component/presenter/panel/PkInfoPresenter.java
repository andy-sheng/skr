package com.wali.live.watchsdk.component.presenter.panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.push.model.BarrageMsgExt;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
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
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_STOP;

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
        mDownTimerSub.clear();
        if (mView != null) {
            mView.hideSelf(true);
        }
    }

    private void startDownTimer(final int startRemainTime, final int pkRemainTime) {
        mDownTimerSub.clear();
        if (pkRemainTime == 0) {
            return;
        }
        if (startRemainTime > 0) {
            mView.showStartTimer(true);
            mView.onUpdateStartTimer(startRemainTime);
            Subscription startTimer = Observable.interval(1, TimeUnit.SECONDS)
                    .onBackpressureDrop()
                    .take(startRemainTime + 1)
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
                                mView.onUpdateStartTimer((int) (startRemainTime - cnt - 1));
                            }
                        }
                    });
            mDownTimerSub.add(startTimer);
        } else {
            mView.showStartTimer(false);
        }
        mView.onUpdateProgressTimer(pkRemainTime);
        Subscription pkTimer = Observable.interval(startRemainTime, 1, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .take(pkRemainTime + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long cnt) {
                        if (mView != null) {
                            mView.onUpdateProgressTimer((int) (pkRemainTime - cnt));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "startDownTimer failed, exception=" + throwable);
                    }
                });
        mDownTimerSub.add(pkTimer);
        Subscription endTimer = Observable.timer(startRemainTime + pkRemainTime + 15, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(PresenterEvent.STOP))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long cnt) {
                        if (isShow() && !isResulting()) {
                            mController.postEvent(MSG_ON_PK_STOP);
                            stopPresenter();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "endTimer failed, exception=" + throwable);
                    }
                });
        mDownTimerSub.add(endTimer);
    }

    public void onPkStart(BarrageMsgExt.PkStartInfo info, boolean isLandscape) {
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

    public void onPkScore(BarrageMsgExt.PkScoreInfo info) {
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

    public void onPkEnd(BarrageMsgExt.PkEndInfo info) {
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
}

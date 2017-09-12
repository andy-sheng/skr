package com.wali.live.watchsdk.component.presenter.panel;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.panel.PkInfoPanel;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
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
public class PkInfoPresenter extends ComponentPresenter<PkInfoPanel.IView>
        implements PkInfoPanel.IPresenter {
    private static final String TAG = "PkInfoPresenter";

    private Handler mUiHandler = new Handler();
    private Runnable mHidePkInfoTask = new Runnable() {
        @Override
        public void run() {
            stopPresenter();
        }
    };

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PkInfoPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    private int ticket1, ticket2;
    private int remainTime;
    private Random random = new Random();
    private Subscription subscription;

    public void startPresenter(boolean isLandscape) {
        MyLog.d(TAG, "startPresenter");
        startPresenter();
        mView.showSelf(true, isLandscape);

        // TEST
        remainTime = Math.abs(random.nextInt()) % 600;
        if (subscription == null || subscription.isUnsubscribed()) {
            Observable.interval(1, 1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .take(300)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            ticket1 += Math.abs(random.nextInt()) % 20;
                            ticket2 += Math.abs(random.nextInt()) % 5;
                            mView.updateScoreInfo(ticket1, ticket2);
                            mView.updateTimeInfo(--remainTime);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            mView.onPkResultInfo(ticket1, ticket2);
                            mUiHandler.post(mHidePkInfoTask);
                        }
                    });
        }
    }

    @Override
    public void stopPresenter() {
        MyLog.d(TAG, "stopPresenter");
        super.stopPresenter();
        unregisterAllAction();
        mUiHandler.removeCallbacksAndMessages(null);
        mView.hideSelf(true);
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

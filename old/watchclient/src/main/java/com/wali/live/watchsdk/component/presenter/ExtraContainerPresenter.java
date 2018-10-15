package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.ExtraContainerView;

import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by xiaolan on 2017/08/17.
 * <p>
 * Generated using create_view_with_presenter.py
 *
 * @module [TODO-COMPONENT add module]
 */
public class ExtraContainerPresenter extends ComponentPresenter<ExtraContainerView.IView>
        implements ExtraContainerView.IPresenter {
    private static final String TAG = "ExtraContainerPresenter";

    private static final int NOTIFY_LIMIT = 5;

    private static final int DELAY_TIME = 30 * 1000;
    private static final int ONE_DAY_TIME = 24 * 60 * 60 * 1000;

    private static final int SHOW_DURATION = 10 * 1000;

    private boolean mIsLandscape;

    private Runnable mRunnable;

    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            mView.hideEditInfo(true);
        }
    };

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ExtraContainerPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_LIVE_SUCCESS);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();

        if (mRunnable != null) {
            mView.getRealView().removeCallbacks(mRunnable);
            mRunnable = null;
        }
        if (mDismissRunnable != null) {
            mView.getRealView().removeCallbacks(mDismissRunnable);
            mDismissRunnable = null;
        }
    }

    private void notifyLiveSuccess() {
        int count = PreferenceUtils.getSettingInt(PreferenceKeys.PRE_KEY_EDIT_INFO_COUNT, 0);
        if (count <= NOTIFY_LIMIT) {
            long editInfoTime = PreferenceUtils.getSettingLong(PreferenceKeys.PRE_KEY_EDIT_INFO_TIME, 0l);
            long currentTime = System.currentTimeMillis();

            if (currentTime - editInfoTime > ONE_DAY_TIME) {
                MyLog.d(TAG, "postRunnable showEditInfo");

                if (mRunnable == null) {
                    mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            showEditInfo();
                        }
                    };
                }
                mView.getRealView().postDelayed(mRunnable, DELAY_TIME);
            }
        }
    }

    private void showEditInfo() {
        // 横屏不要出现，因为会挡住视频
        final String nickName = MyUserInfoManager.getInstance().getNickname();
        if (!mIsLandscape && UserAccountManager.getInstance().hasAccount()
                && !TextUtils.isEmpty(nickName) && TextUtils.isDigitsOnly(nickName)) {
            MyLog.d(TAG, "runnable showEditInfo");
            mView.showEditInfo();
            long currentTime = System.currentTimeMillis();
            PreferenceUtils.setSettingLong(PreferenceKeys.PRE_KEY_EDIT_INFO_TIME, currentTime);
            PreferenceUtils.increaseSettingInt(PreferenceKeys.PRE_KEY_EDIT_INFO_COUNT);

            if (mDismissRunnable == null) {
                mDismissRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mView.hideEditInfo(true);
                    }
                };
            }
            mView.getRealView().postDelayed(mDismissRunnable, SHOW_DURATION);
        }
    }

    private void notifyLandscape() {
        mView.hideEditInfo(false);
    }

    public void reset() {
        mView.hideEditInfo(false);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                notifyLandscape();
                return true;
            case MSG_ON_LIVE_SUCCESS:
                notifyLiveSuccess();
                return true;
            default:
                break;
        }
        return false;
    }
}

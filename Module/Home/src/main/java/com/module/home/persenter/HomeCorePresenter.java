package com.module.home.persenter;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.log.MyLog;
import com.common.utils.NetworkUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.view.PermissionTipsView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class HomeCorePresenter {

    public final static String TAG = "HomePresenter";

    DialogPlus mPerTipsDialogPlus;

    long mLastCheckTs = 0;

    Handler mUiHandler = new Handler();

    private Runnable mNetworkChangeRunnable = new Runnable() {
        @Override
        public void run() {
            showNetworkDisConnectDialog();
        }
    };

    public HomeCorePresenter() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void destroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void checkPermiss(Activity activity) {
        MyLog.d(TAG, "checkPermiss");
        long now = System.currentTimeMillis();
        if (now - mLastCheckTs > 2000) {
            /**
             * 这里  U.getPermissionUtils().requestExternalStorage 会启动
             * RxPermissionFragment ，RxPermissionFragment结束后出发 Activity的生命周期
             * 所以这里加个判断，防止onResume 被触发导致不断回调
             */
            mLastCheckTs = now;
            check1(activity);
        }
    }

    void check1(Activity activity) {
        if (!U.getPermissionUtils().checkExternalStorage(activity)) {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                    check2(activity);
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    onReject();
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    onReject();
                }
            }, activity);
        } else {
            check2(activity);
        }
    }

    void check2(Activity activity) {
        if (!U.getPermissionUtils().checkRecordAudio(activity)) {
            U.getPermissionUtils().requestRecordAudio(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                    onAgree();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    onReject();
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                    onReject();
                }
            }, activity);
        } else {
            onAgree();
        }
    }

    public void onReject() {
        if (mPerTipsDialogPlus == null) {
            Activity activity = U.getActivityUtils().getTopActivity();
            mPerTipsDialogPlus = DialogPlus.newDialog(activity)
                    .setCancelable(false)
                    .setGravity(Gravity.CENTER)
                    .setContentHolder(new ViewHolder(new PermissionTipsView(activity)))
                    .create();
        }
        mPerTipsDialogPlus.show();
    }

    public void onAgree() {
        if (mPerTipsDialogPlus != null) {
            mPerTipsDialogPlus.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkUtils.NetworkChangeEvent event) {
        if (event.type == -1) {
            mUiHandler.postDelayed(mNetworkChangeRunnable, 3000);
        } else {
            mUiHandler.removeCallbacks(mNetworkChangeRunnable);
        }
    }


    private void showNetworkDisConnectDialog() {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                .setMessageTip("网络连接失败\n请检查网络异常，请检查网络")
                .setOkBtnTip("确认")
                .build();

        DialogPlus.newDialog(U.getActivityUtils().getTopActivity())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == R.id.ok_btn) {
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .create().show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        // 账号已经设定
        checkUserInfo("SetAccountEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoLoadOkEvent event) {
        // 个人信息已经读取
        checkUserInfo("UserInfoLoadOkEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.LogoffAccountEvent event) {
        if (event.reason == AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED) {
            MyLog.w(TAG, "LogoffAccountEvent" + " 账号已经过期，需要重新登录,跳到登录页面");
        }
        ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN).navigation();
    }

    public void checkUserInfo(String from) {
        MyLog.d(TAG,"checkUserInfo" + " from=" + from);
        if (UserAccountManager.getInstance().hasLoadAccountFromDB()) {
            if (!UserAccountManager.getInstance().hasAccount()) {
                // 到时会有广告页或者启动页挡一下的，先不用管
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN).navigation();
            } else {
                if (MyUserInfoManager.getInstance().hasLoadFromDB()) {
                    // 如果有账号了
                    if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())
                            || MyUserInfoManager.getInstance().getSex() == 0
                            || TextUtils.isEmpty(MyUserInfoManager.getInstance().getBirthday())) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_UPLOAD)
                                .greenChannel().navigation();
                    }
                } else {

                }
            }
        }
    }
}

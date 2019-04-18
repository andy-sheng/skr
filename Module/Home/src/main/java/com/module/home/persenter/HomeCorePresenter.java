package com.module.home.persenter;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.login.LoginActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.upgrade.UpgradeData;
import com.common.log.MyLog;
import com.common.utils.NetworkUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.common.ICallback;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.module.home.view.IHomeActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeCorePresenter {

    public final static String TAG = "HomePresenter";

    DialogPlus mPerTipsDialogPlus;
    TipsDialogView mTipsDialogView;

    Handler mUiHandler = new Handler();

    IHomeActivity mView;
    BaseActivity mBaseActivity;
    DialogPlus mDialogPlus;


    private Runnable mNetworkChangeRunnable = new Runnable() {
        @Override
        public void run() {
            showNetworkDisConnectDialog();
        }
    };

    private ICallback mUnreadICallback = new ICallback() {
        @Override
        public void onSucess(Object obj) {
            mView.showUnReadNum((int) obj);
        }

        @Override
        public void onFailed(Object obj, int errcode, String message) {

        }
    };

    public HomeCorePresenter(BaseActivity baseActivity, IHomeActivity view) {
        mView = view;
        mBaseActivity = baseActivity;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        ModuleServiceManager.getInstance().getMsgService().addUnReadMessageCountChangedObserver(mUnreadICallback);
    }

    public void destroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
        mView = null;
        ModuleServiceManager.getInstance().getMsgService().removeUnReadMessageCountChangedObserver(mUnreadICallback);
    }

    public void onReject(String text) {

        if (mPerTipsDialogPlus == null) {
            Activity activity = U.getActivityUtils().getTopActivity();
            mTipsDialogView = new TipsDialogView.Builder(activity)
                    .setMessageTip(text)
                    .setOkBtnTip("去设置")
                    .setOkBtnClickListener(new AnimateClickListener() {
                        @Override
                        public void click(View view) {
                            U.getPermissionUtils().goToPermissionManager(activity);
                        }
                    })
                    .build();

            mPerTipsDialogPlus = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(mTipsDialogView))
                    .setGravity(Gravity.BOTTOM)
//                    .setCancelable(false)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }
        mTipsDialogView.mMessageTv.setText(text);
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
                .setMessageTip("网络异常\n请检查网络连接后重试")
                .setOkBtnTip("确认")
                .setOkBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(U.getActivityUtils().getTopActivity())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .create();
        mDialogPlus.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        // 账号已经设定
        checkUserInfo("SetAccountEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent event) {
        // 个人信息已经读取
        checkUserInfo("UserInfoLoadOkEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 0)
    public void onEvent(AccountEvent.LogoffAccountEvent event) {
        if (event.reason == AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED) {
            MyLog.w(TAG, "LogoffAccountEvent" + " 账号已经过期，需要重新登录,跳到登录页面");
        }
        if (!UserAccountManager.getInstance().hasAccount()) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                    .withInt(LoginActivity.KEY_REASON, LoginActivity.REASON_LOGOFF)
                    .navigation();
            mView.onLogoff();
        }

        UMShareAPI.get(U.app()).deleteOauth(mBaseActivity, SHARE_MEDIA.WEIXIN, null);
        UMShareAPI.get(U.app()).deleteOauth(mBaseActivity, SHARE_MEDIA.QQ, null);
        BgMusicManager.getInstance().destory();
    }

    public void checkUserInfo(String from) {
        MyLog.d(TAG, "checkUserInfo" + " from=" + from);
        if (UserAccountManager.getInstance().hasLoadAccountFromDB()) {
            if (!UserAccountManager.getInstance().hasAccount()) {
                // 到时会有广告页或者启动页挡一下的，先不用管
                LoginActivity.open(mBaseActivity);
//                ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN).navigation();
            } else {
                if (MyUserInfoManager.getInstance().hasMyUserInfo() && MyUserInfoManager.getInstance().isUserInfoFromServer()) {
                    // 如果有账号了
                    if (MyUserInfoManager.getInstance().isNeedCompleteInfo()) {
                        boolean isUpAc = U.getActivityUtils().getTopActivity() instanceof UploadAccountInfoActivity;
                        if (!isUpAc) {
                            // 顶层的不是这个activity
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_UPLOAD)
                                    .greenChannel().navigation();
                        }else{
                            MyLog.d(TAG,"顶部已经是UploadAccountInfoActivity");
                        }
                    } else {
                        //MyUserInfoManager.getInstance().trySyncLocation();
                        //账号正常
                        mView.tryJumpSchemeIfNeed();
                    }
                } else {

                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        if (mView != null) {
            mView.updatePersonIconRedDot();
        }
    }

}

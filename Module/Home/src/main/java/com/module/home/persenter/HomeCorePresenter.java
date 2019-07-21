package com.module.home.persenter;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.core.account.event.AccountEvent;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.upgrade.UpgradeData;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.dialog.view.TipsDialogView;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.home.R;
import com.module.home.view.IHomeActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeCorePresenter {

    public final String TAG = "HomePresenter";

    DialogPlus mPerTipsDialogPlus;
    TipsDialogView mTipsDialogView;

    Handler mUiHandler = new Handler();

    IHomeActivity mView;
    BaseActivity mBaseActivity;

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
    public void onEvent(AccountEvent.SetAccountEvent event) {
        // 账号已经设定
        checkUserInfo("SetAccountEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent event) {
        // 个人信息已经读取
        checkUserInfo("UserInfoLoadOkEvent");
    }

    public void checkUserInfo(String from) {
        MyLog.d(TAG, "checkUserInfo" + " from=" + from);
        mView.tryJumpSchemeIfNeed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        if (mView != null) {
            mView.updatePersonIconRedDot();
        }
    }

}

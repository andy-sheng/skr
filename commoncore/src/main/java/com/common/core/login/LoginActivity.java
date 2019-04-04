package com.common.core.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.login.fragment.LoginFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.umeng.socialize.UMShareAPI;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.module.RouterConstants.ACTIVITY_UPLOAD;

@Route(path = RouterConstants.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity {

    public static final String KEY_SHOW_TOAST = "key_show_toast";
    public static final String KEY_ORIGIN_PATH = "key_origin_path";
    public static final String KEY_REASON = "key_reason"; // 退出的原因
    public static final int REASON_NORMAL = 0; // 因为没有账号到这个页面
    public static final int REASON_LOGOFF = 1; // 因为退出登录所以要到登录页
    int mReason = REASON_NORMAL;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(KEY_SHOW_TOAST, false)) {
            U.getToastUtil().showShort("请先登录");
        }
        if (intent != null) {
            mReason = intent.getIntExtra(KEY_REASON, 0);
        }
        MyLog.d(TAG, "initData" + " mReason=" + mReason);
        U.getFragmentUtils().addFragment(FragmentUtils
                .newAddParamsBuilder(LoginActivity.this, LoginFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
        if (mReason == REASON_LOGOFF) {
            // 因为是因为退出登录 或者 被踢 才到这个登录页面的，所以要清除除了 LoginActivity 外的所有 Activity
            for (Activity activity : U.getActivityUtils().getActivityList()) {
                if (activity != this) {
                    activity.finish();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent setAccountEvent) {
        //登陆成功
        if (mReason == REASON_LOGOFF) {
            // 因为是因为退出登录 或者 被踢 才到这个登录页面的，所以要清除除了 LoginActivity 外的所有 Activity
            // 所以这里还要跳到 HomeActivity
            ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME)
                    .navigation();
        } else {
            Intent intent = getIntent();
            String originPath = intent.getStringExtra(KEY_ORIGIN_PATH);
            MyLog.d(TAG, "登录成功，跳回原页面 originPath:" + originPath);
            if (!TextUtils.isEmpty(originPath)) {
                // 登录成功后，跳回原页面
                ARouter.getInstance().build(originPath)
                        .with(intent.getExtras())
                        .navigation();
            } else {
                // 必须放在这，防止当前栈中没有activity导致底部露出
                if (!isHomeActivityExist()) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME)
                            .navigation();
                } else {
                }
            }
        }
        finish();
    }

    private boolean isHomeActivityExist() {
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if(U.getActivityUtils().isHomeActivity(activity)){
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean onBackPressedForActivity() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            moveTaskToBack(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(U.app()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void destroy() {
        super.destroy();
        UMShareAPI.get(U.app()).release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        UMShareAPI.get(U.app()).onSaveInstanceState(outState);
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        // 自己处理有键盘时的整体布局
        return true;
    }

}

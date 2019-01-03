package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.login.fragment.LoginByPhoneFragment;
import com.common.core.login.fragment.LoginFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.view.ex.ExTextView;
import com.module.RouterConstants;
import com.common.core.account.event.AccountEvent;
import com.common.utils.U;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.module.RouterConstants.ACTIVITY_UPLOAD;

@Route(path = RouterConstants.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity {

    public static final String KEY_SHOW_TOAST = "key_show_toast";
    public static final String KEY_ORIGIN_PATH = "key_origin_path";


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getBooleanExtra(KEY_SHOW_TOAST, false)) {
            U.getToastUtil().showShort("请先登录");
        }

        U.getFragmentUtils().addFragment(FragmentUtils
                .newAddParamsBuilder(LoginActivity.this, LoginFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(true)
                .build());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent setAccountEvent) {
        //登陆成功
        U.getToastUtil().showShort("登录成功");
        // 昵称不能为空
        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName()) || TextUtils.isEmpty(MyUserInfoManager.getInstance().getAvatar())) {
            // 无头像，昵称，性别和生日都进到指定页面
            ARouter.getInstance()
                    .build(ACTIVITY_UPLOAD)
                    .withInt("jump_to_foot", 1)
                    .navigation();
        } else if (MyUserInfoManager.getInstance().getSex() == 0) {
            // 无性别，进到指定页面
            ARouter.getInstance()
                    .build(ACTIVITY_UPLOAD)
                    .withInt("jump_to_foot", 2)
                    .navigation();
        } else if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getBirthday())) {
            // 无生日，进到指定页面
            ARouter.getInstance()
                    .build(ACTIVITY_UPLOAD)
                    .withInt("jump_to_foot", 3)
                    .navigation();
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String path = bundle.getString(KEY_ORIGIN_PATH);
                if (!TextUtils.isEmpty(path)) {
                    // 跳转到原页面，并带上参数
                    ARouter.getInstance().build(path).with(bundle).navigation();
                }
            }
        }

        finish();
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
        moveTaskToBack(true);
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}

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
import com.common.core.login.fragment.VerifyCodeFragment;
import com.common.utils.FragmentUtils;
import com.common.view.ex.ExTextView;
import com.module.RouterConstants;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.account.event.AccountEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.Observable;

@Route(path = RouterConstants.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity {

    public static final String KEY_SHOW_TOAST = "key_show_toast";
    RelativeLayout mMainActContainer;
    ExTextView mLogoTv;
    ExTextView mWeixinLoginTv;
    ExTextView mPhoneLoginTv;
    ExTextView mWeiboLoginTv;


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getBooleanExtra(KEY_SHOW_TOAST, false)) {
            U.getToastUtil().showShort("请先登录");
        }

        mMainActContainer = (RelativeLayout)findViewById(R.id.main_act_container);
        mLogoTv = (ExTextView)findViewById(R.id.logo_tv);
        mWeixinLoginTv = (ExTextView)findViewById(R.id.weixin_login_tv);
        mPhoneLoginTv = (ExTextView)findViewById(R.id.phone_login_tv);
        mWeiboLoginTv = (ExTextView)findViewById(R.id.weibo_login_tv);


        mPhoneLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }

                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(LoginActivity.this, LoginByPhoneFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        findViewById(R.id.green_channel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME).greenChannel().navigation();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent setAccountEvent) {
        //登陆成功
        finish();
        U.getToastUtil().showShort("登录成功");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}

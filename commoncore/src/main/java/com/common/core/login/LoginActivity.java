package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
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
    CommonTitleBar mTitlebar;
    RelativeLayout mMainContainer;
    NoLeakEditText mInputPhoneEt;
    ExButton mSendMsgBtn;
    NoLeakEditText mVerifyCodeEt;
    ExButton mLoginBtn;


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getBooleanExtra(KEY_SHOW_TOAST, false)) {
            U.getToastUtil().showShort("请先登录");
        }
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);
        mInputPhoneEt = (NoLeakEditText) findViewById(R.id.input_phone_et);
        mSendMsgBtn = (ExButton) findViewById(R.id.send_msg_btn);
        mVerifyCodeEt = (NoLeakEditText) findViewById(R.id.verify_code_et);
        mLoginBtn = (ExButton) findViewById(R.id.login_btn);
        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
                Observable<ApiResult> observer = userAccountServerApi.sendSmsVerifyCode(mInputPhoneEt.getText().toString());
                ApiMethods.subscribe(observer, null);
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                String phoneNum = mInputPhoneEt.getText().toString();
                String verifyCode = mVerifyCodeEt.getText().toString();
                UserAccountManager.getInstance().loginByPhoneNum(phoneNum, verifyCode);
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

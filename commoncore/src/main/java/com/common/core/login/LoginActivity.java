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
    CommonTitleBar mTitlebar;
    RelativeLayout mMainContainer;
    ExTextView mLogoTv;
    NoLeakEditText mInputPhoneEt;
    ExTextView mPhoneHintTv;
    ExButton mNextBtn;

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
        mLogoTv = (ExTextView) findViewById(R.id.logo_tv);
        mInputPhoneEt = (NoLeakEditText) findViewById(R.id.input_phone_et);
        mPhoneHintTv = (ExTextView) findViewById(R.id.phone_hint_tv);
        mNextBtn = (ExButton) findViewById(R.id.next_btn);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                String phoneNumber = mInputPhoneEt.getText().toString().trim();
                if (checkPhoneNumber(phoneNumber)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(VerifyCodeFragment.EXTRA_PHONE_NUMBER, phoneNumber);
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newParamsBuilder(LoginActivity.this, VerifyCodeFragment.class)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .setBundle(bundle)
                            .build());
                }
            }
        });

        findViewById(R.id.green_channel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME).greenChannel().navigation();
            }
        });
    }

    /**
     * 检查手机号是否正确
     *
     * @return
     */
    private boolean checkPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber) && TextUtils.isDigitsOnly(phoneNumber)
                && phoneNumber.length() == 11 && phoneNumber.startsWith("1")) {
            return true;
        }

        mPhoneHintTv.setVisibility(View.VISIBLE);
        mPhoneHintTv.setText("请输入正确的手机号");
        return false;
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

package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.RouterConstants;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.oauth.XiaoMiOAuth;
import com.common.core.userinfo.UserInfo;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.utils.CommonUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.wali.live.proto.User.GetHomepageResp;
import com.wali.live.proto.User.GetUserInfoByIdRsp;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

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
                Observable<ResponseBody> observer = userAccountServerApi.sendSmsVerifyCode(mInputPhoneEt.getText().toString());
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

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}

package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.oauth.XiaoMiOAuth;
import com.common.view.titlebar.CommonTitleBar;
import com.common.utils.U;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

@Route(path = "/core/login")
public class LoginActivity extends BaseActivity {

    private TextView mMiBtn;

    CommonTitleBar mTitlebar;


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);

        mTitlebar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMiBtn = (TextView) this.findViewById(R.id.mi_btn);

        mMiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        String code = XiaoMiOAuth.getOAuthCode(LoginActivity.this);
                        UserAccountManager.getInstance().loginByMiOauth(code);
                        emitter.onComplete();
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });
        boolean showToast = getIntent().getBooleanExtra("key_show_toast", false);
        if (showToast) {
            U.getActivityUtils().showSnackbar("请先登录", true);
        }
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

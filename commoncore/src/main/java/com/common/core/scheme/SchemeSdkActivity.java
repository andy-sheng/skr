package com.common.core.scheme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.login.interceptor.SkrArouterInterceptor;
import com.common.core.scheme.processor.SkrSchemeProcessor;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;

/**
 * 所有的push，不管是umeng的还是厂家的
 * 最终都会走到这
 */
@Route(path = RouterConstants.ACTIVITY_SCHEME, extras = SkrArouterInterceptor.NO_NEED_LOGIN)
public class SchemeSdkActivity extends BaseActivity {

    private Uri mUri;
    private Handler mHandler = new Handler();

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                process(intent);
            }
        });
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getStatusBarUtil().setTransparentBar(this, true);
        overridePendingTransition(R.anim.slide_in_right, 0);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                process(getIntent());
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    private void process(Intent intent) {
        if (intent == null) {
            MyLog.w(getTAG(), "process intent is null");
            finish();
            return;
        }

        String uri = intent.getStringExtra("uri");
        if (TextUtils.isEmpty(uri)) {
            uri = getIntent().getDataString();
            if(TextUtils.isEmpty(uri)){
                MyLog.w(getTAG(), "uri is null");
                finish();
                return;
            }
        }
        mUri = Uri.parse(uri);
        SkrSchemeProcessor.INSTANCE.process(mUri,this,true);
        if (!isHomeActivityExist()) {
            MyLog.w(getTAG(), "HomeActivity不存在，需要先启动HomeActivity");
            ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME)
                    .withString("from_scheme", uri)
                    .navigation();
            finish();
            return;
        } else {
            MyLog.w(getTAG(), "HomeActivity存在");
        }

        if (TextUtils.isEmpty(uri)) {
            MyLog.w(getTAG(), "process uri is empty or null");
            finish();
            return;
        }


        if (mUri == null) {
            MyLog.w(getTAG(), "process intent data uri is null");
            finish();
            return;
        }

        try {
            process(mUri);
        } catch (Exception e) {
            MyLog.e(getTAG(), e);
        } finally {
            finish();
        }
    }

    private boolean isHomeActivityExist() {
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (U.getActivityUtils().isHomeActivity(activity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void destroy() {
        super.destroy();
    }

    private void process(final Uri uri) throws Exception {
        MyLog.w(getTAG(), "process uri=" + uri);
        SkrSchemeProcessor.INSTANCE.process(mUri,this,false);
    }

}

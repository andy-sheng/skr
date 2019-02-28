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
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.core.scheme.processor.ZqSchemeProcessorManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.IHomeService;

/**
 * 所有的push，不管是umeng的还是厂家的
 * 最终都会走到这
 */
@Route(path = RouterConstants.ACTIVITY_SCHEME, extras = JudgeLoginInterceptor.NO_NEED_LOGIN)
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
            MyLog.w(TAG, "process intent is null");
            finish();
            return;
        }

        String uri = intent.getStringExtra("uri");
        if (TextUtils.isEmpty(uri)) {
            uri = getIntent().getDataString();
        }
        mUri = Uri.parse(uri);
        ZqSchemeProcessorManager.getInstance().process(mUri,this,true);
        if (!isHomeActivityExist()) {
            MyLog.w(TAG, "HomeActivity不存在，需要先启动HomeActivity");
            ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME)
                    .withString("from_scheme", uri)
                    .navigation();
            finish();
            return;
        } else {
            MyLog.w(TAG, "HomeActivity存在");
        }

        if (TextUtils.isEmpty(uri)) {
            MyLog.w(TAG, "process uri is empty or null");
            finish();
            return;
        }


        if (mUri == null) {
            MyLog.w(TAG, "process intent data uri is null");
            finish();
            return;
        }

        try {
            process(mUri);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            finish();
        }
    }

    private boolean isHomeActivityExist() {
        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
        String homeActivityName = (String) channelService.getData(1, "");
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity.getClass().getSimpleName().equals(homeActivityName)) {
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
        MyLog.w(TAG, "process uri=" + uri);
        ZqSchemeProcessorManager.getInstance().process(mUri,this,false);
    }

}

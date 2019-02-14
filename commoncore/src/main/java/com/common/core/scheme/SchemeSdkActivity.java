package com.common.core.scheme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.cta.CTANotifyFragment;
import com.common.core.scheme.processor.ZqSchemeProcessorManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;

@Route(path = RouterConstants.ACTIVITY_SCHEME)
public class SchemeSdkActivity extends BaseActivity {

    private Intent mIntent;
    private Uri mUri;
    private Handler mHandler = new Handler();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {

        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getStatusBarUtil().setTransparentBar(this, true);
        overridePendingTransition(R.anim.slide_in_right, 0);

        if (isNeedShowCtaDialog()) {
            CTANotifyFragment.openFragment(this, new CTANotifyFragment.CTANotifyButtonClickListener() {

                @Override
                public void onClickCancelButton() {
                    finish();
                }

                @Override
                public void onClickConfirmButton() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            process();
                        }
                    });
                }
            });
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    process();
                }
            });
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    private void process() {
        mIntent = getIntent();
        if (mIntent == null) {
            MyLog.w(TAG, "process intent is null");
            finish();
            return;
        }

        String uri = mIntent.getStringExtra("uri");
        if(TextUtils.isEmpty(uri)){
            MyLog.w(TAG, "getStringExtra(uri) is empty");
            uri = getIntent().getDataString();
        }

        if(TextUtils.isEmpty(uri)){
            MyLog.w(TAG, "process uri is empty or null");
            finish();
            return;
        }

        mUri = Uri.parse(uri);
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

    @Override
    protected void destroy() {
        super.destroy();
    }

    private void process(final Uri uri) throws Exception {
        MyLog.w(TAG, "process uri=" + uri);
        ZqSchemeProcessorManager.getInstance().process(uri, this);
    }

    /**
     * 是否工信部的包
     * "5005_1_android".equalsIgnoreCase(Constants.ReleaseChannel)
     *
     * @return
     */
    public boolean isNeedShowCtaDialog() {

        return false;
    }
}

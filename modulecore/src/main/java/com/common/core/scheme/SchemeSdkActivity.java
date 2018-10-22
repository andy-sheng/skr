package com.common.core.scheme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.preference.PreferenceUtils;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.RouterConstants;
import com.common.core.cta.CTANotifyFragment;
import com.common.core.scheme.processor.WaliliveProcessor;
import com.common.core.scheme.specific.SpecificProcessor;
import com.common.log.MyLog;
import com.common.utils.U;

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
        mUri = Uri.parse(uri);
        if (mUri == null) {
            MyLog.w(TAG, "process intent data uri is null");
            finish();
        }
        try {
            process(mUri);
        } catch (Exception e) {
            finish();
        }
    }

    private void process(final Uri uri) throws Exception {
        MyLog.w(TAG, "process uri=" + uri);
        if (uri == null) {
            finish();
            return;
        }

        String scheme = uri.getScheme();
        MyLog.w(TAG, "process scheme=" + scheme);
        if (TextUtils.isEmpty(scheme)) {
            finish();
            return;
        }

        final String host = uri.getHost();
        MyLog.w(TAG, "process host=" + host);
        if (TextUtils.isEmpty(host)) {
            finish();
            return;
        }

        if (scheme.equals(SchemeConstants.SCHEME_LIVESDK)
                || host.equals(SchemeConstants.HOST_ZHIBO_COM)) {
            final int channelId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_CHANNEL, 0);
            String packageName = uri.getQueryParameter(SchemeConstants.PARAM_PACKAGE_NAME);
            String channelSecret = uri.getQueryParameter(SchemeConstants.PARAM_CHANNEL_SECRET);
            if (channelSecret == null) {
                channelSecret = "";
            }

//            MiLiveSdkBinder.getInstance().secureOperate(channelId, packageName, channelSecret,
//                    new SecureCommonCallBack() {
//                        @Override
//                        public void postSuccess() {
//                            MyLog.w(TAG, "postSuccess callback");
//                            if (SchemeProcessor.process(uri, host, SchemeSdkActivity.this, true)) {
//                                // activity finish 内置处理
//                                String key = String.format(StatisticsKey.KEY_VIEW_COUNT, channelId);
//                                MyLog.d(TAG, "scheme process statistics=" + key);
//                                if (!TextUtils.isEmpty(key)) {
//                                    MilinkStatistics.getInstance().statisticsOtherActive(key, 1, channelId);
//                                }
//                            } else {
//                                finish();
//                            }
//                        }
//
//                        @Override
//                        public void postError() {
//                            MyLog.w(TAG, "postError");
//                            finish();
//                        }
//
//                        @Override
//                        public void processFailure() {
//                            MyLog.w(TAG, "processFailure");
//                            finish();
//                        }
//                    });
        } else if (scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
            // 内部处理，不对外暴露
            if (WaliliveProcessor.process(uri, host, this, true)) {
                // activity finish 内置处理
            } else {
                finish();
            }
        } else if (SpecificProcessor.process(uri, scheme, this)) {
            finish();
        } else {
            finish();
        }
    }

    /**
     * 是否工信部的包
     * "5005_1_android".equalsIgnoreCase(Constants.ReleaseChannel)
     *
     * @return
     */
    public boolean isNeedShowCtaDialog() {
        if ("5005_1_android".equals(U.getChannelUtils().getChannel())) {
            return PreferenceUtils.getSettingBoolean(CTANotifyFragment.PREF_KEY_NEED_SHOW_CTA, true);
        }
        return false;
    }
}

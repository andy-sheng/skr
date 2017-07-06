package com.wali.live.watchsdk.scheme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.wali.live.watchsdk.callback.SecureCommonCallBack;
import com.wali.live.michannel.ChannelParam;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.scheme.processor.SchemeProcessor;
import com.wali.live.watchsdk.scheme.processor.WaliliveProcessor;
import com.wali.live.watchsdk.scheme.specific.SpecificProcessor;

/**
 * Created by lan on 17/2/21.
 */
public class SchemeSdkActivity extends BaseSdkActivity {
    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    private static final String EXTRA_CHANNEL_SECRET = "extra_channel_secret";

    private Intent mIntent;
    private Uri mUri;
    private Handler mHandler = new Handler();
    @Override
    protected String getTAG() {
        return SchemeConstants.LOG_PREFIX + SchemeSdkActivity.class.getSimpleName() + "@" + hashCode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });
    }

    private void process() {
        mIntent = getIntent();
        if (mIntent == null) {
            MyLog.w(TAG, "process intent is null");
            finish();
            return;
        }
        mUri = mIntent.getData();
        if (mUri == null) {
            MyLog.w(TAG, "process intent data uri is null");
            finish();
        }
        MyLog.d(TAG, "process intent data uri=" + mUri);
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

        if (scheme.equals(SchemeConstants.SCHEME_LIVESDK)) {
            int channelId = mIntent.getIntExtra(EXTRA_CHANNEL_ID, 0);
            String packageName = mIntent.getStringExtra(EXTRA_PACKAGE_NAME);
            String channelSecret = mIntent.getStringExtra(EXTRA_CHANNEL_SECRET);
            if (channelSecret == null) {
                channelSecret = "";
            }

            /**
             * TODO 登录的回调方式可能需要重新修改为 SecureLoginCallback
             */
            MiLiveSdkBinder.getInstance().secureOperate(channelId, packageName, channelSecret,
                    new SecureCommonCallBack() {
                        @Override
                        public void postSuccess() {
                            MyLog.w(TAG, "postSuccess callback");
                            if (SchemeProcessor.process(uri, host, SchemeSdkActivity.this, true)) {
                                // activity finish 内置处理
                            } else {
                                finish();
                            }
                        }

                        @Override
                        public void postError() {
                            MyLog.w(TAG, "postError");
                            finish();
                        }

                        @Override
                        public void processFailure() {
                            MyLog.w(TAG, "processFailure");
                            finish();
                        }
                    });
        } else if (scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
            // 内部处理，不对外暴露
            if (WaliliveProcessor.process(uri, host, this, true)) {
                // activity finish 内置处理
            } else {
                finish();
            }
        } else if (SpecificProcessor.process(uri, scheme, this)) {
            // activity finish 内置处理
        } else {
            finish();
        }
    }

    public static void openActivity(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, SchemeSdkActivity.class);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}

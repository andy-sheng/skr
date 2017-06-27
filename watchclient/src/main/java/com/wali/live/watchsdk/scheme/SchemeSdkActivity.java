package com.wali.live.watchsdk.scheme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.wali.live.watchsdk.callback.ISecureCallBack;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.scheme.processor.SchemeProcessor;
import com.wali.live.watchsdk.scheme.processor.WaliliveProcessor;

/**
 * Created by lan on 17/2/21.
 */
public class SchemeSdkActivity extends BaseSdkActivity {
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
        Intent intent = getIntent();
        if (intent == null) {
            MyLog.w(TAG, "process intent is null");
            finish();
            return;
        }
        mUri = intent.getData();
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

        if (scheme.equals(SchemeConstants.SCHEME_LIVESDK)) {
            final String host = uri.getHost();
            MyLog.w(TAG, "process host=" + host);
            if (TextUtils.isEmpty(host)) {
                finish();
                return;
            }

            int channelId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_CHANNEL_ID, 0);
            String packageName = uri.getQueryParameter(SchemeConstants.PARAM_PACKAGE_NAME);
            String channelSecret = uri.getQueryParameter(SchemeConstants.PARAM_CHANNEL_SECRET);
            if (channelSecret == null) {
                channelSecret = "";
            }

            MiLiveSdkBinder.getInstance().secureOperate(channelId, packageName, channelSecret, new ISecureCallBack() {
                @Override
                public void process(Object... objects) {
                    if (SchemeProcessor.process(uri, host, SchemeSdkActivity.this, true)) {
                        // activity finish 内置处理
                    } else {
                        finish();
                    }
                }

                @Override
                public void processFailure() {
                    MyLog.w(TAG, "processFailure");
                    finish();
                }
            });
        } else if (scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
            String host = uri.getHost();
            MyLog.w(TAG, "process host=" + host);
            if (TextUtils.isEmpty(host)) {
                finish();
                return;
            }

            if (WaliliveProcessor.process(uri, host, this, true)) {
                // activity finish 内置处理
            } else {
                finish();
            }
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

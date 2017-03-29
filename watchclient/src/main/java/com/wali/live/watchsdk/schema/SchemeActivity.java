package com.wali.live.watchsdk.schema;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.michannel.ChannelParam;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.schema.processor.WaliliveProcessor;

/**
 * 处理各种scheme URI, 中转的<br/>
 * wiki:<a>http://wiki.n.miui.com/pages/viewpage.action?pageId=20024995</a><br/>
 * Created by chengsimin on 16/3/9.<br/>
 * Changed by yaojian on 16/03/15<br/>
 *
 * @module scheme
 */
public class SchemeActivity extends RxActivity {
    public final static String TAG = SchemeActivity.class.getSimpleName();
    public final static String HOST_WAKE_UP = "wakeup";             // 用户唤醒的schema
    public final static String PARAMETER_WEAK_UP_URL = "url";       // 用户唤醒之后需要跳转的schema

    public static final String EXTRA_RECOMMEND_TAG = "extra_recommend_tag";
    public static final String EXTRA_CHANNEL_PARAM = "extra_channel_param";

    private Uri mUriFromIntent;

    private ChannelParam mChannelParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_launcher);

        MyLog.w(TAG, "onCreate intent action()=" + getIntent().getAction());
        processIntent();
    }

    private void processIntent() {
        boolean hasAccount = UserAccountManager.getInstance().hasAccount();
        MyLog.d(TAG, "processIntent hasAccount=" + hasAccount);

        Intent intent = getIntent();
        if (intent == null) {
            MyLog.w(TAG, "processIntent intent is null");
            finish();
            return;
        }
        mUriFromIntent = intent.getData();
        try {
            process(mUriFromIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void process(Uri uri) throws Exception {
        MyLog.v(TAG, "process uri=" + uri);
        if (uri == null) {
            finish();
            return;
        }

        String scheme = uri.getScheme();
        MyLog.v(TAG, "process scheme=" + scheme);
        if (TextUtils.isEmpty(scheme)) {
            finish();
            return;
        }

        if (scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
            String host = uri.getHost();
            MyLog.v(TAG, "process host=" + host);
            if (TextUtils.isEmpty(host)) {
                finish();
                return;
            }

            if (WaliliveProcessor.process(uri, host, this, true)) {
                // activity finish 内置处理
            } else {
                MyLog.w(TAG, "process unknown host=" + host);
                finish();
            }
        } else {
            MyLog.w(TAG, "process scheme unknown, uri=" + uri);
            finish();
        }
    }

    public static void openActivity(Activity activity, Uri uri) {
        Intent intent = new Intent(activity, SchemeActivity.class);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}

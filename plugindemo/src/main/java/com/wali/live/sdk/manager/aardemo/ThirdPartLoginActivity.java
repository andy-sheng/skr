package com.wali.live.sdk.manager.aardemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.aardemo.global.GlobalData;
import com.wali.live.sdk.manager.aardemo.utils.RSASignature;
import com.wali.live.sdk.manager.aardemo.utils.ToastUtils;

public class ThirdPartLoginActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String FEMALE_HEAD_URL = "http://img.woyaogexing.com/touxiang/katong/20140110/3464c9c3a113f542.jpg!200X200.jpg";

    private static final String MALE_HEAD_URL = "http://img.woyaogexing.com/touxiang/katong/20140110/feda12f439549afd.jpg!200X200.jpg";

    public static final String KEY_CHANNELID = "channelId";

    private static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMC8ISWECSak6Z1X" +
                            "tgTy9jrq85dZ7Z95CndJ6Sz0ty5fiVqiJ4WrRf7d+78hlEOvlE0fwLQraHZ28gkD" +
                            "kdNX1ycFDV+SBDTn+rFnRJQZjA8t3cQGiJmpyFIpaSzpz9PMTScxDmmxygUzsTXe" +
                            "sCcFV8p9thCyJj5kGsUFxzkfwR7dAgMBAAECgYAqUCMmzVoE9eej94GqjHyqarKX" +
                            "49JbVIOLtNpQWFlvAOJy12691eBEGBAQ4hpe0clJNVNlOrJwb6SrffEh6QL+2Aht" +
                            "oocO7ST4kGpYTk53ofkK9AOwdZkhhzn226qRlDFN+OyAedLsv5sZ3166KTfxaCkO" +
                            "5/KeXuD9BucT4eHTMQJBAPUGugXFJBVUZimsqwi5PKBGtmqQEJAi5M0vZvGz4vtq" +
                            "H8pXQVYHOwAQA2Kmx7LSWqUa5EZCfKQIHE88dhmcru8CQQDJXd825pM0FW6ENr/9" +
                            "IZLZBMgOlFG06WkVa442trbViGP0TPJMeEzBHoCDtlxDUxKcbFworXvVk+f8SYUo" +
                            "6g7zAkAJSIb1vwFd+YOhYpRcUUBVxjgVE349J8VJbNlWoP0hj2TC8slb7Aw1NWYb" +
                            "b7wzLzsV9E3fx5cXU+NWsTC8Sa5rAkEAw8DL4/UWmQVUoJcQ4KUoumwZh4LMQ1C8" +
                            "5SPf5nSNHNwwPygmTAyOoRZj3KcE3jX9267DkI/F2ISmeu2F05Zl3QJAX8qggola" +
                            "wpkdbvZn81X80lFuye6b0KjSWqlrrQLtjSR9/ov/avbuEDI+Ni4rDZn5a0rkGuaN" +
                            "DzBZBemtWvPkjg==";

    private EditText mUidEt;

    private EditText mNameEt;

    private RadioGroup mRadioGroup;

    private RadioButton mMaleRb;

    private RadioButton mFemaleRb;

    private Button submitBtn;

    private int sex;

    private int channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_part_login);
        mUidEt = (EditText) findViewById(R.id.uid);
        mNameEt = (EditText) findViewById(R.id.name);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mMaleRb = (RadioButton) findViewById(R.id.radioMale);
        mFemaleRb = (RadioButton) findViewById(R.id.radioFemale);
        submitBtn = (Button) findViewById(R.id.submit_btn);

        mRadioGroup.setOnCheckedChangeListener(this);
        submitBtn.setOnClickListener(this);

        Intent intent = getIntent();
        channelId = intent.getIntExtra(KEY_CHANNELID, 0);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mMaleRb.getId() == checkedId) {
            sex = 1;
        } else {
            sex = 2;
        }
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mUidEt.getText().toString())) {
            ToastUtils.showToast(this, R.string.uid_is_empty);
            return;
        }
        if (TextUtils.isEmpty(mNameEt.getText().toString())) {
            ToastUtils.showToast(this, R.string.name_is_empty);
            return;
        }
        String uid = mUidEt.getText().toString();
        String name = mNameEt.getText().toString();

        String headUrl;
        if (sex == 1) {
            headUrl = MALE_HEAD_URL;
        } else {
            headUrl = FEMALE_HEAD_URL;
        }

        String signStr = "channelId=" + channelId + "&headUrl=" + headUrl + "&nickname=" + name + "&sex=" + sex + "&xuid=" + uid;
        String sign = RSASignature.sign(signStr, RSA_PRIVATE_KEY, "UTF-8");
        MiLiveSdkController.getInstance().thirdPartLogin(channelId, uid, sex, name, headUrl, sign, new IMiLiveSdk.IAssistantCallback() {
            @Override
            public void notifyVersionLow() {
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.version_low);
            }

            @Override
            public void notifyNotInstall() {
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.not_install);
            }
        });
        finish();

    }

}

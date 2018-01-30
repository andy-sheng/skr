package com.wali.live.sdk.manager.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.aardemo.R;
import com.wali.live.sdk.manager.demo.global.GlobalData;
import com.wali.live.sdk.manager.demo.utils.RSASignature;
import com.wali.live.sdk.manager.demo.utils.ToastUtils;

public class EditUserInfoActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String FEMALE_HEAD_URL = "http://img.woyaogexing.com/touxiang/katong/20140110/3464c9c3a113f542.jpg!200X200.jpg";

    private static final String MALE_HEAD_URL = "http://img.woyaogexing.com/touxiang/katong/20140110/feda12f439549afd.jpg!200X200.jpg";

    public static final String KEY_CHANNELID = "channelId";

    private EditText mUidEt;

    private EditText mNameEt;

    private RadioGroup mRadioGroup;

    private RadioButton mMaleRb;

    private RadioButton mFemaleRb;

    private Button submitBtn;

    private EditText channelIdEt;

    private Button channelJumpBtn;

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

        initChannel();
    }

    private void initChannel() {
        channelIdEt = (EditText) findViewById(R.id.channel_et);
        channelJumpBtn = (Button) findViewById(R.id.channel_btn);
        channelJumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String channelId = channelIdEt.getText().toString();
                if (!TextUtils.isEmpty(channelId)) {
                    String uri = "livesdk://channel?channel=50001&package_name=com.wali.live.sdk.manager.demo&channel_id=" + channelId;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                } else {
                    ToastUtils.showToast("channel id empty");
                }
            }
        });
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
        MiLiveSdkController.getInstance().editUserInfo(channelId, uid, sex, name, headUrl, new IMiLiveSdk.IAssistantCallback() {
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

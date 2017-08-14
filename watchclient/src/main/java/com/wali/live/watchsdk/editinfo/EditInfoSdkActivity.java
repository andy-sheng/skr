package com.wali.live.watchsdk.editinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 2017/8/14.
 */
public class EditInfoSdkActivity extends BaseSdkActivity implements View.OnClickListener {
    public final static String EXTRA_OUT_INFO_CHANGED = "info_changed";

    private BackTitleBar mTitleBar;

    private boolean mInfoChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        initView();
    }

    private void initView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(getString(R.string.change_info));
        mTitleBar.getBackBtn().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            clickBackBtn();
        }
    }

    private void clickBackBtn() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUT_INFO_CHANGED, mInfoChanged);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, EditInfoSdkActivity.class);
        activity.startActivity(intent);
    }
}

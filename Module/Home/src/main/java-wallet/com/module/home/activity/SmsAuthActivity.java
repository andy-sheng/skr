package com.module.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.fragment.SmsAuthFragment;
import com.module.home.fragment.WithdrawFragment;

@Route(path = RouterConstants.ACTIVITY_SMS_AUTH)
public class SmsAuthActivity extends BaseActivity {
    public RelativeLayout mMainActContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "onCreate" + " savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // TODO: 2019/3/11
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
//        inframesker://wallet/withdraw?from=alipay
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, SmsAuthFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}

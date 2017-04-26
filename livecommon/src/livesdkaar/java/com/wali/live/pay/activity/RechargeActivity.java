package com.wali.live.pay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.base.activity.BaseActivity;
import com.base.fragment.FragmentListener;
import com.live.module.common.R;
import com.wali.live.recharge.view.RechargeFragment;

/**
 * @module 充值
 * Created by chengsimin on 16/2/23.
 * Google Wallet部分由rongzhisheng修改 on 16/5/20
 * MVP重构
 * 增加PayPal支付方式 by rongzhisheng
 * 增加小米钱包支付方式 by rongzhisheng
 * 支持滑动重构 by rongzhisheng 16/7/12
 */
public class RechargeActivity extends BaseActivity {
    public static String TAG = RechargeActivity.class.getSimpleName();

    private RechargeFragment mRechargeFragment;

    public void setRechargeFragment(@NonNull RechargeFragment rechargeFragment) {
        mRechargeFragment = rechargeFragment;
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    public static void openActivity(@NonNull Activity fromActivity, @Nullable Bundle bundle) {
        if (fromActivity == null) {
            return;
        }
        Intent intent = new Intent(fromActivity, RechargeActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        fromActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recharge_activity_layout);
        Bundle bundle = getIntent() == null ? null : getIntent().getExtras();
        mRechargeFragment = RechargeFragment.openFragment(this, R.id.main_act_container, bundle, false);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mRechargeFragment != null) {
            mRechargeFragment.onFragmentResult(requestCode, resultCode, data == null ? null : data.getExtras());
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                if (null != fragment && fragment instanceof FragmentListener) {
                    if (((FragmentListener) fragment).onBackPressed()) {
                        return;
                    }
                }
            }
        }

        super.onBackPressed();
    }

}

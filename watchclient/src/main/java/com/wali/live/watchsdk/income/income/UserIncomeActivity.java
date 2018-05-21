package com.wali.live.watchsdk.income.income;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.region.Region;
import com.wali.live.proto.UserProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * @module 提现
 * Created by qianyuan on 2/27/16.
 */
public class UserIncomeActivity extends BaseSdkActivity {
    private static final String TAG = UserIncomeActivity.class.getSimpleName();

    public static final String BUNDLE_TADAY_EXCHANGE_MONEY = "budle_today_can_exchange_money";
    public static final String BUNDLE_VERIFICATION_STATE = "bundle_verification_state";
    public static final String BUNDLE_BIND_ACCOUNT = "bundle_bind_account";
    public static final String BUNDLE_BIND_AVATAR = "bundle_bind_avatar";
    public static final String BUNDLE_BIND_FRIST_NAME = "bundle_bind_first_name";
    public static final String BUNDLE_BIND_LAST_NAME = "bundle_bind_last_name";

    private final int ACTION_INCOME_TITLEBAR_BACKBTN = 100;
    private final int ACTION_INCOME_TITLEBAR_RIGHTBTN = 102;

    private final int ACTION_INCOME_EXCHANGE_DIAMOND = 103;
    private final int ACTION_INCOME_GET_ALI_MONEY = 201;
    private final int ACTION_INCOME_GET_WX_MONEY = 202;
    private final int ACTION_INCOME_GET_PAYPAL_MONEY = 203;

    public static final int REQUEST_CODE_WX_WITHDRAW = 1001;

    public static final int REQUEST_CODE_PAYPAL_WITHDRAW = 1002;

    private boolean mIsAddFragment = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_income_activity);

        if (AccountAuthManager.triggerActionNeedAccount(this)) {
            addFragment();
        }
    }

    private void addFragment() {
        if (mIsAddFragment) {
            return;
        }
        mIsAddFragment = true;

        Region region = MyUserInfoManager.getInstance().getRegion();
        if (region != null && "IN".equals(region.getCountryCode())) {
            FragmentNaviUtils.addFragment(this, R.id.main_act_container, IndiaIncomeFragment.class, null, true, false, true);
        } else {
            FragmentNaviUtils.addFragment(this, R.id.main_act_container, NormalIncomeFragment.class, null, true, false, true);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            FragmentNaviUtils.popFragment(this);
            return;
        }
        finish();
    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusLogined event) {
        MyLog.w(TAG, "receive event : statusLogin");
        if (event != null) {
            addFragment();
        }
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, UserIncomeActivity.class);
        activity.startActivity(intent);
    }
}

package com.wali.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.liveassistant.R;
import com.wali.live.watchsdk.login.LoginPresenter;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Deprecated
public class LoginActivity extends BaseSdkActivity {
    private TextView mRegisterTv;
    private TextView mTestTv;
    private TextView mTest2Tv;
    private LoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initPresenters();
    }

    private void initViews() {
        mRegisterTv = $(R.id.register_tv);
        mRegisterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginPresenter.systemLogin(0);
            }
        });

        mTestTv = $(R.id.test_tv);
        mTestTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, WatchSdkActivity.class);
                RoomInfo mRoomInfo = RoomInfo.Builder.newInstance(100199, "1111", "null").build();

                intent.putExtra(WatchSdkActivity.EXTRA_ROOM_INFO, mRoomInfo);
                LoginActivity.this.startActivity(intent);
                finish();
            }
        });

        mTest2Tv = $(R.id.test2_tv);
        mTest2Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    private void initPresenters() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventLogin(AccountEventController.LoginEvent event) {
        if (event != null) {
            MyLog.w(TAG, "onEventLogin AccountEventController.LoginEvent");
            MainActivity.openActivity(this);
            finish();
        }
    }

    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }
}

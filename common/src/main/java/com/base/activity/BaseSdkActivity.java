package com.base.activity;

import android.os.Bundle;

import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.base.utils.SelfUpdateManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

/**
 * Created by lan on 15/11/12.
 *
 * @module sdk activity
 */
public abstract class BaseSdkActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyLog.w(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        MyLog.w(TAG, "onStart");
        super.onStart();
    }


    @Override
    protected void onStop() {
        MyLog.w(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        MyLog.w(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        MyLog.w(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SdkEventClass.FinishActivityEvent event) {
    }
}

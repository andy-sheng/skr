package com.base.permission.rxpermission;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;

import com.base.activity.BaseActivity;

public abstract class EnsureSameProcessActivity extends BaseActivity {
    private static final String KEY_ORIGINAL_PID = "key_original_pid";
    private int mOriginalProcessId;

    public EnsureSameProcessActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            this.mOriginalProcessId = Process.myPid();
        } else {
            this.mOriginalProcessId = savedInstanceState.getInt("key_original_pid", this.mOriginalProcessId);
            boolean restoredInAnotherProcess = this.mOriginalProcessId != Process.myPid();
            if(restoredInAnotherProcess) {
                this.finish();
            }
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("key_original_pid", this.mOriginalProcessId);
    }
}

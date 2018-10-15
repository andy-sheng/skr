package com.base.permission.rxpermission;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.base.event.None;

import org.greenrobot.eventbus.Subscribe;

@TargetApi(23)
public class ShadowActivity extends EnsureSameProcessActivity {
    private boolean[] shouldShowRequestPermissionRationale;

    public ShadowActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //remove statusbar

        if (savedInstanceState == null) {
            this.handleIntent(this.getIntent());
        }
    }

    @Subscribe
    public void onEvent(None none) {

    }

    protected void onNewIntent(Intent intent) {
        this.handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String[] permissions = intent.getStringArrayExtra("permissions");
        this.requestPermissions(permissions, 42);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        this.shouldShowRequestPermissionRationale = new boolean[permissions.length];

        for (int i = 0; i < permissions.length; ++i) {
            this.shouldShowRequestPermissionRationale[i] = this.shouldShowRequestPermissionRationale(permissions[i]);
        }

        RxPermissions.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults, this.shouldShowRequestPermissionRationale);
        this.finish();
    }

}

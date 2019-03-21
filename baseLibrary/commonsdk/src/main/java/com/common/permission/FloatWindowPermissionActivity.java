package com.common.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.common.floatwindow.PermissionListener;

/**
 * 用于在内部自动申请权限
 * https://github.com/yhaolpz
 */

public class FloatWindowPermissionActivity extends Activity {

    private static PermissionUtils.RequestPermission mPermissionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatWindowPermission.requestPermission(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 756232212) {
            if (mPermissionListener != null) {
                if (FloatWindowPermission.checkFloatWindow(this)) {
                    mPermissionListener.onRequestPermissionSuccess();
                } else {
                    mPermissionListener.onRequestPermissionFailure(null);
                }
            }
            mPermissionListener = null;
        }
        finish();
    }

    static synchronized void request(Context context, PermissionUtils.RequestPermission permissionListener) {
        if (FloatWindowPermission.checkFloatWindow(context)) {
            if (permissionListener != null) {
                permissionListener.onRequestPermissionSuccess();
            }
            return;
        }
        mPermissionListener = permissionListener;
        Intent intent = new Intent(context, FloatWindowPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}

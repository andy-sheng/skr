package com.common.core.permission;

import android.Manifest;
import android.app.Activity;

public class SkrSdcardPermission extends SkrBasePermission {

    public SkrSdcardPermission() {
        super(Manifest.permission.WRITE_EXTERNAL_STORAGE, "请开启撕歌Skr手机存储读写权限,保证app的正常使用", true);
    }

    public void onRequestPermissionFailure1(Activity activity, boolean goSettingIfRefuse) {
        if (goSettingIfRefuse) {
            onReject(activity,mGoPermissionManagerTips);
        }
    }
}

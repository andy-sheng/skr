package com.common.core.permission;

import android.Manifest;

public class SkrPhoneStatePermission extends SkrBasePermission{

    public SkrPhoneStatePermission() {
        super(Manifest.permission.READ_PHONE_STATE, "请开启撕歌Skr手机信息获取权限,保证app的正常使用",true);
    }
}

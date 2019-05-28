package com.common.core.permission;

import android.Manifest;

public class SkrLocationPermission extends SkrBasePermission{

    public SkrLocationPermission() {
        super(Manifest.permission.ACCESS_COARSE_LOCATION, "请开启撕歌Skr定位获取权限,保证app的正常使用",true);
    }
}

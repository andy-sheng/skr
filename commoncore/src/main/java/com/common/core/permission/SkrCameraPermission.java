package com.common.core.permission;

import android.Manifest;

public class SkrCameraPermission extends SkrBasePermission{

    public SkrCameraPermission() {
        super(Manifest.permission.CAMERA, "请开启撕歌Skr相机权限,保证app的正常使用",true);
    }
}

package com.common.core.permission;

import android.Manifest;
import android.app.Activity;

public class SkrAudioPermission extends SkrBasePermission{

    public SkrAudioPermission() {
        this(null);
    }

    public SkrAudioPermission(Activity activity) {
        super(activity,Manifest.permission.RECORD_AUDIO, "请开启撕歌Skr录音权限,保证app的正常使用",true);
    }
}

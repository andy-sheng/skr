package com.common.core.permission;

import android.Manifest;

public class SkrAudioPermission extends SkrBasePermission{

    public SkrAudioPermission() {
        super(Manifest.permission.RECORD_AUDIO, "请开启撕歌Skr录音权限,保证app的正常使用",true);
    }
}

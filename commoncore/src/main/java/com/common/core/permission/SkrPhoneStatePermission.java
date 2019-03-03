package com.common.core.permission;

import android.Manifest;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;

import com.common.core.R;
import com.common.log.MyLog;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.dialog.view.TipsDialogView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.List;

public class SkrPhoneStatePermission extends SkrBasePermission{

    public SkrPhoneStatePermission() {
        super(Manifest.permission.READ_PHONE_STATE, "请开启撕歌Skr手机信息获取权限,保证app的正常使用",true);
    }
}

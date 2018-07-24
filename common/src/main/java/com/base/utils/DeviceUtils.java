package com.base.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.permission.rxpermission.Permission;
import com.base.permission.rxpermission.RxPermissions;
import com.mi.milink.sdk.base.Global;

import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by lizhigang on 15/3/5.
 */
public class DeviceUtils {
    private static String imei;
    private static Subscription mPermissionSubsription;

    /**
     * @return imei号
     */
    public static String getDeviceId() {
        // 整理 考虑中途用户又给了权限的场景
        if (imei == null || "N/A".equals(imei)) {
            if (PermissionUtils.checkReadPhoneState(GlobalData.app())) {
                // 有权限,直接拿
                try {
                    TelephonyManager tm = (TelephonyManager) GlobalData.app().getSystemService(Context.TELEPHONY_SERVICE);
                    imei = tm.getDeviceId();
                } catch (Exception e) {
                    imei = "N/A";
                }
            } else if (imei == null) {
                try {
                    return Settings.Secure.getString(Global.getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                } catch (Exception e2) {
                    return "N/A";
                }
            }
        }
        return imei;
    }

    public static boolean isMi3TD() {
        return android.os.Build.PRODUCT.contains("pisces");
    }

    public static boolean isMi3C() {
        String deviceName = Build.MODEL.toLowerCase();
        return deviceName.equals("mi 3c");
    }

    public static boolean isMi2A() {
        String deviceName = Build.MODEL.toLowerCase();
        return deviceName.equals("mi 2a");
    }

    public static boolean isCompatibleMode() {
        return isMi3C() || isMi3TD() || isMi2A();
    }
}

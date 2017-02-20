package com.base.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.permission.rxpermission.Permission;
import com.base.permission.rxpermission.RxPermissions;

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
                if (mPermissionSubsription == null || mPermissionSubsription.isUnsubscribed()) {
                    mPermissionSubsription = RxPermissions.getInstance(GlobalData.app())
                            .requestEach(Manifest.permission.READ_PHONE_STATE)
                            .subscribe(new Action1<Permission>() {
                                @Override
                                public void call(Permission permission) {
                                    if (permission.granted) {
                                        TelephonyManager tm = (TelephonyManager) GlobalData.app().getSystemService(Context.TELEPHONY_SERVICE);
                                        imei = tm.getDeviceId();
                                    } else if (permission.shouldShowRequestPermissionRationale) {
                                        imei = "N/A";
//                                        ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.permission_deny_read_phone_state));
                                    } else {
                                        imei = "N/A";
//                                        ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.permission_deny_read_phone_state));
                                    }
                                    mPermissionSubsription = null;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    MyLog.e(throwable);
                                }
                            });
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

/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.common.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.common.log.MyLog;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ================================================
 * 权限请求工具类
 *
 * @see <a href="https://github.com/JessYanCoding/MVPArms/wiki#3.9">PermissionUtil wiki 官方文档</a>
 * Created by JessYan on 17/10/2016 10:09
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PermissionUtils {
    public static final String TAG = "Permission";


    PermissionUtils() {
    }

    public boolean checkReadPhoneState(Activity activity) {
        return checkPermission(activity, Manifest.permission.READ_PHONE_STATE);
    }

    public boolean checkRecordAudio(Activity activity) {
        return checkPermission(activity, Manifest.permission.RECORD_AUDIO);
    }

    public boolean checkExternalStorage(Activity activity) {
        return checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean checkCamera(Activity activity) {
        return checkPermission(activity, Manifest.permission.CAMERA);
    }

    public boolean checkLocation(Activity activity) {
        return checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean checkPermission(Activity activity, String permission) {
        if (activity == null) {
            activity = U.getActivityUtils().getTopActivity();
            if (activity == null) {
                return false;
            }
        }
        return new RxPermissions(activity).isGranted(permission);
    }

    public boolean checkPermission(RxPermissions rxPermissions, String permission) {
        return rxPermissions.isGranted(permission);
    }


    public interface RequestPermission {
        /**
         * 权限请求成功
         */
        void onRequestPermissionSuccess();

        /**
         * 用户拒绝了权限请求, 权限请求失败, 但还可以继续请求该权限
         *
         * @param permissions 请求失败的权限名
         */
        void onRequestPermissionFailure(List<String> permissions);

        /**
         * 用户拒绝了权限请求并且用户选择了以后不再询问, 权限请求失败, 这时将不能继续请求该权限, 需要提示用户进入设置页面打开该权限
         *
         * @param permissions 请求失败的权限名
         */
        void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions);
    }

    public void requestPermission(final RequestPermission requestPermission, Activity activity, String... permissions) {
        if (activity == null) {
            activity = U.getActivityUtils().getTopActivity();
            if (activity == null) {
                return;
            }
        }
        requestPermission(requestPermission, new RxPermissions(activity), permissions);
    }

    public void requestPermission(final RequestPermission requestPermission, RxPermissions rxPermissions, String... permissions) {
        if (permissions == null || permissions.length == 0) return;

        List<String> needRequest = new ArrayList<>();
        for (String permission : permissions) { //过滤调已经申请过的权限
            if (!rxPermissions.isGranted(permission)) {
                needRequest.add(permission);
            }
        }

        if (needRequest.isEmpty()) {//全部权限都已经申请过，直接执行操作
            requestPermission.onRequestPermissionSuccess();
        } else {//没有申请过,则开始申请
            rxPermissions
                    .requestEach(needRequest.toArray(new String[needRequest.size()]))
                    .buffer(permissions.length)
                    .subscribe(new Observer<List<Permission>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(List<Permission> permissions) {
                            for (Permission p : permissions) {
                                if (!p.granted) {
                                    if (p.shouldShowRequestPermissionRationale) {
                                        requestPermission.onRequestPermissionFailure(Arrays.asList(p.name));
                                        return;
                                    } else {
                                        requestPermission.onRequestPermissionFailureWithAskNeverAgain(Arrays.asList(p.name));
                                        return;
                                    }
                                }
                            }
                            requestPermission.onRequestPermissionSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

    }

    /**
     * 请求Location
     */
    public void requestLocation(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * 请求录音权限
     */
    public void requestRecordAudio(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * 请求摄像头权限
     */
    public void requestCamera(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }


    /**
     * 请求外部存储的权限
     */
    public void requestExternalStorage(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    /**
     * 请求发送短信权限
     */
    public void requestSendSms(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.SEND_SMS);
    }


    /**
     * 请求打电话权限
     */
    public void requestCallPhone(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.CALL_PHONE);
    }


    /**
     * 请求获取手机状态的权限
     */
    public void requestReadPhonestate(RequestPermission requestPermission, Activity activity) {
        requestPermission(requestPermission, activity, Manifest.permission.READ_PHONE_STATE);
    }



    /**
     * 跳转到APP权限设置界面
     */
    public void goToPermissionManager(Activity refs) {
            Intent intent;
            if (U.getDeviceUtils().isMiui()) {
                PackageManager pm = refs.getPackageManager();
                PackageInfo info;
                try {
                    info = pm.getPackageInfo(refs.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    com.common.log.MyLog.e(e);
                    return;
                }
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                // i.setClassName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor");
                intent.putExtra("extra_pkgname", refs.getPackageName());      // for MIUI 6
                intent.putExtra("extra_package_uid", info.applicationInfo.uid);  // for MIUI 5
            } else {
                intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
            }
            if (U.getCommonUtils().isIntentAvailable(refs,intent)) {
                refs.startActivity(intent);
            }
    }
}


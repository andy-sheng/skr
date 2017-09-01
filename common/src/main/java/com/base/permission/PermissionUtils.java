package com.base.permission;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.base.activity.BaseActivity;
import com.base.common.R;
import com.base.dialog.DialogUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.permission.rxpermission.Permission;
import com.base.permission.rxpermission.RxPermissions;
import com.base.utils.CommonUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import rx.functions.Action1;

/**
 * Created by chenyong on 16/2/29.
 */
public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    /**
     * 权限类型，枚举类型
     */
    public enum PermissionType {
        CAMERA,                 // 相机
        RECORD_AUDIO,           // 录音
        SYSTEM_ALERT_WINDOW,    // 悬浮窗
        WRITE_EXTERNAL_STORAGE, // sdcard
        ACCESS_COARSE_LOCATION, // 粗略定位
        ACCESS_FINE_LOCATION,   // 精细定位
        READ_PHONE_STATE,       // 手机信息
        GET_ACCOUNTS,           // 帐号
    }

    /**
     * 检查相机权限
     */
    public static boolean checkCamera(Context context) {
        return checkPermissionWithType(context, PermissionType.CAMERA);
    }

    /**
     * 检查录音权限
     */
    public static boolean checkRecordAudio(Context context) {
        return checkPermissionWithType(context, PermissionType.RECORD_AUDIO);
    }

    /**
     * 检查悬浮窗权限
     */
    public static boolean checkSystemAlertWindow(Context context) {
        return checkPermissionWithType(context, PermissionType.SYSTEM_ALERT_WINDOW);
    }

    /**
     * 检查sdcard权限
     */
    public static boolean checkSdcardAlertWindow(Context context) {
        return checkPermissionWithType(context, PermissionType.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 检查定位权限
     */
    public static boolean checkAccessLocation(Context context) {
        return checkPermissionWithType(context, PermissionType.ACCESS_COARSE_LOCATION)
                || checkPermissionWithType(context, PermissionType.ACCESS_FINE_LOCATION);
    }

    /**
     * 检查相机权限
     */
    public static boolean checkReadPhoneState(Context context) {
        return checkPermissionWithType(context, PermissionType.READ_PHONE_STATE);
    }

    /**
     * 检查访问手机帐号权限
     */
    public static boolean checkGetAccounts(Context context) {
        return checkPermissionWithType(context, PermissionType.GET_ACCOUNTS);
    }

    /**
     * 检查是否具有type指定的权限
     */
    private static boolean checkPermissionWithType(Context context, PermissionType type) {
        boolean result = true;
        String permission = "" + type;
        if (context != null) {
            if (type == PermissionType.SYSTEM_ALERT_WINDOW && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = Settings.canDrawOverlays(context);
            } else {
                result = ContextCompat.checkSelfPermission(context, "android.permission." + type) == PackageManager.PERMISSION_GRANTED;
            }
        } else {
            /**下面暂时都用不到**/
            // 是miui且不是国际版
            if (CommonUtils.isMIUIGlobal() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return checkPermission(context, "android.permission." + permission);
            }
            // 6.0且不是miui
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !CommonUtils.isMIUI()) {
                return checkPermission(context, "android.permission." + permission);
            }
            // 特定判断国内miui
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    AppOpsManager mgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                    PackageManager pm = context.getPackageManager();
                    PackageInfo info;
                    info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                    Class<?> classType = android.app.AppOpsManager.class;
                    Field f = classType.getField("OP_" + permission);
                    Method m = classType.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                    m.setAccessible(true);
                    int status = (Integer) m.invoke(mgr, f.getInt(mgr), info.applicationInfo.uid, info.packageName);
                    // chenyong1 MIUI8用4来做权限标示
                    result = (status != AppOpsManager.MODE_ERRORED && status != AppOpsManager.MODE_IGNORED && status != 4);
                } catch (Exception e) {
                    MyLog.e(TAG, "权限检查出错时默认返回有权限，异常代码：" + e);
                }
            } else {
                result = checkPermission(context, "android.permission." + permission);
            }
        }
        MyLog.d(TAG, "call checkPermissionWithType: " + type + " = " + result);
        return result;
    }


    /**
     * 检查某个权限（属于静态检查，即该权限是否在manifest文件中声明）
     */
    private static boolean checkPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permission, context.getPackageName()));
    }

    /**
     * 跳转到APP权限设置界面
     */
    public static void startPermissionManager(Activity activity) {
        Intent intent;
        if (CommonUtils.isMIUI() || CommonUtils.isMIUIRom()) {
            PackageManager pm = activity.getPackageManager();
            PackageInfo info;
            try {
                info = pm.getPackageInfo(activity.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                MyLog.e(e);
                return;
            }
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            // i.setClassName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor");
            intent.putExtra("extra_pkgname", activity.getPackageName());      // for MIUI 6
            intent.putExtra("extra_package_uid", info.applicationInfo.uid);  // for MIUI 5
        } else {
            intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
        }
        if (CommonUtils.isIntentAvailable(activity, intent)) {
            activity.startActivity(intent);
        }
    }

    /**
     * 当在系统权限管理中没有相应权限时，显示开启权限提示弹窗
     */
    public static void requestPermissionDialog(final Activity activity, final PermissionType type) {
        requestPermissionDialog(activity, type, null, null, null);
    }

    public static void requestPermissionDialog(final Activity activity, final PermissionType type, final IPermissionCallback grantCallback) {
        requestPermissionDialog(activity, type, grantCallback, null, null);
    }

    /**
     * 当在系统权限管理中没有相应权限时，显示开启权限提示弹窗
     */
    public static void requestPermissionDialog(final Activity activity, final PermissionType type, final IPermissionCallback grantCallback, final DialogUtils.IDialogCallback okCallback, final DialogUtils.IDialogCallback cancelCallback) {
        String permission = "android.permission." + type;
        if (!PermissionUtils.checkPermissionWithType(activity, type)) {
            MyLog.d(TAG, "requestPermissionDialog permission: " + permission);
            RxPermissions.getInstance(GlobalData.app()).clearSubjects();
            RxPermissions.getInstance(GlobalData.app())
                    .requestEach(permission)
                    .subscribe(new Action1<Permission>() {
                        @Override
                        public void call(Permission permission) {
                            MyLog.d(TAG, "requestPermissionDialog permission: " + permission);
                            if (permission.granted) {
                                if (grantCallback != null) {
                                    grantCallback.okProcess();
                                }
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                showPermissionDialog(activity, type, okCallback, cancelCallback);
                            } else {
                                showPermissionDialog(activity, type, okCallback, cancelCallback);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(throwable);
                        }
                    });
        } else {
            if (grantCallback != null) {
                grantCallback.okProcess();
            }
        }
    }

    public static void showPermissionDialog(final Activity activity, final PermissionType type) {
        showPermissionDialog(activity, type, null, null);
    }

    public static void showPermissionDialog(final Activity activity, final PermissionType type, DialogUtils.IDialogCallback okCallback, DialogUtils.IDialogCallback cancelCallback) {
        if (okCallback == null) {
            okCallback = new DialogUtils.IDialogCallback() {
                @Override
                public void process(DialogInterface dialogInterface, int i) {
                    startPermissionManager(activity);
                }
            };
        }
        if (cancelCallback == null) {
            cancelCallback = new DialogUtils.IDialogCallback() {
                @Override
                public void process(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            };
        }
        switch (type) {
            case CAMERA:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_camera_permission,
                        R.string.check_camera_video_message,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case RECORD_AUDIO:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_record_audio_permission,
                        R.string.message_record_audio_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case SYSTEM_ALERT_WINDOW:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_floating_window_permission,
                        R.string.message_floating_window_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case WRITE_EXTERNAL_STORAGE:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_sdcard_permission,
                        R.string.message_sdcard_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case ACCESS_COARSE_LOCATION: // fall through
            case ACCESS_FINE_LOCATION:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_location_permission,
                        R.string.message_location_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case GET_ACCOUNTS:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_get_accounts_permission,
                        R.string.message_get_accounts_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            case READ_PHONE_STATE:
                DialogUtils.showNormalDialog(activity,
                        R.string.title_phone_permission,
                        R.string.message_phone_permission,
                        R.string.setting_title,
                        R.string.cancel,
                        okCallback,
                        cancelCallback);
                break;
            default:
                break;
        }
    }

    static boolean sCheckNecessaryPermissionDialogShow = false;

    /**
     * 检查这个app使用必须要的权限
     */
    public static void checkNecessaryPermission(final Activity activity) {
        // 需要判断什么么
        MyLog.d(TAG, "sCheckNecessaryPermissionDialogShow: " + sCheckNecessaryPermissionDialogShow);
        if (!PermissionUtils.checkSdcardAlertWindow(GlobalData.app()) && !sCheckNecessaryPermissionDialogShow) {
            // 自杀
            PermissionUtils.requestPermissionDialog(activity, PermissionType.WRITE_EXTERNAL_STORAGE, null, new DialogUtils.IDialogCallback() {
                @Override
                public void process(DialogInterface dialogInterface, int i) {
                    MyLog.d(TAG, "checkNecessaryPermission ok");
                    startPermissionManager(activity);
                }
            }, new DialogUtils.IDialogCallback() {
                @Override
                public void process(DialogInterface dialogInterface, int i) {
                    MyLog.d(TAG, "checkNecessaryPermission cancel");
                    dialogInterface.dismiss();
                    Process.killProcess(Process.myPid());
                }
            });
        }
    }

    /**
     * 检查相机权限 注意:IPermissionCallback 不要传空值
     */
    public static void checkPermissionByType(BaseActivity baseActivity, PermissionType permissionType, final IPermissionCallback okCallback) {
        if (baseActivity == null || okCallback == null) {
            return;
        }
        if (checkPermissionWithType(baseActivity, permissionType)) {
            okCallback.okProcess();
        } else {
            requestPermissionDialog(baseActivity, permissionType);
        }
    }

    public interface IPermissionCallback {
        void okProcess();
    }

}

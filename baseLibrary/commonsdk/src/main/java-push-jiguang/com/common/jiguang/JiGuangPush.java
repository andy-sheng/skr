package com.common.jiguang;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

public class JiGuangPush {

    public final static String TAG = "JiGuangPush";

    static boolean hasInit = false;
    static String sAlias;

    public static void init(boolean coreProcess) {
        if (hasInit) {
            return;
        }
        MyLog.d(TAG, "init coreProcess=" + coreProcess);
        try {
            /**
             * 强制关闭 MiPush 的日志捕获
             */
            U.getReflectUtils().writeField(MiPushClient.class, null, "isCrashHandlerSuggested", Boolean.valueOf(true));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        JPushInterface.setDebugMode(MyLog.isDebugLogOpen());
        //JPushInterface.stopCrashHandler(U.app());
        JPushInterface.init(U.app());
        JPushInterface.stopCrashHandler(U.app());

        if (!TextUtils.isEmpty(sAlias)) {
            JPushInterface.setAlias(U.app(), 2, sAlias);
            U.getPreferenceUtils().setSettingBoolean("jpush_alias_set", true);
            sAlias = null;
        }
        hasInit = true;
    }

    /**
     * sequence
     * <p>
     * 用户自定义的操作序列号，同操作结果一起返回，用来标识一次操作的唯一性。
     * alias
     * <p>
     * 每次调用设置有效的别名，覆盖之前的设置。
     *
     * @param alias
     */
    public static void setAlias(String alias) {
        MyLog.w(TAG, "setAlias" + " alias=" + alias);
        if (!U.getPreferenceUtils().getSettingBoolean("jpush_alias_set", false)) {
            MyLog.w(TAG, "setAlias" + " alias=" + alias);
            if (hasInit) {
                JPushInterface.setAlias(U.app(), 2, alias);
                U.getPreferenceUtils().setSettingBoolean("jpush_alias_set", true);
            } else {
                sAlias = alias;
            }
        } else {
            MyLog.w("已经设置了alias,cancel");
        }
    }

    public static void clearAlias(String alias) {
        JPushInterface.deleteAlias(U.app(), 2);
    }
}

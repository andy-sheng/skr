package com.common.jiguang;

import com.common.log.MyLog;
import com.common.utils.U;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

public class JiGuangPush {

    static boolean hasInit = false;

    public static void init() {
        if (hasInit) {
            return;
        }
        JPushInterface.setDebugMode(MyLog.isDebugLogOpen());
        JPushInterface.init(U.app());
        JPushInterface.stopCrashHandler(U.app());
        try {
            /**
             * 强制关闭 MiPush 的日志捕获
             */
            U.getReflectUtils().writeField(MiPushClient.class,null,"isCrashHandlerSuggested",Boolean.valueOf(true));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
        init();
        if (!U.getPreferenceUtils().getSettingBoolean("jpush_alias_set", false)) {
            JPushInterface.setAlias(U.app(), 2, alias);
            U.getPreferenceUtils().setSettingBoolean("jpush_alias_set", true);
        }
    }

    public static void clearAlias(String alias) {
        JPushInterface.deleteAlias(U.app(), 2);
    }
}

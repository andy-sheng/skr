package com.common.jiguang;

import com.common.log.MyLog;
import com.common.utils.U;

import cn.jpush.android.api.JPushInterface;

public class JiGuangPush {

    public static void init(){
        JPushInterface.setDebugMode(MyLog.isDebugLogOpen());
        JPushInterface.init(U.app());
        JPushInterface.stopCrashHandler(U.app());
    }

    /**
     * sequence
     *
     * 用户自定义的操作序列号，同操作结果一起返回，用来标识一次操作的唯一性。
     * alias
     *
     * 每次调用设置有效的别名，覆盖之前的设置。
     * @param alias
     */
    public static void setAlias(String alias) {
        JPushInterface.setAlias(U.app(),2,alias);
    }

    public static void clearAlias(String alias) {
        JPushInterface.deleteAlias(U.app(),2);
    }
}

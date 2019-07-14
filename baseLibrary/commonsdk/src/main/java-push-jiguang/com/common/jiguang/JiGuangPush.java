package com.common.jiguang;

import android.os.Bundle;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;

import java.util.HashSet;

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
            Class cls = Class.forName("com.xiaomi.mipush.sdk.MiPushClient");
            U.getReflectUtils().writeField(cls, null, "isCrashHandlerSuggested", Boolean.valueOf(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPushInterface.setDebugMode(MyLog.isDebugLogOpen());
        //JPushInterface.stopCrashHandler(U.app());
        JPushInterface.setChannel(U.app(), U.getChannelUtils().getChannel());
        JPushInterface.init(U.app());
        JPushInterface.stopCrashHandler(U.app());

        if (!TextUtils.isEmpty(sAlias)) {
            JPushInterface.setAlias(U.app(), 1, sAlias);
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
        JPushInterface.deleteAlias(U.app(), 3);
    }

    static String mPendingRoomId;

    public static void joinSkrRoomId(String roomid) {
        if (hasInit) {
            MyLog.d(TAG, "joinSkrRoomId" + " roomid=" + roomid);
            if (U.getChannelUtils().isStaging()) {
                roomid = "dev_" + roomid;
            }
            JPushInterface.cleanTags(U.app(), 4);
            mPendingRoomId = roomid;
        }
    }

    static void joinSkrRoomId2() {
        if (!TextUtils.isEmpty(mPendingRoomId)) {
            HashSet<String> set = new HashSet<>();
            set.add(mPendingRoomId);
            JPushInterface.setTags(U.app(), 5, set);
        }
    }

    public static void exitSkrRoomId(String roomid) {
        if (hasInit) {
            MyLog.d(TAG, "exitSkrRoomId" + " roomid=" + roomid);
            JPushInterface.cleanTags(U.app(), 6);
            mPendingRoomId = null;
        }
    }

    public static void setCustomMsgListener(JPushCustomMsgListener l) {
        sJPushCustomMsgListener = l;
    }

    public static void onReceiveCustomMsg(Bundle bundle) {
        String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String contentType = bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE);
        if (msg != null && contentType != null) {
            byte[] data = U.getBase64Utils().decode(msg);
            if (sJPushCustomMsgListener != null) {
                sJPushCustomMsgListener.onReceive(contentType, data);
            }
        }
    }

    static JPushCustomMsgListener sJPushCustomMsgListener;

    public interface JPushCustomMsgListener {
        void onReceive(String contentType, byte[] data);
    }
}

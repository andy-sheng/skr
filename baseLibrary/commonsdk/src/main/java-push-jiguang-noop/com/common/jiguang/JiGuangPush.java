package com.common.jiguang;

import android.os.Bundle;

public class JiGuangPush {

    public final static String TAG = "JiGuangPush";

    public static void init(boolean coreProcess) {
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
    }

    public static void clearAlias(String alias) {
    }

    public static void joinSkrRoomId(String roomid) {
    }

    static void joinSkrRoomId2() {
    }

    public static void exitSkrRoomId(String roomid) {
    }

    public static void setCustomMsgListener(JPushCustomMsgListener l) {
    }

    public static void onReceiveCustomMsg(Bundle bundle) {
    }

    public interface JPushCustomMsgListener {
        void onReceive(String contentType, byte[] data);
    }
}

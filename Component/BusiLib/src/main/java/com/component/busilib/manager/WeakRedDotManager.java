package com.component.busilib.manager;

import com.common.log.MyLog;
import com.common.utils.U;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 弱红点管理类
 */
public class WeakRedDotManager {

    public final static String TAG = "WeakRedDotManager";

    public final static String SP_KEY_NEW_FRIEND = "SP_KEY_NEW_FRIEND";  //从外到内 个人中心icon2 个人中心好友1
    public final static String SP_KEY_NEW_FANS = "SP_KEY_NEW_FANS";    //从外到内 个人中心icon2 个人中心粉丝1

    public static final int FANS_RED_ROD_TYPE = 1;
    public static final int FRIEND_RED_ROD_TYPE = 2;

    HashMap<Integer, HashSet<WeakRedDotListener>> mMap = new HashMap<>();

    private static class WeakRedDotManagerHolder {
        private static final WeakRedDotManager INSTANCE = new WeakRedDotManager();
    }

    private WeakRedDotManager() {

    }

    public static final WeakRedDotManager getInstance() {
        return WeakRedDotManagerHolder.INSTANCE;
    }

    public synchronized void addListener(WeakRedDotListener listener) {
        MyLog.d(TAG, "addListener" + " listener=" + listener);
        for (int msgType : listener.acceptType()) {
            HashSet<WeakRedDotListener> listenerSet = mMap.get(msgType);
            if (listenerSet == null) {
                listenerSet = new HashSet<>();
                mMap.put(msgType, listenerSet);
            }
            listenerSet.add(listener);
        }
    }

    public synchronized void removeListener(WeakRedDotListener listener) {
        MyLog.d(TAG, "removeListener" + " listener=" + listener);
        for (int msgType : listener.acceptType()) {
            HashSet<WeakRedDotListener> listenerSet = mMap.get(msgType);
            if (listenerSet != null) {
                listenerSet.remove(listener);
            }
        }
    }

    /**
     * 更新红点
     *
     * @param type  红点的类型
     * @param value 红点的值
     */
    public void updateWeakRedRot(int type, int value) {
        MyLog.d(TAG, "updateWeakRedRot" + " type=" + type + " value=" + value);
        if (type == FANS_RED_ROD_TYPE) {
            if (U.getPreferenceUtils().getSettingInt(SP_KEY_NEW_FANS, 0) < value) {
                return;
            } else {
                U.getPreferenceUtils().setSettingInt(SP_KEY_NEW_FANS, value);
            }
        }

        if (type == FRIEND_RED_ROD_TYPE) {
            if (U.getPreferenceUtils().getSettingInt(SP_KEY_NEW_FRIEND, 0) < value) {
                return;
            } else {
                U.getPreferenceUtils().setSettingInt(SP_KEY_NEW_FRIEND, value);
            }
        }

        HashSet<WeakRedDotListener> listenerSet = mMap.get(type);
        if (listenerSet != null) {
            for (WeakRedDotListener weakRedDotListener : listenerSet) {
                weakRedDotListener.onWeakRedDotChange(type, value);
            }
        }
    }

    public interface WeakRedDotListener {
        /**
         * 接受关注的红点类型
         *
         * @return
         */
        int[] acceptType();

        /**
         * 红点状态改变
         *
         * @param type  红点类型
         * @param value 红点对应的值
         */
        void onWeakRedDotChange(int type, int value);
    }

}

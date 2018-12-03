package com.common.statistics;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

public class UmengStatistics {
    public static final String TAG = "UmengStatistics";

    /**
     * 计数事件
     * @param eventId
     */
    public static void recordCountEventNoParams(String eventId){
        if(TextUtils.isEmpty(eventId)){
            MyLog.w(TAG, "eventid is empty");
            return;
        }

        recordCountEvent(eventId, null);
    }

    /**
     * 计数事件
     * @param eventId
     * @param param
     */
    public static void recordCountEvent(String eventId, Map<String, String> param){
        if(TextUtils.isEmpty(eventId)){
            MyLog.w(TAG, "eventid is empty");
            return;
        }

        MobclickAgent.onEvent(U.app().getApplicationContext(), eventId, param);
    }

     /**计算事件
     * @param eventId
     * @param param
     * @param cal  需要计算的
     */
    public static void recordCalculateEvent(String eventId, Map<String, String> param, int cal){
        if(TextUtils.isEmpty(eventId)){
            MyLog.w(TAG, "eventid is empty");
            return;
        }

        MobclickAgent.onEventValue(U.app().getApplicationContext(), eventId, param, cal);
    }
}

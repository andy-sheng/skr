package com.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import com.common.log.MyLog

/**
 * 静态广播 工具类广播 可以实现一些工具类信息的监听
 * 监听网络变化
 * 使用adb 发命令 实现一些工具类的效果
 * adb shell am broadcast -a com.android.test
 */
class CommonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        MyLog.w(TAG, "onReceive context=$context action=$action intent=$intent")
        if ("android.net.conn.CONNECTIVITY_CHANGE" == action) {
            // 网络变化
            MyLog.w("NetworkReceiver", "network changed, NetworkReceiver action=" + intent.action!!)
            U.getNetworkUtils().notifyNetworkChange()
        }else if((U.getAppInfoUtils().packageName+".FLUSH_LOG") == action){
            MyLog.w("CommonReceiver", "刷新日志到文件")
            MyLog.flushLog()
        }
    }

    companion object {
        val TAG = "CommonReceiver"

        @JvmStatic
        fun register() {
            val commonReceiver = CommonReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            intentFilter.addAction(U.getAppInfoUtils().packageName+".FLUSH_LOG")
            U.app().registerReceiver(commonReceiver, intentFilter)
        }
    }
}

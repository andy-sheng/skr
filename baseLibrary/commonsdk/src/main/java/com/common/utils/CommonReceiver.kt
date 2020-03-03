package com.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import android.util.Log

import com.common.log.MyLog
import org.greenrobot.eventbus.EventBus

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
        } else if ((U.getAppInfoUtils().packageName + ".FLUSH_LOG") == action) {
            MyLog.w("CommonReceiver", "刷新日志到文件")
            MyLog.flushLog()
        } else if ("android.intent.action.PHONE_STATE" == action) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d(TAG, "PhoneStateReceiver onReceive state: $state")
            EventBus.getDefault().post(DeviceUtils.IncomingCallEvent(state))
//            TelephonyManager.EXTRA_STATE_IDLE
//            TelephonyManager.EXTRA_STATE_RINGING
//            TelephonyManager.EXTRA_STATE_OFFHOOK
        }
    }

    companion object {
        val TAG = "CommonReceiver"

        @JvmStatic
        fun register() {
            val commonReceiver = CommonReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            intentFilter.addAction(U.getAppInfoUtils().packageName + ".FLUSH_LOG")
            intentFilter.addAction("android.intent.action.PHONE_STATE")
            U.app().registerReceiver(commonReceiver, intentFilter)


            //registerPhoneStateListener()
        }

        // 另一个方式监听来电
        private fun registerPhoneStateListener() {
            val customPhoneStateListener = CustomPhoneStateListener(U.app())
            val telephonyManager = U.app().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager?.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        class CustomPhoneStateListener(private val mContext: Context) : PhoneStateListener() {

            override fun onServiceStateChanged(serviceState: ServiceState) {
                super.onServiceStateChanged(serviceState)
                Log.d(TAG, "CustomPhoneStateListener onServiceStateChanged: $serviceState")
            }

            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                Log.d(TAG, "CustomPhoneStateListener state: $state incomingNumber: $incomingNumber")
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> {
                    }
                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.d(TAG, "CustomPhoneStateListener onCallStateChanged endCall")
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                    }
                }
            }
        }
    }
}

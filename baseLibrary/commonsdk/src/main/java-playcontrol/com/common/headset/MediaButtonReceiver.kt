package com.common.headset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import com.common.log.MyLog
import com.common.playcontrol.PlayOrPauseEvent
import com.common.playcontrol.RemoteControlEvent
import com.common.playcontrol.RemoteControlHelper
import org.greenrobot.eventbus.EventBus

class MediaButtonReceiver : BroadcastReceiver() {
    // 500ms 测试发现每次都会起新的 Receiver 且反注册了仍然能收到事件
    val TAG = "MediaButtonReceiver"+hashCode()
    override fun onReceive(context: Context?, intent: Intent?) {
        RemoteControlHelper.handleKeyEvent(intent)
    }

}
package com.common.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.common.log.MyLog

const val PRE_SONG_ACTION = "SKR_PRE_SONG_ACTION"
const val NEXT_SONG_ACTION = "SKR_NEXT_SONG_ACTION"
const val START_PLAY_SONG_ACTION = "SKR_START_PLAY_SONG_ACTION"
const val STOP_PLAY_SONG_ACTION = "SKR_STOP_PLAY_SONG_ACTION"
const val TRY_WAKEUP_HOME_ACTION = "TRY_WAKEUP_HOME_ACTION"

class NotifitionPlayerActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        MyLog.d("NotifitionPlayerActionReceiver", "onReceive context = $context, intent = $intent")
        when (intent?.action) {
            PRE_SONG_ACTION -> {

            }
            NEXT_SONG_ACTION -> {

            }
            START_PLAY_SONG_ACTION -> {

            }
            STOP_PLAY_SONG_ACTION -> {

            }
            TRY_WAKEUP_HOME_ACTION -> {
                ARouter.getInstance().build("/core/SchemeSdkActivity")
                        .withString("uri", "inframeskr://home/trywakeup")
                        .navigation()
            }
        }
    }

}
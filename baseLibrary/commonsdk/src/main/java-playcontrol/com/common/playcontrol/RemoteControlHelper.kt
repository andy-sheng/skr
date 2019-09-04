package com.common.playcontrol

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import com.common.headset.MediaSessionHelper
import com.common.log.MyLog
import com.common.sensor.SensorManagerHelper
import com.common.utils.DeviceUtils
import com.common.utils.U
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

object RemoteControlHelper {
    val TAG = "PlayControlHelper"
    val sensorManagerHelper = SensorManagerHelper()
    val mediaSessionHelper = MediaSessionHelper()
    internal val shakePageSet = HashSet<String>()
    internal val headsetControlSet = HashSet<String>()

    init {
        EventBus.getDefault().register(this)
    }

    // 注册摇一摇
    fun registerShake(tag: String) {
        MyLog.d(TAG, "register tag = $tag")
        if (shakePageSet.isEmpty()) {
            // 启动传感器
            sensorManagerHelper.register()
        }
        shakePageSet.add(tag)
    }

    fun unregisterShake(tag: String) {
        MyLog.d(TAG, "unregister tag = $tag")
        if(!shakePageSet.isEmpty()){
            shakePageSet.remove(tag)
            if (shakePageSet.isEmpty()) {
                sensorManagerHelper.unregister()
            }
        }
    }

    // 注册线控
    fun registerHeadsetControl(tag: String) {
        if (headsetControlSet.isEmpty()) {
            if (U.getDeviceUtils().headsetPlugOn) {
                // 插着耳机的
                mediaSessionHelper.register()
            }
        }
        headsetControlSet.add(tag)
    }

    fun unregisterHeadsetControl(tag: String) {
        headsetControlSet.remove(tag)
        if (headsetControlSet.isEmpty()) {
            mediaSessionHelper.unregister()
        }
    }

    @Subscribe
    fun onEvent(e: DeviceUtils.HeadsetPlugEvent) {
        if (U.getDeviceUtils().headsetPlugOn && !headsetControlSet.isEmpty()) {
            mediaSessionHelper.register()
        } else {
            mediaSessionHelper.unregister()
        }
    }

    fun startSensor() {
        sensorManagerHelper.startSensor()
    }

    fun stopSensor() {
        sensorManagerHelper.stopSensor()
    }


    private val MSG_PAUSE_OR_PLAY = 10
    private var lastHeadsetHookTs = 0L
    private val uiHanlder = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                MSG_PAUSE_OR_PLAY -> {
                    EventBus.getDefault().post(PlayOrPauseEvent(PlayOrPauseEvent.FROM_HEADSET))
                }
            }
        }
    }

    internal fun handleKeyEvent(intent: Intent?) {
        if (headsetControlSet.isEmpty()) {
            return
        }
        val keyEvent = intent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
            MyLog.d(TAG, "onReceive keyEvent?.keyCode = ${keyEvent?.keyCode} lastHeadsetHookTs=$lastHeadsetHookTs")
            when (keyEvent?.keyCode) {
                KeyEvent.KEYCODE_HEADSETHOOK,
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                -> {
                    MyLog.d(TAG, "KeyEvent.KEYCODE_HEADSETHOOK")
                    val now = System.currentTimeMillis()
                    uiHanlder.removeMessages(MSG_PAUSE_OR_PLAY)
                    if (now - lastHeadsetHookTs < 500) {
                        // 距离上一次点击小于500ms
                        EventBus.getDefault().post(RemoteControlEvent(RemoteControlEvent.FROM_HEADSET))
                        lastHeadsetHookTs = 0
                    } else {
                        uiHanlder.sendEmptyMessageDelayed(MSG_PAUSE_OR_PLAY, 500)
                        lastHeadsetHookTs = now
                    }
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    MyLog.d(TAG, "KeyEvent.KEYCODE_HEADSETHOOK")
                }
            }
        }
    }

}
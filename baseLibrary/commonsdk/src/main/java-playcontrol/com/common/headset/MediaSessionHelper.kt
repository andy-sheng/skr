package com.common.headset

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import com.common.log.MyLog
import com.common.utils.U

class MediaSessionHelper {
    val TAG = "MediaSessionHelper"
    var audioManager: AudioManager? = null
    var componentName: ComponentName? = null

    fun register() {
        register2()
    }

    fun unregister() {
        unregister2()
    }

    private fun register2() {
        MyLog.d(TAG, "register2")
        // 这个ok
        audioManager = U.app().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        // AudioManager注册一个MediaButton对象
        componentName = ComponentName(U.app().packageName, MediaButtonReceiver::class.java.name)
        audioManager?.registerMediaButtonEventReceiver(componentName)
    }

    private fun unregister2() {
        MyLog.d(TAG, "unregister2")
        audioManager?.unregisterMediaButtonEventReceiver(componentName)
    }

    private fun register1() {
        MyLog.d("MediaSessionHelper", "register")
        val componentName = ComponentName(U.app().packageName, MediaButtonReceiver::class.java.name)
        // 组件设置为可用状态
        U.app().packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setComponent(componentName)

        val pendingIntent = PendingIntent.getBroadcast(U.app(), 0, mediaButtonIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mediaSession = MediaSessionCompat(U.app(), "mbr", componentName, null)
        // 指明支持的按键类型
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setMediaButtonReceiver(pendingIntent)

        // 这里制定可以接收的来自锁屏页面的按键信息
//        val state = PlaybackStateCompat.Builder().setActions(
//                PlaybackStateCompat.ACTION_FAST_FORWARD or
//                        PlaybackStateCompat.ACTION_PAUSE or
//                        PlaybackStateCompat.ACTION_PLAY or
//                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
//                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
//                        PlaybackStateCompat.ACTION_STOP
//        ).build()
//        mediaSession.setPlaybackState(state)

        // android 5.0 以后的版本线控信息在这里处理
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                MyLog.d("MediaSessionHelper", "onMediaButtonEvent mediaButtonEvent = $mediaButtonEvent")
                val r = MediaButtonReceiver()
                r.onReceive(U.app(), mediaButtonEvent)
                return true
            }
        }, Handler(Looper.getMainLooper()))
        MyLog.d("MediaSessionHelper", "mediaSession.isActive=${mediaSession.isActive}")
        if (!mediaSession.isActive) {
            mediaSession.isActive = true
        }
    }


}
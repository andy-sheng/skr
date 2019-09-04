package com.common.player

import android.media.MediaPlayer
import android.os.Build
import com.common.log.MyLog
import com.common.playcontrol.RemoteControlHelper

object SinglePlayer : IPlayerEx {

    val player = MyMediaPlayer()
    var startFrom = "" // 当前player 被谁使用
    val callbackMap = HashMap<String, IPlayerCallback>()

    init {
        // 根据系统版本决定使用哪个播放器
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            player.useAndroidMediaPlayer = false
        } else {
            player.useAndroidMediaPlayer = false
        }
        player.setCallback(object : IPlayerCallback {
            override fun onTimeFlyMonitor(pos: Long, duration: Long) {
                callbackMap[startFrom]?.onTimeFlyMonitor(pos, duration)
            }

            override fun openTimeFlyMonitor(): Boolean {
                return callbackMap[startFrom]?.openTimeFlyMonitor() ?: false
            }

            override fun onPrepared() {
                callbackMap[startFrom]?.onPrepared()
            }

            override fun onCompletion() {
                callbackMap[startFrom]?.onCompletion()
            }

            override fun onSeekComplete() {
                callbackMap[startFrom]?.onSeekComplete()
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
                callbackMap[startFrom]?.onVideoSizeChanged(width, height)
            }

            override fun onError(what: Int, extra: Int) {
                callbackMap[startFrom]?.onError(what, extra)
            }

            override fun onInfo(what: Int, extra: Int) {
                callbackMap[startFrom]?.onInfo(what, extra)
            }

            override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                callbackMap[startFrom]?.onBufferingUpdate(mp, percent)
            }
        })
    }

    override fun addCallback(from: String, callback: IPlayerCallback) {
        callbackMap[from] = callback
    }

    override fun removeCallback(from: String) {
        callbackMap.remove(from)
    }


    override fun startPlay(from: String, path: String): Boolean {
        startFrom = from
        MyLog.d("SinglePlayer", "startPlayfrom = $from")
        if (startFrom.startsWith("ProducationWallView")
                || startFrom.startsWith("PersonWatchView")
                || startFrom.startsWith("FollowWatchView")
                || startFrom.startsWith("RecommendWatchView")
        ) {

        } else {
            //RemoteControlHelper.registerShake(startFrom)
        }
        return player.startPlay(path)
    }

    override fun pause(from: String?) {
        RemoteControlHelper.unregisterShake(startFrom)
        MyLog.d("SinglePlayer", "pausefrom=$from startFrom=$startFrom")
        if (startFrom == from) {
            player.pause()
        }
    }

    override fun resume(from: String?) {
        if (startFrom == from) {
            player.resume()
        }
    }

    override fun stop(from: String?) {
        RemoteControlHelper.unregisterShake(startFrom)
        if (startFrom == from) {
            player.stop()
        }
    }

    override fun reset(from: String?) {
        RemoteControlHelper.unregisterShake(startFrom)
        MyLog.d("SinglePlayer", "resetfrom=$from startFrom=$startFrom")
        if (startFrom == from) {
            player.reset()
        }
    }

    override fun release(from: String?) {
        if (startFrom == from) {
            player.release()
        }
    }

    override fun seekTo(from: String?, msec: Long) {
        if (startFrom == from) {
            player.seekTo(msec)
        }
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun isBufferingOk(): Boolean {
        return player.isBufferingOk
    }

}
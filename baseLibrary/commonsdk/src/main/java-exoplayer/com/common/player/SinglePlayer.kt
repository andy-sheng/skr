package com.common.player

import android.media.MediaPlayer

object SinglePlayer : IPlayerEx {

    val player = MyMediaPlayer()
    var startFrom = "" // 当前player 被谁使用
    val callbackMap = HashMap<String, IPlayerCallback>()

    init {
        // false 为使用ExoPlayer，否则使用系统的
        player.useAndroidMediaPlayer = false
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
        return player.startPlay(path)
    }

    override fun pause(from: String?) {
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
        if (startFrom == from) {
            player.stop()
        }
    }

    override fun reset(from: String?) {
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
package com.common.player

import android.media.MediaPlayer

object SinglePlayer : IPlayerEx {
    val player = MyMediaPlayer()
    var startFrom = "" // 当前player 被谁使用
    val callbackMap = HashMap<String, IPlayerCallback>()

    init {
        player.setCallback(object : IPlayerCallback {
            override fun onPrepared() {
                callbackMap[startFrom]?.onPrepared()
            }

            override fun onCompletion() {
                callbackMap[startFrom]?.onCompletion()
            }

            override fun onSeekComplete() {
            }

            override fun onVideoSizeChanged(width: Int, height: Int) {
            }

            override fun onError(what: Int, extra: Int) {
            }

            override fun onInfo(what: Int, extra: Int) {
            }

            override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
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

}
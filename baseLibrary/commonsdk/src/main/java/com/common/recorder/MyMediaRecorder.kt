package com.common.recorder

import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message

import com.common.log.MyLog

class MyMediaRecorder {

    //    String filePath;
    val MSG_UPDATE_VOLUME = 81

    var audioSource = MediaRecorder.AudioSource.MIC
    var outputFormat = MediaRecorder.OutputFormat.MPEG_4
    var audioEncoder = MediaRecorder.AudioEncoder.AAC
    var audioChannel = 1
    var audioSamplingRate = 44100
    var audioEncodingBitRate = 192000
    private var mStartRecordingTs: Long = 0
    /**
     * 单位ms
     *
     * @return
     */
    var mDuration = 0
        internal set
    private var mMediaRecorder: MediaRecorder? = null
    private var mRecording = false
    private var mUiHandler: Handler? = null
    private var mVolumeListener: ((Int) -> Unit)? = null

    private fun config(filePath: String) {
        if (mMediaRecorder == null) {
            mMediaRecorder = MediaRecorder()
        }
        mMediaRecorder?.setAudioSource(audioSource)
        mMediaRecorder?.setOutputFormat(outputFormat)
        mMediaRecorder?.setOutputFile(filePath)
        mMediaRecorder?.setAudioEncoder(audioEncoder)
        mMediaRecorder?.setAudioChannels(audioChannel)
        mMediaRecorder?.setAudioSamplingRate(audioSamplingRate)
        mMediaRecorder?.setAudioEncodingBitRate(audioEncodingBitRate)
    }

    fun start(filePath: String, callback: ((Int) -> Unit)?) {
        config(filePath)
        try {
            if (mRecording) {
                mMediaRecorder?.reset()
            }
            mMediaRecorder?.prepare()
            mMediaRecorder?.start()
            mDuration = 0
            mStartRecordingTs = System.currentTimeMillis()
            mRecording = true
            mVolumeListener = callback
            if (callback != null) {
                updateVolume()
            }
        } catch (e: Exception) {
            MyLog.e(TAG, e)
        }
    }

    fun stop() {
        if (mRecording) {
            mDuration = (System.currentTimeMillis() - mStartRecordingTs).toInt()
        }
        mRecording = false
        mMediaRecorder?.reset()
        mUiHandler?.removeCallbacksAndMessages(null)
        mVolumeListener = null
    }

    fun destroy() {
        mRecording = false
        mMediaRecorder?.reset()
        mMediaRecorder?.release()
        mMediaRecorder = null
        mUiHandler?.removeCallbacksAndMessages(null)
        mUiHandler = null
        mVolumeListener = null
    }

    private fun updateVolume() {
        if (mMediaRecorder != null) {
            var maxAmplitude = mMediaRecorder?.maxAmplitude ?: 0
            var level = 9* 3 * maxAmplitude / (32768*2) + 1
            if (mVolumeListener != null) {
                mVolumeListener?.invoke(level)
            }
            getHandler()?.removeMessages(MSG_UPDATE_VOLUME)
            getHandler()?.sendEmptyMessageDelayed(MSG_UPDATE_VOLUME, 200)
        }
    }

    private fun getHandler(): Handler? {
        if (mUiHandler == null) {
            mUiHandler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    if (msg.what == MSG_UPDATE_VOLUME) {
                        updateVolume()
                    }
                }
            }
        }
        return mUiHandler;
    }

    class Builder internal constructor() {
        internal var mParams = MyMediaRecorder()

        fun setAudioSource(audioSource: Int): Builder {
            mParams.audioSource = audioSource
            return this
        }

        fun setOutputFormat(outputFormat: Int): Builder {
            mParams.outputFormat = outputFormat
            return this
        }

        fun setAudioEncoder(audioEncoder: Int): Builder {
            mParams.audioEncoder = audioEncoder
            return this
        }

        fun setAudioChannel(audioChannel: Int): Builder {
            mParams.audioChannel = audioChannel
            return this
        }

        fun setAudioSamplingRate(audioSamplingRate: Int): Builder {
            mParams.audioSamplingRate = audioSamplingRate
            return this
        }

        fun setAudioEncodingBitRate(audioEncodingBitRate: Int): Builder {
            mParams.audioEncodingBitRate = audioEncodingBitRate
            return this
        }

        fun build(): MyMediaRecorder {
            return mParams
        }

    }

    companion object {
        val TAG = "MyMediaRecorder"

        fun newBuilder(): Builder {
            return Builder()
        }
    }

}

package com.module.playways.grab.room.voicemsg

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import com.common.log.MyLog
import com.common.recorder.MyMediaRecorder
import com.common.utils.HandlerTaskTimer
import com.common.utils.U

import com.common.view.ex.ExTextView
import com.module.playways.songmanager.event.BeginRecordCustomGameEvent
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

class VoiceRecordTextView : ExTextView {

    companion object {
        private val STATE_IDLE = 1       //正常空闲（默认状态）
        private val STATE_RECORDING = 2  //正在录音
        private val STATE_CANCEL = 3     //取消录音
        private val STATE_RECORD_OK = 4  //录音完成
        private val DISTANCE_Y_CANCEL = 50  //判断上滑取消距离
    }

    val TAG = "VoiceRecordTextView"

    var mCurrentState = STATE_IDLE
    var mBeginRecordingTs: Long = 0
    var isRecording = false

    var mHandlerTaskTimer: HandlerTaskTimer? = null
    var mMyMediaRecorder: MyMediaRecorder? = null

    var mShowTips: ((isShow: Boolean) -> Unit)? = null
    var mChangeVoiceLevel: ((level: Int) -> Unit)? = null
    var mCancelRecord: (() -> Unit)? = null
    var mRemainTime: ((text: String) -> Unit)? = null
    var mTimeLimit: ((short: Boolean) -> Unit)? = null

    val mVoiceDir = U.getAppInfoUtils().getSubDirFile("voice")
    var mRecordAudioFilePath: String? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        // 每次进来可以直接清空音频文件
        U.getFileUtils().deleteAllFiles(mVoiceDir)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x
        val y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                MyLog.d(TAG, "ACTION_DOWN")
                if (mCurrentState == STATE_IDLE || mCurrentState == STATE_RECORD_OK) {
                    // 开始录音，显示手指上滑，取消发送
                    mCurrentState = STATE_RECORDING
                    mBeginRecordingTs = System.currentTimeMillis()
                    mShowTips?.invoke(true)
                    text = "松开 结束"
                    alpha = 0.5f
                    startCountDown()
                    EventBus.getDefault().post(BeginRecordCustomGameEvent(true))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                MyLog.d(TAG, "ACTION_MOVE")
                if (wantToCancle(x.toInt(), y.toInt())) {
                    // 停止录音，显示松开手指，取消发送
                    mCurrentState = STATE_CANCEL
                    cancelCountDown()
                    mCancelRecord?.invoke()
                    U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
                    EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
                }
            }
            MotionEvent.ACTION_UP -> {
                MyLog.d(TAG, "ACTION_UP $mCurrentState")
                // 停止录音
                cancelCountDown()
                text = "按住说话"
                alpha = 1f
                if (mCurrentState == STATE_RECORDING) {
                    if ((System.currentTimeMillis() - mBeginRecordingTs) < 3 * 1000) {
                        // 时间太短, 提示太短再消失
                        mTimeLimit?.invoke(true)
                        mCurrentState = STATE_IDLE
                    } else {
                        // 上传录音文件，并发送(同时需要处理用户可能的再录制)
                        mCurrentState = STATE_RECORD_OK
                        mShowTips?.invoke(false)
                    }
                } else if (mCurrentState == STATE_CANCEL) {
                    // 弹框消失
                    mCurrentState = STATE_IDLE
                    mShowTips?.invoke(false)
                }
            }
        }
        return true
    }

    private fun startCountDown() {
        cancelCountDown()
        isRecording = true
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(20)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        val t = 20 - integer
                        if (t <= 5) {
                            mRemainTime?.invoke("还可以说$t" + "秒")
                        }
                    }

                    override fun onComplete() {
                        super.onComplete()
                        mTimeLimit?.invoke(false)
                        mMyMediaRecorder?.stop()
                        EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
                    }
                })
        if (mMyMediaRecorder == null) {
            mMyMediaRecorder = MyMediaRecorder.newBuilder().build()
        }

        var file = File(mVoiceDir, "${System.currentTimeMillis()}.aac")
        if (!file.exists()) {
            file.parentFile.mkdir()
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mRecordAudioFilePath = file.path
        mMyMediaRecorder?.start(mRecordAudioFilePath!!) {
            when (it) {
                in 0..16 -> mChangeVoiceLevel?.invoke(1)
                in 17..32 -> mChangeVoiceLevel?.invoke(2)
                in 33..48 -> mChangeVoiceLevel?.invoke(3)
                in 49..64 -> mChangeVoiceLevel?.invoke(4)
                in 65..80 -> mChangeVoiceLevel?.invoke(5)
                in 81..96 -> mChangeVoiceLevel?.invoke(6)
                in 97..112 -> mChangeVoiceLevel?.invoke(7)
                in 113..128 -> mChangeVoiceLevel?.invoke(8)
                else -> mChangeVoiceLevel?.invoke(9)
            }
        }
    }

    private fun cancelCountDown() {
        mHandlerTaskTimer?.dispose()
        mMyMediaRecorder?.stop()
        isRecording = false
    }

    private fun wantToCancle(x: Int, y: Int): Boolean {
        MyLog.d(TAG, "x = $x" + " y = $y" + " height = $height")
        // 超过按钮的宽度
        if (x < 0 || x > getWidth()) {
            return true
        }
        // 超过按钮的高度
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true
        }

        return false
    }

}

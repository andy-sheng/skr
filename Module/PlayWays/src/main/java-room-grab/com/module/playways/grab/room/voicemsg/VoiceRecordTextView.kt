package com.module.playways.grab.room.voicemsg

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import com.alibaba.fastjson.JSON
import com.common.anim.ObjectPlayControlTemplate
import com.common.log.MyLog
import com.common.recorder.MyMediaRecorder
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.HandlerTaskTimer
import com.common.utils.U

import com.common.view.ex.ExTextView
import com.module.playways.room.msg.event.EventHelper
import com.module.playways.room.room.RankRoomServerApi
import com.module.playways.songmanager.event.BeginRecordCustomGameEvent
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.HashMap

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

    val minDuration = 1 * 1000;
    val maxDuration = 15 * 1000;

    val mVoiceDir = U.getAppInfoUtils().getSubDirFile("voice")
    var mRecordAudioFilePath: String? = null
    var mDuration: Long = 0
    var mGameId: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    var mIsUpload = false;

    internal var mPlayControlTemplate: ObjectPlayControlTemplate<AudioFile, VoiceRecordTextView?> = object : ObjectPlayControlTemplate<AudioFile, VoiceRecordTextView?>() {
        override fun accept(cur: AudioFile): VoiceRecordTextView? {
            if (mIsUpload) {
                return null
            }
            return this@VoiceRecordTextView
        }

        override fun onStart(file: AudioFile, view: VoiceRecordTextView?) {
            execUploadAudio(file)
        }

        override fun onEnd(file: AudioFile) {
            MyLog.d(TAG, "onEnd 上传结束 File=$file")
        }
    }

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
                    if ((System.currentTimeMillis() - mBeginRecordingTs) < minDuration) {
                        // 时间太短, 提示太短再消失
                        mTimeLimit?.invoke(true)
                        U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
                        mCurrentState = STATE_IDLE
                    } else {
                        // 上传录音文件，并发送(同时需要处理用户可能的再录制)
                        mCurrentState = STATE_RECORD_OK
                        mDuration = mMyMediaRecorder!!.duration.toLong()
                        mPlayControlTemplate.add(AudioFile(mRecordAudioFilePath!!, mDuration), true)
                        mShowTips?.invoke(false)
                    }
                } else if (mCurrentState == STATE_CANCEL) {
                    // 弹框消失
                    mCurrentState = STATE_IDLE
                    mShowTips?.invoke(false)
                    U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
                } else {
                    U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
                }
            }
        }
        return true
    }

    private fun startCountDown() {
        cancelCountDown()
        isRecording = true
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(maxDuration / 1000)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        val t = maxDuration / 1000 - integer
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

    private fun execUploadAudio(file: AudioFile) {
        MyLog.d(TAG, "execUploadAudio file=$file")
        mIsUpload = true;
        val uploadTask = UploadParams.newBuilder(file.localPath)
                .setFileType(UploadParams.FileType.msgAudio)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {

                    }

                    override fun onSuccessNotInUiThread(url: String) {
                        MyLog.d(TAG, "上传成功 url=$url")
                        // 向服务器发送语音消息
                        sendToServer(file, url)
                        mIsUpload = false
                        mPlayControlTemplate.endCurrent(file)
                    }

                    override fun onFailureNotInUiThread(msg: String) {
                        MyLog.d(TAG, "上传失败 msg=$msg")
                    }
                })
    }


    private fun sendToServer(file: VoiceRecordTextView.AudioFile, url: String) {
        val roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi::class.java)
        val map = HashMap<String, Any>()
        map["gameID"] = mGameId
        map["msgUrl"] = url
        map["duration"] = file.duration

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(roomServerApi.sendAudioMsg(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                }
            }
        })

        EventHelper.pretendAudioPush(file.localPath, file.duration, url, mGameId);
    }


    class AudioFile(var localPath: String, var duration: Long) {}

}

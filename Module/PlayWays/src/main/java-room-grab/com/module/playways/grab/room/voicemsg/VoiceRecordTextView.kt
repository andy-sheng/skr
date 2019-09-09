package com.module.playways.grab.room.voicemsg

import android.content.Context
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
import com.module.playways.BaseRoomData
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.msg.event.EventHelper
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.room.RankRoomServerApi
import com.module.playways.songmanager.event.MuteAllVoiceEvent
import com.zq.live.proto.Room.EQRoundStatus
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.*

class VoiceRecordTextView : ExTextView {

    companion object {
        private val STATE_IDLE = 1       //正常空闲（默认状态）
        private val STATE_RECORDING = 2  //正在录音
        private val STATE_CANCEL = 3     //取消录音
        private val DISTANCE_Y_CANCEL = 50  //判断上滑取消距离
    }

    val TAG = "VoiceRecordTextView"

    var mCurrentState = STATE_IDLE

    var mHandlerTaskTimer: HandlerTaskTimer? = null
    var mMyMediaRecorder: MyMediaRecorder? = null

    var mShowTipsListener: ((isShow: Boolean) -> Unit)? = null
    var mChangeVoiceLevelListener: ((level: Int) -> Unit)? = null
    var mCancelRecordListener: (() -> Unit)? = null
    var mRemainTimeListener: ((text: String) -> Unit)? = null
    var mTimeLimitListener: ((short: Boolean) -> Unit)? = null

    val minDuration = 1 * 1000
    val maxDuration = 15 * 1000

    val mVoiceDir = U.getAppInfoUtils().getSubDirFile("voice")
    var mRecordAudioFilePath: String? = null
    var mRoomData: BaseRoomData<*>? = null

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

        override fun onEnd(file: AudioFile?) {
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
                if (mRoomData is GrabRoomData) {
                    val roundInfoModel = mRoomData?.realRoundInfo as GrabRoundInfoModel?
                    if (roundInfoModel != null && roundInfoModel.isSingStatus && roundInfoModel!!.singBySelf()) {
                        U.getToastUtil().showShort("演唱中无法录音")
                        return false
                    }
                    if (roundInfoModel != null && roundInfoModel.isFreeMicRound) {
                        U.getToastUtil().showShort("自由麦轮次无法录音")
                        return false
                    }
                    if (roundInfoModel != null && roundInfoModel.status == EQRoundStatus.QRS_INTRO.value && roundInfoModel.isSelfGrab) {
                        U.getToastUtil().showShort("参与抢唱无法录音")
                        return false
                    }
                    if (ZqEngineKit.getInstance().params.isAnchor && !ZqEngineKit.getInstance().params.isLocalAudioStreamMute) {
                        // 是主播切开麦不能录音
                        U.getToastUtil().showShort("在麦上无法录音")
                        return false
                    }
                }

                if (mRoomData is RaceRoomData) {
                    val roundInfoModel = mRoomData?.realRoundInfo as RaceRoundInfoModel?
                    roundInfoModel?.let {
                        if (it.isSingerNowBySelf()) {
                            U.getToastUtil().showShort("演唱中无法录音")
                            return false
                        }

                        if (ZqEngineKit.getInstance().params.isAnchor && !ZqEngineKit.getInstance().params.isLocalAudioStreamMute) {
                            // 是主播切开麦不能录音
                            U.getToastUtil().showShort("在麦上无法录音")
                            return false
                        }
                    }
                }

                if (mCurrentState == STATE_IDLE) {
                    // 开始录音，显示手指上滑，取消发送
                    mCurrentState = STATE_RECORDING
                    mShowTipsListener?.invoke(true)
                    text = "松开 结束"
                    alpha = 0.5f
                    startRecordCountDown()
                    EventBus.getDefault().post(MuteAllVoiceEvent(true))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                MyLog.d(TAG, "ACTION_MOVE")
                if (wantToCancle(x.toInt(), y.toInt())) {
                    if (mCurrentState == STATE_RECORDING) {
                        mCurrentState = STATE_CANCEL
                        mCancelRecordListener?.invoke()
                    }
                } else {
                    if (mCurrentState == STATE_CANCEL) {
                        mCurrentState = STATE_RECORDING
                        mShowTipsListener?.invoke(true)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                MyLog.d(TAG, "ACTION_UP $mCurrentState")
                // 停止录音
                onActionUp()
            }
        }
        return true
    }

    private fun startRecordCountDown() {
        cancelRecordCountDown()
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .delay(1000)
                .take(maxDuration / 1000)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        val t = maxDuration / 1000 - integer
                        if (t <= 5 && mCurrentState == STATE_RECORDING) {
                            mRemainTimeListener?.invoke("还可以说${t}秒")
                        }
                    }

                    override fun onComplete() {
                        super.onComplete()
                        onActionUp()
                        mTimeLimitListener?.invoke(false)
                    }
                })
        if (mMyMediaRecorder == null) {
            mMyMediaRecorder = MyMediaRecorder.newBuilder().build()
        }

        var file = File(mVoiceDir, "${mRoomData?.gameId}-${System.currentTimeMillis()}.m4a")
        if (!file.exists()) {
            file.parentFile.mkdir()
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mRecordAudioFilePath = file.path
        mRecordAudioFilePath?.let {
            mMyMediaRecorder?.start(it) {
                mChangeVoiceLevelListener?.invoke(it)
            }
        }
    }

    private fun onActionUp() {
        cancelRecordCountDown()
        text = "按住说话"
        alpha = 1f
        if (mCurrentState == STATE_RECORDING) {
            val duration = mMyMediaRecorder?.mDuration?.toLong() ?: 0L
            if (duration < 1000) {
                // 时间太短, 提示太短再消失
                mTimeLimitListener?.invoke(true)
                U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
            } else {
                // 上传录音文件，并发送(同时需要处理用户可能的再录制)
                mPlayControlTemplate.add(AudioFile(mRecordAudioFilePath, duration), true)
                mShowTipsListener?.invoke(false)
            }
        } else if (mCurrentState == STATE_CANCEL) {
            // 弹框消失
            mShowTipsListener?.invoke(false)
            U.getFileUtils().deleteAllFiles(mRecordAudioFilePath)
        }
        mCurrentState = STATE_IDLE
        EventBus.getDefault().post(MuteAllVoiceEvent(false))
    }

    private fun cancelRecordCountDown() {
        mHandlerTaskTimer?.dispose()
        mMyMediaRecorder?.stop()
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
        mIsUpload = true
        file.localPath?.let {
            val file2 = File(it)
            if (file2.exists() && file2.isFile && file2.length() > 10) {
                UploadParams.newBuilder(file2.path)
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
                                mIsUpload = false
                                mPlayControlTemplate.endCurrent(file)
                            }
                        })
            }
        }

    }


    private fun sendToServer(file: AudioFile, url: String) {
        val roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi::class.java)
        val map = HashMap<String, Any>()
        map["gameID"] = mRoomData!!.gameId
        map["msgUrl"] = url
        map["duration"] = file.duration

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(roomServerApi.sendAudioMsg(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {

                }
            }
        })

        EventHelper.pretendAudioPush(file.localPath, file.duration, url, mRoomData!!.gameId)
    }


    class AudioFile(var localPath: String?, var duration: Long)

}

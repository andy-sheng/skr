package com.module.playways.songmanager.customgame

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.player.IPlayer
import com.common.player.MyMediaPlayer
import com.common.player.VideoPlayerAdapter
import com.common.recorder.MyMediaRecorder
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.upload.UploadTask
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.songmanager.event.AddCustomGameEvent
import com.module.playways.songmanager.event.BeginRecordCustomGameEvent
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

import org.greenrobot.eventbus.EventBus

import java.io.File
import java.util.HashMap

import okhttp3.MediaType
import okhttp3.RequestBody

class MakeGamePanelView : RelativeLayout {
    internal var mSubmitProgressBar: ProgressBar? = null
    internal var mTitleTv: TextView? = null
    internal var mDescTv: TextView? = null
    internal var mCountDownTv: TextView? = null
    internal var mPlayBtn: ExImageView? = null
    internal var mCircleCountDownView: CircleCountDownView? = null
    internal var mRecordingTipsTv: TextView? = null
    internal var mReRecordBtn: ExTextView? = null
    internal var mSubmitBtn: ExTextView? = null
    internal var mTime60Btn: ExTextView? = null
    internal var mTime90Btn: ExTextView? = null
    internal var mTime120Btn: ExTextView? = null

    internal var mStatus = STATUS_IDLE
    internal var mBeginRecordingTs: Long = 0
    internal var mPlayTimeExpect = 60
    internal var mRoomID: Int = 0

    internal var mMyMediaRecorder: MyMediaRecorder? = null

    internal var mUploadUrl: String? = null

    internal var mMakeAudioFilePath = File(U.getAppInfoUtils().mainDir, "make_game_intro.aac").path

    internal var mUploadTask: UploadTask? = null

    internal var mUploading = false

    internal var mMediaPlayer: IPlayer? = null

    internal var mDownTouching = false

    internal var mUiHandler = Handler()

    internal var mHandlerTaskTimer: HandlerTaskTimer? = null

    internal var mDialogPlus: DialogPlus? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    internal fun init() {
        View.inflate(context, R.layout.make_game_panel_view_layout, this)
        mTitleTv = this.findViewById<View>(R.id.title_tv) as TextView
        mDescTv = this.findViewById<View>(R.id.desc_tv) as TextView
        mCountDownTv = this.findViewById<View>(R.id.count_down_tv) as TextView
        mPlayBtn = this.findViewById<View>(R.id.play_btn) as ExImageView
        mCircleCountDownView = this.findViewById<View>(R.id.circle_count_down_view) as CircleCountDownView
        mRecordingTipsTv = this.findViewById<View>(R.id.recording_tips_tv) as TextView
        mReRecordBtn = this.findViewById<View>(R.id.re_record_btn) as ExTextView
        mSubmitBtn = this.findViewById<View>(R.id.submit_btn) as ExTextView
        mTime60Btn = this.findViewById<View>(R.id.time60_btn) as ExTextView
        mTime90Btn = this.findViewById<View>(R.id.time90_btn) as ExTextView
        mTime120Btn = this.findViewById<View>(R.id.time120_btn) as ExTextView
        mSubmitProgressBar = this.findViewById<View>(R.id.submit_progress_bar) as ProgressBar

        mPlayBtn?.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mDownTouching = true
                    if (mStatus == STATUS_IDLE) {
                        // 开始录音
                        mStatus = STATUS_RECORDING
                        mRecordingTipsTv?.text = "录音中..."
                        mCircleCountDownView?.visibility = View.VISIBLE
                        mCircleCountDownView?.go(0, 15 * 1000)
                        mBeginRecordingTs = System.currentTimeMillis()
                        startCountDown()
                        EventBus.getDefault().post(BeginRecordCustomGameEvent(true))
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mDownTouching = false
                    if (mStatus == STATUS_RECORDING) {
                        cancelCountDown()
                        // 停止录音
                        mCircleCountDownView?.visibility = View.GONE
                        if (System.currentTimeMillis() - mBeginRecordingTs < 5 * 1000) {
                            mStatus = STATUS_IDLE
                            U.getToastUtil().showShort("至少录5秒钟哦")
                            changeToRecordBegin()
                        } else {
                            // 录制成功
                            mStatus = STATUS_RECORD_OK
                            changeToRecordOk()
                        }
                        EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
                    } else if (mStatus == STATUS_RECORD_OK) {
                        mStatus = STATUS_RECORD_PLAYING
                        mPlayBtn?.setImageResource(R.drawable.make_game_zanting)
                        playRecorderRes(true)
                        mRecordingTipsTv?.text = "停止"
                    } else if (mStatus == STATUS_RECORD_PLAYING) {
                        mStatus = STATUS_RECORD_OK
                        mPlayBtn?.setImageResource(R.drawable.make_game_bofang)
                        playRecorderRes(false)
                        mRecordingTipsTv?.text = "播放"
                    }
                }
            }
            true
        }

        mTime60Btn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDownTouching) {
                    return
                }
                mPlayTimeExpect = 60
                mTime60Btn?.isEnabled = false
                mTime90Btn?.isEnabled = true
                mTime120Btn?.isEnabled = true
            }
        })

        mTime90Btn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDownTouching) {
                    return
                }
                mPlayTimeExpect = 90
                mTime60Btn?.isEnabled = true
                mTime90Btn?.isEnabled = false
                mTime120Btn?.isEnabled = true
            }
        })

        mTime120Btn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDownTouching) {
                    return
                }
                mPlayTimeExpect = 120
                mTime60Btn?.isEnabled = true
                mTime90Btn?.isEnabled = true
                mTime120Btn?.isEnabled = false
            }
        })

        mReRecordBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDownTouching) {
                    return
                }
                mStatus = STATUS_IDLE
                changeToRecordBegin()
            }
        })
        mSubmitBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mDownTouching) {
                    return
                }
                // 调研录音
                uploadAudioRes()
            }
        })
        changeToRecordBegin()
    }

    private fun playRecorderRes(play: Boolean) {
        if (play) {
            if (mMediaPlayer == null) {
                mMediaPlayer = MyMediaPlayer()
                mMediaPlayer!!.setCallback(object : VideoPlayerAdapter.PlayerCallbackAdapter() {
                    override fun onCompletion() {
                        super.onCompletion()
                        mMediaPlayer!!.reset()
                        mStatus = STATUS_RECORD_OK
                        mPlayBtn?.setImageResource(R.drawable.make_game_bofang)
                        mRecordingTipsTv?.text = "播放"
                        EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
                    }
                })
            }
            mMediaPlayer!!.startPlay(mMakeAudioFilePath)
            EventBus.getDefault().post(BeginRecordCustomGameEvent(true))
        } else {
            EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
            if (mMediaPlayer != null) {
                mMediaPlayer!!.reset()
            }
        }
    }

    /**
     * 上传音频资源
     */
    private fun uploadAudioRes() {
        if (TextUtils.isEmpty(mUploadUrl)) {
            if (!mUploading) {
                mSubmitProgressBar?.visibility = View.VISIBLE
                mUploading = true
                mUploadTask = UploadParams.newBuilder(mMakeAudioFilePath)
                        .setFileType(UploadParams.FileType.customGame)
                        .startUploadAsync(object : UploadCallback {
                            override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {

                            }

                            override fun onSuccessNotInUiThread(url: String) {
                                mUploadUrl = url
                                sendToServer()
                                mUploading = false
                                mUiHandler.post { mSubmitProgressBar?.visibility = View.GONE }

                            }

                            override fun onFailureNotInUiThread(msg: String) {
                                mUploading = false
                                U.getToastUtil().showShort("上传失败")
                                mUiHandler.post { mSubmitProgressBar?.visibility = View.GONE }
                            }
                        })
            }
        } else {
            sendToServer()
        }
    }

    /**
     * 传到服务器
     */
    fun sendToServer() {
        // 上传提交
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomID
        map["standIntro"] = mUploadUrl?:""
        map["standIntroEndT"] = mMyMediaRecorder!!.duration
        map["totalMs"] = mPlayTimeExpect * 1000

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        val grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
        if (grabRoomServerApi != null) {
            ApiMethods.subscribe(grabRoomServerApi.addCustomGame(body), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult) {
                    mSubmitProgressBar?.visibility = View.GONE
                    if (obj.errno == 0) {
                        U.getToastUtil().showShort("添加成功")
                        EventBus.getDefault().post(AddCustomGameEvent())
                        // 刷新ui
                        if (mDialogPlus != null) {
                            mDialogPlus!!.dismiss()
                        }
                    }
                }

                override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                    super.onNetworkError(errorType)
                    mSubmitProgressBar?.visibility = View.GONE
                }
            }, ApiMethods.RequestControl("addCustomGame", ApiMethods.ControlType.CancelThis))
        }
    }

    private fun cancelCountDown() {
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer!!.dispose()
        }
        if (mMyMediaRecorder != null) {
            mMyMediaRecorder!!.stop()
        }
        mCountDownTv?.text = "15s"
    }

    private fun startCountDown() {
        cancelCountDown()
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(16)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        val t = (16 - integer).toString() + "s"
                        mCountDownTv?.text = t
                    }

                    override fun onComplete() {
                        super.onComplete()
                        if (mMyMediaRecorder != null) {
                            mMyMediaRecorder!!.stop()
                        }
                        changeToRecordOk()
                        EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
                    }
                })
        if (mMyMediaRecorder == null) {
            mMyMediaRecorder = MyMediaRecorder.newBuilder().build()
        }
        mMyMediaRecorder!!.start(mMakeAudioFilePath, null!!)
    }

    private fun changeToRecordOk() {
        mCountDownTv?.visibility = View.GONE
        mReRecordBtn?.visibility = View.VISIBLE
        mSubmitBtn?.visibility = View.VISIBLE
        mDescTv?.visibility = View.GONE
        mTime60Btn?.visibility = View.VISIBLE
        mTime90Btn?.visibility = View.VISIBLE
        mTime120Btn?.visibility = View.VISIBLE
        if (mPlayTimeExpect == 60) {
            mTime60Btn?.isEnabled = false
            mTime90Btn?.isEnabled = true
            mTime120Btn?.isEnabled = true
        }
        mPlayBtn?.setImageResource(R.drawable.make_game_bofang)
        mRecordingTipsTv?.text = "播放"
        mTitleTv?.text = "选择表演时间"
        mSubmitProgressBar?.visibility = View.GONE
    }

    private fun changeToRecordBegin() {
        mCountDownTv?.visibility = View.VISIBLE
        mReRecordBtn?.visibility = View.GONE
        mSubmitBtn?.visibility = View.GONE
        mDescTv?.visibility = View.VISIBLE
        mTime60Btn?.visibility = View.GONE
        mTime90Btn?.visibility = View.GONE
        mTime120Btn?.visibility = View.GONE
        mPlayBtn?.setImageResource(R.drawable.make_game_luyin)
        mRecordingTipsTv?.text = "按住录音"
        mTitleTv?.text = "一句话描述游戏规则"
        mSubmitProgressBar?.visibility = View.GONE
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
        }
    }

    override fun onDetachedFromWindow() {
        MyLog.d(TAG, "onDetachedFromWindow")
        super.onDetachedFromWindow()
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer!!.dispose()
        }
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
        }
        if (mMyMediaRecorder != null) {
            mMyMediaRecorder!!.destroy()
        }
        if (mUploadTask != null) {
            mUploadTask!!.cancel()
        }
        EventBus.getDefault().post(BeginRecordCustomGameEvent(false))
    }

    fun showByDialog(roomId: Int) {
        this.mRoomID = roomId
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(false)
        }
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setContentBackgroundResource(com.common.core.R.color.transparent)
                .setOverlayBackgroundResource(com.common.core.R.color.black_trans_80)
                .setExpanded(false)
                .create()
        mDialogPlus!!.show()
    }

    fun dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss()
        }
    }

    companion object {

        val TAG = "MakeGamePanelView"

        internal val STATUS_IDLE = 1
        internal val STATUS_RECORDING = 2
        internal val STATUS_RECORD_OK = 3
        internal val STATUS_RECORD_PLAYING = 4
    }
}

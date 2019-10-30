package com.module.posts.publish.voice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.UserInfoServerApi
import com.common.player.SinglePlayer
import com.common.recorder.MyMediaRecorder
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.DiffuseView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.CircleCountDownView
import com.component.busilib.view.SkrProgressView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.PostsPublishModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.util.*

//帖子声音录制和个人中心声音录制
@Route(path = RouterConstants.ACTIVITY_VOICE_RECORD)
class VoiceRecordActivity : BaseActivity() {
    companion object {
        const val REQ_CODE_VOICE_RECORD = 11

        const val FROM_POSTS = 1    // 从帖子中来
        const val FROM_PERSON = 2    // 从个人中心来
        const val FROM_MIC_AUDIO_CHECK = 2    // 从小k房校验来的
    }

    private var from = FROM_POSTS   //默认来源于帖子
    private var recordPath = PostsPublishModel.POSTS_PUBLISH_AUDIO_FILE_PATH  // 默认来自帖子路径

    internal val STATUS_IDLE = 1
    internal val STATUS_RECORDING = 2
    internal val STATUS_RECORD_OK = 3
    internal val STATUS_RECORD_PLAYING = 4

    var status = STATUS_IDLE
    var myMediaRecorder: MyMediaRecorder? = null

    private val playerTag = TAG + hashCode()

    lateinit var mainActContainer: ConstraintLayout
    lateinit var titleBar: CommonTitleBar
    lateinit var playBtn: ExImageView
    lateinit var playTipsTv: ExTextView
    lateinit var countDownTv: TextView
    lateinit var abandonIv: ExImageView
    lateinit var abandonTv: ExTextView
    lateinit var okIv: ExImageView
    lateinit var okTv: ExTextView
    lateinit var recordDiffuseView: DiffuseView
    lateinit var circleCountDownView: CircleCountDownView


    lateinit var tipsTv: TextView
    lateinit var progressView: SkrProgressView

    var startRecordTs = 0L

    private val skrAudioPermission = SkrAudioPermission()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_voice_record_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        from = intent.getIntExtra("from", FROM_POSTS)
        if (from == FROM_POSTS) {
            recordPath = PostsPublishModel.POSTS_PUBLISH_AUDIO_FILE_PATH
        } else if (from == FROM_PERSON) {
            recordPath = File(U.getAppInfoUtils().mainDir, "person_audio_tag.m4a").path
        }

        mainActContainer = findViewById(R.id.main_act_container)
        titleBar = findViewById(R.id.title_bar)
        playBtn = findViewById(R.id.play_btn)
        playTipsTv = findViewById(R.id.play_tips_tv)
        countDownTv = findViewById(R.id.count_down_tv)
        abandonIv = findViewById(R.id.abandon_iv)
        abandonTv = findViewById(R.id.abandon_tv)
        okIv = findViewById(R.id.ok_iv)
        okTv = findViewById(R.id.ok_tv)
        recordDiffuseView = findViewById(R.id.record_diffuse_view)
        circleCountDownView = findViewById(R.id.circle_count_down_view)
        progressView = findViewById(R.id.progress_view)
        tipsTv = findViewById(R.id.tips_tv)

        if (from == FROM_PERSON) {
            tipsTv.visibility = View.VISIBLE
        }

        titleBar.leftImageButton.setOnClickListener {
            finish()
        }
        playBtn.setOnClickListener {
            when (status) {
                STATUS_IDLE -> skrAudioPermission.ensurePermission({
                    startRecord()
                }, true)
                STATUS_RECORDING -> {
                    if ((System.currentTimeMillis() - startRecordTs) < 2 * 1000) {
                        U.getToastUtil().showShort("太短了，多录制几句吧")
                        return@setOnClickListener
                    }
                    stopRecord()
                }
                STATUS_RECORD_OK -> // 播放
                    play()
                STATUS_RECORD_PLAYING -> stop()
            }
        }
        abandonIv.setOnClickListener {
            reset()
        }
        okIv.setOnClickListener {
            if (from == FROM_POSTS) {
                val data = Intent()
                data.putExtra("duration", myMediaRecorder?.mDuration ?: 0)
                setResult(Activity.RESULT_OK, data)
                finish()
            } else if (from == FROM_PERSON) {
                // 上传个人录音
                progressView.visibility = View.VISIBLE
                UploadParams.newBuilder(recordPath)
                        .setFileType(UploadParams.FileType.audioAi)
                        .startUploadAsync(object : UploadCallback {
                            override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                            }

                            override fun onSuccessNotInUiThread(url: String?) {
                                url?.let {
                                    uploadLabelToServer(recordPath, it)
                                }
                            }

                            override fun onFailureNotInUiThread(msg: String?) {
                                progressView.visibility = View.GONE
                                U.getToastUtil().showShort("录音上传失败")
                            }
                        })
            }

        }
    }

    private fun uploadLabelToServer(localPath: String, audioUrl: String) {
        val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

        val map = HashMap<String, Any>()
        map["duration"] = U.getMediaUtils().getDuration(localPath, 0)
        map["songID"] = 0
        map["songName"] = ""
        map["voiceURL"] = audioUrl
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe<ApiResult>(userServerApi.uploadVoiceTag(body), object : ApiObserver<ApiResult>() {
            override fun process(apiResult: ApiResult) {
                if (apiResult.errno == 0) {
                    finish()
                    U.getToastUtil().showShort("等待审核中")
                } else {
                    progressView.visibility = View.GONE
                    U.getToastUtil().showShort(apiResult.errmsg)
                }
            }
        })
    }

    var recordJob: Job? = null

    private fun startRecord() {
        if (myMediaRecorder == null) {
            myMediaRecorder = MyMediaRecorder.newBuilder().build()
        }
        if (myMediaRecorder?.start(recordPath, null) == true) {
            status = STATUS_RECORDING
            playTipsTv.text = "点击停止"
            abandonIv.visibility = View.GONE
            abandonTv.visibility = View.GONE
            okIv.visibility = View.GONE
            okTv.visibility = View.GONE
            countDownTv.visibility = View.VISIBLE
            countDownTv.text = "0s"
            playBtn.setImageResource(R.drawable.yuyin_weikaishi)

            recordJob = launch {
                for (i in 0 until 60) {
                    recordDiffuseView.start(2000)
                    delay(1000)
                    countDownTv.text = "${i + 1}s"
                }
                stopRecord()
            }

            startRecordTs = System.currentTimeMillis()
        } else {
            U.getToastUtil().showShort("启动录制失败")
        }
    }

    private fun stopRecord() {
        status = STATUS_RECORD_OK
        recordDiffuseView.stop()
        circleCountDownView.visibility = View.GONE
        myMediaRecorder?.stop()
        recordJob?.cancel()
        playBtn.setImageResource(R.drawable.yuyin_zanting)
        playTipsTv.text = "播放"
        abandonIv.visibility = View.VISIBLE
        abandonTv.visibility = View.VISIBLE
        okIv.visibility = View.VISIBLE
        okTv.visibility = View.VISIBLE
    }

    private fun play() {
        status = STATUS_RECORD_PLAYING
        playBtn.setImageResource(R.drawable.yuyin_bofang)
        circleCountDownView.visibility = View.VISIBLE
        recordDiffuseView.visibility = View.GONE
        circleCountDownView.go(0, myMediaRecorder?.mDuration ?: 0) {
            stop()
        }
        playTipsTv.text = "暂停"
        recordJob = launch {
            for (i in 0 until 60) {
                delay(1000)
                countDownTv.text = "${i + 1}s"
            }
            stop()
        }
        SinglePlayer.startPlay(playerTag, recordPath)
    }

    private fun stop() {
        SinglePlayer.stop(playerTag)
        stopRecord()
    }

    private fun reset() {
        stop()
        status = STATUS_IDLE
        playBtn.setImageResource(R.drawable.yuyin_weikaishi)
        playTipsTv.text = "点击录音"
        abandonIv.visibility = View.GONE
        abandonTv.visibility = View.GONE
        okIv.visibility = View.GONE
        okTv.visibility = View.GONE
        countDownTv.visibility = View.GONE
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.stop(playerTag)
        SinglePlayer.removeCallback(playerTag)
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

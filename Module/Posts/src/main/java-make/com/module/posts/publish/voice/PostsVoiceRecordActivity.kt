package com.module.posts.publish.voice

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.player.SinglePlayer
import com.common.recorder.MyMediaRecorder
import com.common.utils.U
import com.common.view.DiffuseView
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.PostsPublishModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_POSTS_VOICE_RECORD)
class PostsVoiceRecordActivity : BaseActivity() {
    companion object {
        const val REQ_CODE_VOICE_RECORD = 11
    }

    internal val STATUS_IDLE = 1
    internal val STATUS_RECORDING = 2
    internal val STATUS_RECORD_OK = 3
    internal val STATUS_RECORD_PLAYING = 4

    var status = STATUS_IDLE
    var myMediaRecorder: MyMediaRecorder? = null

    lateinit var postsPublushModel: PostsPublishModel

    val playTag = TAG + hashCode()

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
    var startRecordTs = 0L

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_voice_record_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        postsPublushModel = intent.getSerializableExtra("model") as PostsPublishModel
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

        titleBar.leftImageButton.setOnClickListener {
            finish()
        }
        playBtn.setOnClickListener {
            if (status == STATUS_IDLE) {
                startRecord()
            } else if (status == STATUS_RECORDING) {
                if ((System.currentTimeMillis() - startRecordTs) < 2 * 1000) {
                    U.getToastUtil().showShort("太短了，多录制几句吧")
                    return@setOnClickListener
                }
                stopRecord()
            } else if (status == STATUS_RECORD_OK) {
                // 播放
                play()
            } else if (status == STATUS_RECORD_PLAYING) {
                stop()
            }
        }
        abandonIv.setOnClickListener {
            reset()
        }
        okIv.setOnClickListener {
            val data = Intent()
            data.putExtra("duration", myMediaRecorder?.mDuration ?: 0)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    var recordJob: Job? = null

    private fun startRecord() {
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
        if (myMediaRecorder == null) {
            myMediaRecorder = MyMediaRecorder.newBuilder().build()
        }
        myMediaRecorder?.start(PostsPublishModel.POSTS_PUBLISH_AUDIO_FILE_PATH, null)
        startRecordTs = System.currentTimeMillis()
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
        SinglePlayer.startPlay(playTag, PostsPublishModel.POSTS_PUBLISH_AUDIO_FILE_PATH)
    }

    private fun stop() {
        SinglePlayer.stop(playTag)
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

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

package com.module.posts.detail.view


import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.player.SinglePlayer
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.component.busilib.view.CircleCountDownView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.posts.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * 帖子k歌录制
 */
class PostsKgeRecordView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "PostsKgeRecordView"
    val playTag = TAG + hashCode()
    internal val STATUS_IDLE = 1
    internal val STATUS_RECORD_OK = 3
    internal val STATUS_RECORD_PLAYING = 4

    var status = STATUS_IDLE
    lateinit var selectSongTv: ExTextView
    lateinit var playBtn: ExImageView
    lateinit var playTipsTv: ExTextView
    lateinit var countDownTv: TextView
    lateinit var abandonIv: ExImageView
    lateinit var abandonTv: ExTextView
    lateinit var okIv: ExImageView
    lateinit var okTv: ExTextView
    lateinit var circleCountDownView: CircleCountDownView
    var recordJob: Job? = null

    var mResetCall: (() -> Unit)? = null

    var okToStopPlayListener: (() -> Unit)? = null

    var okClickListener: (() -> Unit)? = null
    var selectSongClickListener: (() -> Unit)? = null

    override fun init(parentView: View) {
        selectSongTv = parentView.findViewById(R.id.select_song_tv)
        playBtn = parentView.findViewById(R.id.play_btn)
        playTipsTv = parentView.findViewById(R.id.play_tips_tv)
        countDownTv = parentView.findViewById(R.id.count_down_tv)
        abandonIv = parentView.findViewById(R.id.abandon_iv)
        abandonTv = parentView.findViewById(R.id.abandon_tv)
        okIv = parentView.findViewById(R.id.ok_iv)
        okTv = parentView.findViewById(R.id.ok_tv)
        circleCountDownView = parentView.findViewById(R.id.circle_count_down_view)

        playBtn.setOnClickListener {
            if (status == STATUS_IDLE) {
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
        okIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                okClickListener?.invoke()
            }
        })
        selectSongTv.setDebounceViewClickListener {
            selectSongClickListener?.invoke()
        }

        parentView.setDebounceViewClickListener {

        }
    }

    override fun layoutDesc(): Int {
        return R.layout.posts_detail_kge_record_view_stub_layout
    }

    var recordVoicePath: String? = null
    var recordDurationMs: Int? = null

    fun recordOk(recordVoicePath: String?, recordDurationMs: Int) {
        this.recordVoicePath = recordVoicePath
        this.recordDurationMs = recordDurationMs

        status = STATUS_RECORD_OK
        circleCountDownView.visibility = View.GONE
        recordJob?.cancel()
        selectSongTv.visibility = View.GONE
        playBtn.visibility = View.VISIBLE
        playBtn.setImageResource(R.drawable.kge_zanting)
        playTipsTv.visibility = View.VISIBLE
        playTipsTv.text = "播放"
        abandonIv.visibility = View.VISIBLE
        abandonTv.visibility = View.VISIBLE
        okIv.visibility = View.VISIBLE
        okTv.visibility = View.VISIBLE
    }

    private fun play() {
        okToStopPlayListener?.invoke()
        status = STATUS_RECORD_PLAYING
        playBtn.setImageResource(R.drawable.kge_bofang)
        circleCountDownView.visibility = View.VISIBLE
        circleCountDownView.go(0, recordDurationMs ?: 0) {
            stop()
        }
        playTipsTv.text = "暂停"
        recordJob = launch {
            for (i in 0 until 120) {
                delay(1000)
                countDownTv.text = "${i + 1}s"
            }
            stop()
        }
        SinglePlayer.startPlay(playTag, recordVoicePath!!)
    }

    fun stop() {
        SinglePlayer.stop(playTag)
        status = STATUS_RECORD_OK
        circleCountDownView.visibility = View.GONE
        recordJob?.cancel()
        playBtn.setImageResource(R.drawable.kge_zanting)
        playTipsTv.text = "播放"
        abandonIv.visibility = View.VISIBLE
        abandonTv.visibility = View.VISIBLE
        okIv.visibility = View.VISIBLE
        okTv.visibility = View.VISIBLE
        selectSongTv.visibility = View.GONE
    }

    fun reset() {
        if(mParentView==null){
            return
        }
        mResetCall?.invoke()
        stop()
        status = STATUS_IDLE
        playBtn.visibility = View.GONE
        playTipsTv.visibility = View.GONE
        abandonIv.visibility = View.GONE
        abandonTv.visibility = View.GONE
        okIv.visibility = View.GONE
        okTv.visibility = View.GONE
        countDownTv.visibility = View.GONE
        selectSongTv.visibility = View.VISIBLE
    }


}


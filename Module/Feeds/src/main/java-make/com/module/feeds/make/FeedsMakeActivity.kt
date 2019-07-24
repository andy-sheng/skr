package com.module.feeds.make

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route

import com.common.base.BaseActivity
import com.module.RouterConstants
import com.component.voice.control.VoiceControlPanelView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.countdown.CircleCountDownView
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.engine.Params
import com.module.feeds.R
import com.module.feeds.watch.model.FeedSongModel
import com.zq.mediaengine.kit.ZqEngineKit
import com.zq.toast.NoImageCommonToastView
import io.agora.rtc.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


@Route(path = RouterConstants.ACTIVITY_FEEDS_MAKE)
class FeedsMakeActivity : BaseActivity() {
    var mTitleBar: CommonTitleBar? = null
    var mResetIv: ImageView? = null
    var mResetTv: TextView? = null
    var mCircleCountDownView: CircleCountDownView? = null
    var mBeginTv: ExTextView? = null
    var mAdjustIv: ImageView? = null
    var mAdjustTv: TextView? = null
    var mVoiceScaleView: VoiceScaleView? = null
    var mManyLyricsView: ManyLyricsView? = null
    var mVoiceControlView: VoiceControlPanelView? = null
    var mFeedsMakeModel: FeedsMakeModel? = null
    internal var mSkrAudioPermission = SkrAudioPermission()
    internal var mLyricAndAccMatchManager = LyricAndAccMatchManager()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val feedsSongModel = intent.getSerializableExtra("song_model") as? FeedSongModel
        feedsSongModel?.let {
            mFeedsMakeModel = FeedsMakeModel(feedsSongModel)
        }

        mTitleBar = findViewById(R.id.title_bar)
        mResetIv = findViewById(R.id.reset_iv) as ImageView
        mResetTv = findViewById(R.id.reset_tv)
        mCircleCountDownView = findViewById(R.id.circle_count_down_view)
        mBeginTv = findViewById(R.id.begin_tv)
        mAdjustIv = findViewById(R.id.adjust_iv) as ImageView
        mAdjustTv = findViewById(R.id.adjust_tv)
        mVoiceScaleView = findViewById(R.id.voice_scale_view)
        mManyLyricsView = findViewById(R.id.many_lyrics_view)
        mVoiceControlView = findViewById(R.id.voice_control_view)

        if (mFeedsMakeModel == null) {
            U.getToastUtil().showShort("参数不正确")
            finish()
            return
        }
        mFeedsMakeModel?.apply {
            mTitleBar?.centerTextView?.text = songModel.songTpl?.songName

        }

        mTitleBar?.leftImageButton?.setOnClickListener(object:DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                finish()
            }
        })

        mBeginTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mFeedsMakeModel?.let {
                    if (it.recordingClick) {
                        if (it.recording) {
                            //真正在录制
                            if (System.currentTimeMillis() - it.beginRecordTs < 10 * 1000) {
                                U.getToastUtil().showSkrCustomShort(NoImageCommonToastView.Builder(U.app())
                                        .setText("太短啦\n再唱几句吧~")
                                        .build())
                            } else {
                                stopRecord()
                                mBeginTv?.isSelected = false
                                mBeginTv?.text = "开始"
                                mCircleCountDownView?.visibility = View.GONE
                                goNext()
                            }
                        } else {
                            //因为一些资源的原因，录制还未真正开启
                        }
                    } else {
                        // 录制按钮没有点击
                        mFeedsMakeModel?.recordingClick = true
                        mBeginTv?.isSelected = true
                        mBeginTv?.text = "完成"
                        mCircleCountDownView?.visibility = View.VISIBLE
                        startRecord()
                    }
                }
            }
        })
        initEngine()
    }

    private fun initEngine() {
        if (!ZqEngineKit.getInstance().isInit) {
            // 不能每次都初始化,播放伴奏
            val params = Params.getFromPref()
            params.apply {
                scene = Params.Scene.audiotest
                isEnableVideo = false
                isEnableAudio = true
                isUseExternalAudio = true
                isUseExternalAudioRecord = true
            }
            ZqEngineKit.getInstance().init("feeds_make", params)
        }
    }

    private fun startRecord() {
        ZqEngineKit.getInstance().stopAudioRecording()
        ZqEngineKit.getInstance().stopAudioMixing()
        mSkrAudioPermission.ensurePermission({ startRecord1() }, true)

    }

    private fun startRecord1() {
        reset()
        mFeedsMakeModel?.recordingClick = true
        playMusic()
        mLyricAndAccMatchManager.setArgs(mManyLyricsView, mVoiceScaleView, mFeedsMakeModel?.songModel?.songTpl?.lrcTs,
                0, mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0,
                0, mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0,
                mFeedsMakeModel?.songModel?.songTpl?.uploadUserID?.toString() ?: "")

        mLyricAndAccMatchManager.start(object : LyricAndAccMatchManager.Listener {
            override fun onLyricParseSuccess() {

            }

            override fun onLyricParseFailed() {

            }

            override fun onLyricEventPost(lineNum: Int) {
                // 开始录音
                ZqEngineKit.getInstance().startAudioRecording(mFeedsMakeModel?.recordSavePath, Constants.AUDIO_RECORDING_QUALITY_HIGH, false)
                mFeedsMakeModel?.beginRecordTs = System.currentTimeMillis()
                mFeedsMakeModel?.recording = true
                mCircleCountDownView?.go(0,mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt()?:0)
            }
        })
    }

    private fun playMusic() {
        //从bundle里面拿音乐相关数据，然后开始试唱
        launch {
            val bgmFileJob = async(Dispatchers.IO) {
                val file = SongResUtils.getAccFileByUrl(mFeedsMakeModel?.songModel?.songTpl?.bgm)
                if (file?.exists() == true) {
                    file
                } else {
                    U.getHttpUtils().downloadFileSync(mFeedsMakeModel?.songModel?.songTpl?.bgm, file, true, null)
                    file
                }
            }
            val bgmFile = bgmFileJob.await()
            ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.getInstance().uid.toInt(), bgmFile.absolutePath, null, 0, false, false, 1)
        }
    }

    private fun reset() {
        stopRecord()
        mFeedsMakeModel?.recordingClick = false
        mFeedsMakeModel?.recording = false
    }

    private fun stopRecord() {
        ZqEngineKit.getInstance().stopAudioMixing()
        ZqEngineKit.getInstance().stopAudioRecording()
    }

    private fun goNext() {

    }

    override fun onDestroy() {
        super.onDestroy()
        ZqEngineKit.getInstance().destroy("feeds_make")
        mManyLyricsView?.release()
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

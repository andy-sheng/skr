package com.module.feeds.make

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route

import com.common.base.BaseActivity
import com.module.RouterConstants
import com.component.voice.control.VoiceControlPanelView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.utils.DeviceUtils
import com.common.utils.HttpUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.countdown.CircleCountDownView
import com.common.view.titlebar.CommonTitleBar
import com.component.feeds.model.FeedSongModel
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.utils.SongResUtils
import com.component.toast.NoImageCommonToastView
import com.engine.EngineEvent
import com.engine.Params
import com.module.feeds.R
import com.module.feeds.make.editor.FeedsEditorActivity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.trello.rxlifecycle2.android.ActivityEvent
import com.zq.mediaengine.kit.ZqEngineKit
import io.agora.rtc.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.HashSet


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
    var mFeedsMakeModel: FeedsMakeModel? = null
    internal var mSkrAudioPermission = SkrAudioPermission()
    internal var mLyricAndAccMatchManager = LyricAndAccMatchManager()

    val mVoiceControlPanelView by lazy {
        VoiceControlPanelView(this).apply { bindData() }
    }

    val mVoiceControlPanelViewDialog by lazy {
        DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(mVoiceControlPanelView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .create()
    }

    var bgmFileJob: Deferred<File>? = null

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

        if (mFeedsMakeModel == null) {
            U.getToastUtil().showShort("参数不正确")
            finish()
            return
        }
        mFeedsMakeModel?.apply {
            mTitleBar?.centerTextView?.text = songModel.songTpl?.songName
        }

        mTitleBar?.leftImageButton?.setOnClickListener(object : DebounceViewClickListener() {
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
                                recordOk()
                            }
                        } else {
                            //因为一些资源的原因，录制还未真正开启
                        }
                    } else {
                        mTitleBar?.rightCustomView?.visibility = View.GONE
                        startRecord()
                    }
                }
            }
        })
        mResetIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                startRecord()
            }
        })
        mAdjustIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mVoiceControlPanelViewDialog.show()
            }
        })
        mTitleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime(mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs
                ?: 0L)
        initEngine()
        // 加载歌词
        LyricsManager.getLyricsManager(U.app())
                .loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.DESTROY))
                .retryWhen(RxRetryAssist(3, "feed歌词下载失败"))
                .subscribe({ lyricsReader ->
                    MyLog.w(TAG, "onEventMainThread " + "play")
                    mManyLyricsView?.visibility = View.VISIBLE
                    mManyLyricsView?.initLrcData()
                    mManyLyricsView?.lyricsReader = lyricsReader
                    val set = HashSet<Int>()
                    set.add(lyricsReader.getLineInfoIdByStartTs(0))
                    mManyLyricsView?.needCountDownLine = set
                    mManyLyricsView?.seekto(0)
                    mManyLyricsView?.pause()
                }, { throwable ->
                    MyLog.e(TAG, throwable)
                    MyLog.d(TAG, "歌词下载失败，采用不滚动方式播放歌词")
                })
        // 提前下载伴奏
        bgmFileJob = async(Dispatchers.IO) {
            val file = SongResUtils.getAccFileByUrl(mFeedsMakeModel?.songModel?.songTpl?.bgm)
            if (file?.exists() == true) {
                MyLog.d(TAG, "伴奏存在")
                file
            } else {
                for (i in 1..10) {
                    val r = U.getHttpUtils().downloadFileSync(mFeedsMakeModel?.songModel?.songTpl?.bgm, file, true, object : HttpUtils.OnDownloadProgress {
                        override fun onCanceled() {
                        }

                        override fun onFailed() {
                        }

                        override fun onDownloaded(downloaded: Long, totalLength: Long) {
                            mFeedsMakeModel?.bgmDownloadProgress = downloaded / totalLength.toFloat()
                        }

                        override fun onCompleted(localPath: String?) {
                            mFeedsMakeModel?.bgmDownloadProgress = 1f
                        }
                    })
                    if (r) {
                        MyLog.d(TAG, "伴奏下载成功")
                        break
                    } else {
                        delay(3000)
                    }
                }
                file
            }
        }
        if (mFeedsMakeModel?.withBgm == true) {
            (mTitleBar?.rightCustomView as TextView).text = "伴奏"
        } else {
            (mTitleBar?.rightCustomView as TextView).text = "清唱"
        }
        mTitleBar?.rightCustomView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mFeedsMakeModel?.withBgm == true) {
                    mFeedsMakeModel?.withBgm = false
                    (mTitleBar?.rightCustomView as TextView).text = "清唱"
                } else {
                    // 清唱变伴奏
                    if (U.getDeviceUtils().getWiredHeadsetPlugOn()) {
                        // 是否插着有限耳机
                        mFeedsMakeModel?.withBgm = true
                        (mTitleBar?.rightCustomView as TextView).text = "伴奏"
                    } else {
                        U.getToastUtil().showShort("仅在插着有线耳机的情况下才可开启伴奏模式")
                    }
                }
            }
        })
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
        stopRecord()
        // 录制按钮没有点击
        mFeedsMakeModel?.recordingClick = true
        mBeginTv?.isSelected = true
        mBeginTv?.text = "完成"
        mCircleCountDownView?.visibility = View.VISIBLE
        mTitleBar?.centerSubTextView?.text = "00:00"
        mSkrAudioPermission.ensurePermission({ startRecordInner() }, true)
    }

    private fun startRecordInner() {

        mFeedsMakeModel?.recordingClick = true
        if (mFeedsMakeModel?.withBgm == true) {
            // 如果开着伴奏
            if (bgmFileJob?.isCompleted == false) {
                U.getToastUtil().showShort("伴奏下载中 ${(mFeedsMakeModel?.bgmDownloadProgress
                        ?: 0f) * 100}%")
            }
            runBlocking {
                val bgmFile = bgmFileJob?.await()
                ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.getInstance().uid.toInt(), bgmFile?.absolutePath, null, 0, false, false, 1)
                goLyric(true)
            }
        } else {
            goLyric(false)
        }
    }

    private fun goLyric(withacc:Boolean) {
        // 直接走
        val configParams = LyricAndAccMatchManager.ConfigParams().apply {
            manyLyricsView = mManyLyricsView
            voiceScaleView = mVoiceScaleView
            lyricUrl = mFeedsMakeModel?.songModel?.songTpl?.lrcTs
            accBeginTs = 0
            accEndTs = mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0
            lyricBeginTs = 0
            lyricEndTs = mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0
            authorName = mFeedsMakeModel?.songModel?.songTpl?.uploader?.nickname
            accLoadOk = !withacc
            needScore = false
        }
        configParams.manyLyricsView = mManyLyricsView

        mLyricAndAccMatchManager.setArgs(configParams)

        mLyricAndAccMatchManager.start(object : LyricAndAccMatchManager.Listener {
            override fun onLyricParseSuccess(reader: LyricsReader) {
                
            }

            override fun onLyricParseFailed() {

            }

            override fun onLyricEventPost(lineNum: Int) {
                // 开始录音
                ZqEngineKit.getInstance().startAudioRecording(mFeedsMakeModel?.recordSavePath, true)
                mFeedsMakeModel?.beginRecordTs = System.currentTimeMillis()
                mFeedsMakeModel?.recording = true
                mCircleCountDownView?.go(0, mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt()
                        ?: 0)
            }
        })
    }

    private fun recordOk() {
        stopRecord()
        mFeedsMakeModel?.recordingClick = false
        mFeedsMakeModel?.recording = false
        mBeginTv?.isSelected = false
        mBeginTv?.text = "开始"
        mCircleCountDownView?.visibility = View.GONE
        mLyricAndAccMatchManager.stop()
        mFeedsMakeModel?.apply {
            recordDuration = System.currentTimeMillis() - beginRecordTs
        }
        val intent = Intent(this,FeedsEditorActivity::class.java)
        intent.putExtra("feeds_make_model", mFeedsMakeModel)
        startActivityForResult(intent,100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100){
            startRecord()
        }
    }

    private fun stopRecord() {
        ZqEngineKit.getInstance().stopAudioMixing()
        ZqEngineKit.getInstance().stopAudioRecording()
        mLyricAndAccMatchManager.stop()
        mCircleCountDownView?.visibility = View.GONE
    }


    override fun onDestroy() {
        super.onDestroy()
        ZqEngineKit.getInstance().destroy("feeds_make")
        mLyricAndAccMatchManager.stop()
        mManyLyricsView?.release()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        when (event.getType()) {
            EngineEvent.TYPE_MUSIC_PLAY_FINISH -> recordOk()
            EngineEvent.TYPE_MUSIC_PLAY_START -> {

            }
            EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER -> {
                val timeInfo = event.obj as EngineEvent.MixMusicTimeInfo
                mTitleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime(timeInfo.current.toLong())
            }
        }
    }

    @Subscribe
    fun onEvent(event: DeviceUtils.HeadsetPlugEvent) {
        // 清唱变伴奏
        if (!U.getDeviceUtils().wiredHeadsetPlugOn && mFeedsMakeModel?.withBgm == true) {
            U.getToastUtil().showShort("无耳机，自动切换为清唱模式")
            // 是否插着有限耳机
            mFeedsMakeModel?.withBgm = false
            (mTitleBar?.rightCustomView as TextView).text = "清唱"
            if(mFeedsMakeModel?.recording == true){
                // 重新录制
                startRecord()
            }
        } else {

        }
    }

    override fun useEventBus(): Boolean {
        return true
    }
}

package com.module.feeds.make.make

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.DeviceUtils
import com.common.utils.HttpUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.DiffuseView
import com.common.view.countdown.RecordProgressBarView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.utils.SongResUtils
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.TxtLyricScrollView
import com.component.lyrics.widget.VoiceScaleView
import com.component.toast.NoImageCommonToastView
import com.dialog.view.TipsDialogView
import com.engine.EngineEvent
import com.engine.Params
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.*
import com.module.feeds.make.editor.FeedsEditorActivity
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedSongTpl
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*


@Route(path = RouterConstants.ACTIVITY_FEEDS_MAKE)
class FeedsMakeActivity : BaseActivity() {
    var titleBar: CommonTitleBar? = null
    var resetIv: ImageView? = null
    var resetTv: TextView? = null
    var qcProgressBarView: RecordProgressBarView? = null
    var beginTv: ExTextView? = null
    var diffuseView: DiffuseView? = null
    var voiceScaleView: VoiceScaleView? = null
    var manyLyricsView: ManyLyricsView? = null
    var txtLyricsView: TxtLyricScrollView? = null
    var recordTipsIv: View? = null
    var changeLyricIv: ImageView? = null
    var changeLyricTv: TextView? = null


    var mFeedsMakeModel: FeedsMakeModel? = null

    internal var mSkrAudioPermission = SkrAudioPermission()
    internal var mLyricAndAccMatchManager = LyricAndAccMatchManager()

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    var bgmFileJob: Deferred<File>? = null

    var countDownJob: Job? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titleBar = findViewById(R.id.title_bar)
        resetIv = findViewById(R.id.reset_iv) as ImageView
        resetTv = findViewById(R.id.reset_tv)
        beginTv = findViewById(R.id.begin_tv)
        voiceScaleView = findViewById(R.id.voice_scale_view)
        manyLyricsView = findViewById(R.id.many_lyrics_view)
        txtLyricsView = findViewById(R.id.txt_lyrics_view)
        qcProgressBarView = findViewById(R.id.qc_progress_bar)
        diffuseView = findViewById(R.id.pick_diffuse_view)
        recordTipsIv = findViewById(R.id.record_tip_iv)
        changeLyricIv = findViewById(R.id.change_lyric_iv)
        changeLyricTv = findViewById(R.id.change_lyric_tv)
        resetIv?.visibility = View.GONE
        resetTv?.visibility = View.GONE
        titleBar?.leftImageButton?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finishPage()
            }
        })
        beginTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mFeedsMakeModel?.songModel == null) {
                    U.getToastUtil().showShort("资源还在准备中")
                    return
                }
                mFeedsMakeModel?.let {
                    if (it.recordingClick) {
                        if (it.recording) {
                            //真正在录制，除去前奏的长度
                            if (System.currentTimeMillis() - it.beginRecordTs - it.firstLyricShiftTs < 20 * 1000) {
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
                        titleBar?.rightCustomView?.visibility = View.GONE
                        resetIv?.isEnabled = true
                        resetIv?.visibility = View.VISIBLE
                        resetTv?.visibility = View.VISIBLE
                        changeLyricIv?.visibility = View.GONE
                        changeLyricTv?.visibility = View.GONE
                        startRecord()
                    }
                }
            }
        })
        resetIv?.isEnabled = false
        resetIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                startRecord()
            }
        })
        changeLyricIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("music_record", "lyric", null)
                openLyricMakeActivity(mFeedsMakeModel, this@FeedsMakeActivity)
            }
        })
        titleBar?.rightCustomView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mFeedsMakeModel?.withBgm == true) {
                    mFeedsMakeModel?.withBgm = false
                    U.getPreferenceUtils().setSettingBoolean("feeds_with_bgm", false)
                    (titleBar?.rightCustomView as TextView).text = "清唱模式"
                    initViewByData()
                } else {
                    // 清唱变伴奏模式
                    // 是否插着有限耳机
                    if (U.getDeviceUtils().getWiredHeadsetPlugOn() || MyLog.isDebugLogOpen()) {
                        if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.bgm)
                                && !TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)) {
                            mFeedsMakeModel?.withBgm = true
                            U.getPreferenceUtils().setSettingBoolean("feeds_with_bgm", true)
                            (titleBar?.rightCustomView as TextView).text = "伴奏模式"
                            initViewByData()
                        } else {
                            U.getToastUtil().showShort("该首歌曲仅支持清唱模式")
                        }
                    } else {
                        U.getToastUtil().showShort("仅在插着有线耳机的情况下才可开启伴奏模式模式")
                    }
                }
            }
        })


        val from = intent.getIntExtra("from", FROM_CHALLENGE)
        val isDraft = intent.getBooleanExtra("isDraft", false)
        if(!isDraft){
            if (from == FROM_CHALLENGE) {
                val challengeID = intent.getLongExtra("challengeID", 0)
                mFeedsMakeModel = FeedsMakeModel()
                mFeedsMakeModel?.challengeID = challengeID
                launch {
                    mFeedsMakeModel?.challengeID?.let {
                        for (i in 1..10) {
                            val result = subscribe { feedsMakeServerApi.getSongTplByChallengeID(it) }
                            if (result?.errno == 0) {
                                val songTpl = JSON.parseObject(result.data.getString("songTpl"), FeedSongTpl::class.java)
                                val workName = result.data.getString("workName")
                                val challengeDesc = result.data.getString("challengeDesc")
                                mFeedsMakeModel?.challengeType = result.data.getIntValue("challengeType")
                                val songModel = FeedSongModel()
                                songModel.challengeDesc = challengeDesc
                                songModel.songTpl = songTpl
                                songModel.challengeID = it.toLong()
                                songModel.workName = workName
//                            if (TextUtils.isEmpty(songModel?.songTpl?.songName)) {
//                                songModel?.songTpl?.songName = songModel.workName
//                            }
                                songModel.playDurMs = songTpl?.bgmDurMs?.toInt() ?: 0

                                mFeedsMakeModel?.songModel = songModel
                                whenDataOk()
                                break
                            } else {
                                delay(3000)
                            }
                        }
                    }
                }
            } else if (from == FROM_QUICK_SING || from == FROM_CHANGE_SING) {
                val feedSongModel = intent.getSerializableExtra("feedSongModel") as FeedSongModel?
                mFeedsMakeModel = FeedsMakeModel()
                if(from==FROM_QUICK_SING){
                    mFeedsMakeModel?.challengeType = CHALLENGE_TYPE_QUICK_SONG
                }else if(from== FROM_CHANGE_SING){
                    mFeedsMakeModel?.challengeType = CHALLENGE_TYPE_CHANGE_SONG
                }
                mFeedsMakeModel?.songModel = feedSongModel
                //mFeedsMakeModel?.songModel?.workName = mFeedsMakeModel?.songModel?.songTpl?.songName
                whenDataOk()
            }
        }else{
            // 从草稿箱进来的
            mFeedsMakeModel = sFeedsMakeModelHolder
            /**
             * 因为是引用传递，所以重新初始化一下相关属性
             */
            mFeedsMakeModel?.hasChangeLyricOrSongNameThisTime = false
            mFeedsMakeModel?.bgmDownloadProgress = 0
            resetValue()

            sFeedsMakeModelHolder = null
            // 将伴奏的reader弄好
            // 加载歌词
            if (TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)) {
                whenDataOk()
            } else {
                LyricsManager
                        .loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)
                        .subscribe({ lyricsReader ->
                            createCustomZrce2ReaderByTxt(lyricsReader, mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr)
                            mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader = lyricsReader
                            whenDataOk()
                        }, { throwable ->
                            MyLog.e(TAG, throwable)
                        })
            }
        }
        if (mFeedsMakeModel == null) {
            U.getToastUtil().showShort("参数不正确")
            finish()
            return
        }
        initEngine()
    }

    override fun onResume() {
        super.onResume()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun whenDataOk() {
        if(mFeedsMakeModel?.challengeType == CHALLENGE_TYPE_QUICK_SONG){
            changeLyricTv?.visibility = View.GONE
            changeLyricIv?.visibility = View.GONE
        }
        mFeedsMakeModel?.withBgm = U.getPreferenceUtils().getSettingBoolean("feeds_with_bgm", false)
                && U.getDeviceUtils().getWiredHeadsetPlugOn()
                && !TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.bgm)
                && !TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)

        if (mFeedsMakeModel?.withBgm == true) {
            (titleBar?.rightCustomView as TextView).text = "伴奏模式"
        } else {
            (titleBar?.rightCustomView as TextView).text = "清唱模式"
        }

        initViewByData()
        // 有伴奏模式提前下载伴奏模式
        mFeedsMakeModel?.songModel?.songTpl?.bgm.let {
            bgmFileJob = async(Dispatchers.IO) {
                val file = SongResUtils.getAccFileByUrl(it)
                if (file?.exists() == true) {
                    MyLog.d(TAG, "伴奏存在")
                    file
                } else {
                    for (i in 1..10) {
                        val r = U.getHttpUtils().downloadFileSync(it, file, true, object : HttpUtils.OnDownloadProgress {
                            override fun onCanceled() {
                            }

                            override fun onFailed() {
                            }

                            override fun onDownloaded(downloaded: Long, totalLength: Long) {
                                mFeedsMakeModel?.bgmDownloadProgress = (100 * downloaded / totalLength).toInt()
                            }

                            override fun onCompleted(localPath: String?) {
                                mFeedsMakeModel?.bgmDownloadProgress = 100
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
        }
    }

    private fun initViewByData() {
        titleBar?.centerTextView?.text = mFeedsMakeModel?.songModel?.getDisplayName()
        qcProgressBarView?.visibility = View.GONE
        voiceScaleView?.visibility = View.GONE
        manyLyricsView?.visibility = View.GONE
        txtLyricsView?.visibility = View.GONE
        val lrcTs = mFeedsMakeModel?.songModel?.songTpl?.lrcTs
        val lrcTxt = mFeedsMakeModel?.songModel?.songTpl?.lrcTxt
        if (mFeedsMakeModel?.withBgm == true) {
            mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.let {
                titleBar?.centerSubTextView?.text = "00:00 / ${U.getDateTimeUtils().formatVideoTime(it)}"
            }
            if (!TextUtils.isEmpty(lrcTs)) {
                voiceScaleView?.visibility = View.VISIBLE
                manyLyricsView?.visibility = View.VISIBLE
                if (mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader != null) {
                    initManyLyricView()
                } else {
                    // 加载歌词
                    LyricsManager
                            .loadStandardLyric(lrcTs)
                            .subscribe({ lyricsReader ->
                                mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader = lyricsReader
                                initManyLyricView()
                            }, { throwable ->
                                MyLog.e(TAG, throwable)
                            })
                }
            } else {
                qcProgressBarView?.visibility = View.VISIBLE
                txtLyricsView?.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr)) {
                    txtLyricsView?.setLyrics(mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr ?: "")
                } else {
                    LyricsManager.loadGrabPlainLyric(lrcTxt)
                            .subscribe({ lyricsReader ->
                                mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr = lyricsReader
                                txtLyricsView?.setLyrics(lyricsReader)
                            }, { throwable ->
                                MyLog.e(TAG, throwable)
                            })
                }
            }
        } else {
            mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.let {
                titleBar?.centerSubTextView?.text = "00:00 / 01:30"
            }
            qcProgressBarView?.visibility = View.VISIBLE
            txtLyricsView?.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr)) {
                txtLyricsView?.setLyrics(mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr ?: "")
            } else {
                if (!TextUtils.isEmpty(lrcTs)) {
                    // 加载歌词
                    LyricsManager
                            .loadStandardLyric(lrcTs)
                            .subscribe({ lyricsReader ->
                                val sb = StringBuilder()
                                lyricsReader?.let {
                                    val it = it.lrcLineInfos.entries.iterator()
                                    while (it.hasNext()) {
                                        val entry = it.next()
                                        val s = entry.value.lineLyrics
                                        sb.append(s).append("\n")
                                    }
                                }
                                mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr = sb.toString()
                                txtLyricsView?.setLyrics(sb.toString())
                            }, { throwable ->
                                MyLog.e(TAG, throwable)
                            })
                } else {
                    LyricsManager.loadGrabPlainLyric(lrcTxt)
                            .subscribe({ lyricsReader ->
                                mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr = lyricsReader
                                txtLyricsView?.setLyrics(lyricsReader)
                            }, { throwable ->
                                MyLog.e(TAG, throwable)
                            })
                }
            }
        }
    }

    private fun initManyLyricView() {
        manyLyricsView?.initLrcData()
        val lyricsReader = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader
        manyLyricsView?.lyricsReader = lyricsReader
        val set = HashSet<Int>()
        set.add(lyricsReader?.getLineInfoIdByStartTs(0) ?: 0)
        manyLyricsView?.needCountDownLine = set
        manyLyricsView?.seekTo(0)
        manyLyricsView?.pause()
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
                audioMixingPlayoutVolume = 100
                recordingSignalVolume = 200
                styleEnum = Params.AudioEffect.none
            }
            ZqEngineKit.getInstance().init("feeds_make" + hashCode(), params)
        }
    }

    private fun startRecord() {
        stopRecord()
        // 录制按钮没有点击
        mFeedsMakeModel?.recordingClick = true
        beginTv?.isSelected = true
        beginTv?.text = "完成"
        titleBar?.centerSubTextView?.text = "00:00"
        mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.let {
            titleBar?.centerSubTextView?.append(" / ${U.getDateTimeUtils().formatVideoTime(it)}")
        }
        mSkrAudioPermission.ensurePermission({ startRecordInner() }, true)
    }

    private fun startRecordInner() {

        mFeedsMakeModel?.recordingClick = true
        if (mFeedsMakeModel?.withBgm == true) {
            // 如果开着伴奏
            if (bgmFileJob?.isCompleted == false) {
                U.getToastUtil().showShort("伴奏下载中 ${mFeedsMakeModel?.bgmDownloadProgress}%")
            }
            runBlocking {
                val bgmFile = bgmFileJob?.await()
                bgmFile?.absolutePath?.let {
                    ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.getInstance().uid.toInt(), it, null, 0, false, false, 1)
                }
                goLyric(true)
            }
        } else {
            goLyric(false)
        }
    }

    private fun goLyric(withacc: Boolean) {
        mFeedsMakeModel?.beginRecordTs = System.currentTimeMillis()
        if (manyLyricsView?.visibility == View.VISIBLE) {
            // 直接走
            qcProgressBarView?.visibility = View.GONE
            val configParams = LyricAndAccMatchManager.ConfigParams().apply {
                manyLyricsView = this@FeedsMakeActivity.manyLyricsView
                voiceScaleView = this@FeedsMakeActivity.voiceScaleView
                //lyricUrl = mFeedsMakeModel?.songModel?.songTpl?.lrcTs
                lyricReader = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader
                accBeginTs = 0
                accEndTs = mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0
                lyricBeginTs = 0
                lyricEndTs = mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt() ?: 0
                authorName = mFeedsMakeModel?.songModel?.songTpl?.uploader?.nickname
                accLoadOk = !withacc
                needScore = false
            }
            configParams.manyLyricsView = manyLyricsView

            mLyricAndAccMatchManager.setArgs(configParams)
            mLyricAndAccMatchManager.start(object : LyricAndAccMatchManager.Listener {
                override fun onLyricParseSuccess(reader: LyricsReader) {
                    mFeedsMakeModel?.firstLyricShiftTs = reader.lrcLineInfos?.get(0)?.startTime ?: 0
                }

                override fun onLyricParseFailed() {

                }

                override fun onLyricEventPost(lineNum: Int) {
                    // 开始录音
                    ZqEngineKit.getInstance().startAudioRecording(mFeedsMakeModel?.recordSavePath, true)
                    mFeedsMakeModel?.beginRecordTs = System.currentTimeMillis()
                    mFeedsMakeModel?.recording = true
                    val leave = mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.toInt()
                            ?: 60 * 1000
                    qcProgressBarView?.go(0, leave) {
                        recordOk()
                    }
                    countDownBegin()
                }
            })
        } else {
            mFeedsMakeModel?.firstLyricShiftTs = 0
            voiceScaleView?.stop(false)
            qcProgressBarView?.visibility = View.VISIBLE
            // 开始录音
            ZqEngineKit.getInstance().startAudioRecording(mFeedsMakeModel?.recordSavePath, true)
            mFeedsMakeModel?.recording = true
            val leave = 90 * 1000
            qcProgressBarView?.go(0, leave) {
                recordOk()
            }
            countDownBegin()
        }
    }

    private fun countDownBegin() {
        countDownJob?.cancel()
        titleBar?.centerTextView?.setEllipsize(TextUtils.TruncateAt.END)
        countDownJob = launch {
            for (i in 0..Int.MAX_VALUE) {
                //MyLog.d(TAG, "countDownBegin run")
                titleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime((i * 1000).toLong())
                if (mFeedsMakeModel?.withBgm == true) {
                    mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.let {
                        titleBar?.centerSubTextView?.append(" / ${U.getDateTimeUtils().formatVideoTime(it)}")
                    }
                } else {
                    mFeedsMakeModel?.songModel?.songTpl?.bgmDurMs?.let {
                        titleBar?.centerSubTextView?.append(" / 01:30")
                    }
                }
                diffuseView?.start(2000)
                recordTipsIv?.visibility = if (i % 2 == 0) View.GONE else View.VISIBLE
                delay(1000)
            }
        }
    }

    private fun recordOk() {
        resetAll()
        mFeedsMakeModel?.apply {
            recordDuration = System.currentTimeMillis() - beginRecordTs
            recordOffsetTs = firstLyricShiftTs + musicFirstFrameTs - recordFirstFrameTs
        }
        sFeedsMakeModelHolder = mFeedsMakeModel
        val intent = Intent(this, FeedsEditorActivity::class.java)
        startActivityForResult(intent, 100)
    }

    private fun resetValue(){
        mFeedsMakeModel?.recordingClick = false
        mFeedsMakeModel?.recordOffsetTs = 0
        mFeedsMakeModel?.firstLyricShiftTs = 0
        mFeedsMakeModel?.recording = false
        mFeedsMakeModel?.beginRecordTs = Long.MAX_VALUE
        mFeedsMakeModel?.recordFirstFrameTs = Long.MAX_VALUE
        mFeedsMakeModel?.musicFirstFrameTs = Long.MAX_VALUE
    }
    private fun resetAll() {
        stopRecord()
        resetValue()
        beginTv?.isSelected = false
        beginTv?.text = "开始"
        qcProgressBarView?.progress=0
        qcProgressBarView?.visibility = View.GONE
        mLyricAndAccMatchManager.stop()
        mLyricAndAccMatchManager.stop()
        resetIv?.visibility = View.GONE
        resetTv?.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                // 重唱
                resetAll()
                initViewByData()
            } else if (requestCode == 101) {
                // 改完歌词
                mFeedsMakeModel = sFeedsMakeModelHolder
                sFeedsMakeModelHolder = null
                initViewByData()
            } else {
                finish()
            }
        } else {
            if (requestCode == 100) {
                finish()
            }
        }
    }

    private fun stopRecord() {
        ZqEngineKit.getInstance().stopAudioMixing()
        ZqEngineKit.getInstance().stopAudioRecording()
        mLyricAndAccMatchManager.stop()
        qcProgressBarView?.visibility = View.GONE
        countDownJob?.cancel()
        mFeedsMakeModel?.recording = false
    }

    override fun onDestroy() {
        super.onDestroy()
        ZqEngineKit.getInstance().destroy("feeds_make" + hashCode())
        mLyricAndAccMatchManager.stop()
        manyLyricsView?.release()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun finishPage() {
        if (mFeedsMakeModel?.hasChangeLyricOrSongNameThisTime == true) {
            val tipsDialogView = TipsDialogView.Builder(this@FeedsMakeActivity)
                    .setConfirmTip("保存")
                    .setCancelTip("直接退出")
                    .setCancelBtnClickListener {
                        finish()
                    }
                    .setMessageTip("是否将改编歌词保存到草稿箱?")
                    .setConfirmBtnClickListener {
                        launch {
                            val j = launch(Dispatchers.IO) {
                                mFeedsMakeModel?.let {
                                    FeedsMakeLocalApi.insert(it)
                                }
                            }
                            // 保存到草稿
                            j.join()
                            if (mFeedsMakeModel?.songModel?.challengeID == 0L) {
                                U.getToastUtil().showShort("已存入翻唱草稿")
                            } else {
                                U.getToastUtil().showShort("已存入打榜草稿")
                            }
                            finish()
                        }
                    }
                    .build()
            tipsDialogView.showByDialog()
        } else {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        when (event.getType()) {
            EngineEvent.TYPE_MUSIC_PLAY_FINISH -> recordOk()
            EngineEvent.TYPE_MUSIC_PLAY_START -> {

            }
            EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER -> {
//                val timeInfo = event.obj as EngineEvent.MixMusicTimeInfo
//                mTitleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime(timeInfo.current.toLong())
            }
            EngineEvent.TYPE_RECORD_AUDIO_FIRST_PKT -> {
                mFeedsMakeModel?.recordFirstFrameTs = event.obj as Long
            }
            EngineEvent.TYPE_MUSIC_PLAY_FIRST_PKT -> {
                mFeedsMakeModel?.musicFirstFrameTs = event.obj as Long
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
            (titleBar?.rightCustomView as TextView).text = "清唱模式"
            if (mFeedsMakeModel?.recording == true) {
                // 重新录制
                startRecord()
            }
        } else {

        }
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onBackPressed() {
        finishPage()
    }
}

fun openFeedsMakeActivityFromChallenge(challenge: Long?) {
    challenge?.let {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
                .withInt("from", FROM_CHALLENGE)
                .withBoolean("isDraft", false)
                .withSerializable("challengeID", it)
                .navigation()
    }
}

fun openFeedsMakeActivityFromQuickSong(model: FeedSongModel?) {
    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
            .withInt("from", FROM_QUICK_SING)
            .withBoolean("isDraft", false)
            .withSerializable("feedSongModel", model)
            .navigation()
}

fun openFeedsMakeActivityFromChangeSong(model: FeedSongModel?) {
    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
            .withInt("from", FROM_CHANGE_SING)
            .withBoolean("isDraft", false)
            .withSerializable("feedSongModel", model)
            .navigation()
}

fun openFeedsMakeActivityFromDraft(draftFrom:Int,model:FeedsMakeModel?) {
    sFeedsMakeModelHolder = model
    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_MAKE)
            .withInt("from", draftFrom)
            .withBoolean("isDraft", true)
            .withSerializable("feedMakeModel",model)
            .navigation()
}

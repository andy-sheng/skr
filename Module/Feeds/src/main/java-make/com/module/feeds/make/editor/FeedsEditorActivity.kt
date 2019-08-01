package com.module.feeds.make.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.LyricsManager
import com.component.lyrics.utils.SongResUtils
import com.component.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY
import com.component.lyrics.widget.ManyLyricsView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.view.AutoScrollLyricView
import com.module.feeds.watch.view.FeedsRecordAnimationView
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.view.FeedsEditorVoiceControlPanelView
import com.module.feeds.make.view.FeedsMakeVoiceControlPanelView
import com.module.feeds.make.view.VocalAlignControlPannelView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.trello.rxlifecycle2.android.ActivityEvent
import com.zq.mediaengine.kit.ZqAudioEditorKit
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*


@Route(path = RouterConstants.ACTIVITY_FEEDS_EDITOR)
class FeedsEditorActivity : BaseActivity() {

    var mTitleBar: CommonTitleBar? = null
    var mPlayBtn: ExImageView? = null
    var mSeekBar: SeekBar? = null
    var mAvatarBg: BaseImageView? = null
    var mRadioView: FeedsRecordAnimationView? = null
    var mManyLyricsView: ManyLyricsView? = null
    var mAutoScrollLyricView: AutoScrollLyricView? = null
    var mVoiceControlView: FeedsEditorVoiceControlPanelView? = null
    var mVaControlView: VocalAlignControlPannelView? = null
    var mMenuBg: View? = null
    var mRenshengIv: ExImageView? = null
    var mEffectIv: ExImageView? = null
    var mResetIv: ExImageView? = null
    var mPublishIv: ExImageView? = null

    var mFeedsMakeModel: FeedsMakeModel? = null

    val mZqAudioEditorKit = ZqAudioEditorKit(U.app())

    var mPlayProgressJob: Job? = null

    var mComposeProgressbarVG: ViewGroup? = null

    var mComposeProgressTipsTv: TextView? = null
    var mCoverView:View?=null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_editor_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = intent.getSerializableExtra("feeds_make_model") as FeedsMakeModel?
        MyLog.d(TAG, "mFeedsMakeModel=$mFeedsMakeModel")
        mTitleBar = findViewById(R.id.title_bar)
        mPlayBtn = findViewById(R.id.play_btn)
        mSeekBar = findViewById(R.id.seek_bar)
        mAvatarBg = findViewById(R.id.avatar_bg)
        mRadioView = findViewById(R.id.radio_view)
        mManyLyricsView = findViewById(R.id.many_lyrics_view)
        mAutoScrollLyricView = AutoScrollLyricView(findViewById(R.id.auto_scroll_lyric_view_layout_viewstub))
        mVoiceControlView = findViewById(R.id.voice_control_view)
        mVaControlView = findViewById(R.id.va_control_view)
        mMenuBg = findViewById(R.id.menu_bg)
        mRenshengIv = findViewById(R.id.rensheng_iv)
        mEffectIv = findViewById(R.id.effect_iv)
        mResetIv = findViewById(R.id.reset_iv)
        mPublishIv = findViewById(R.id.publish_iv)
        mComposeProgressbarVG = findViewById(R.id.compose_progressbar_vg)
        mComposeProgressTipsTv = findViewById(R.id.progress_tips_tv)
        mComposeProgressbarVG?.setOnClickListener {
            // 吃掉点击事件
        }

        mCoverView  = findViewById<View>(R.id.cover_view)
        mCoverView?.setOnClickListener {
            mRenshengIv?.isSelected = false
            mEffectIv?.isSelected = false
            mVaControlView?.visibility = View.GONE
            mVoiceControlView?.visibility = View.GONE
        }
        
        mTitleBar?.centerTextView?.text = mFeedsMakeModel?.songModel?.workName
        mTitleBar?.leftImageButton?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })
        mTitleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime(mFeedsMakeModel?.recordDuration?.toLong()
                ?: 0L)

        mPlayBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mPlayBtn?.isSelected == true) {
                    pausePreview()
                } else {
                    resumePreview()
                }
            }
        })
        mSeekBar?.max = mFeedsMakeModel?.recordDuration!!.toInt() - mFeedsMakeModel?.firstLyricShiftTs!!
        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mZqAudioEditorKit.seekTo(seekBar?.progress?.toLong() ?: 0)
            }

        })

        AvatarUtils.loadAvatarByUrl(mAvatarBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(10.dp().toFloat())
                .setBlur(true)
                .build())
        mRadioView?.setAvatar(MyUserInfoManager.getInstance().avatar)
        mRadioView?.pause()

        if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)) {
            mAutoScrollLyricView?.visibility = View.GONE
            LyricsManager
                    .loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs, -1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.bindUntilEvent(ActivityEvent.DESTROY))
                    .retryWhen(RxRetryAssist(3, "feed歌词下载失败"))
                    .subscribe({ lyricsReader ->
                        MyLog.w(TAG, "onEventMainThread " + "play")
                        mManyLyricsView?.visibility = View.VISIBLE
                        mManyLyricsView?.initLrcData()
                        mManyLyricsView?.lyricsReader = lyricsReader
                        mManyLyricsView?.seekTo(0)
                        mManyLyricsView?.pause()
                    }, { throwable ->
                        MyLog.e(TAG, throwable)
                        MyLog.d(TAG, "歌词下载失败，采用不滚动方式播放歌词")
                    })
        } else {
            mManyLyricsView?.visibility = View.GONE
            mFeedsMakeModel?.songModel?.let {
                mAutoScrollLyricView?.setSongModel(it, -1)
            }
        }
        mVaControlView?.audioEditorKit = mZqAudioEditorKit
        // 面板控制
        mRenshengIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mFeedsMakeModel?.withBgm == false) {
                    U.getToastUtil().showShort("清唱模式下无需人声对齐")
                    return
                }
                mRenshengIv?.isSelected = true
                mEffectIv?.isSelected = false
                mVaControlView?.visibility = View.VISIBLE
                mVoiceControlView?.visibility = View.GONE
                mCoverView?.visibility = View.VISIBLE
            }
        })

        mVoiceControlView?.mZqAudioEditorKit = mZqAudioEditorKit

        mEffectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mRenshengIv?.isSelected = false
                mEffectIv?.isSelected = true
                mVaControlView?.visibility = View.GONE
                mVoiceControlView?.visibility = View.VISIBLE
                mCoverView?.visibility = View.VISIBLE
            }
        })

        mResetIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        mPublishIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 开始合成
                pausePreview()
                mComposeProgressbarVG?.visibility = View.VISIBLE
                mZqAudioEditorKit.startCompose()
            }
        })
        mZqAudioEditorKit.setOnErrorListener(object : ZqAudioEditorKit.OnErrorListener {
            override fun onError(what: Int, msg1: Int, msg2: Int) {
                MyLog.e(TAG, "onError what=$what msg1=$msg1 msg2=$msg2")
            }
        })

        mZqAudioEditorKit.setOnPreviewInfoListener(object : ZqAudioEditorKit.OnPreviewInfoListener {
            override fun onStarted() {
                MyLog.d(TAG, "onStarted")
            }

            override fun onCompletion() {
                MyLog.d(TAG, "preview onCompletion")
                //startPreview()
            }

            override fun onLoopCount(count: Int) {
                MyLog.d(TAG, "onLoopCount count:$count")
            }
        })

        mZqAudioEditorKit.setOnComposeInfoListener(object : ZqAudioEditorKit.OnComposeInfoListener {
            override fun onProgress(progress: Float) {
                launch {
                    mComposeProgressTipsTv?.text = "合成进度 ${(progress * 100).toInt()}%"
                }
            }

            override fun onCompletion() {
                MyLog.d(TAG, "compose onCompletion")
                launch {
                    mComposeProgressbarVG?.visibility = View.GONE
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_PUBLISH)
                            .withSerializable("feeds_make_model", mFeedsMakeModel)
                            .navigation(this@FeedsEditorActivity, 9)
                }
            }
        })

        if (mFeedsMakeModel?.withBgm == true) {
            mVoiceControlView?.mPeopleVoiceIndex = 1
            runBlocking {
                val bgmFileJob = async(Dispatchers.IO) {
                    val file = SongResUtils.getAccFileByUrl(mFeedsMakeModel?.songModel?.songTpl?.bgm)
                    if (file?.exists() == true) {
                        MyLog.d(TAG, "伴奏存在")
                        file
                    } else {
                        for (i in 1..10) {
                            val r = U.getHttpUtils().downloadFileSync(mFeedsMakeModel?.songModel?.songTpl?.bgm, file, true, null)
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
                //播放音乐
                mZqAudioEditorKit.setDataSource(0, bgmFileJob.await().path, mFeedsMakeModel?.firstLyricShiftTs?.toLong()
                        ?: 0L, mFeedsMakeModel?.recordDuration
                        ?: -1)
                mZqAudioEditorKit.setDataSource(1, mFeedsMakeModel?.recordSavePath, mFeedsMakeModel?.firstLyricShiftTs?.toLong()
                        ?: 0L, -1)
                mZqAudioEditorKit.setInputVolume(0, ZqEngineKit.getInstance().params.audioMixingPlayoutVolume / 100.0f)
                mZqAudioEditorKit.setInputVolume(1, ZqEngineKit.getInstance().params.recordingSignalVolume / 100.0f)
                mZqAudioEditorKit.setAudioEffect(1, ZqEngineKit.getInstance().params.styleEnum.ordinal)
                initWhenEngineReady()
            }
        } else {
            mVoiceControlView?.mPeopleVoiceIndex = 0
            mZqAudioEditorKit.setDataSource(0, mFeedsMakeModel?.recordSavePath, 0, mFeedsMakeModel?.recordDuration
                    ?: -1)
            mZqAudioEditorKit.setInputVolume(0, ZqEngineKit.getInstance().params.recordingSignalVolume / 100.0f)
            mZqAudioEditorKit.setAudioEffect(0, ZqEngineKit.getInstance().params.styleEnum.ordinal)
            initWhenEngineReady()
        }
    }

    private fun initWhenEngineReady() {
        MyLog.d(TAG, "initWhenEngineReady")
        mZqAudioEditorKit.outputPath = mFeedsMakeModel?.composeSavePath
        mVoiceControlView?.bindData()
        mVaControlView?.bindData()
//        mEffectIv?.isSelected = true
//        mVoiceControlView?.visibility = View.VISIBLE
        mZqAudioEditorKit.startPreview(-1)
        resumePreview()
    }

    private fun resumePreview() {
        MyLog.d(TAG, "startPreview")
        // 预览开始了
        mZqAudioEditorKit?.resumePreview()
        mPlayBtn?.isSelected = true
        mRadioView?.play()

        if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)) {
            mManyLyricsView?.play(mZqAudioEditorKit.position.toInt())
        } else {
            mAutoScrollLyricView?.playLyric()
        }

        mPlayProgressJob?.cancel()
        mPlayProgressJob = launch {
            for (i in 1..1000) {
                mTitleBar?.centerSubTextView?.text = U.getDateTimeUtils().formatVideoTime(mZqAudioEditorKit.position)
                if (mManyLyricsView?.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                    mManyLyricsView?.resume()
                }
                if (!TextUtils.isEmpty(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)) {
                    mManyLyricsView?.seekTo(mZqAudioEditorKit.position.toInt())
                } else {
                    mAutoScrollLyricView?.seekTo(mZqAudioEditorKit.position.toInt())
                }
                mSeekBar?.progress = mZqAudioEditorKit.position.toInt()
                delay(1000)
            }
        }
    }

    private fun pausePreview() {
        MyLog.d(TAG, "pausePreview")
        mZqAudioEditorKit.pausePreview()
        mPlayProgressJob?.cancel()
        mPlayBtn?.isSelected = false
        mManyLyricsView?.pause()
        mRadioView?.pause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mManyLyricsView?.release()
        mZqAudioEditorKit.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 9) {
                // 从发布页返回
                mZqAudioEditorKit.startPreview(-1)
                resumePreview()
            }
        }
    }
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: EngineEvent) {
//        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
//        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_START) {
//        }
//    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

package com.component.lyrics

import android.os.Handler
import android.os.Message
import android.view.View

import com.common.engine.ScoreConfig
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.utils.U
import com.engine.EngineEvent
import com.engine.arccloud.SongInfo
import com.component.lyrics.event.LrcEvent
import com.component.lyrics.event.LyricEventLauncher
import com.component.lyrics.widget.AbstractLrcView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.zq.mediaengine.kit.ZqEngineKit

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList
import java.util.HashSet

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import com.component.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY

/**
 * 这个类的职责是负责
 * 歌词与伴奏 以及 歌词结束时间 以及歌词条运动动画 的完整匹配
 * 因为 调音间 排位赛 一adb唱到底伴奏模式 很多地方都要有类似的校准
 * 特别是 加入 agora token 验证后，播放音乐就有延迟了
 */
class LyricAndAccMatchManager {
    val TAG = "LyricAndAccMatchManager"
    var params: ConfigParams? = null
    internal var mDisposable: Disposable? = null
    internal var mListener: Listener? = null

    internal var mLyricEventLauncher = LyricEventLauncher()
    internal var mLyricsReader: LyricsReader? = null
    // 按理 歌词 和 伴奏 都ok了 才抛出歌词end事件，但事件的时间戳要做矫正
    internal var mLrcLoadOk = false
    internal var mHasLauncher = false

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ENSURE_LAUNCHER -> {
                    MyLog.d(TAG, "handleMessage acc 加载超时，不等了，直接发事件")
                    launchLyricEvent(LAUNCHER_DELAY)
                }
                else -> {
                    val lineNo = (msg.what - MSG_SHOW_SCORE_EVENT) / 100
                    MyLog.d(TAG, "handleMessage lineNo=$lineNo mLastLineNum=$mLastLineNum")
                    if (lineNo > mLastLineNum) {
                        mAcrScore = -2
                        if (ScoreConfig.isMelp2Enable()) {
                            if (mMelp2Score >= 0) {
                                processScore("handleMessage", mMelp2Score, mAcrScore, lineNo)
                            } else {
                                // 这样等melp2 回调ok了还可以继续走
                            }
                        } else if (ScoreConfig.isMelpEnable()) {
                            val melp1Score = ZqEngineKit.getInstance().lineScore1
                            if (melp1Score > mAcrScore) {
                                processScore("handleMessage", melp1Score, mAcrScore, lineNo)
                            } else {
                                processScore("handleMessage", melp1Score, mAcrScore, lineNo)
                            }
                        }
                    }
                }
            }
        }
    }

    private var mLastLineNum = -1
    internal var mMelp2Score = -1// 本轮 Melp2 打分
    internal var mAcrScore = -1// 本轮 acr 打分

    fun setArgs(params: ConfigParams
    ) {
        MyLog.w(TAG, "setArgs params=$params")
        this.params = params
        mLrcLoadOk = false
        mHasLauncher = false
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun start(l: Listener) {
        MyLog.d(TAG, "start l=$l")
        mUiHandler.removeCallbacksAndMessages(null)
        mLastLineNum = -1
        mListener = l
        mHasLauncher = false
        parseLyric()
    }

    fun stop() {
        MyLog.d(TAG, "stop")
        EventBus.getDefault().unregister(this)
        params?.voiceScaleView?.startWithData(ArrayList(), 0)
        mUiHandler.removeCallbacksAndMessages(null)
        mLyricEventLauncher.destroy()
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
        mLastLineNum = -1
        mHasLauncher = false
        mListener = null
    }

    private fun parseLyric() {
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
        if(params?.lyricReader!=null){
            parseReader(params!!.lyricReader!!)
        }else{
            mDisposable = LyricsManager
                    .loadStandardLyric(params?.lyricUrl)
                    .subscribe({ lyricsReader ->
                        parseReader(lyricsReader)
                    }, { throwable ->
                        MyLog.e(TAG, throwable)
                        MyLog.d(TAG, "歌词下载失败，采用不滚动方式播放歌词")
                        if (mListener != null) {
                            mListener!!.onLyricParseFailed()
                        }
                    })
        }
    }

    private fun parseReader(lyricsReader:LyricsReader){
        MyLog.w(TAG, "onEventMainThread " + "play")
        mListener?.onLyricParseSuccess(lyricsReader)
        params?.manyLyricsView?.visibility = View.VISIBLE
        params?.manyLyricsView?.initLrcData()
        lyricsReader.cut(params?.lyricBeginTs?.toLong()
                ?: 0, params?.lyricEndTs?.toLong() ?: Long.MAX_VALUE)
        params?.manyLyricsView?.lyricsReader = lyricsReader
        val set = HashSet<Int>()
        set.add(lyricsReader.getLineInfoIdByStartTs(params?.lyricBeginTs?.toLong()
                ?: 0))
        params?.manyLyricsView?.needCountDownLine = set
        if (params?.manyLyricsView?.lrcStatus == AbstractLrcView.LRCSTATUS_LRC && params?.manyLyricsView?.lrcPlayerStatus != LRCPLAYERSTATUS_PLAY) {
            //                            mManyLyricsView.play(mAccBeginTs);
            params?.manyLyricsView?.seekTo(params?.accBeginTs ?: 0)
            params?.manyLyricsView?.pause()
            mLyricsReader = lyricsReader
            if (params?.accLoadOk == true) {
                launchLyricEvent(ZqEngineKit.getInstance().audioMixingCurrentPosition)
            } else {
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_LAUNCHER, LAUNCHER_DELAY.toLong())
            }
            mLrcLoadOk = true
            // 这里是假设 伴奏 和 歌词一起初始化完毕的， 实际两者会有偏差优化下
            //                            int lineNum = mLyricEventLauncher.postLyricEvent(lyricsReader, lrcBeginTs - GrabRoomData.ACC_OFFSET_BY_LYRIC, lrcBeginTs + totalMs - GrabRoomData.ACC_OFFSET_BY_LYRIC, null);
            //                            mRoomData.setSongLineNum(lineNum);
        }
    }

    //发射歌词事件
    internal fun launchLyricEvent(accPlayTs: Int) {
        MyLog.d(TAG, "launchLyricEvent accPlayTs=" + accPlayTs + "mAccLoadOk=" + params?.accLoadOk + " mLryLoadOk=" + mLrcLoadOk)
        if (mLyricsReader == null) {
            return
        }
        if (mHasLauncher) {
            MyLog.d(TAG, "launchLyricEvent 事件已经发射过了，取消这次")
            return
        }
        mHasLauncher = true
        mUiHandler.removeMessages(MSG_ENSURE_LAUNCHER)
        params?.manyLyricsView?.play((params?.accBeginTs ?: 0) + accPlayTs)

        val lineNum = mLyricEventLauncher.postLyricEvent(mLyricsReader, (params?.accBeginTs ?: 0)
                + accPlayTs, params?.accEndTs ?: 0, null)
        if (mListener != null) {
            mListener!!.onLyricEventPost(lineNum)
        }
        if (params?.manyLyricsView?.visibility == View.VISIBLE) {
            params?.voiceScaleView?.visibility = View.VISIBLE
            params?.voiceScaleView?.startWithData(mLyricsReader?.lyricsLineInfoList, (params?.accBeginTs
                    ?: 0) + accPlayTs)
        }
    }

    /**
     * 会偶现播伴奏失败，即没有这个调整事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            val `in` = event.getObj<EngineEvent.MixMusicTimeInfo>()
            MyLog.d(TAG, "伴奏 ts=" + `in`!!.current)
            if (`in` != null && `in`.current > 0) {
                if (params?.accLoadOk == false) {
                    if (mLrcLoadOk) {
                        launchLyricEvent(`in`.current)
                    }
                }
                params?.accLoadOk = true
                if (params?.manyLyricsView?.visibility == View.VISIBLE) {
                    val a1 = params?.manyLyricsView?.curPlayingTime?:0
                    val a2 = params?.manyLyricsView?.playerSpendTime?:0
                    val ts1 = a1+a2
                    val ts2 = (`in`.current + (params?.accBeginTs ?: 0)).toLong()
                    if (Math.abs(ts1 - ts2) > 500) {
                        MyLog.d(TAG, "伴奏与歌词的时间戳差距较大时,矫正一下,歌词ts=$ts1 伴奏ts=$ts2")
                        params?.manyLyricsView?.seekTo(ts2.toInt())
                    }
                }
            }
        }
    }


    fun onAcrResult(result: String, list: List<SongInfo>, targetSongInfo: SongInfo?, lineNo: Int) {
        MyLog.d(TAG, "onAcrResult result=$result list=$list targetSongInfo=$targetSongInfo lineNo=$lineNo mLastLineNum=$mLastLineNum")
        mUiHandler.removeMessages(MSG_SHOW_SCORE_EVENT + lineNo * 100)
        if (lineNo > mLastLineNum) {
            mAcrScore = 0
            if (targetSongInfo != null) {
                mAcrScore = (targetSongInfo.score * 100).toInt()
            } else {
            }
            if (ScoreConfig.isMelp2Enable()) {
                if (mMelp2Score >= 0) {
                    processScore("onAcrResult", mMelp2Score, mAcrScore, lineNo)
                } else {
                    // Melp2 没返回
                }
            } else {
                if (ScoreConfig.isMelpEnable()) {
                    val melp1Score = ZqEngineKit.getInstance().lineScore1
                    if (melp1Score > mAcrScore) {
                        processScore("onAcrResult", melp1Score, mAcrScore, lineNo)
                    } else {
                        processScore("onAcrResult", melp1Score, mAcrScore, lineNo)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: LrcEvent.LineLineEndEvent) {
        MyLog.w(TAG, "LineLineEndEvent event=$event")
        if (this.params?.needScore == false) {
            return
        }
        if (ScoreConfig.isMelp2Enable()) {
            ZqEngineKit.getInstance().getLineScore2(event.lineNum) { lineNum, score ->
                MyLog.d(TAG, "melp2 onGetScore lineNum=$lineNum score=$score")
                mMelp2Score = score
                if (ScoreConfig.isAcrEnable()) {
                    if (mAcrScore >= 0 || mAcrScore == -2) {
                        processScore("mMelp2Score", mMelp2Score, mAcrScore, event.lineNum)
                    } else {
                        // 没返回
                    }
                } else {
                    processScore("mMelp2Score", mMelp2Score, mAcrScore, event.lineNum)
                }
            }
        }
        if (ScoreConfig.isAcrEnable()) {
            ZqEngineKit.getInstance().recognizeInManualMode(event.lineNum)
            val msg = mUiHandler.obtainMessage(MSG_SHOW_SCORE_EVENT + event.lineNum * 100)
            mUiHandler.sendMessageDelayed(msg, 1000)
        } else {
            if (!ScoreConfig.isMelp2Enable()) {
                if (ScoreConfig.isMelpEnable()) {
                    val score = ZqEngineKit.getInstance().lineScore1
                    processScore("mMelp1Score", score, mAcrScore, event.lineNum)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: LrcEvent.LyricStartEvent) {
        MyLog.d(TAG, "onEvent LineStartEvent")
        mLastLineNum = -1
        val params = ZqEngineKit.getInstance().params
        if (params != null) {
            params.lrcHasStart = true
        }
    }

    private fun processScore(from: String, melpScore: Int, acrScore: Int, line: Int) {
        MyLog.w(TAG, "processScore from=$from melpScore=$melpScore acrScore=$acrScore line=$line mLastLineNum=$mLastLineNum")
        if (line <= mLastLineNum) {
            return
        }
        mLastLineNum = line
        EventBus.getDefault().post(ScoreResultEvent(from, melpScore, acrScore, line))
        // 处理
        mAcrScore = -1
        mMelp2Score = -1
    }

    fun setListener(l: Listener) {
        mListener = l
    }

    interface Listener {
        fun onLyricParseSuccess(reader: LyricsReader)

        fun onLyricParseFailed()

        fun onLyricEventPost(lineNum: Int)

        //void onScoreResult(String from,int melpScore, int acrScore, int line);
    }

    class ScoreResultEvent(var from: String, var melpScore: Int, var acrScore: Int, var line: Int)

    class ConfigParams {
        var lyricReader: LyricsReader? = null
        var manyLyricsView: ManyLyricsView? = null
        var voiceScaleView: VoiceScaleView? = null
        var lyricUrl: String? = null
        var lyricBeginTs: Int = 0
        var lyricEndTs: Int = 0
        var accBeginTs: Int = 0
        var accEndTs: Int = 0
        var authorName: String? = null
        var accLoadOk: Boolean = false
        var needScore: Boolean = true
        override fun toString(): String {
            return "ConfigParams(lyricUrl=$lyricUrl, lyricBeginTs=$lyricBeginTs, lyricEndTs=$lyricEndTs, accBeginTs=$accBeginTs, accEndTs=$accEndTs, authorName=$authorName, accLoadOk=$accLoadOk, needScore=$needScore)"
        }

    }

    internal val MSG_ENSURE_LAUNCHER = 1
    internal val MSG_SHOW_SCORE_EVENT = 32

    internal val LAUNCHER_DELAY = 5000
}

package com.module.playways.room.prepare.presenter

import android.text.TextUtils
import com.common.log.MyLog
import com.common.mvp.PresenterEvent
import com.common.mvp.RxLifeCyclePresenter
import com.common.utils.HttpUtils
import com.common.utils.U
import com.component.lyrics.LyricsManager
import com.component.lyrics.model.UrlRes
import com.component.lyrics.utils.SongResUtils
import com.component.lyrics.utils.ZipUrlResourceManager
import com.module.playways.room.prepare.view.IPrepareResView
import com.module.playways.room.song.model.SongModel
import org.greenrobot.greendao.annotation.NotNull
import java.util.*

class PrepareSongPresenter(@param:NotNull internal var mOnDownloadProgress: HttpUtils.OnDownloadProgress, internal var mIPrepareResView: IPrepareResView, @param:NotNull internal var mSongModel: SongModel) : RxLifeCyclePresenter() {

    internal var mSongResourceZhang: ZipUrlResourceManager?=null

    init {
        MyLog.d(TAG, "PrepareSongPresenter mOnDownloadProgress=$mOnDownloadProgress mSongModel=$mSongModel")
    }

    fun prepareRes() {
        val songResList = LinkedList<UrlRes>()
        //        String lyricUrl = mSongModel.getLyric();
        //        if (!TextUtils.isEmpty(lyricUrl)) {
        //            UrlRes lyric = new UrlRes(lyricUrl, SongResUtils.getLyricDir(), SongResUtils.SUFF_ZRCE);
        //            songResList.add(lyric);
        //        }

        //伴奏
        val accUrl = mSongModel.acc
        if (!TextUtils.isEmpty(accUrl)) {
            val acc = UrlRes(accUrl, SongResUtils.getACCDir(), U.getFileUtils().getSuffixFromUrl(accUrl, SongResUtils.SUFF_ACC))
            songResList.add(acc)
        }

        //原唱
        //        String oriUrl = mSongModel.getOri();
        //        if (!TextUtils.isEmpty(oriUrl)) {
        //            UrlRes acc = new UrlRes(oriUrl, SongResUtils.getORIDir(),U.getFileUtils().getSuffixFromUrl(oriUrl,SongResUtils.SUFF_ORI));
        //            songResList.add(acc);
        //        }

        //评分文件
        val midiUrl = mSongModel.midi
        if (!TextUtils.isEmpty(midiUrl)) {
            val midi = UrlRes(midiUrl, SongResUtils.getMIDIDir(), U.getFileUtils().getSuffixFromUrl(midiUrl, SongResUtils.SUFF_MIDI))
            songResList.add(midi)
        }

        mSongResourceZhang = ZipUrlResourceManager(songResList, mOnDownloadProgress)
        mSongResourceZhang?.go()
        showLyric()
    }

    private fun showLyric() {
        LyricsManager
                .loadStandardLyric(mSongModel.lyric)
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe({ lyricsReader ->
                    val lyricsLineInfos = lyricsReader.lrcLineInfos

                    for (i in 0 until lyricsLineInfos.size) {
                        val lyricsLineInfo = lyricsLineInfos[i]
                        if (lyricsLineInfo!!.getStartTime() >= mSongModel.rankLrcBeginT) {
                            var l = ""
                            val count = if (lyricsLineInfos.size - i > 6) 6 else lyricsLineInfos.size - i
                            for (j in 0 until count) {
                                l = l + lyricsLineInfos[i + j]!!.getLineLyrics() + "\n"
                            }

                            mIPrepareResView.onLyricReady(l)
                            break
                        }
                    }
                }, { throwable ->
                    MyLog.e(TAG, throwable)
                    mIPrepareResView.lyricReadyFailed()
                })
    }

    fun cancelTask() {
        mSongResourceZhang?.cancelAllTask()
    }
}

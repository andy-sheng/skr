package com.module.feeds.make

import com.common.utils.U
import com.component.lyrics.LyricsReader
import java.io.Serializable
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedSongTpl

class FeedsMakeModel : Serializable {

    @Transient
    val composeSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds", "feeds_compose.m4a")
    @Transient
    val recordSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds", "feeds_make.m4a")

    @Transient
    var firstLyricShiftTs = 0

    //var uploadFeedsId: String? = null // 上传后生成的feedsid
    //var uploadSongName: String? = null // 上传后歌曲名称
    //var uploadSongDesc: String? = null // 上传后歌曲描述
    @Transient
    var bgmDownloadProgress: Int = 0

    @Transient
    var recording: Boolean = false
    @Transient
    var beginRecordTs: Long = Long.MAX_VALUE
    @Transient
    var recordFirstFrameTs: Long = Long.MAX_VALUE // 录制获取到第一个音频包时的时间
    @Transient
    var musicFirstFrameTs: Long = Long.MAX_VALUE  // 伴奏第一帧播放出来的时间
    @Transient
    var recordOffsetTs: Long = 0 // 录音的偏移
    @Transient
    var recordingClick: Boolean = false
    @Transient
    var hasChangeLyricOrSongNameThisTime = false // 本次是否改变了歌词，因为可能从草稿箱进去

    var from:Int = FROM_QUICK_SING //来自哪个?打榜 快唱 改编
    var songModel: FeedSongModel? = null
    var challengeID: Long = 0L
    var audioUploadUrl: String? = null
    var draftUpdateTs = 0L // 对应的草稿箱更新睡哪
    var draftID = 0L //草稿箱ID
    var withBgm = false
    var hasChangeLyric = false // 是否改变了歌词
    var recordDuration: Long = 0 // 录音时间

    override fun toString(): String {
        return "FeedsMakeModel(challengeID=$challengeID, songModel=$songModel, composeSavePath='$composeSavePath', bgmDownloadProgress=$bgmDownloadProgress, recordDuration=$recordDuration, recording=$recording, beginRecordTs=$beginRecordTs, recordSavePath='$recordSavePath', recordingClick=$recordingClick, withBgm=$withBgm)"
    }
}
const val FROM_CHALLENGE = 1
const val FROM_QUICK_SING = 2
const val FROM_CHANGE_SING = 3

var sFeedsMakeModelHolder: FeedsMakeModel? = null

fun createCustomZrce2ReaderByTxt(lyricsReader: LyricsReader, lrcTxtStr: String?) {
    if (lrcTxtStr.isNullOrEmpty()) {
        return
    }
    val changeLyrics = lrcTxtStr?.split("\n")
    var index = 0
    lyricsReader.lrcLineInfos.forEach {
        if (index < (changeLyrics?.size ?: 0)) {
            it.value.lineLyrics = changeLyrics?.get(index)
            index++
        }
    }
}
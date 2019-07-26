package com.module.feeds.make

import com.common.utils.U
import java.io.Serializable
import com.module.feeds.feeds.model.FeedSongModel

class FeedsMakeModel(var songModel: FeedSongModel):Serializable{

    val composeSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds","feeds_compose.aac")
    var bgmDownloadProgress: Float = 0f

    var recordDuration: Long = 0 // 录音时间
    var recording: Boolean = false
    var beginRecordTs: Long = Long.MAX_VALUE

    val recordSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds","feeds_make.aac")

    var recordingClick: Boolean = false
    var withBgm  = false
}
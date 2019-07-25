package com.module.feeds.make

import com.common.utils.U
import java.io.Serializable
import com.component.feeds.model.FeedSongModel

class FeedsMakeModel(var songModel: FeedSongModel):Serializable{

    var bgmDownloadProgress: Float = 0f

    var recordDuration: Long = 0 // 录音时间
    var recording: Boolean = false
    var beginRecordTs: Long = Long.MAX_VALUE

    val recordSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds","feeds_make.aac")

    var recordingClick: Boolean = false
    var withBgm  = false
}
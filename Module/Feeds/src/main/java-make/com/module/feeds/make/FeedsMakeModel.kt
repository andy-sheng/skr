package com.module.feeds.make

import com.common.utils.U
import com.component.feeds.model.FeedSongModel

class FeedsMakeModel(var songModel: FeedSongModel){

    var recording: Boolean = false
    var beginRecordTs: Long = Long.MAX_VALUE

    val recordSavePath: String = U.getAppInfoUtils().getFilePathInSubDir("feeds","feeds_make.aac")

    var recordingClick: Boolean = false
}
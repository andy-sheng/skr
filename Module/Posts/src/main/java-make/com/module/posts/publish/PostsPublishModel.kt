package com.module.posts.publish

import com.common.utils.U
import com.module.posts.publish.redpkg.RedPkgModel
import com.module.posts.publish.topic.Topic
import java.io.File
import java.io.Serializable

class PostsPublishModel : Serializable {
    var songId: Int = 0
    var songName:String?=null
    var topic: Topic? = null
    val imgUploadMap = LinkedHashMap<String,String>() // 本地路径->服务器url
    var recordVoiceUrl: String? = null
    var recordVoicePath: String? = null
    var recordDurationMs: Int = 0 // 毫秒
    var voteList = ArrayList<String>()
    var redPkg:RedPkgModel? = null

    companion object {
        val POSTS_PUBLISH_AUDIO_FILE_PATH = File(U.getAppInfoUtils().mainDir, "posts_publish.m4a").path
        val MAX_RECORD_TS = 60 * 1000L
    }

}
package com.module.posts.publish

import com.common.utils.U
import com.module.posts.publish.redpkg.RedPkgModel
import java.io.File
import java.io.Serializable

class PostsPublishModel : Serializable {
    val imgUploadMap = LinkedHashMap<String,String>() // 本地路径->服务器url
    var recordVoiceUrl: String? = null
    var topicId = 0
    var recordVoicePath: String? = null
    var recordDurationMs: Int = 0 // 毫秒
    var voteList = ArrayList<String>()
    var redPkg:RedPkgModel? = null

    companion object {
        val POSTS_PUBLISH_AUDIO_FILE_PATH = File(U.getAppInfoUtils().mainDir, "posts_publish.aac").path
        val MAX_RECORD_TS = 60 * 1000L
    }

}
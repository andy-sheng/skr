package com.component.busilib.friends

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class VoiceInfoModel : Serializable {
    @JSONField(name = "voiceID")
    var voiceID: Int = 0
    @JSONField(name = "songID")
    var songID: Int = 0
    @JSONField(name = "songName")
    var songName: String = ""
    @JSONField(name = "voiceURL")
    var voiceURL: String = ""
    @JSONField(name = "duration")
    var duration: Long = 0
}
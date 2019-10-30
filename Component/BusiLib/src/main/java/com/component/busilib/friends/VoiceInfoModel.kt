package com.component.busilib.friends

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class VoiceInfoModel : Serializable {

    companion object{
        const val EVAS_UN_AUDIT = 1  //未审核
        const val EVAS_AUDIT_OK = 2  //审核通过
        const val EVAS_AUDIT_NO = 3  //审核不通过
    }

    @JSONField(name = "auditStatus")
    var auditStatus:Int = 0
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
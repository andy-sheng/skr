package com.module.club.homepage

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class ClubPhotoDetail : Serializable {
    @JSONField(name = "nickName")
    var nickName: String = ""
    @JSONField(name = "picInfo")
    var picInfo: ClubPhotoInfoModel? = null
}

class ClubPhotoInfoModel : Serializable {
    @JSONField(name = "familyID")
    var familyID: Int = 0
    @JSONField(name = "picID")
    var picID: Int = 0
    @JSONField(name = "picPath")
    var picPath: String = ""
    @JSONField(name = "userID")
    var userID: Int = 0
}
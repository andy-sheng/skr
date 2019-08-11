package com.module.feeds.make.make

import org.greenrobot.greendao.annotation.Entity
import org.greenrobot.greendao.annotation.Generated
import org.greenrobot.greendao.annotation.Id
import java.io.Serializable

@Entity
class FeedsLyricDB : Serializable {
    @Id
    var draftID: Long? = null
    var feedsMakeModelJson:String? = null
}

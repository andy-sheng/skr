package com.module.posts.publish.redpkg

import java.io.Serializable

class RedPkgModel : Serializable {
    var redpacketDesc:String?=null
    var redpacketNum = 0
    var redpacketType = 0 //1为金币 2为钻石
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedPkgModel

        if (redpacketNum != other.redpacketNum) return false
        if (redpacketType != other.redpacketType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = redpacketNum
        result = 31 * result + redpacketType
        return result
    }


}

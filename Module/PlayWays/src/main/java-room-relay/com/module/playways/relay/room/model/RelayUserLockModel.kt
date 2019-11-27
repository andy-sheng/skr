package com.module.playways.relay.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.RelayRoom.RUserLockInfo
import java.io.Serializable

// 解锁信息
class RelayUserLockModel : Serializable {

    @JSONField(name = "userID")
    var userID: Int = 0
    @JSONField(name = "hasLock")
    var hasLock: Boolean? = null

    companion object {
        fun parseFromPB(msg: RUserLockInfo): RelayUserLockModel {
            val result = RelayUserLockModel()
            result.userID = msg.userID
            result.hasLock = msg.hasLock
            return result
        }

        fun parseFromPB(list: List<RUserLockInfo>?): List<RelayUserLockModel> {
            val modelArrayList = ArrayList<RelayUserLockModel>()
            return if (list.isNullOrEmpty()) {
                modelArrayList
            } else {
                list.forEach {
                    modelArrayList.add(parseFromPB(it))
                }
                modelArrayList
            }
        }
    }
}
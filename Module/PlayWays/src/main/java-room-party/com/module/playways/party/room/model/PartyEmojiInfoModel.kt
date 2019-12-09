package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.PartyRoom.PDynamicEmoji
import com.zq.live.proto.PartyRoom.PDynamicEmojiMsg
import java.io.Serializable

// 表情
class PartyEmojiInfoModel : Serializable {
    @JSONField(name = "id")
    var id: Int = 0
    @JSONField(name = "smallEmojiURL")
    var smallEmojiURL: String = ""
    @JSONField(name = "bigEmojiURL")
    var bigEmojiURL: String = ""
    @JSONField(name = "desc")
    var desc: String = ""
    @JSONField(name = "status")
    var status: Int = 0   // ES_UNKNOWN, ES_ONLINE(在线), ES_OFFLINE(离线)

    companion object {
        fun parseFromPB(msg: PDynamicEmoji): PartyEmojiInfoModel {
            val result = PartyEmojiInfoModel()
            result.id = msg.id
            result.smallEmojiURL = msg.smallEmojiURL
            result.bigEmojiURL = msg.bigEmojiURL
            result.desc = msg.desc
            return result
        }
    }

}
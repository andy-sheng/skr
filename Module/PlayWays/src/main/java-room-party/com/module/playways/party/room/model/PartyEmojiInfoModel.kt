package com.module.playways.party.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.PartyRoom.PDynamicEmojiMsg
import java.io.Serializable

class PartyEmojiInfoModel : Serializable {
    @JSONField(name = "id")
    var id: Int = 0
    @JSONField(name = "smallEmojiURL")
    var smallEmojiURL: String = ""
    @JSONField(name = "bigEmojiURL")
    var bigEmojiURL: String = ""
    @JSONField(name = "user")
    var player: PartyPlayerInfoModel? = null

    //todo  应该缺一个描述的字段

    override fun toString(): String {
        return "PartyEmojiInfoModel(id=$id, smallEmojiURL='$smallEmojiURL', bigEmojiURL='$bigEmojiURL', player=$player)"
    }

    companion object {
        fun parseFromPB(msg: PDynamicEmojiMsg): PartyEmojiInfoModel {
            val result = PartyEmojiInfoModel()
            result.id = msg.id
            result.smallEmojiURL = msg.smallEmojiURL
            result.bigEmojiURL = msg.bigEmojiURL
            result.player = PartyPlayerInfoModel.parseFromPb(msg.user)
            return result
        }
    }

}
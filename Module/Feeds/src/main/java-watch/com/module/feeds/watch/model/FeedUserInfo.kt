package com.module.feeds.watch.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class FeedUserInfo : Serializable {
    companion object {
        fun parseFeedUserInfo(content: String?): FeedUserInfo? {
            content?.let {
                val jsonObject = JSON.parseObject(it, JSONObject::class.java)
                return jsonObject.getObject("user", FeedUserInfo::class.java)
            }
            return null
        }
    }

    @JSONField(name = "avatar")
    var avatar: String? = null
    @JSONField(name = "nickname")
    var nickname: String? = null
    @JSONField(name = "userID")
    var userID: Int = 0

    override fun toString(): String {
        return "FeedUserInfo(avatar=$avatar, nickname=$nickname, userID=$userID)"
    }

}
package com.module.playways.friendroom

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.mic.home.RecommendMicInfoModel
import com.module.playways.mic.home.RecommendMicRoomModel
import java.io.Serializable

class RecommendRoomModel : Serializable {
    companion object {
        const val EGST_STAND = 1  //抢唱房间
        const val EGST_MIC = 2    //排麦房间
    }

    @JSONField(name = "gameSceneType")
    var gameSceneType: Int = 0
    @JSONField(name = "micRoomList")
    var micRoom: RecommendMicInfoModel? = null
    @JSONField(name = "standRoomList")
    var grabRoom: RecommendGrabRoomModel? = null

    override fun toString(): String {
        return "RecommendRoomModel(gameSceneType=$gameSceneType, micRoom=$micRoom, grabRoom=$grabRoom)"
    }

}
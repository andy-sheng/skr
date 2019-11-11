package com.module.playways.race.room.model

import com.zq.live.proto.RaceRoom.FakeUserInfo
import java.io.Serializable

class FakeUserInfoModel : Serializable {
    var nickName:String?=null

    companion object{
        fun parseFromPB(model: FakeUserInfo): FakeUserInfoModel {
            val userInfoModel = FakeUserInfoModel()
            if (model != null) {
                userInfoModel.nickName = model.nickName
            }
            return userInfoModel
        }
    }

}
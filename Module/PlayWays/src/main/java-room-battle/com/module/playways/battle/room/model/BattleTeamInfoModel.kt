package com.module.playways.battle.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.zq.live.proto.BattleRoom.BRoundResult
import com.zq.live.proto.BattleRoom.BTeamInfo
import java.io.Serializable

class BattleTeamInfoModel : Serializable {

    @JSONField(name = "teamTag")
    var teamTag = ""

    var teamUsers: ArrayList<UserInfoModel> = ArrayList()

    companion object {
        fun parseFromPb(pb: BTeamInfo): BattleTeamInfoModel {
            var info = BattleTeamInfoModel()
            info.teamTag = pb.teamTag
            for (u in pb.teamUsersList) {
                info.teamUsers.add(UserInfoModel.parseFromPB(u))
            }
            return info
        }

        fun parseToList(pb: List<BTeamInfo>?): ArrayList<BattleTeamInfoModel> {
            val list = ArrayList<BattleTeamInfoModel>()
            pb?.forEach {
                list.add(parseFromPb(it))
            }
            return list
        }
    }
}

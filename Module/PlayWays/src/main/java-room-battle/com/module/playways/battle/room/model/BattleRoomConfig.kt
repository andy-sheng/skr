package com.module.playways.battle.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.userinfo.model.UserInfoModel
import com.zq.live.proto.BattleRoom.BRoundResult
import com.zq.live.proto.BattleRoom.BTeamInfo
import java.io.Serializable

class BattleRoomConfig : Serializable {

    var totalMusicCnt = 12 //总共多少首比赛曲目
    var helpCardCnt = 2 //可以使用的帮唱卡数量
    var switchCardCnt = 1 //可以使用的换歌卡数量

}

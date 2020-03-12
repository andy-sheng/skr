package com.module.playways.battle.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.module.playways.battle.room.event.BattleSwitchCardChangeEvent
import org.greenrobot.eventbus.EventBus
import java.io.Serializable

class BattleRoomConfig : Serializable {

    @JSONField(name = "totalMusicCnt")
    var totalMusicCnt = 12 //总共多少首比赛曲目
    @JSONField(name = "helpCardCnt")
    var helpCardCnt = 2 //可以使用的帮唱卡数量
        set(value) {
            field = value
            EventBus.getDefault().post(BattleSwitchCardChangeEvent())
        }

    @JSONField(name = "switchCardCnt")
    var switchCardCnt = 1 //可以使用的换歌卡数量
        set(value) {
            field = value
            EventBus.getDefault().post(BattleSwitchCardChangeEvent())
        }

    companion object {
        fun parseFromPB(msg: com.zq.live.proto.BattleRoom.BattleRoomConfig): BattleRoomConfig {
            val result = BattleRoomConfig()
            result.totalMusicCnt = msg.totalMusicCnt
            result.helpCardCnt = msg.helpCardCnt
            result.switchCardCnt = msg.switchCardCnt
            return result
        }
    }

}

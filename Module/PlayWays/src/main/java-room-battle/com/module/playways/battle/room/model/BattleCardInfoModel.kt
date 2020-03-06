package com.module.playways.battle.room.model
import com.alibaba.fastjson.annotation.JSONField
import com.zq.live.proto.BattleRoom.BCardInfo


data class BattleCardInfoModel(
        @JSONField(name = "cardType")
        var cardType: Int = 0,
        @JSONField(name = "helpCard")
        var helpCard: HelpCard? = null,
        @JSONField(name = "switchCard")
        var switchCard: SwitchCard? = null
) {
    data class HelpCard(
            @JSONField(name = "userID")
            var userID: Int? = null
    )

    class SwitchCard(
    )

    companion object{
        fun parseFromPb(pb: BCardInfo): BattleCardInfoModel {
            var info = BattleCardInfoModel()
            info.cardType = pb.cardType.value
            info.helpCard = HelpCard(pb.helpCard.userID)
            if(pb.switchCard!=null){
                info.switchCard = SwitchCard()
            }
            return info
        }
    }
}
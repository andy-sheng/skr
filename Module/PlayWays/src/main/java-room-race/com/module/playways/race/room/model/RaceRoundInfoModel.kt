package com.module.playways.race.room.model

import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel

class RaceRoundInfoModel : BaseRoundInfoModel() {

    //    protected int overReason; // 结束的原因
    //  protected int roundSeq;// 本局轮次
    var status = 0
    var scores:List<RaceScore>? = null
    var subRoundSeq = 0
    var subRoundInfo:List<RaceSubRoundInfo>? = null
    var games:List<RaceGameInfo>? = null
    var playUsers:List<RacePlayerInfoModel>?=null
    var waitUsers:List<RacePlayerInfoModel>?=null
    /**
     * 一唱到底 结束原因
     * 0未知
     * 1上个轮次结束
     * 2没人抢唱
     * 3当前玩家退出
     * 4多人灭灯
     * 5自己放弃演唱
     *
     *
     * 排位赛 结束原因
     * 0 未知
     * 1 正常
     * 2 玩家退出
     * 3 多人灭灯
     * 4
     */


    override fun getType(): Int {
        return 0
    }

    override fun tryUpdateRoundInfoModel(round: BaseRoundInfoModel, notify: Boolean) {

    }
}

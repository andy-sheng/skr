package com.module.playways.party.room.model

import com.zq.live.proto.PartyRoom.*
import java.io.Serializable
import android.R.string


class PartyGameInfoModel : Serializable {
    var gameType = EPGameType.PGT_Unknown.value
    var gameRule: GameRuleModel? = null
    var play: GamePlayModel? = null
    var question: GameQuestionModel? = null
    override fun toString(): String {
        return "PartyGameInfoModel(gameType=$gameType, gameRule=$gameRule, play=$play, question=$question)"
    }

    companion object {
        fun parseFromItemInfo(pb: PGameItemInfo): PartyGameInfoModel {
            var p = PartyGameInfoModel()
            p.gameType = pb.gameType.value
            if (pb.hasGameRule()) {
                p.gameRule = GameRuleModel.parseFromItemInfo(pb.gameRule)
            }
            if (pb.hasPlay()) {
                p.play = GamePlayModel.parseFromItemInfo(pb.play)
            }
            if (pb.hasQuestion()) {
                p.question = GameQuestionModel.parseFromItemInfo(pb.question)
            }
            return p
        }
    }
}

class GameRuleModel : Serializable {
    var ruleID = 0//游戏规则标识
    var ruleName = ""//游戏规则名称
    var ruleDesc = ""//游戏规则描述

    override fun toString(): String {
        return "GameRuleModel(ruleID=$ruleID, ruleName='$ruleName', ruleDesc='$ruleDesc')"
    }

    companion object {
        fun parseFromItemInfo(pb: PGameRule): GameRuleModel {
            var p = GameRuleModel()
            p.ruleID = pb.ruleID
            p.ruleName = pb.ruleName
            p.ruleDesc = pb.ruleDesc
            return p
        }
    }
}

class GamePlayModel : Serializable {
    var playID = 0
    var playName = "" //剧本名称
    var playContent = "" //剧本内容
    var playCard = "" //剧本手卡
    override fun toString(): String {
        return "GamePlayModel(playID=$playID, playName='$playName', playContent='$playContent', playCard='$playCard')"
    }

    companion object {
        fun parseFromItemInfo(pb: PGamePlay): GamePlayModel {
            var p = GamePlayModel()
            p.playID = pb.playID
            p.playName = pb.playName
            p.playContent = pb.playContent
            p.playCard = pb.playCard
            return p
        }
    }
}

class GameQuestionModel : Serializable {
    var questionID = 0 //问题标识
    var questionContent = "" //问题内容
    var questionPic: ArrayList<String>? = null //问题图片
    var answerContent = "" //问题答案
    override fun toString(): String {
        return "GameQuestionModel(questionID=$questionID, questionContent='$questionContent', questionPic=$questionPic, answerContent='$answerContent')"
    }

    companion object {
        fun parseFromItemInfo(pb: PGameQuestion): GameQuestionModel {
            var p = GameQuestionModel()
            p.questionID = pb.questionID
            p.questionContent = pb.questionContent
            if (!pb.questionPicList.isNullOrEmpty()) {
                p.questionPic = ArrayList<String>()
                for (c in pb.questionPicList) {
                    p.questionPic?.add(c)
                }
            }
            p.answerContent = pb.answerContent
            return p
        }
    }
}
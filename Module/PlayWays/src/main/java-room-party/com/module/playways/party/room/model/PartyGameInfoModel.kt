package com.module.playways.party.room.model

import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.PartyRoom.*
import java.io.Serializable


class PartyGameInfoModel : Serializable {
    var sceneTag: String? = "" //场景标识
    var rule: GameRuleModel? = null
    var play: GamePlayScene? = null
    var question: GameQuestionSceneModel? = null
    var free: GameFreeSceneModel? = null
    var ktv: GameKTVSceneModel? = null


    companion object {
        fun parseFromItemInfo(pb: PGameSceneInfo): PartyGameInfoModel {
            var p = PartyGameInfoModel()
            p.sceneTag = pb.sceneTag
            if (pb.hasRule()) {
                p.rule = GameRuleModel.parseFromItemInfo(pb.rule)
            }
            if (pb.hasPlay()) {
                p.play = GamePlayScene.parseFromPb(pb.play)
            }
            if (pb.hasQuestion()) {
                p.question = GameQuestionSceneModel.parseFromItemInfo(pb.question)
            }
            if (pb.hasFree()) {
                p.free = GameFreeSceneModel.parseFromPb(pb.free)
            }
            if (pb.hasKtv()) {
                p.ktv = GameKTVSceneModel.parseFromPb(pb.ktv)
            }
            return p
        }
    }

    override fun toString(): String {
        return "PartyGameInfoModel(sceneTag=$sceneTag, rule=$rule, play=$play, question=$question, free=$free, ktv=$ktv)"
    }
}

class GameRuleModel : Serializable {
    var ruleID = 0//游戏规则标识
    var ruleName = ""//游戏规则名称
    var ruleDesc = ""//游戏规则描述
    var ruleType: Int = 0 //游戏类型

    override fun toString(): String {
        return "GameRuleModel(ruleID=$ruleID, ruleName='$ruleName', ruleDesc='$ruleDesc')"
    }

    companion object {
        fun parseFromItemInfo(pb: PGameRule): GameRuleModel {
            var p = GameRuleModel()
            p.ruleID = pb.ruleID
            p.ruleName = pb.ruleName
            p.ruleDesc = pb.ruleDesc
            p.ruleType = pb.ruleType.value
            return p
        }
    }
}

class GamePlayScene : Serializable {
    var palyInfo: GamePlayModel? = null

    companion object {
        fun parseFromPb(pb: PPlayScene): GamePlayScene {
            var p = GamePlayScene()
            p.palyInfo = GamePlayModel.parseFromItemInfo(pb.palyInfo)
            return p
        }
    }

    override fun toString(): String {
        return "GamePlayScene(palyInfo=$palyInfo)"
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

class GameQuestionSceneModel : Serializable {
    var questionInfo: GameQuestionModel? = null
    var hasNextquestion: Boolean = false
    var questionSeq: Int = 0 //题目序号

    companion object {
        fun parseFromItemInfo(pb: PQuestionScene): GameQuestionSceneModel {
            var p = GameQuestionSceneModel()
            p.questionInfo = GameQuestionModel.parseFromItemInfo(pb.questionInfo)
            p.hasNextquestion = pb.hasNextquestion
            p.questionSeq = pb.questionSeq
            return p
        }
    }

    override fun toString(): String {
        return "GameQuestionSceneModel(questionInfo=$questionInfo, hasNextquestion=$hasNextquestion, questionSeq=$questionSeq)"
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

class GameFreeSceneModel : Serializable {
    companion object {
        fun parseFromPb(pb: PFreeScene): GameFreeSceneModel {
            var p = GameFreeSceneModel()
            return p
        }
    }
}

class GameKTVSceneModel : Serializable {
    var music: SongModel? = null
    var hasNextMusic: Boolean = false
    var musicCnt: Int = 0
    var userID: Int = 0
    var singTimeMs: Int = 0


    companion object {
        fun parseFromPb(pb: PKTVScene): GameKTVSceneModel {
            var p = GameKTVSceneModel()
            p.music = SongModel()
            p.music?.parse(pb.music)
            p.hasNextMusic = pb.hasNextMusic
            p.musicCnt = pb.musicCnt
            p.userID = pb.userID
            p.singTimeMs = pb.singTimeMs
            return p
        }
    }

    override fun toString(): String {
        return "GameKTVSceneModel(music=$music, hasNextMusic=$hasNextMusic, musicCnt=$musicCnt, userID=$userID)"
    }


}
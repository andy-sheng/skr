package com.component.busilib.model

import com.alibaba.fastjson.JSON
import com.zq.live.proto.Common.BLightShowInfo
import com.zq.live.proto.Common.BackgroundShowInfo
import java.io.Serializable

class GameBackgroundEffectModel : Serializable {
    var sourcesJson: String? = null
        set(value) {
            effectModel = JSON.parseObject(value, EffectModel::class.java)
            field = value
        }

    var effectModel: EffectModel? = null

    companion object {
        fun parseToList(jsonList: List<BackgroundShowInfo>?): List<GameBackgroundEffectModel> {
            val list = ArrayList<GameBackgroundEffectModel>()
            jsonList?.forEach {
                list.add(GameBackgroundEffectModel().apply {
                    sourcesJson = it.sourcesJson
                })
            }

            return list
        }

        fun parseBackgroundEffectModelFromPb(backgroundEffectModel: BackgroundShowInfo): GameBackgroundEffectModel {
            var gameBackgroundEffectModel = GameBackgroundEffectModel()
            gameBackgroundEffectModel.sourcesJson = backgroundEffectModel.sourcesJson
            return gameBackgroundEffectModel
        }

        fun parseBLightEffectModelFromPb(bLightShowInfo: BLightShowInfo?): GameBackgroundEffectModel? {
            var gameBackgroundEffectModel = GameBackgroundEffectModel()
            gameBackgroundEffectModel.sourcesJson = bLightShowInfo?.sourcesJson
            return gameBackgroundEffectModel
        }

    }
}
package com.component.busilib.model

import com.zq.live.proto.Common.BackgroundShowInfo
import java.io.Serializable


class BackgroundEffectModel : Serializable {
    var sourceURL: String = "" //资源，使用效果
    var bgColor: String = "" //背景颜色

    constructor()

    companion object {
        fun parseBackgroundEffectModelListFromPb(list: List<BackgroundShowInfo>): List<BackgroundEffectModel> {
            val backgroundEffectModelList = ArrayList<BackgroundEffectModel>()
            list?.forEach {
                backgroundEffectModelList.add(parseBackgroundEffectModelFromPb(it))
            }

            return backgroundEffectModelList
        }

        fun parseBackgroundEffectModelFromPb(backgroundEffectModel: BackgroundShowInfo): BackgroundEffectModel {
            return BackgroundEffectModel().apply {
                sourceURL = backgroundEffectModel.sourceURL
                bgColor = backgroundEffectModel.bgColor
            }
        }
    }
}
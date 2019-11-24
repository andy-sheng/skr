package com.component.busilib.model

import com.zq.live.proto.Common.BLightShowInfo
import java.io.Serializable


class BLightEffectModel : Serializable {
    var sourceURL: String = "" //资源，使用效果

    companion object {
        fun parseBLightEffectModelFromPb(backgroundEffectModel: BLightShowInfo?): BLightEffectModel? {
            if (backgroundEffectModel == null) {
                return null
            }

            return BLightEffectModel().apply {
                sourceURL = backgroundEffectModel.sourceURL
            }
        }
    }
}
package com.component.busilib.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.common.utils.U
import com.zq.live.proto.Common.BLightShowInfo
import com.zq.live.proto.Common.BackgroundShowInfo
import java.io.Serializable

/**
 * 如果是背景需要EffectObg，简单的只需要sourceURL就可以了，比如爆灯
 * ！！！！！这里需要注意，dynamic效果不可以即有无限循环的也有开始或结尾播放的效果
 */
class EffectModel : Serializable {
    //默认返回第一个的sourceUrl,
    var sourceURL: String = ""
        get() {
            items?.let {
                if (it.size > 0) {
                    return it[0].sourceUrl
                }
            }

            return ""
        }
    var bgColor: String = "" //背景颜色

    @JSONField(name = "items")
    var items: List<EffectObg>? = null

    constructor()

    //每一个需要播的动画都是一个Obg
    class EffectObg : Serializable {
        //分背景和动态图片，1为背景，2为动态图
        var type: Int = 0
        //资源的地址
        var sourceUrl = ""
        //开始播放的时间，如果是正数就是开始多少多少，如果是是负数就是倒数多少时间的时候
        var startTs = 0
        //循环的时候的间隔
        var interval: Int = 0

        companion object {
            fun parseFromJsonArray(json: String): ArrayList<EffectObg>? {
                if (U.getStringUtils().isJSON(json)) {
                    val list = ArrayList<EffectObg>()
                    val jsonArray = JSON.parseArray(json)

                    if (jsonArray != null && jsonArray.size > 0) {
                        for (jsonObj in jsonArray) {
                            parseFromJson(jsonObj as JSONObject)?.let {
                                list.add(it)
                            }
                        }
                    }

                    if (list.size > 0) {
                        return list
                    }
                }

                return null
            }

            fun parseFromJson(obj: JSONObject): EffectObg? {
                val effectObg = EffectObg()
                if (obj.containsKey("type")) {
                    effectObg.type = obj.getIntValue("type")
                }

                if (obj.containsKey("sourceUrl")) {
                    effectObg.sourceUrl = obj.getString("sourceUrl")
                }

                if (obj.containsKey("startTs")) {
                    effectObg.startTs = obj.getIntValue("startTs")
                }

                if (obj.containsKey("interval")) {
                    effectObg.interval = obj.getIntValue("interval")
                }

                return effectObg

                return null
            }
        }

        override fun toString(): String {
            return "EffectObg(type=$type, sourceUrl='$sourceUrl', startTs=$startTs, interval=$interval)"
        }


    }

    override fun toString(): String {
        return "EffectModel(bgColor='$bgColor', items=$items)"
    }
}
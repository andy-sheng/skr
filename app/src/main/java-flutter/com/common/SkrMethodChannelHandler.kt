package com.common

import com.alibaba.fastjson.JSON
import com.common.flutter.plugin.MethodHandler
import com.common.rxretrofit.httpGet
import com.common.utils.U
import com.module.playways.party.bgmusic.getLocalMusicInfo
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class SkrMethodChannelHandler : MethodHandler("SkrMethodChannelHandler") {
    override fun handle(call: MethodCall, result: MethodChannel.Result): Boolean {
        when {
            call.method == "getDeviceID" -> {
                result.success(U.getDeviceUtils().deviceID)
                return true
            }
            call.method == "getChannel" -> {
                result.success(U.getChannelUtils().channel)
                return true
            }
            call.method == "httpGet" -> {
                var url = call.argument<String>("url")
                var params = call.argument<HashMap<String, Any?>>("params")
                httpGet(url!!, params) { r ->
                    result.success(r)
                }
                return true
            }
            call.method == "loadLocalBGM" -> {
                var l = getLocalMusicInfo()
                result.success(JSON.toJSONString(l))
                return true
            }
        }
        return false
    }

}
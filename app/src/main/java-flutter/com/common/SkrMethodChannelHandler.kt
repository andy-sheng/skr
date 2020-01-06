package com.common

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.flutter.plugin.MethodHandler
import com.common.rxretrofit.httpGet
import com.common.utils.U
import com.module.playways.party.bgmusic.getLocalMusicInfo
import com.zq.mediaengine.kit.ZqEngineKit
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
            call.method == "getEngineMusicPath" -> {
                var l = ZqEngineKit.getInstance().params.mixMusicFilePath
                result.success(l)
                return true
            }
            call.method == "startAudioMixing" -> {
                var filePath = call.argument<String>("filePath")
                var cycle = call.argument<Int>("cycle")
                ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), filePath, null, 0, false, false, cycle
                        ?: 1)
                result.success(null)
                return true
            }
            call.method == "stopAudioMixing" -> {
                ZqEngineKit.getInstance().stopAudioMixing()
                result.success(null)
                return true
            }
        }
        return false
    }

}
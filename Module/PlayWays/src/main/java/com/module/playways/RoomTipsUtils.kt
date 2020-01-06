package com.module.playways

import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.engine.EngineConfigFromServer
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import org.greenrobot.eventbus.EventBus

fun pretendHeadSetSystemMsg(gameType: Int) {
    var tips2 = ""
    //合唱房不展示这些
    if (gameType != GameModeType.GAME_MODE_RELAY) {
        var r = EngineConfigFromServer.getSelfCollectionSwitch()
        var configFromServer = EngineConfigFromServer.getDefault()
        if (r == 0) {
            var num = U.getPreferenceUtils().getSettingInt("ear_monitor_tips_num", 0)
            if (num < 3) {
                if (configFromServer.hasServerConfig) {
                    if (configFromServer.isEnableAudioPreview) {
                        tips2 = " 可在调音面板中开关耳返"
                    } else {
                        tips2 = "判断您的耳返效果可能不佳，您也可以在调音面板中开启体验"
                    }
                } else {
                    tips2 = " 在设置中开启“实验性耳返功能”，调音面板中会新增耳返选项并可进行体验"
                }
                U.getPreferenceUtils().setSettingInt("ear_monitor_tips_num", ++num)
            }
        } else {
            if (r == 1) {
                // 强开自采集
            } else if (r == 2) {
                // 强关自采集
            }
        }
    }
    val stringBuilder = SpanUtils()
            .append(" 温馨提示：佩戴耳机能获得最佳演唱效果 $tips2").setForegroundColor(CommentModel.RANK_SYSTEM_COLOR)
            .create()
    val commentSysModel = CommentSysModel(gameType, stringBuilder)
    EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))

}
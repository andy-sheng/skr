package com.module.playways.room.room.comment.model

import android.graphics.Color
import android.text.SpannableStringBuilder

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.module.playways.R
import com.module.playways.race.room.model.FakeUserInfoModel
import com.zq.live.proto.RaceRoom.FakeUserInfo

/**
 * 消息的基类
 */
abstract class CommentModel {

    var commentType = 0                   //消息类型

    var userInfo: UserInfoModel? = null                //消息发送者信息(头像，昵称，vip和id)
    var fakeUserInfo: FakeUserInfoModel? = null             //蒙面信息
    var avatarColor: Int = 0                       //消息发送者头像颜色
    var nameBuilder: SpannableStringBuilder? = null   //昵称的内容
    var stringBuilder: SpannableStringBuilder? = null //消息的内容

    companion object {
        val TYPE_SYSTEM = 1     // 系统消息
        val TYPE_AI = 2         // AI裁判消息
        val TYPE_TEXT = 101     // 普通文本聊天消息
        val TYPE_LIGHT = 102    // 爆灭灯消息
        val TYPE_DYNAMIC = 103  // 特殊表情消息
        val TYPE_GIFT = 104     // 礼物消息
        val TYPE_AUDIO = 105    // 语音消息

        val AVATAR_COLOR = Color.WHITE     // 头像圈的颜色

        val RANK_NAME_COLOR = Color.parseColor("#FFC15B")    // 昵称颜色（排位）
        val RANK_TEXT_COLOR = U.getColor(R.color.white_trans_60)          // 文本颜色 （排位）
        val RANK_SYSTEM_COLOR = Color.parseColor("#FF8AB6")    // 系统文案颜色（排位）
        val RANK_SYSTEM_HIGH_COLOR = Color.parseColor("#FF8AB6") // 系统文案的高亮颜色（排位)

        val GRAB_NAME_COLOR = Color.parseColor("#FFC15B")   // 昵称颜色（抢唱）
        val GRAB_TEXT_COLOR = U.getColor(R.color.white_trans_60)       // 文本颜色 （抢唱）
        val GRAB_SYSTEM_COLOR = Color.parseColor("#FF8AB6")  // 系统文案颜色（抢唱）
        val GRAB_SYSTEM_HIGH_COLOR = Color.parseColor("#FF8AB6")  // 系统文案的高亮颜色（抢唱)
    }
}

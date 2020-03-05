package com.component.person.view

import com.common.core.userinfo.model.UserInfoModel
import com.component.busilib.friends.VoiceInfoModel

import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel

interface IOtherPersonView {
    // 展示homepage回来的结果
    fun showHomePageInfo(userInfoModel: UserInfoModel,
                         relationNumModels: List<RelationNumModel>?,
                         meiLiCntTotal: Int, qinMiCntTotal: Int, postCnt: Int,
                         voiceInfoModel: VoiceInfoModel?)

    fun getHomePageFail()

    fun refreshRelation(isFriend: Boolean, isFollow: Boolean, isSpFollow: Boolean)

    fun showSpFollowVip()
}

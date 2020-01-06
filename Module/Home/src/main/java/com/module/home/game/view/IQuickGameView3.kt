package com.module.home.game.view

import com.component.busilib.model.PartyRoomInfoModel
import com.component.person.model.UserRankModel
import com.module.home.game.model.GrabSpecialModel
import com.module.home.model.SlideShowModel

interface IQuickGameView3 {
    fun setBannerImage(slideShowModelList: List<SlideShowModel>?)

    fun showTaskRedDot(show: Boolean)

    fun setRegionDiff(model: UserRankModel?)

    fun setPartyRoomList(list: List<PartyRoomInfoModel>?)
}

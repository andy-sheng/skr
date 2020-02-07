package com.module.playways.party.room.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.ui.IPartyGameView

class PartyGamePresenter(var mRoomData: PartyRoomData, var iPartyGameView: IPartyGameView) : RxLifeCyclePresenter() {
    init {

    }

    //获取当前游戏的状态
    private fun getCurrentGameState() {

    }
}
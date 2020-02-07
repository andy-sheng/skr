package com.module.playways.party.room.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.ui.IPartyGameView

class PartyGamePresenter(var mRoomData: PartyRoomData, var iPartyGameView: IPartyGameView) : RxLifeCyclePresenter() {
    internal var mRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    init {

    }

    //获取当前游戏的状态
    private fun getCurrentGameState() {

    }
}
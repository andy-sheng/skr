package com.module.playways.party.room.ui

import com.module.playways.party.room.model.PartyPunishInfoModel

interface IPartyGameView {
    fun showPunishList(list: ArrayList<PartyPunishInfoModel>)
    fun updateGame(index: Int, model: PartyPunishInfoModel, duration: Long)
    fun getNextPunishFailed()
}
package com.module.playways.party.room.presenter

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyPunishInfoModel
import com.module.playways.party.room.ui.IPartyGameView
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class PartyGamePresenter(var mRoomData: PartyRoomData, var iPartyGameView: IPartyGameView) : RxLifeCyclePresenter() {
    val zxh_type = 1 //: 真心话惩罚
    val dmx_type = 2 //: 大冒险惩罚

    internal var mRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    val zxhList = ArrayList<PartyPunishInfoModel>()
    val dmxList = ArrayList<PartyPunishInfoModel>()

    var list = zxhList
    var type = zxh_type
        set(value) {
            field = value
            if (field == zxh_type) {
                list = zxhList
            } else {
                list = dmxList
            }
        }

    fun getCurList(t: Int): ArrayList<PartyPunishInfoModel> {
        return if (t == zxh_type) zxhList else dmxList
    }

    fun getGameList() {
        getGameList(null)
    }

    //获取当前游戏的状态
    fun getGameList(call: (() -> Unit)?) {
        val rType = type
        if (rType == zxh_type && zxhList.size > 0) {
            iPartyGameView.showPunishList(zxhList)
            call?.invoke()
            return
        } else if (rType == dmx_type && dmxList.size > 0) {
            iPartyGameView.showPunishList(dmxList)
            call?.invoke()
            return
        }

        launch {
            val result = subscribe(RequestControl("getGameList", ControlType.CancelThis)) {
                mRoomServerApi.getPunishList(mRoomData.gameId, rType)
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("punishs"), PartyPunishInfoModel::class.java)
                list?.let {
                    if (rType == zxh_type) {
                        zxhList.addAll(list)
                        iPartyGameView.showPunishList(zxhList)
                        call?.invoke()
                    } else {
                        dmxList.addAll(list)
                        iPartyGameView.showPunishList(dmxList)
                        call?.invoke()
                    }
                }
            } else {
                U.getToastUtil().showShort(result.errmsg)
                MyLog.w("PartyGamePresenter", "getGameList error is ${result.errno}")
            }
        }
    }

    fun getNextPunish() {
        if ((type == zxh_type && zxhList.size == 0) || (type == dmx_type && dmxList.size == 0)) {
            U.getToastUtil().showShort("数据还未准备好，请稍后...")
            getGameList()
            return
        }

        launch {
            val map = mutableMapOf(
                    "punishType" to type,
                    "roomID" to mRoomData.gameId
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("loadRoomListData", ControlType.CancelThis)) {
                mRoomServerApi.beginPunish(body)
            }

            if (result.errno == 0) {
                val model = JSON.parseObject(result.data.getString("punishInfo"), PartyPunishInfoModel::class.java)
                val duration = result.data.getLongValue("endTimeMs") - result.data.getLongValue("beginTimeMs")
                scrollToModel(model, duration)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    fun scrollToModel(model: PartyPunishInfoModel, duration: Long) {
        var modelIndex = -1
        if (type == zxh_type) {
            zxhList.forEachIndexed { index, it ->
                if (model.punishID == it.punishID) {
                    modelIndex = index
                }
            }

            if (modelIndex != -1) {
                iPartyGameView.updateGame(modelIndex, zxhList[modelIndex], duration)
            } else {
                zxhList.add(model)
                iPartyGameView.updateGame(zxhList.size, model, duration)
            }
        } else {
            dmxList.forEachIndexed { index, it ->
                if (model.punishID == it.punishID) {
                    modelIndex = index
                }
            }

            if (modelIndex != -1) {
                iPartyGameView.updateGame(modelIndex, dmxList[modelIndex], duration)
            } else {
                dmxList.add(model)
                iPartyGameView.updateGame(dmxList.size, model, duration)
            }
        }
    }
}
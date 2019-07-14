package com.module.playways.songmanager.presenter

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.event.RoomNameChangeEvent
import com.module.playways.songmanager.model.RecommendTagModel
import com.module.playways.songmanager.view.ISongManageView
import com.module.playways.room.song.model.SongModel

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.HashMap

import okhttp3.MediaType
import okhttp3.RequestBody

class GrabSongManagePresenter(internal var mIOwnerManageView: ISongManageView, internal var mRoomData: GrabRoomData) : RxLifeCyclePresenter() {
    var mGrabRoomServerApi: GrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun updateRoomName(roomID: Int, roomName: String) {
        MyLog.d(TAG, "updateRoomName roomID=$roomID roomName=$roomName")
        val map = HashMap<String, Any>()
        map["roomID"] = roomID
        map["roomName"] = roomName

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.updateRoomName(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    U.getToastUtil().showShort("修改房间名成功")
                    mRoomData.roomName = roomName
                    mIOwnerManageView.showRoomName(roomName)
                    // TODO: 2019-06-23 由于此时的roomData和Room里面的不一样，需要发一个改变的事件
                    EventBus.getDefault().post(RoomNameChangeEvent(roomName))
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        }, this)
    }

    fun getRecommendTag() {
        ApiMethods.subscribe(mGrabRoomServerApi.standBillBoards, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val recommendTagModelArrayList = JSONObject.parseArray(result.data!!.getString("items"), RecommendTagModel::class.java)
                    mIOwnerManageView.showRecommendSong(recommendTagModelArrayList)
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        }, this, ApiMethods.RequestControl("getStandBillBoards", ApiMethods.ControlType.CancelThis))
    }

    // 向房主推荐新歌
    private fun suggestSong(songModel: SongModel) {
        MyLog.d(TAG, "suggestSong songModel=$songModel")
        val map = HashMap<String, Any>()
        map["itemID"] = songModel.itemID
        map["roomID"] = mRoomData.gameId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.suggestMusic(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "addSong process" + " result=" + result.errno)
                if (result.errno == 0) {
                    U.getToastUtil().showShort(songModel.itemName + " 推荐成功")
                } else {
                    MyLog.w(TAG, "addSong failed, " + " traceid is " + result.traceId)
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        if (!mRoomData.isOwner) {
            suggestSong(event.songModel)
        }
    }

    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

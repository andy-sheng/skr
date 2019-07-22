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
import com.module.playways.songmanager.view.IGrabWishManageView
import com.module.playways.songmanager.event.AddSuggestSongEvent
import com.module.playways.songmanager.model.GrabWishSongModel

import org.greenrobot.eventbus.EventBus

import java.util.HashMap

import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 * 愿望清单这个view 的 presenter
 */
class GrabWishSongPresenter(internal var mView: IGrabWishManageView, internal var mGrabRoomData: GrabRoomData) : RxLifeCyclePresenter() {
    var mGrabRoomServerApi: GrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
    var mGetSuggestListTask: Disposable? = null
    var mLimit = 20

    init {
        addToLifeCycle()
    }

    fun getListMusicSuggested(offset: Long) {
        if (mGetSuggestListTask != null && !mGetSuggestListTask!!.isDisposed) {
            MyLog.w(TAG, "已经加载中了...")
            return
        }
        mGetSuggestListTask = ApiMethods.subscribe(mGrabRoomServerApi.getListMusicSuggested(mGrabRoomData.gameId, offset, mLimit), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val grabWishSongModels = JSONObject.parseArray(result.data!!.getString("items"), GrabWishSongModel::class.java)
                    val newOffset = result.data!!.getLongValue("offset")
                    if (offset == 0L) {
                        mView.addGrabWishSongModels(true, newOffset, grabWishSongModels)
                    } else {
                        mView.addGrabWishSongModels(false, newOffset, grabWishSongModels)
                    }
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        }, this)
    }

    fun addWishSong(songModel: GrabWishSongModel) {
        MyLog.d(TAG, "addWishSong songModel=$songModel")
        val map = HashMap<String, Any>()
        map["itemID"] = songModel.itemID
        map["roomID"] = mGrabRoomData.gameId
        map["userID"] = songModel.suggester!!.userId
        map["pID"] = songModel.getpID()

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.addSuggestMusic(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "addWishSong result=$result")
                if (result.errno == 0) {
                    // 通知本页面，删除该model
                    mView.deleteWishSong(songModel)
                    // 通知GrabSongManagePresenter 接受新的数据
                    EventBus.getDefault().post(AddSuggestSongEvent(songModel))
                    U.getToastUtil().showShort("添加成功")
                } else {
                    MyLog.w(TAG, "addWishSong failed, " + " traceid is " + result.traceId)
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }

    fun deleteWishSong(songModel: GrabWishSongModel) {
        MyLog.d(TAG, "deleteWishSong songModel=$songModel")
        val map = HashMap<String, Any>()
        map["itemID"] = songModel.itemID
        map["roomID"] = mGrabRoomData.gameId
        map["userID"] = songModel.suggester!!.userId
        map["pID"] = songModel.getpID()

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.deleteSuggestMusic(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "addWishSong result=$result")
                if (result.errno == 0) {
                    // 通知本页面，删除该model
                    mView.deleteWishSong(songModel)
                    U.getToastUtil().showShort("删除成功")
                } else {
                    MyLog.w(TAG, "addWishSong failed, " + " traceid is " + result.traceId)
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }
}

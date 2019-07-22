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
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.model.RecommendTagModel
import com.module.playways.songmanager.view.ISongManageView

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.HashMap

import okhttp3.MediaType
import okhttp3.RequestBody

class DoubleSongManagePresenter(internal var mSongManageView: ISongManageView, internal var mDoubleRoomData: DoubleRoomData) : RxLifeCyclePresenter() {

    internal var mDoubleRoomServerApi: DoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun getRecommendTag() {
        ApiMethods.subscribe(mDoubleRoomServerApi.doubleStandBillBoards, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val recommendTagModelArrayList = JSONObject.parseArray(result.data!!.getString("items"), RecommendTagModel::class.java)
                    mSongManageView.showRecommendSong(recommendTagModelArrayList)
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        }, this, ApiMethods.RequestControl("getStandBillBoards", ApiMethods.ControlType.CancelThis))
    }

    fun getAddMusicCnt() {
        ApiMethods.subscribe(mDoubleRoomServerApi.getAddMusicCnt(mDoubleRoomData.gameId), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val musicCnt = result.data.getIntValue("musicCnt")
                    mSongManageView.showAddSongCnt(musicCnt)
                } else {
                    U.getToastUtil().showShort(result.errmsg + "")
                }

            }
        }, this, ApiMethods.RequestControl("getAddMusicCnt", ApiMethods.ControlType.CancelThis))
    }


    // 添加新歌
    fun addSong(songModel: SongModel) {
        MyLog.d(TAG, "addSong")
        val map = HashMap<String, Any>()
        map["itemID"] = songModel.itemID
        map["roomID"] = mDoubleRoomData.gameId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mDoubleRoomServerApi.addSong(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "addSong process" + " result=" + result.errno)
                if (result.errno == 0) {
                    U.getToastUtil().showShort(songModel.itemName + " 添加成功")
                    // 更新下歌曲数量
                    getAddMusicCnt()
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

    /**
     * 自己添加的歌曲
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        // 双人房都可以点歌
        addSong(event.songModel)
    }

    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

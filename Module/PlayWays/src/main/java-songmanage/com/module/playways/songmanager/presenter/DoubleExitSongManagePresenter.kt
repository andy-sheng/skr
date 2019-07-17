package com.module.playways.songmanager.presenter

import android.os.Handler

import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.model.DoubleCurSongInfoEvent
import com.module.playways.doubleplay.pushEvent.DoubleAddMusicEvent
import com.module.playways.doubleplay.pushEvent.DoubleDelMusicEvent
import com.module.playways.grab.room.event.GrabRoundChangeEvent
import com.module.playways.songmanager.view.IExistSongManageView
import com.module.playways.songmanager.event.AddCustomGameEvent
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.room.song.model.SongModel

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList
import java.util.HashMap

import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 * 房主可以看到的 所有轮次 的歌曲 view
 */
class DoubleExitSongManagePresenter(internal var mIGrabSongManageView: IExistSongManageView, internal var mDoubleRoomData: DoubleRoomData) : RxLifeCyclePresenter() {

    val mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    var mGetSongModelListTask: Disposable? = null
    var mGrabRoomSongModelList: MutableList<GrabRoomSongModel>? = ArrayList()

    var mUiHandler: Handler = Handler()
    var mOffset = 0
    var mHasMore = false
    var mTotalNum = 0
    var mLimit = 20

    init {
        addToLifeCycle()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun getPlayBookList(isRefresh: Boolean) {
        if (mGetSongModelListTask != null) {
            mGetSongModelListTask!!.dispose()
        }

        if (isRefresh) {
            mOffset = 0
        }
        mGetSongModelListTask = ApiMethods.subscribe(mDoubleRoomServerApi.getSongList(mDoubleRoomData.gameId, mOffset.toLong(), mLimit), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val grabRoomSongModels = JSON.parseArray(result.data!!.getString("playbook"), GrabRoomSongModel::class.java)
                    mOffset = result.data!!.getIntValue("offset")
                    if (grabRoomSongModels == null || grabRoomSongModels.size == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false)
                        mHasMore = false
                        MyLog.d(TAG, "process grabRoomSongModels size is 0")
                        return
                    }

                    MyLog.d(TAG, "process grabRoomSongModels size is is " + grabRoomSongModels.size)

                    mIGrabSongManageView.hasMoreSongList(true)
                    mHasMore = true
                    mGrabRoomSongModelList!!.addAll(grabRoomSongModels)
                    updateSongList()

                    val total = result.data!!.getIntValue("total")
                    mTotalNum = total
                    mIGrabSongManageView.showNum(total)
                } else {
                    MyLog.w(TAG, "getFriendList failed, " + result.errmsg + ", traceid is " + result.traceId)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                MyLog.e(TAG, "getFriendList 网络延迟")
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }

    fun deleteSong(grabRoomSongModel: GrabRoomSongModel) {
        MyLog.d(TAG, "deleteSong")
        val roundSeq = grabRoomSongModel.roundSeq

        if (roundSeq < 0) {
            MyLog.d(TAG, "deleteSong but roundReq is $roundSeq")
            return
        }

        val map = HashMap<String, Any>()
        map["roomID"] = mDoubleRoomData.gameId
        map["uniqTag"] = grabRoomSongModel.uniqTag

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mDoubleRoomServerApi.deleteSong(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "process" + " result=" + result.errno)
                if (result.errno == 0) {
                    if (mGrabRoomSongModelList != null) {
                        mIGrabSongManageView.showNum(--mTotalNum)
                        mIGrabSongManageView.deleteSong(grabRoomSongModel)

                        mUiHandler.removeCallbacksAndMessages(null)
                    }
                } else {
                    MyLog.w(TAG, "deleteSong failed, " + " traceid is " + result.traceId)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }

    fun updateSongList() {
        mIGrabSongManageView.updateSongList(mGrabRoomSongModelList!!)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabRoundChangeEvent) {
        updateSongList()
    }

    /**
     * 对方添加的音乐
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleAddMusicEvent) {
        // 双人房都可以点歌
        mGrabRoomSongModelList!!.clear()
        getPlayBookList(true)
    }

    /**
     * 对方删除的歌曲
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleDelMusicEvent) {
        // 双人房都可以点歌
        val grabRoomSongModelIterator = mGrabRoomSongModelList!!.iterator()
        while (grabRoomSongModelIterator.hasNext()) {
            val grabRoomSongModel = grabRoomSongModelIterator.next()
            if (grabRoomSongModel.uniqTag == event.uniqTag) {
                grabRoomSongModelIterator.remove()
                break
            }
        }

        updateSongList()
        mIGrabSongManageView.showNum(mGrabRoomSongModelList!!.size)
    }


    /**
     * 添加自定义小游戏成功
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddCustomGameEvent) {
        // 添加非房主想唱的歌曲
        val grabRoomSongModel = GrabRoomSongModel()
        grabRoomSongModel.owner = MyUserInfoManager.getInstance().nickName
        grabRoomSongModel.itemName = "自定义小游戏"
        grabRoomSongModel.itemID = SongModel.ID_CUSTOM_GAME
        grabRoomSongModel.playType = 4
        grabRoomSongModel.isChallengeAvailable = false
        addToUiList(grabRoomSongModel)
    }

    /**
     * 如果list size >=2 加到 index =2的位置 ，并把之前所有的seq++
     * 否则加到最后
     *
     * @param grabRoomSongModel
     */
    internal fun addToUiList(grabRoomSongModel: GrabRoomSongModel) {
        mGrabRoomSongModelList!!.add(grabRoomSongModel)
        mIGrabSongManageView.showNum(++mTotalNum)
        updateSongList()
    }

    override fun destroy() {
        super.destroy()
        mUiHandler.removeCallbacksAndMessages(null)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

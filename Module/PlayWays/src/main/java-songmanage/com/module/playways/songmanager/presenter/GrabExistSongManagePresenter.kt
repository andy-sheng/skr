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
import com.component.busilib.friends.SpecialModel
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.grab.room.event.GrabRoundChangeEvent
import com.module.playways.songmanager.view.IExistSongManageView
import com.module.playways.songmanager.event.AddCustomGameEvent
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.event.AddSuggestSongEvent
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.songmanager.model.GrabWishSongModel
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
class GrabExistSongManagePresenter(internal var mIGrabSongManageView: IExistSongManageView, internal var mGrabRoomData: GrabRoomData) : RxLifeCyclePresenter() {

    var mGrabRoomServerApi: GrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
    var mGetTagsTask: Disposable? = null
    var mGetSongModelListTask: Disposable? = null
    var mSpecialModelList: List<SpecialModel>? = null
    var mGrabRoomSongModelList: MutableList<GrabRoomSongModel>? = ArrayList()

    var mUiHandler: Handler

    var mHasMore = false
    var mTotalNum = 0
    var mLimit = 20

    init {
        mGrabRoomServerApi
        mUiHandler = Handler()
        addToLifeCycle()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun getTagList() {
        if (mSpecialModelList != null && mSpecialModelList!!.size > 0) {
            mIGrabSongManageView.showTagList(mSpecialModelList!!)
            return
        }

        if (mGetTagsTask != null && !mGetTagsTask!!.isDisposed) {
            MyLog.w(TAG, "已经加载中了...")
            return
        }
        MyLog.d(TAG, "getTagList")

        mGetTagsTask = ApiMethods.subscribe(mGrabRoomServerApi.getSepcialList(0, 20, if (mGrabRoomData.isVideoRoom) 2 else 1), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mSpecialModelList = JSON.parseArray(obj.data!!.getString("tags"), SpecialModel::class.java)
                    if (mSpecialModelList != null && mSpecialModelList!!.size > 0) {
                        mIGrabSongManageView.showTagList(mSpecialModelList!!)
                    }
                } else {
                    MyLog.d(TAG, "getTagList failed, result is $obj")
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }

    fun getPlayBookList() {
        if (mGetSongModelListTask != null) {
            mGetSongModelListTask!!.dispose()
        }

        var offset: Int

        if (!mGrabRoomData.hasGameBegin()) {
            offset = 0
        } else {
            offset = mGrabRoomData.realRoundSeq - 1
        }
        if (mGrabRoomSongModelList != null && mGrabRoomSongModelList!!.size > 0) {
            offset = mGrabRoomSongModelList!![mGrabRoomSongModelList!!.size - 1].roundSeq
        }

        MyLog.d(TAG, "getPlayBookList offset is $offset")

        mGetSongModelListTask = ApiMethods.subscribe(mGrabRoomServerApi.getPlaybook(mGrabRoomData.gameId, offset.toLong(), mLimit), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val grabRoomSongModels = JSON.parseArray(result.data!!.getString("playbook"), GrabRoomSongModel::class.java)
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
        val playbookItemId = grabRoomSongModel.itemID
        val roundSeq = grabRoomSongModel.roundSeq

        if (roundSeq < 0) {
            MyLog.d(TAG, "deleteSong but roundReq is $roundSeq")
            return
        }

        val map = HashMap<String, Any>()
        map["itemID"] = playbookItemId
        map["roomID"] = mGrabRoomData.gameId
        map["roundSeq"] = grabRoomSongModel.roundSeq

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.delMusic(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "process" + " result=" + result.errno)
                if (result.errno == 0) {
                    if (mGrabRoomSongModelList != null) {
                        val iterator = mGrabRoomSongModelList!!.iterator()
                        while (iterator.hasNext()) {
                            val grabRoomSongModel = iterator.next()
                            if (grabRoomSongModel.roundSeq == roundSeq) {
                                iterator.remove()
                            } else if (grabRoomSongModel.roundSeq > roundSeq) {
                                grabRoomSongModel.roundSeq = grabRoomSongModel.roundSeq - 1
                            }
                        }

                        mIGrabSongManageView.showNum(--mTotalNum)
                        mIGrabSongManageView.deleteSong(grabRoomSongModel)

                        mUiHandler.removeCallbacksAndMessages(null)
                        mUiHandler.postDelayed({
                            if (mGrabRoomSongModelList!!.size < 10 && mHasMore) {
                                getPlayBookList()
                            }
                        }, 300)
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
        val iterator = mGrabRoomSongModelList!!.iterator()
        while (iterator.hasNext()) {
            val grabRoomSongModel = iterator.next()
            if (grabRoomSongModel.roundSeq < mGrabRoomData.realRoundSeq) {
                iterator.remove()
            } else if (grabRoomSongModel.roundSeq > mGrabRoomData.realRoundSeq) {
                break
            }
        }

        mIGrabSongManageView.updateSongList(mGrabRoomSongModelList!!)

        if (mGrabRoomSongModelList!!.size < 5 && mHasMore) {
            getPlayBookList()
        }
    }

    // 添加新歌
    fun addSong(songModel: SongModel) {
        MyLog.d(TAG, "addSong")
        val map = HashMap<String, Any>()
        map["itemID"] = songModel.itemID
        map["roomID"] = mGrabRoomData.gameId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.addMusic(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "addSong process" + " result=" + result.errno)
                if (result.errno == 0) {
                    if (mGrabRoomSongModelList != null && mGrabRoomSongModelList!!.size > 0) {
                        //加一个保护
                        val grabRoomSongModel = GrabRoomSongModel()
                        grabRoomSongModel.owner = songModel.owner
                        grabRoomSongModel.itemName = songModel.itemName
                        grabRoomSongModel.itemID = songModel.itemID
                        grabRoomSongModel.playType = songModel.playType
                        grabRoomSongModel.isChallengeAvailable = songModel.isChallengeAvailable
                        addToUiList(grabRoomSongModel)
                    }
                    U.getToastUtil().showShort(songModel.itemName + " 添加成功")
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

    fun changeMusicTag(specialModel: SpecialModel, roomID: Int) {
        MyLog.d(TAG, "changeMusicTag")
        val map = HashMap<String, Any>()
        map["newTagID"] = specialModel.tagID
        map["roomID"] = roomID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        ApiMethods.subscribe(mGrabRoomServerApi.changeMusicTag(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.d(TAG, "changeMusicTag process" + " result=" + result.errno)
                if (result.errno == 0) {
                    mIGrabSongManageView.changeTagSuccess(specialModel)
                    val grabRoomSongModels = JSON.parseArray(result.data!!.getString("playbook"), GrabRoomSongModel::class.java)
                    if (grabRoomSongModels == null || grabRoomSongModels.size == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false)
                        return
                    }

                    val total = result.data!!.getIntValue("total")
                    mTotalNum = total
                    mIGrabSongManageView.showNum(mTotalNum)

                    mIGrabSongManageView.hasMoreSongList(true)
                    mGrabRoomSongModelList!!.clear()
                    mGrabRoomSongModelList!!.addAll(grabRoomSongModels)
                    updateSongList()
                } else {
                    MyLog.w(TAG, "changeMusicTag failed, " + " traceid is " + result.traceId)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabRoundChangeEvent) {
        if (event.newRoundInfo != null && event.newRoundInfo.roundSeq != 1) {
            mIGrabSongManageView.showNum(--mTotalNum)
        }
        /**
         * 因为现在用Activity 里，所以这里的 mGrabRoomData 跟之前不是一个引用了
         */
        mGrabRoomData.expectRoundInfo = event.newRoundInfo
        mGrabRoomData.realRoundInfo = event.newRoundInfo
        updateSongList()
    }

    /**
     * 增加歌曲
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        if (mGrabRoomData.isOwner) {
            addSong(event.songModel)
        }
    }

    /**
     * 房主处理愿望清单
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSuggestSongEvent) {
        // 添加非房主想唱的歌曲
        val grabWishSongModel = event.grabWishSongModel
        addToUiList(grabWishSongModel)
    }

    /**
     * 添加自定义小游戏成功
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddCustomGameEvent) {
        MyLog.d(TAG, "onEvent event=$event")
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
        MyLog.d(TAG, "addToUiList grabRoomSongModel=$grabRoomSongModel")
        if (mGrabRoomSongModelList!!.size >= 2) {
            for (i in 2 until mGrabRoomSongModelList!!.size) {
                val g = mGrabRoomSongModelList!![i]
                g.roundSeq = g.roundSeq + 1
            }
            grabRoomSongModel.roundSeq = mGrabRoomSongModelList!![1].roundSeq + 1
            mGrabRoomSongModelList!!.add(2, grabRoomSongModel)
        } else {
            if (mGrabRoomSongModelList!!.size == 0) {
                mGrabRoomSongModelList!!.add(grabRoomSongModel)
            } else {
                grabRoomSongModel.roundSeq = mGrabRoomSongModelList!![mGrabRoomSongModelList!!.size - 1].roundSeq + 1
                mGrabRoomSongModelList!!.add(grabRoomSongModel)
            }
        }
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

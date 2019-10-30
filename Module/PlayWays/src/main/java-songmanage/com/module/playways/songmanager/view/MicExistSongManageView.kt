package com.module.playways.songmanager.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.mic.room.model.MicUserMusicModel
import com.module.playways.songmanager.SongManagerServerApi
import com.module.playways.songmanager.adapter.MicExistSongAdapter
import com.module.playways.songmanager.model.MicExistSongModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.zq.live.proto.MicRoom.MAddMusicMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap
import com.module.playways.songmanager.adapter.MicExistListener as MicExistListener

// 排麦房的已点
class MicExistSongManageView(context: Context, internal var mRoomData: MicRoomData) : FrameLayout(context), CoroutineScope by MainScope() {
    val mTag = "MicExistSongManageView"

    val songManagerServerApi = ApiManager.getInstance().createService(SongManagerServerApi::class.java)
    val refreshLayout: SmartRefreshLayout
    val recyclerView: RecyclerView
    val manageSongAdapter: MicExistSongAdapter

    var offset = 0
    var hasMore = true
    val mCnt = 20

    var isSongChange = false

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        View.inflate(context, R.layout.mic_exist_song_manage_view_layout, this)
        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(false)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadMore()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        manageSongAdapter = MicExistSongAdapter(object : MicExistListener {
            override fun onClickDelete(model: MicExistSongModel?, position: Int) {
                // 删除
                model?.let { deleteSong(it) }

            }

            override fun onStick(model: MicExistSongModel?, position: Int) {
                // 置顶
                model?.let { stickSong(it) }
            }

        })
        recyclerView.adapter = manageSongAdapter

        // 默认展示，所有默认去拉数据
        tryLoad()
    }

    private fun addSongList(list: List<MicExistSongModel>?, clear: Boolean) {
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()

        if (clear) {
            manageSongAdapter.mDataList.clear()
            manageSongAdapter.hasSing = false  // 重置一下
            if (!list.isNullOrEmpty()) {
                manageSongAdapter.mDataList.addAll(list)
            }
            manageSongAdapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                manageSongAdapter.mDataList.addAll(list)
                manageSongAdapter.notifyDataSetChanged()
            }
        }
    }

    fun tryLoad() {
        if (manageSongAdapter.mDataList.isEmpty() || isSongChange) {
            getMicExistSongList(0, true)
        }
    }

    fun loadMore() {
        getMicExistSongList(offset, false)
    }

    private fun getMicExistSongList(off: Int, isClear: Boolean) {
        launch {
            var result = subscribe { songManagerServerApi.getMicExistSongList(mRoomData.gameId, MyUserInfoManager.uid.toInt(), off, mCnt) }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("musics"), MicExistSongModel::class.java)
                addSongList(list, isClear)
                // 每次拉到歌重置标记位
                isSongChange = false
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun deleteSong(model: MicExistSongModel) {
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["uniqTag"] = model.uniqTag!!

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe { songManagerServerApi.deleteMicSong(body) }
            if (result.errno == 0) {
                // 删除成功，需要刷新页面
                manageSongAdapter.mDataList.remove(model)
                manageSongAdapter.notifyDataSetChanged()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun stickSong(model: MicExistSongModel) {
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["uniqTag"] = model.uniqTag!!

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe { songManagerServerApi.stickMicSong(body) }
            if (result.errno == 0) {
                // 置顶
                manageSongAdapter.mDataList.remove(model)
                if (manageSongAdapter.hasSing) {
                    // 演唱中，至少得有三首歌才能置顶，删除了一首
                    if (manageSongAdapter.mDataList.size >= 2) {
                        manageSongAdapter.mDataList.add(1, model)
                    } else {
                        MyLog.e(mTag, "stickSong 置顶在演唱中，数目有问题")
                    }
                } else {
                    // 无演唱中，至少得有两首歌才能置顶，删除了一首
                    if (manageSongAdapter.mDataList.size >= 1) {
                        manageSongAdapter.mDataList.add(0, model)
                    } else {
                        MyLog.e(mTag, "stickSong 置顶无演唱中，数目有问题")
                    }
                }
                manageSongAdapter.notifyDataSetChanged()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MAddMusicMsg) {
        val userMusicModel = MicUserMusicModel.parseFromInfoPB(event.detail)
        if (userMusicModel.userID == MyUserInfoManager.uid.toInt()) {
            isSongChange = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        if(event.newRound?.singBySelf() == true){
            // TODO 当前页面被选中
            if(true){

            }else{
                isSongChange = true
            }
        }
    }

    fun destory() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        cancel()
    }
}
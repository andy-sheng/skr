package com.module.playways.songmanager.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.songmanager.adapter.MicExistSongAdapter
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import com.module.playways.songmanager.adapter.MicExistListener as MicExistListener

// 排麦房的已点
class MicExistSongManageView(context: Context, internal var mRoomData: MicRoomData) : FrameLayout(context), CoroutineScope by MainScope() {
    val mTag = "MicExistSongManageView"

    val refreshLayout: SmartRefreshLayout
    val recyclerView: RecyclerView

    val mManageSongAdapter: MicExistSongAdapter

    var offset = 0
    var hasMore = true
    val mCnt = 20

    init {
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
        mManageSongAdapter = MicExistSongAdapter(object : MicExistListener {
            override fun onClickDelete(model: GrabRoomSongModel?, position: Int) {
                // 删除
            }

            override fun onStick(model: GrabRoomSongModel?, position: Int) {
                // 置顶
            }

        })
        recyclerView.adapter = mManageSongAdapter

        getMicExistSongList(0)
    }

    fun loadMore() {
        getMicExistSongList(offset)
    }

    fun getMicExistSongList(off: Int) {
        // todo 等服务器给接口吧
    }

    fun destory() {
        cancel()
    }
}
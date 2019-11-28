package com.module.playways.songmanager.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import com.common.rxretrofit.ApiManager
import com.module.playways.R
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.songmanager.SongManagerServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.greenrobot.eventbus.EventBus

// 已点  包括删除和置顶
class RelayExistSongManageView(context: Context, roomData: RelayRoomData) : FrameLayout(context), CoroutineScope by MainScope() {

    val songManagerServerApi = ApiManager.getInstance().createService(SongManagerServerApi::class.java)
    val refreshLayout: SmartRefreshLayout
    val recyclerView: RecyclerView

    var offset = 0
    var hasMore = true
    val mCnt = 20

    var isSongChange = false

    init {
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }

        View.inflate(context, R.layout.relay_exist_song_manage_view_layout, this)
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
    }

    fun tryLoad() {

    }

    fun loadMore() {

    }

    fun destory() {
        cancel()
    }
}
package com.module.playways.songmanager.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout

import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.songmanager.adapter.WishSongAdapter
import com.module.playways.songmanager.model.GrabWishSongModel
import com.module.playways.songmanager.presenter.GrabWishSongPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

/**
 * 愿望歌单
 */
class GrabSongWishView(context: Context, internal var mGrabRoomData: GrabRoomData) : FrameLayout(context), IGrabWishManageView {

    val TAG = "GrabSongWishView"

    private var mRecyclerView: RecyclerView
    private var mRefreshLayout: SmartRefreshLayout

    private var mWishSongAdapter: WishSongAdapter? = null
    private var mGrabWishSongPresenter: GrabWishSongPresenter? = null
    private var mLoadService: LoadService<*>? = null
    private var mOffset: Long = 0   //此处是时间戳，int64

    init {
        View.inflate(context, R.layout.grab_song_wish_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)
        initData()
    }

    private fun initData() {
        mGrabWishSongPresenter = GrabWishSongPresenter(this, mGrabRoomData)
        mWishSongAdapter = WishSongAdapter(object : WishSongAdapter.Listener {
            override fun onClickDeleteWish(view: View, position: Int, songModel: GrabWishSongModel) {
                // 删除用户选的歌曲
                mGrabWishSongPresenter!!.deleteWishSong(songModel)
            }

            override fun onClickSelectWish(view: View, position: Int, songModel: GrabWishSongModel) {
                // 添加用户选的歌曲
                mGrabWishSongPresenter!!.addWishSong(songModel)
            }
        })
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.adapter = mWishSongAdapter

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(false)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mGrabWishSongPresenter!!.getListMusicSuggested(mOffset)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mGrabWishSongPresenter!!.getListMusicSuggested(0)
            }
        })

        val mLoadSir = LoadSir.Builder()
                .addCallback(GrabWishEmptyCallback())
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout) { mGrabWishSongPresenter!!.getListMusicSuggested(0) }
        mGrabWishSongPresenter!!.getListMusicSuggested(0)
    }

    fun destroy() {
        if (mGrabWishSongPresenter != null) {
            mGrabWishSongPresenter!!.destroy()
        }
    }

    fun tryLoad() {
        mGrabWishSongPresenter!!.getListMusicSuggested(0)
    }

    override fun addGrabWishSongModels(clear: Boolean, newOffset: Long, grabWishSongModels: List<GrabWishSongModel>?) {
        if (clear) {
            mWishSongAdapter?.dataList?.clear()
        }
        mOffset = newOffset
        mRefreshLayout.finishLoadMore()
        if (grabWishSongModels != null) {
            mWishSongAdapter?.dataList?.addAll(grabWishSongModels)
            mWishSongAdapter?.notifyDataSetChanged()
        }
        if (mWishSongAdapter?.dataList != null) {
            // 没有更多了
            mLoadService?.showSuccess()
        } else {
            // 空页面
            mLoadService?.showCallback(GrabWishEmptyCallback::class.java)
        }
    }

    override fun deleteWishSong(grabWishSongModel: GrabWishSongModel) {
        mWishSongAdapter?.delete(grabWishSongModel)
        if (mWishSongAdapter?.dataList != null) {
            // 没有更多了
            mLoadService?.showSuccess()
        } else {
            // 空页面
            mLoadService?.showCallback(GrabWishEmptyCallback::class.java)
        }
    }

}

package com.module.playways.songmanager.fragment

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import com.common.base.BaseFragment
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.friends.SpecialModel
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.adapter.ManageSongAdapter
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.songmanager.presenter.DoubleExitSongManagePresenter
import com.module.playways.songmanager.view.IExistSongManageView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class DoubleExistSongManageFragment : BaseFragment(), IExistSongManageView {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView

    lateinit var mPresenter: DoubleExitSongManagePresenter
    lateinit var mManageSongAdapter: ManageSongAdapter

    private var mDoubleRoomData: DoubleRoomData? = null

    override fun initView(): Int {
        return R.layout.double_exist_song_manage_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        mTitlebar = mRootView.findViewById(R.id.titlebar)
        mRefreshLayout = mRootView.findViewById(R.id.refreshLayout)
        mRecyclerView = mRootView.findViewById(R.id.recycler_view)

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mManageSongAdapter = ManageSongAdapter(SongManagerActivity.TYPE_FROM_DOUBLE)
        mRecyclerView.adapter = mManageSongAdapter

        mPresenter = DoubleExitSongManagePresenter(this, mDoubleRoomData!!)
        addPresent(mPresenter)

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mPresenter.getPlayBookList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPresenter.getPlayBookList()
            }
        })

        val defaultItemAnimator = DefaultItemAnimator()
        defaultItemAnimator.addDuration = 50
        defaultItemAnimator.removeDuration = 50
        mRecyclerView.itemAnimator = defaultItemAnimator

        mPresenter.getPlayBookList()


        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                U.getFragmentUtils().popFragment(this@DoubleExistSongManageFragment)
            }
        })

        mManageSongAdapter.onClickDelete = {
            it?.let {
                mPresenter.deleteSong(it)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mDoubleRoomData = data as DoubleRoomData?
        }
    }

    override fun showTagList(specialModelList: List<SpecialModel>) {

    }

    override fun updateSongList(grabRoomSongModelsList: List<GrabRoomSongModel>) {
        mManageSongAdapter.dataList = grabRoomSongModelsList
    }

    override fun hasMoreSongList(hasMore: Boolean) {
        mRefreshLayout.setEnableLoadMore(hasMore)
        mRefreshLayout.finishLoadMore()
    }

    override fun changeTagSuccess(specialModel: SpecialModel) {

    }

    override fun showNum(num: Int) {

    }

    override fun deleteSong(grabRoomSongModel: GrabRoomSongModel) {
        mManageSongAdapter.deleteSong(grabRoomSongModel)
    }
}

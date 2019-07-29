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
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.friends.SpecialModel
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.adapter.ManageSongAdapter
import com.module.playways.songmanager.event.SongNumChangeEvent
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.songmanager.presenter.DoubleExitSongManagePresenter
import com.module.playways.songmanager.view.IExistSongManageView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import org.greenrobot.eventbus.EventBus

class DoubleExistSongManageFragment : BaseFragment(), IExistSongManageView {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView

    lateinit var mPresenter: DoubleExitSongManagePresenter
    lateinit var mManageSongAdapter: ManageSongAdapter

    private var mDoubleRoomData: DoubleRoomData? = null

    lateinit var mLoadService: LoadService<*>

    override fun initView(): Int {
        return R.layout.double_exist_song_manage_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        mTitlebar = rootView.findViewById(R.id.titlebar)
        mRefreshLayout = rootView.findViewById(R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)

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
                mPresenter.getPlayBookList(false)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPresenter.getPlayBookList(true)
            }
        })

        val defaultItemAnimator = DefaultItemAnimator()
        defaultItemAnimator.addDuration = 50
        defaultItemAnimator.removeDuration = 50
        mRecyclerView.itemAnimator = defaultItemAnimator

        mPresenter.getPlayBookList(true)


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

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.song_exit_empty_icon, "大家还没有点歌", "#99ffffff"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
            mPresenter.getPlayBookList(true)

        })
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

        if (mManageSongAdapter.dataList != null && mManageSongAdapter.dataList.isNotEmpty()) {
            mLoadService.showSuccess()
        }
    }

    override fun hasMoreSongList(hasMore: Boolean) {
        mRefreshLayout.setEnableLoadMore(hasMore)
        mRefreshLayout.finishLoadMore()
        if (!hasMore) {
            if (mManageSongAdapter.dataList == null || mManageSongAdapter.dataList.isEmpty()) {
                mLoadService.showCallback(EmptyCallback::class.java)
            }
        } else {
            mLoadService.showSuccess()
        }
    }

    override fun changeTagSuccess(specialModel: SpecialModel) {

    }

    override fun showNum(num: Int) {
        EventBus.getDefault().post(SongNumChangeEvent(num))
    }

    override fun deleteSong(grabRoomSongModel: GrabRoomSongModel) {
        mManageSongAdapter.deleteSong(grabRoomSongModel)
    }
}

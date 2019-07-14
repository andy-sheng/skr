package com.module.playways.songmanager.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow

import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.friends.SpecialModel
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.adapter.ManageSongAdapter
import com.module.playways.songmanager.event.SongNumChangeEvent
import com.module.playways.songmanager.event.ChangeTagSuccessEvent
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.songmanager.presenter.GrabExistSongManagePresenter
import com.module.playways.songmanager.adapter.GrabTagsAdapter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

import org.greenrobot.eventbus.EventBus

/**
 * 已点歌单
 */
class GrabExistSongManageView(context: Context, internal var mRoomData: GrabRoomData) : FrameLayout(context), IExistSongManageView {

    val TAG = "GrabSongManageView"

    private var mRefreshLayout: SmartRefreshLayout
    private var mRecyclerView: RecyclerView
    private var mTvSelectedTag: ExTextView
    private var mTopTagView: FrameLayout
    private var mIvArrow: ImageView

    private var mGrabSongTagsView: GrabSongTagsView? = null
    private var mPopupWindow: PopupWindow? = null

    private var mManageSongAdapter: ManageSongAdapter
    private var mGrabSongManagePresenter: GrabExistSongManagePresenter
    private var mSpecialModelId: Int = 0

    init {
        View.inflate(context, R.layout.grab_song_manage_view_layout, this)

        mIvArrow = findViewById(R.id.iv_arrow)
        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)
        mTvSelectedTag = findViewById(R.id.selected_tag)
        mTopTagView = findViewById(R.id.top_tag_view)

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mManageSongAdapter = ManageSongAdapter(SongManagerActivity.TYPE_FROM_GRAB)
        mRecyclerView.adapter = mManageSongAdapter

        mGrabSongManagePresenter = GrabExistSongManagePresenter(this, mRoomData)

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mGrabSongManagePresenter.getPlayBookList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mGrabSongManagePresenter.getPlayBookList()
            }
        })

        val defaultItemAnimator = DefaultItemAnimator()
        defaultItemAnimator.addDuration = 50
        defaultItemAnimator.removeDuration = 50
        mRecyclerView.itemAnimator = defaultItemAnimator
        initData()
    }

    fun initData() {
        initListener()
        if (mRoomData.specialModel != null) {
            setTagTv(mRoomData.specialModel)
        }
        mGrabSongManagePresenter.getPlayBookList()
    }

    fun tryLoad() {
        if (mManageSongAdapter.dataList.isEmpty()) {
            mGrabSongManagePresenter.getPlayBookList()
        }
    }

    private fun initListener() {
        mTvSelectedTag.setOnClickListener { v ->
            if (mGrabSongTagsView == null) {
                mGrabSongTagsView = GrabSongTagsView(context)

                mGrabSongTagsView?.mGrabTagsAdapter?.onDismissDialog = {
                    mPopupWindow?.dismiss()
                }

                mGrabSongTagsView?.mGrabTagsAdapter?.onClickItem = {
                    mGrabSongManagePresenter.changeMusicTag(it!!, mRoomData.gameId)
                }
                mPopupWindow = PopupWindow(mGrabSongTagsView)
                mPopupWindow!!.width = mTvSelectedTag.width
                mPopupWindow!!.isOutsideTouchable = true
                mPopupWindow!!.isFocusable = true

                MyLog.d(TAG, "initListener Build.VERSION.SDK_INT " + Build.VERSION.SDK_INT)
                if (Build.VERSION.SDK_INT < 23) {
                    mPopupWindow!!.setBackgroundDrawable(BitmapDrawable())
                }

                mPopupWindow!!.setOnDismissListener { mIvArrow.background = U.getDrawable(R.drawable.fz_shuxing_xia) }
            }
            mGrabSongTagsView!!.mCurSpecialModelId = mSpecialModelId
            if (mPopupWindow != null && mPopupWindow!!.isShowing) {
                mPopupWindow!!.dismiss()
            } else {
                mGrabSongManagePresenter.getTagList()
            }
        }
        // TODO: 2019-07-11 展示让其不能点击切换
        mTvSelectedTag.isClickable = false

        mManageSongAdapter.onClickDelete = {
            it?.let {
                mGrabSongManagePresenter.deleteSong(it)
            }
        }

        mManageSongAdapter.setGrabRoomData(mRoomData)
    }

    private fun setTagTv(specialModel: SpecialModel) {
        mRoomData.specialModel = specialModel
        mRoomData.tagId = specialModel.tagID

        mSpecialModelId = specialModel.tagID
        mTvSelectedTag.text = specialModel.tagName
        if (!TextUtils.isEmpty(specialModel.bgColor)) {
            mTvSelectedTag.setTextColor(Color.parseColor(specialModel.bgColor))
        }
    }

    override fun changeTagSuccess(specialModel: SpecialModel) {
        setTagTv(specialModel)
        EventBus.getDefault().post(ChangeTagSuccessEvent(specialModel))
        if (mPopupWindow != null) {
            mPopupWindow!!.dismiss()
        }
    }

    override fun showNum(num: Int) {
        if (num < 0) {
            mGrabSongManagePresenter.getPlayBookList()
            return
        }

        EventBus.getDefault().post(SongNumChangeEvent(num))

    }

    override fun deleteSong(grabRoomSongModel: GrabRoomSongModel) {
        mManageSongAdapter.deleteSong(grabRoomSongModel)
    }

    override fun hasMoreSongList(hasMore: Boolean) {
        mRefreshLayout.setEnableLoadMore(hasMore)
        mRefreshLayout.finishLoadMore()
    }

    fun destroy() {
        mGrabSongManagePresenter.destroy()
    }

    override fun showTagList(specialModelList: List<SpecialModel>) {

        val height = U.getDisplayUtils().dip2px((if (specialModelList.size > 4) 190 else 55 * (specialModelList.size - 1)).toFloat())
        if (mGrabSongTagsView != null) {
            mGrabSongTagsView!!.setSpecialModelList(specialModelList)
            mPopupWindow!!.height = height
        }

        val location = IntArray(2)
        mTvSelectedTag.getLocationOnScreen(location)
        mPopupWindow!!.showAsDropDown(mTvSelectedTag)
        mIvArrow.background = U.getDrawable(R.drawable.fz_shuxing_shang)

    }

    override fun updateSongList(grabRoomSongModelsList: List<GrabRoomSongModel>) {
        mManageSongAdapter.dataList = grabRoomSongModelsList
    }
}

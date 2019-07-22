package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.module.feeds.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.common.view.ex.ExImageView
import android.support.constraint.Group
import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.module.feeds.watch.adapter.FeedsLikeViewAdapter
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.presenter.FeedLikeViewPresenter

class FeedsLikeView(var fragment: BaseFragment) : ConstraintLayout(fragment.context), IFeedLikeView {

    private val mTopAreaBg: SimpleDraweeView
    private val mPlayDescTv: TextView

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mRecyclerView: RecyclerView

    private val mPlayTypeIv: ImageView
    private val mPlayLikeIv: ExImageView
    private val mRecordGroup: Group
    private val mRecordFilm: ImageView
    private val mRecordCover: SimpleDraweeView
    private val mRecordPlayIv: ImageView
    private val mPlayLastIv: ImageView
    private val mPlayNextIv: ImageView

    private val mPersenter: FeedLikeViewPresenter
    private val mAdapter: FeedsLikeViewAdapter

    init {
        View.inflate(context, R.layout.feed_like_view_layout, this)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mTopAreaBg = findViewById(R.id.top_area_bg)
        mPlayDescTv = findViewById(R.id.play_desc_tv)

        mPlayTypeIv = findViewById(R.id.play_type_iv)
        mPlayLikeIv = findViewById(R.id.play_like_iv)
        mRecordGroup = findViewById(R.id.record_group)
        mRecordFilm = findViewById(R.id.record_film)
        mRecordCover = findViewById(R.id.record_cover)
        mRecordPlayIv = findViewById(R.id.record_play_iv)
        mPlayLastIv = findViewById(R.id.play_last_iv)
        mPlayNextIv = findViewById(R.id.play_next_iv)

        mPersenter = FeedLikeViewPresenter(this)
        mAdapter = FeedsLikeViewAdapter()

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter

        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(true)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                MyLog.d("FeedsCollectView", "onRefresh")
            }
        })
    }

    fun initData(flag: Boolean) {
        mPersenter.getFeedsLikeList()
    }

    override fun addLikeList(list: List<FeedsLikeModel>, offset: Int, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
        }

        bindTopData()
        mAdapter.mDataList.addAll(list)
        mAdapter.notifyDataSetChanged()
    }

    override fun requestError() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun bindTopData() {
        AvatarUtils.loadAvatarByUrl(mTopAreaBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setBlur(true)
                .build())


        AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCircle(true)
                .build())
    }

    fun destory() {
        mPersenter.destroy()
    }
}
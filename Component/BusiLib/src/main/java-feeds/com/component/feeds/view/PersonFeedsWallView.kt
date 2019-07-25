package com.component.feeds.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.userinfo.model.UserInfoModel
import com.common.player.IPlayer
import com.component.busilib.R
import com.component.feeds.adapter.FeedsWallViewAdapter
import com.component.feeds.listener.FeedsListener
import com.component.feeds.model.FeedsWatchModel
import com.component.feeds.presenter.FeedsWallViewPresenter
import com.component.person.view.RequestCallBack
import com.module.RouterConstants

class PersonFeedsWallView(var fragment: BaseFragment, var userInfoModel: UserInfoModel, internal var mCallBack: RequestCallBack?) : RelativeLayout(fragment.context), IFeedsWatchView {

    private val mRecyclerView: RecyclerView

    private val mAdapter: FeedsWallViewAdapter
    private val mPersenter: FeedsWallViewPresenter

    private var mMediaPlayer: IPlayer? = null

    init {
        View.inflate(context, R.layout.feed_wall_view_layout, this)

        mRecyclerView = findViewById(R.id.recycler_view)

        mPersenter = FeedsWallViewPresenter(this, userInfoModel)
        mAdapter = FeedsWallViewAdapter(object : FeedsListener {
            override fun onClickMoreListener(watchModel: FeedsWatchModel?) {
                // 更多
            }

            override fun onClickLikeListener(watchModel: FeedsWatchModel?) {
                // 喜欢
            }

            override fun onClickCommentListener(watchModel: FeedsWatchModel?) {
                // 评论
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withSerializable("feed_model", watchModel)
                        .navigation()
            }

            override fun onClickHitListener(watchModel: FeedsWatchModel?) {
                // 无打榜
            }

            override fun onClickDetailListener(watchModel: FeedsWatchModel?) {
                // 详情
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                        .withSerializable("feed_model", watchModel)
                        .navigation()
            }

            override fun onClickCDListener(watchModel: FeedsWatchModel?) {
            }

        })
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
    }

    fun getFeeds(flag: Boolean) {
        mPersenter.getFeeds(flag)
    }

    fun getMoreFeeds() {
        mPersenter.getMoreFeeds()
    }

    override fun requestError() {
        mCallBack?.onRequestSucess()
    }

    override fun requestTimeShort() {

    }

    override fun addWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        mCallBack?.onRequestSucess()

        if (isClear) {
            mAdapter.mDataList.clear()
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
            }
            mAdapter.notifyDataSetChanged()
        } else {
            if (list != null && list.isNotEmpty()) {
                mAdapter.mDataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

}


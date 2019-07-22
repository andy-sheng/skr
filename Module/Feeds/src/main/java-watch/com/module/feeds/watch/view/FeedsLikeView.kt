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
import com.common.view.DebounceViewClickListener
import com.module.feeds.watch.adapter.FeedsLikeViewAdapter
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.presenter.FeedLikeViewPresenter

class FeedsLikeView(var fragment: BaseFragment) : ConstraintLayout(fragment.context), IFeedLikeView {

    val ALL_REPEAT_PLAY_TYPE = 1      //全部循环
    val SINGLE_REPEAT_PLAY_TYPE = 2   //单曲循环
    val RANDOM_PLAY_TYPE = 3          //随机播放 (只在已经拉到的列表里面随机)

    var mCurrentType = ALL_REPEAT_PLAY_TYPE  //当前播放类型
    var isPlaying = false
    var mTopModel: FeedsLikeModel? = null

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

            }
        })

        mPlayTypeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 更新游戏类别 全部循环，单曲循环，随机播放
                when (mCurrentType) {
                    ALL_REPEAT_PLAY_TYPE -> {
                        mCurrentType = SINGLE_REPEAT_PLAY_TYPE
                        mPlayTypeIv.background = U.getDrawable(R.drawable.like_single_repeat_icon)
                        U.getToastUtil().showShort("单曲循环")
                    }
                    SINGLE_REPEAT_PLAY_TYPE -> {
                        mCurrentType = RANDOM_PLAY_TYPE
                        mPlayTypeIv.background = U.getDrawable(R.drawable.like_random_icon)
                        U.getToastUtil().showShort("随机播放")
                    }
                    RANDOM_PLAY_TYPE -> {
                        mCurrentType = ALL_REPEAT_PLAY_TYPE
                        mPlayTypeIv.background = U.getDrawable(R.drawable.like_all_repeat_icon)
                        U.getToastUtil().showShort("列表循环")
                    }
                }
            }
        })


        mPlayLikeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 不需要对界面进行任何更新
                if (mPlayLikeIv.isSelected) {
                    // 喜欢
                } else {
                    // 不喜欢
                }
                mPlayLikeIv.isSelected = !mPlayLikeIv.isSelected
            }
        })

        mRecordFilm.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mTopModel?.let {
                    playOrPause(it)
                }
            }

        })

        mPlayLastIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 上一首
            }
        })

        mPlayNextIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 下一首
            }
        })

        mAdapter.onClickPlayListener = { model ->
            model?.let {
                playOrPause(it)
            }
        }
    }

    fun playOrPause(model: FeedsLikeModel) {
        if (mAdapter.mCurrentPlayModel == model) {
            // 暂停播放
            isPlaying = false
            bindTopData(model, false)
            mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_play_icon)
            mAdapter.mCurrentPlayModel = null
            mAdapter.notifyDataSetChanged()
        } else {
            // 开始播放
            isPlaying = true
            bindTopData(model, true)
            mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_pause_icon)
            mAdapter.mCurrentPlayModel = mTopModel
            mAdapter.notifyDataSetChanged()
        }
    }

    fun initData(flag: Boolean) {
        mPersenter.getFeedsLikeList()
    }

    override fun addLikeList(list: List<FeedsLikeModel>, offset: Int, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
        }

        mAdapter.mDataList.addAll(list)
        if (mAdapter.mDataList.isNotEmpty() && isClear) {
            bindTopData(mAdapter.mDataList[0], false)
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun requestError() {

    }

    private fun bindTopData(model: FeedsLikeModel?, isPlay: Boolean) {
        if (this.mTopModel != model) {
            this.mTopModel = model
            AvatarUtils.loadAvatarByUrl(mTopAreaBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                    .setBlur(true)
                    .build())


            AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                    .setCircle(true)
                    .build())
        }

        // 开启和关闭动画
    }

    fun destory() {
        mPersenter.destroy()
    }
}
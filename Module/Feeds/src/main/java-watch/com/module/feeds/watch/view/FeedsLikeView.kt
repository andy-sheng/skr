package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.base.BaseFragment
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.player.IPlayer
import com.common.player.MyMediaPlayer
import com.common.player.VideoPlayerAdapter
import com.common.recorder.MyMediaRecorder
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.feeds.watch.adapter.FeedsLikeViewAdapter
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.presenter.FeedLikeViewPresenter
import kotlin.collections.ArrayList

class FeedsLikeView(var fragment: BaseFragment) : ConstraintLayout(fragment.context), IFeedLikeView {


    val ALL_REPEAT_PLAY_TYPE = 1      //全部循环
    val SINGLE_REPEAT_PLAY_TYPE = 2   //单曲循环
    val RANDOM_PLAY_TYPE = 3          //随机播放 (只在已经拉到的列表里面随机)

    var mCurrentType = ALL_REPEAT_PLAY_TYPE  //当前播放类型
    var isPlaying = false
    var mTopModel: FeedsLikeModel? = null
    var mTopPosition: Int = 0      // 顶部在播放队列中的位置
    var isFirstRandom = true      // 是否第一次随机clear
    var mRandomList = ArrayList<FeedsLikeModel>()  // 随机播放队列
    var mMediaPlayer: IPlayer? = null  // 播放器

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

    var mCDRotateAnimation: RotateAnimation? = null
    var mCoverRotateAnimation: RotateAnimation? = null


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
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mPersenter.loadMoreFeedLikeList()
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
                mTopModel?.let { mPersenter.likeOrUnLikeFeed(it) }
            }
        })

        mRecordFilm.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mTopModel?.let {
                    playOrPause(it, mTopPosition, false)
                }
            }

        })

        mPlayLastIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 上一首
                playWithType(false)
            }
        })

        mPlayNextIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 下一首
                playWithType(true)
            }
        })

        mAdapter.onClickPlayListener = { model, position ->
            model?.let {
                playOrPause(it, position, false)
            }
        }
    }

    // 初始化数据
    fun initData(flag: Boolean) {
        mPersenter.initFeedLikeList(flag)
    }

    // 停止播放
    fun stopPlay() {
        if (isPlaying) {
            isPlaying = false
            bindTopData(mTopPosition, mTopModel, false)
            mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_play_icon)
            mAdapter.mCurrentPlayModel = null
            mAdapter.notifyDataSetChanged()
            mMediaPlayer?.reset()
        }
    }

    private fun playWithType(isNext: Boolean) {
        when (mCurrentType) {
            SINGLE_REPEAT_PLAY_TYPE -> {
                mTopModel?.let {
                    playOrPause(it, mTopPosition, true)
                }
            }
            ALL_REPEAT_PLAY_TYPE -> {
                allRepeatPlay(isNext)
            }
            RANDOM_PLAY_TYPE -> {
                randomPlay()
            }
        }
    }

    private fun allRepeatPlay(isNext: Boolean) {
        if (isNext) {
            if ((mTopPosition + 1) >= mAdapter.mDataList.size) {
                playOrPause(mAdapter.mDataList[0], 0, true)
            } else {
                playOrPause(mAdapter.mDataList[mTopPosition + 1], mTopPosition + 1, true)
            }
        } else {
            if ((mTopPosition - 1) < 0) {
                playOrPause(mAdapter.mDataList[mAdapter.mDataList.size - 1], mAdapter.mDataList.size - 1, true)
            } else {
                playOrPause(mAdapter.mDataList[mTopPosition - 1], mTopPosition - 1, true)
            }
        }

    }

    private fun randomPlay() {
        // 对于随机播放，下一首和上一首，都是在队列里面播下一首
        if (isFirstRandom) {
            isFirstRandom = false
            mRandomList.clear()
            mRandomList.addAll(mAdapter.mDataList)
            mRandomList.shuffle()
        }

        if ((mTopPosition + 1) >= mRandomList.size) {
            playOrPause(mRandomList[0], 0, true)
            isFirstRandom = true
        } else {
            playOrPause(mRandomList[mTopPosition + 1], mTopPosition + 1, true)
        }
    }

    fun playOrPause(model: FeedsLikeModel, position: Int, isMust: Boolean) {
        if (isMust) {
            // 开始播放
            play(model, position)
        } else {
            if (mAdapter.mCurrentPlayModel == model) {
                // 暂停播放
                pause(model, position)
            } else {
                // 开始播放
                play(model, position)
            }
        }
    }

    private fun play(model: FeedsLikeModel, position: Int) {
        isPlaying = true
        bindTopData(position, model, true)
        mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_pause_icon)
        mAdapter.mCurrentPlayModel = mTopModel
        mAdapter.notifyDataSetChanged()
        if (mMediaPlayer == null) {
            mMediaPlayer = MyMediaPlayer()
        }
        mMediaPlayer?.setCallback(object : VideoPlayerAdapter.PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                // 自动播放下一首
                playWithType(true)
            }
        })
        model?.song?.playURL?.let {
            mMediaPlayer?.startPlay(it)
        }
    }

    private fun pause(model: FeedsLikeModel, position: Int) {
        isPlaying = false
        bindTopData(position, model, false)
        mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_play_icon)
        mAdapter.mCurrentPlayModel = null
        mAdapter.notifyDataSetChanged()
        mMediaPlayer?.reset()
    }

    override fun addLikeList(list: List<FeedsLikeModel>, isClear: Boolean) {
        if (isClear) {
            mAdapter.mDataList.clear()
        }

        mAdapter.mDataList.addAll(list)
        if (mAdapter.mDataList.isNotEmpty() && isClear) {
            bindTopData(0, mAdapter.mDataList[0], false)
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun showLike(model: FeedsLikeModel) {
        this.mTopModel = model
        mPlayLikeIv.isSelected = !model.isLiked
        // 更新数据
        mAdapter.update(mTopPosition, model)
        for (i in 0 until mRandomList.size) {
            if (mRandomList[i].feedID == model.feedID) {
                mRandomList[i] = model
                return
            }
        }
    }

    override fun requestError() {

    }

    private fun bindTopData(position: Int, model: FeedsLikeModel?, isPlay: Boolean) {
        this.mTopPosition = position
        if (this.mTopModel != model) {
            this.mTopModel = model
            AvatarUtils.loadAvatarByUrl(mTopAreaBg, AvatarUtils.newParamsBuilder(mTopModel?.user?.avatar)
                    .setBlur(true)
                    .build())

            AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(mTopModel?.user?.avatar)
                    .setCircle(true)
                    .build())
        }

        // 开启和关闭动画
        if (isPlay) {
            if (mCDRotateAnimation == null) {
                mCDRotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                mCDRotateAnimation?.duration = 3000
                mCDRotateAnimation?.repeatCount = Animation.INFINITE
                mCDRotateAnimation?.fillAfter = true
                mCDRotateAnimation?.interpolator = LinearInterpolator()
            }
            if (mCoverRotateAnimation == null) {
                mCoverRotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                mCoverRotateAnimation?.duration = 3000
                mCoverRotateAnimation?.repeatCount = Animation.INFINITE
                mCoverRotateAnimation?.fillAfter = true
                mCoverRotateAnimation?.interpolator = LinearInterpolator()
            }
            mRecordFilm.startAnimation(mCDRotateAnimation)
            mRecordCover.startAnimation(mCoverRotateAnimation)
        } else {
            mCDRotateAnimation?.setAnimationListener(null)
            mCDRotateAnimation?.cancel()
            mCoverRotateAnimation?.setAnimationListener(null)
            mCoverRotateAnimation?.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destory()
    }

    fun destory() {
        mCDRotateAnimation?.setAnimationListener(null)
        mCDRotateAnimation?.cancel()
        mCoverRotateAnimation?.setAnimationListener(null)
        mCoverRotateAnimation?.cancel()
        mPersenter.destroy()
    }
}
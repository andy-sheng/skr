package com.module.feeds.watch.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.player.IPlayer
import com.common.player.MyMediaPlayer
import com.common.player.PlayerCallbackAdapter
import com.common.player.VideoPlayerAdapter
import com.common.view.DebounceViewClickListener
import com.component.dialog.FeedsMoreDialogView
import com.module.feeds.watch.adapter.FeedsWallViewAdapter
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.presenter.FeedsWallViewPresenter
import com.component.person.view.RequestCallBack
import com.module.RouterConstants
import com.module.feeds.IPersonFeedsWall
import com.module.feeds.R

class PersonFeedsWallView(var fragment: BaseFragment, var userInfoModel: UserInfoModel, internal var mCallBack: RequestCallBack?) : RelativeLayout(fragment.context), IFeedsWatchView, IPersonFeedsWall {
    override fun unselected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mRecyclerView: RecyclerView

    private val mAdapter: FeedsWallViewAdapter
    private val mPersenter: FeedsWallViewPresenter

    private var mMediaPlayer: IPlayer? = null
    var mFeedsMoreDialogView: FeedsMoreDialogView? = null

    init {
        View.inflate(context, R.layout.feed_wall_view_layout, this)

        mRecyclerView = findViewById(R.id.recycler_view)

        mPersenter = FeedsWallViewPresenter(this, userInfoModel)
        mAdapter = FeedsWallViewAdapter(object : FeedsListener {
            override fun onClickCDListener(position: Int, watchModel: FeedsWatchModel?) {
                watchModel?.let {
                    startPlay(position,it, false)
                }
            }

            override fun onclickRankListener(watchModel: FeedsWatchModel?) {
                // 排行榜详情
                watchModel?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                            .withString("rankTitle", it.rank?.rankTitle)
                            .withLong("challengeID", it.song?.challengeID ?: 0L)
                            .navigation()
                }
            }

            override fun onClickMoreListener(watchModel: FeedsWatchModel?) {
                // 更多
                watchModel?.let {
                    if (fragment.activity != null) {
                        if (userInfoModel.userId.toLong() == MyUserInfoManager.getInstance().uid) {
                            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_PERSON,
                                    it.user?.userID ?: 0,
                                    it.song?.songID ?: 0,
                                    0)
                                    .apply {
                                        mFuncationTv.text = "分享"
                                        mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                                            override fun clickValid(v: View?) {
                                                dismiss(false)
                                            }
                                        })
                                        mReportTv.text = "删除"
                                        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
                                            override fun clickValid(v: View?) {
                                                dismiss(false)
                                            }
                                        })
                                    }
                        } else {
                            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_OTHER_PERSON,
                                    it.user?.userID ?: 0,
                                    it.song?.songID ?: 0,
                                    0)
                                    .apply {
                                        mFuncationTv.text = "分享"
                                        mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                                            override fun clickValid(v: View?) {
                                                dismiss(false)
                                            }
                                        })
                                    }
                        }

                        mFeedsMoreDialogView?.showByDialog()
                    }

                }
            }

            override fun onClickLikeListener(position: Int, watchModel: FeedsWatchModel?) {
                // 喜欢
                watchModel?.let { mPersenter.feedLike(position, it) }
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
        })
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
    }


    override fun getFeeds(flag: Boolean) {
        mPersenter.getFeeds(flag)
    }

    override fun getMoreFeeds() {
        mPersenter.getMoreFeeds()
    }

    override fun setUserInfoModel(userInfoModel: Any?) {
        this.userInfoModel = userInfoModel as UserInfoModel
    }

     fun stopPlay() {
        mAdapter.updatePlayModel(-1,null)
        mMediaPlayer?.reset()
    }

    override fun requestError() {
        mCallBack?.onRequestSucess()
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

    override fun feedLikeResult(position: Int, model: FeedsWatchModel, isLike: Boolean) {
        model.isLiked = isLike
        if (isLike) {
            model.starCnt = model.starCnt?.plus(1)
        } else {
            model.starCnt = model.starCnt?.minus(1)
        }
        mAdapter.update(position, model, FeedsWallViewAdapter.REFRESH_TYPE_LIKE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }

    fun startPlay(pos:Int,model: FeedsWatchModel, isMustPlay: Boolean) {
        if (isMustPlay) {
            startPlay(pos,model)
        } else {
            if (mAdapter.mCurrentModel?.feedID != model.feedID) {
                startPlay(pos,model)
            } else {
                stopPlay()
            }
        }
    }

    fun startPlay(pos:Int,model: FeedsWatchModel) {
        mAdapter.updatePlayModel(pos,model)
        if (mMediaPlayer == null) {
            mMediaPlayer = MyMediaPlayer()
        }
        mMediaPlayer?.setCallback(object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                // 重复播放
                model?.song?.playURL?.let {
                    mMediaPlayer?.startPlay(it)
                }
            }
        })
        model.song?.playURL?.let {
            mMediaPlayer?.startPlay(it)
        }
    }

    fun destroy() {
        stopPlay()
        mPersenter.destroy()
        mMediaPlayer?.release()
    }

}


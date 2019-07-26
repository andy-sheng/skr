package com.module.feeds.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.detail.adapter.FeedsCommentAdapter
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.CommentCountModel
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsSecondCommentPresenter
import com.module.feeds.detail.view.FeedsInputContainerView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener


class FeedsCommentDetailFragment : BaseFragment(), IFirstLevelCommentView {
    val mTag = "FeedsCommentDetailFragment"
    var mTitlebar: CommonTitleBar? = null
    var mCommentTv: ExTextView? = null
    var mXinIv: ExImageView? = null
    var mXinNumTv: ExTextView? = null
    var mShareIv: ExImageView? = null
    var mShareNumTv: ExTextView? = null
    var mFeedsInputContainerView: FeedsInputContainerView? = null
    var mFirstLevelCommentModel: FirstLevelCommentModel? = null
    var mRefuseModel: FirstLevelCommentModel? = null
    var mRefreshLayout: SmartRefreshLayout? = null
    var mRecyclerView: RecyclerView? = null
    var feedsCommendAdapter: FeedsCommentAdapter? = null
    var mFeedsSecondCommentPresenter: FeedsSecondCommentPresenter? = null
    var mFeedsID: Int? = null

    override fun initView(): Int {
        return com.module.feeds.R.layout.feeds_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mFirstLevelCommentModel == null) {
            activity?.finish()
            return
        }

        mCommentTv = rootView.findViewById(com.module.feeds.R.id.comment_tv)
        mXinIv = rootView.findViewById(com.module.feeds.R.id.xin_iv)
        mXinNumTv = rootView.findViewById(com.module.feeds.R.id.xin_num_tv)
        mShareIv = rootView.findViewById(com.module.feeds.R.id.share_iv)
        mShareNumTv = rootView.findViewById(com.module.feeds.R.id.share_num_tv)
        mFeedsInputContainerView = rootView.findViewById(com.module.feeds.R.id.feeds_input_container_view)

        mTitlebar = rootView.findViewById(com.module.feeds.R.id.titlebar)
        mTitlebar?.leftTextView?.setDebounceViewClickListener {
            activity?.finish()
        }
        mTitlebar?.centerTextView?.text = "${mFirstLevelCommentModel?.comment?.subCommentCnt.toString()}条回复"

        mFeedsSecondCommentPresenter = FeedsSecondCommentPresenter(0, this)

        mRefreshLayout = rootView.findViewById(com.module.feeds.R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(com.module.feeds.R.id.recycler_view)

        feedsCommendAdapter = FeedsCommentAdapter(true)
        feedsCommendAdapter?.mCommentNum = mFirstLevelCommentModel!!.comment!!.subCommentCnt
        feedsCommendAdapter?.mIFirstLevelCommentListener = object : FeedsCommentAdapter.IFirstLevelCommentListener {
            override fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int) {
                mFeedsSecondCommentPresenter?.likeComment(firstLevelCommentModel, mFeedsID!!, like, position)
            }

            override fun onClickContent(firstLevelCommentModel: FirstLevelCommentModel) {
                mRefuseModel = firstLevelCommentModel
                mFeedsInputContainerView?.showSoftInput()
            }

            override fun onClickIcon(userID: Int) {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", userID)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mRecyclerView?.adapter = feedsCommendAdapter

        mRefreshLayout?.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout?.setEnableOverScrollDrag(false)
        mRefreshLayout?.setEnableLoadMore(true)
        mRefreshLayout?.setEnableRefresh(false)
        mRefreshLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mFeedsSecondCommentPresenter?.getSecondLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        mXinNumTv!!.text = mFirstLevelCommentModel!!.comment.likedCnt.toString()
        mXinIv!!.isSelected = mFirstLevelCommentModel!!.isLiked
        mXinIv?.setDebounceViewClickListener {
            mFeedsSecondCommentPresenter?.likeComment(mFirstLevelCommentModel!!, mFeedsID!!, !mXinIv!!.isSelected, 0)
        }

        mShareIv?.setDebounceViewClickListener {
            val sharePanel = SharePanel(activity)
            sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png")
            sharePanel.show(ShareType.IMAGE_RUL)
        }

        mCommentTv?.setDebounceViewClickListener {
            mRefuseModel = mFirstLevelCommentModel
            mFeedsInputContainerView?.showSoftInput()
        }

        mFeedsInputContainerView?.mSendCallBack = {
            mFeedsSecondCommentPresenter?.addComment(it, mFeedsID!!, mRefuseModel!!) {
                mFirstLevelCommentModel!!.comment!!.subCommentCnt++
                feedsCommendAdapter?.mCommentNum = mFirstLevelCommentModel!!.comment!!.subCommentCnt
                feedsCommendAdapter?.notifyItemChanged(1)
                mTitlebar?.centerTextView?.text = "${mFirstLevelCommentModel?.comment?.subCommentCnt.toString()}条回复"
                mFeedsSecondCommentPresenter?.mModelList?.add(0, it)
                mFeedsSecondCommentPresenter?.updateCommentList()
            }
        }

        mFeedsSecondCommentPresenter?.getSecondLevelCommentList()
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun noMore() {
        mRefreshLayout?.finishLoadMore()
        mRefreshLayout?.setEnableLoadMore(false)
    }

    override fun updateList(list: List<FirstLevelCommentModel>?) {
        list?.let {
            val mList: ArrayList<Any> = ArrayList(list)
            mList.add(0, CommentCountModel(509))
            mList.add(0, mFirstLevelCommentModel!!)
            feedsCommendAdapter?.dataList = mList
        }

        mRefreshLayout?.finishLoadMore()
    }

    override fun likeFinish(firstLevelCommentModel: FirstLevelCommentModel, position: Int, like: Boolean) {
        feedsCommendAdapter?.notifyItemChanged(position)
        if (position == 0) {
            mXinIv?.isSelected = like
            mXinNumTv?.text = firstLevelCommentModel.comment.likedCnt.toString()
        }
    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mFirstLevelCommentModel = data as FirstLevelCommentModel
        } else if (type == 1) {
            mFeedsID = data as Int
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
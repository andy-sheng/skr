package com.module.feeds.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.person.utils.StringFromatUtils
import com.module.RouterConstants
import com.module.feeds.detail.adapter.FeedsCommentAdapter
import com.module.feeds.detail.event.AddCommentEvent
import com.module.feeds.detail.event.LikeFirstLevelCommentEvent
import com.module.feeds.detail.inter.IFirstLevelCommentView
import com.module.feeds.detail.model.CommentCountModel
import com.module.feeds.detail.model.FeedsCommentEmptyModel
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsSecondCommentPresenter
import com.module.feeds.detail.view.FeedCommentMoreDialog
import com.module.feeds.detail.view.FeedsInputContainerView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import org.greenrobot.eventbus.EventBus


class FeedsCommentDetailFragment : BaseFragment(), IFirstLevelCommentView {
    val mTag = "FeedsCommentDetailFragment"
    var mTitlebar: CommonTitleBar? = null
    var mCommentTv: ExTextView? = null
    var mXinIv: ExImageView? = null
    var mXinNumTv: ExTextView? = null
    var mFeedsInputContainerView: FeedsInputContainerView? = null
    var mFirstLevelCommentModel: FirstLevelCommentModel? = null
    var mRefuseModel: FirstLevelCommentModel? = null
    var mRefreshLayout: SmartRefreshLayout? = null
    var mRecyclerView: RecyclerView? = null
    var feedsCommendAdapter: FeedsCommentAdapter? = null
    var mFeedsSecondCommentPresenter: FeedsSecondCommentPresenter? = null
    var mFeedsID: Int? = null

    var mMoreDialogPlus: FeedCommentMoreDialog? = null

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
        mFeedsInputContainerView = rootView.findViewById(com.module.feeds.R.id.feeds_input_container_view)

        mTitlebar = rootView.findViewById(com.module.feeds.R.id.titlebar)
        mTitlebar?.leftTextView?.setDebounceViewClickListener {
            activity?.finish()
        }
        mTitlebar?.centerTextView?.text = "${mFirstLevelCommentModel?.comment?.subCommentCnt.toString()}条回复"

        mFeedsSecondCommentPresenter = FeedsSecondCommentPresenter(mFeedsID!!, this)

        mRefreshLayout = rootView.findViewById(com.module.feeds.R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(com.module.feeds.R.id.recycler_view)

        feedsCommendAdapter = FeedsCommentAdapter(true)
        feedsCommendAdapter?.mCommentNum = mFirstLevelCommentModel!!.comment!!.subCommentCnt
        feedsCommendAdapter?.mIFirstLevelCommentListener = object : FeedsCommentAdapter.IFirstLevelCommentListener {
            override fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int) {
                mFeedsSecondCommentPresenter?.likeComment(firstLevelCommentModel, mFeedsID!!, like, position)
            }

            override fun onClickContent(firstLevelCommentModel: FirstLevelCommentModel) {
                showCommentOp(firstLevelCommentModel)
            }

            override fun onClickMore(firstLevelCommentModel: FirstLevelCommentModel) {

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
                mFeedsSecondCommentPresenter?.getSecondLevelCommentList(mFirstLevelCommentModel!!.comment.commentID)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        mXinNumTv!!.text = StringFromatUtils.formatTenThousand(mFirstLevelCommentModel!!.comment.likedCnt)
        mXinIv!!.isSelected = mFirstLevelCommentModel!!.isLiked()
        mXinIv?.setDebounceViewClickListener {
            mFeedsSecondCommentPresenter?.likeComment(mFirstLevelCommentModel!!, mFeedsID!!, !mXinIv!!.isSelected, 0)
        }

        mCommentTv?.setDebounceViewClickListener {
            mRefuseModel = mFirstLevelCommentModel
            mFeedsInputContainerView?.showSoftInput()
            mFeedsInputContainerView?.setETHint("回复 ${mRefuseModel?.commentUser?.nickname}")
        }

        mFeedsInputContainerView?.mSendCallBack = {
            mFeedsSecondCommentPresenter?.addComment(it, mFeedsID!!, mFirstLevelCommentModel!!.comment.commentID, mRefuseModel!!) {
                mFirstLevelCommentModel!!.comment!!.subCommentCnt++
                feedsCommendAdapter?.mCommentNum = mFirstLevelCommentModel!!.comment!!.subCommentCnt
                feedsCommendAdapter?.notifyItemChanged(1)
                mTitlebar?.centerTextView?.text = "${mFirstLevelCommentModel?.comment?.subCommentCnt.toString()}条回复"
                mFeedsSecondCommentPresenter?.mModelList?.add(0, it)
                mFeedsSecondCommentPresenter?.mOffset = mFeedsSecondCommentPresenter?.mOffset!! + 1
                mFeedsSecondCommentPresenter?.updateCommentList()
                EventBus.getDefault().post(AddCommentEvent(mFirstLevelCommentModel!!.comment.commentID))
                mRecyclerView?.scrollToPosition(0)
            }
        }

        mFeedsSecondCommentPresenter?.updateCommentList()
        mFeedsSecondCommentPresenter?.getSecondLevelCommentList(mFirstLevelCommentModel!!.comment.commentID)
    }

    private fun showCommentOp(model: FirstLevelCommentModel) {
        mMoreDialogPlus?.dismiss()
        activity?.let {
            mMoreDialogPlus = FeedCommentMoreDialog(it, model)
                    .apply {
                        mReplyTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                dismiss()
                                mRefuseModel = model
                                mFeedsInputContainerView?.showSoftInput()
                                mFeedsInputContainerView?.setETHint("回复 ${mRefuseModel?.commentUser?.nickname}")
                            }
                        })
                    }
            mMoreDialogPlus?.showByDialog()
        }
    }

    override fun showNum(count: Int) {

    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun noMore(isEmpty: Boolean) {
        if (isEmpty) {
            val mList: ArrayList<Any> = ArrayList()
            mList.add(0, FeedsCommentEmptyModel())
            mList.add(0, CommentCountModel())
            mList.add(0, mFirstLevelCommentModel!!)
            feedsCommendAdapter?.dataList = mList
        }
        mRefreshLayout?.finishLoadMore()
        mRefreshLayout?.setEnableLoadMore(false)
    }

    override fun finishLoadMore() {
        mRefreshLayout?.finishLoadMore()
    }

    override fun updateList(list: List<FirstLevelCommentModel>?) {
        list?.let {
            val mList: ArrayList<Any> = ArrayList(list)
            mList.add(0, CommentCountModel())
            mList.add(0, mFirstLevelCommentModel!!)
            feedsCommendAdapter?.dataList = mList
        }

        mRefreshLayout?.finishLoadMore()
    }

    override fun likeFinish(firstLevelCommentModel: FirstLevelCommentModel, position: Int, like: Boolean) {
        feedsCommendAdapter?.updatePart(position, firstLevelCommentModel, FeedsCommentAdapter.TYPE_LIKE)
        if (position == 0) {
            mXinIv?.isSelected = like
            mXinNumTv?.text = StringFromatUtils.formatTenThousand(firstLevelCommentModel.comment.likedCnt)
            EventBus.getDefault().post(LikeFirstLevelCommentEvent(firstLevelCommentModel.comment.commentID, like))
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
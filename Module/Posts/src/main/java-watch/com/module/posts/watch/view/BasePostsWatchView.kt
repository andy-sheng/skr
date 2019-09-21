package com.module.posts.watch.view

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.callback.Callback
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.redpkg.PostsRedPkgDialogView
import com.module.posts.watch.PostsWatchServerApi
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.adapter.PostsWatchViewAdapter
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BasePostsWatchView(val activity: FragmentActivity, val type: Int) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    val TAG = when (type) {
        TYPE_POST_FOLLOW -> "FollowPostsWatchView"
        TYPE_POST_RECOMMEND -> "FollowWatchView"
        TYPE_POST_LAST -> "RecommendPostsWatchView"
        TYPE_POST_PERSON -> "PersonPostsWatchView"
        else -> "BasePostsWatchView"
    }

    companion object {
        const val TYPE_POST_FOLLOW = 1     // 关注
        const val TYPE_POST_RECOMMEND = 2  // 推荐
        const val TYPE_POST_LAST = 3       // 最新
        const val TYPE_POST_PERSON = 4     // 个人中心
        const val TYPE_POST_TOPIC = 5      // 话题
    }

    val postsWatchServerApi = ApiManager.getInstance().createService(PostsWatchServerApi::class.java)

    val playerTag = TAG + hashCode()
    val playCallback: PlayerCallbackAdapter

    var isSeleted = false  // 是否选中
    var mHasInitData = false  //关注和推荐是否初始化过数据
    var hasMore = true // 是否可以加载更多
    var mOffset = 0   //偏移量
    val mCNT = 20  // 默认拉去的个数

    private val refreshLayout: SmartRefreshLayout
    private val classicsHeader: ClassicsHeader
    private val recyclerView: RecyclerView

    var adapter: PostsWatchViewAdapter? = null

    var postsMoreDialogView: PostsMoreDialogView? = null
    var postsRedPkgDialogView: PostsRedPkgDialogView? = null

    fun dismissDialog() {
        postsMoreDialogView?.dismiss(false)
        postsRedPkgDialogView?.dismiss(false)
    }

    init {
        View.inflate(context, R.layout.posts_watch_view_layout, this)
        refreshLayout = this.findViewById(R.id.refreshLayout)
        classicsHeader = this.findViewById(R.id.classics_header)
        recyclerView = this.findViewById(R.id.recycler_view)

        adapter = PostsWatchViewAdapter(object : PostsWatchListener {
            override fun onClickPostsAvatar(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickPostsAvatar")
                model?.user?.let {
                    openOtherPersonCenter(it.userId)
                }
            }

            override fun onClickPostsMore(position: Int, model: PostsWatchModel?) {
                dismissDialog()
                onClickMore(position, model)
            }

            override fun onClickPostsAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                U.getToastUtil().showShort("onClickPostsAudio")
                if (isPlaying) {
                    SinglePlayer.stop(playerTag)
                } else {
                    model?.posts?.audios?.let {
                        SinglePlayer.startPlay(playerTag, it[0].url)
                    }
                }
                adapter?.startOrPauseAudio(position, model, false)
            }

            override fun onClickPostsImage(position: Int, model: PostsWatchModel?, index: Int, url: String?) {
                U.getToastUtil().showShort("onClickPostsImage")
                goBigImageBrowse(index, model)
            }

            override fun onClickPostsRedPkg(position: Int, model: PostsWatchModel?) {
                dismissDialog()
                model?.posts?.redpacketInfo?.let {
                    postsRedPkgDialogView?.dismiss(false)
                    postsRedPkgDialogView = PostsRedPkgDialogView(activity, it)
                    postsRedPkgDialogView?.showByDialog()
                }
            }

            override fun onClickPostsTopic(position: Int, model: PostsWatchModel?) {
                if (type == TYPE_POST_TOPIC) {
                    U.getToastUtil().showShort("话题页面不可点击")
                } else {
                    model?.posts?.topicInfo?.let {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_TOPIC)
                                .withSerializable("topicInfo", it)
                                .navigation()
                    }
                }
            }

            override fun onClickPostsComment(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickPostsComment")
            }

            override fun onClickPostsLike(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickPostsLike")
            }

            override fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int) {
                U.getToastUtil().showShort("onClickPostsVote")
            }

            override fun onClickCommentAvatar(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickCommentAvatar")
                model?.bestComment?.user?.let {
                    openOtherPersonCenter(it.userId)
                }
            }

            override fun onClickCommentLike(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickCommentLike")
            }

            override fun onClickCommentAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                U.getToastUtil().showShort("onClickCommentAudio")
                if (isPlaying) {
                    SinglePlayer.stop(playerTag)
                } else {
                    model?.bestComment?.comment?.audios?.let {
                        SinglePlayer.startPlay(playerTag, it[0].url)
                    }
                }
                adapter?.startOrPauseAudio(position, model, true)
            }

            override fun onClickCommentImage(position: Int, model: PostsWatchModel?, index: Int, url: String?) {
                U.getToastUtil().showShort("onClickCommentImage")
                goBigImageBrowse(index, model)
            }
        })
        refreshLayout.apply {
            setEnableRefresh(type != TYPE_POST_PERSON && type != TYPE_POST_TOPIC)
            setEnableLoadMore(type != TYPE_POST_PERSON && type != TYPE_POST_TOPIC)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(type != TYPE_POST_PERSON && type != TYPE_POST_TOPIC)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getMorePosts()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    initPostsList(true)
                }
            })
        }
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        playCallback = object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                adapter?.stopPlay()
            }
        }
        SinglePlayer.addCallback(playerTag, playCallback)
    }

    private fun goBigImageBrowse(index: Int, model: PostsWatchModel?) {
        BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<String>() {
            override fun init() {

            }

            override fun load(imageBrowseView: ImageBrowseView, position: Int, item: String) {
                imageBrowseView.load(item)
            }

            override fun getInitCurrentItemPostion(): Int {
                return index
            }

            override fun getInitList(): List<String>? {
                return model?.posts?.pictures
            }

            override fun loadMore(backward: Boolean, position: Int, data: String, callback: Callback<List<String>>?) {
                if (backward) {
                    // 向后加载
                }
            }

            override fun hasMore(backward: Boolean, position: Int, data: String): Boolean {
                return if (backward) {
                    return false
                } else false
            }

            override fun hasMenu(): Boolean {
                return false
            }
        })
    }

    private fun openOtherPersonCenter(userID: Int) {
        val bundle = Bundle()
        bundle.putInt("bundle_user_id", userID)
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                .with(bundle)
                .navigation()
    }

    open fun unselected(reason: Int) {
        isSeleted = false
        SinglePlayer.reset(playerTag)
        adapter?.stopPlay()
    }

    open fun selected() {
        isSeleted = true
    }

    fun addWatchPosts(list: List<PostsWatchModel>?, clear: Boolean) {
        if (clear) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
                adapter?.notifyDataSetChanged()
            }
        }

        if (adapter?.mDataList.isNullOrEmpty()) {
            // 数据为空
        } else {

        }
    }

    fun finishRefreshOrLoadMore() {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)
    }

    abstract fun onClickMore(position: Int, model: PostsWatchModel?)

    // 加载数据
    abstract fun initPostsList(flag: Boolean): Boolean

    // 加载更多数据
    abstract fun getMorePosts()

    fun destory() {
        cancel()
    }
}
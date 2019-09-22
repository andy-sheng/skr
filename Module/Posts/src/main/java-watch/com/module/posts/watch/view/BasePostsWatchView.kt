package com.module.posts.watch.view

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
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
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

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

        adapter = PostsWatchViewAdapter(type, object : PostsWatchListener {
            override fun onClickPostsAvatar(position: Int, model: PostsWatchModel?) {
                U.getToastUtil().showShort("onClickPostsAvatar")
                model?.user?.let {
                    openOtherPersonCenter(it.userId)
                }
            }

            override fun onClickPostsMore(position: Int, model: PostsWatchModel?) {
                dismissDialog()
                model?.let {
                    postsMoreDialogView?.dismiss(false)
                    var from = PostsMoreDialogView.FROM_POSTS_HOME
                    if (type == TYPE_POST_PERSON) {
                        from = PostsMoreDialogView.FROM_POSTS_PERSON
                    } else if (type == TYPE_POST_TOPIC) {
                        from = PostsMoreDialogView.FROM_POSTS_TOPIC
                    }
                    postsMoreDialogView = PostsMoreDialogView(activity, from, it)
                    if (it.user?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                        postsMoreDialogView?.apply {
                            reportTv.text = "删除"
                            reportTv.setOnClickListener(object : DebounceViewClickListener() {
                                override fun clickValid(v: View?) {
                                    postsMoreDialogView?.dismiss(false)
                                    deletePosts(position, model)
                                }
                            })
                        }
                    }
                    postsMoreDialogView?.showByDialog(true)
                }
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
                adapter?.startOrPauseAudio(position, model, PostsWatchViewAdapter.PLAY_POSTS_AUDIO)
            }

            override fun onClickPostsSong(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                if (isPlaying) {
                    SinglePlayer.stop(playerTag)
                } else {
                    model?.posts?.song?.playURL?.let {
                        SinglePlayer.startPlay(playerTag, it)
                    }
                }
                adapter?.startOrPauseAudio(position, model, PostsWatchViewAdapter.PLAY_POSTS_SONG)
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
                model?.let {
                    postsLikeOrUnLike(position, it)
                }
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
                model?.let {
                    postsCommentLikeOrUnLike(position, model)
                }
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
                adapter?.startOrPauseAudio(position, model, PostsWatchViewAdapter.PLAY_POSTS_COMMENT_AUDIO)
            }

            override fun onClickCommentSong(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                if (isPlaying) {
                    SinglePlayer.stop(playerTag)
                } else {
                    model?.bestComment?.comment?.song?.playURL?.let {
                        SinglePlayer.startPlay(playerTag, it)
                    }
                }
                adapter?.startOrPauseAudio(position, model, PostsWatchViewAdapter.PLAY_POSTS_COMMENT_SONG)
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

    // 加载数据
    abstract fun initPostsList(flag: Boolean): Boolean

    // 加载更多数据
    abstract fun getMorePosts()

    fun postsLikeOrUnLike(position: Int, model: PostsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["postsID"] = model.posts?.postsID ?: 0
            map["like"] = !model.isLiked
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

            val result = subscribe(RequestControl("postsLikeOrUnLike", ControlType.CancelThis)) {
                postsWatchServerApi.postsLikeOrUnLike(body)
            }
            if (result.errno == 0) {
                model.isLiked = !model.isLiked
                if (model.isLiked) {
                    // 点赞
                    model.numeric?.starCnt = model.numeric?.starCnt?.plus(1)
                } else {
                    // 取消赞
                    model.numeric?.starCnt = model.numeric?.starCnt?.minus(1)
                }
                adapter?.update(position, model, PostsWatchViewAdapter.REFRESH_POSTS_LIKE)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    fun postsCommentLikeOrUnLike(position: Int, model: PostsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["commentID"] = model.bestComment?.comment?.commentID ?: 0
            map["postsID"] = model.posts?.postsID ?: 0
            map["like"] = (model.bestComment?.isLiked == false)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

            val result = subscribe(RequestControl("postsCommentLikeOrUnLike", ControlType.CancelThis)) {
                postsWatchServerApi.postsCommentLikeOrUnLike(body)
            }
            if (result.errno == 0) {
                model.bestComment?.isLiked = (model.bestComment?.isLiked == false)
                if (model.bestComment?.isLiked == true) {
                    // 评论点赞
                    model.bestComment?.comment?.likedCnt = model.bestComment?.comment?.likedCnt?.plus(1)
                            ?: 0
                } else {
                    // 评论取消赞
                    model.bestComment?.comment?.likedCnt = model.bestComment?.comment?.likedCnt?.minus(1)
                            ?: 0
                }
                adapter?.update(position, model, PostsWatchViewAdapter.REFRESH_POSTS_COMMENT_LIKE)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    fun deletePosts(position: Int, model: PostsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["postsID"] = model.posts?.postsID ?: 0
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

            val result = subscribe(RequestControl("deletePosts", ControlType.CancelThis)) {
                postsWatchServerApi.deletePosts(body)
            }
            if (result.errno == 0) {
                adapter?.deletePosts(model)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    open fun destory() {
        cancel()
    }
}
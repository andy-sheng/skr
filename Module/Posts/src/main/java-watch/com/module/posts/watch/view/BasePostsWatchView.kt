package com.module.posts.watch.view

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AbsListView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.busilib.callback.EmptyCallback
import com.dialog.view.TipsDialogView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseActivity
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.activity.PostsDetailActivity
import com.module.posts.detail.event.PostsDetailEvent
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.redpkg.PostsRedPkgDialogView
import com.module.posts.statistics.PostsStatistics
import com.module.posts.watch.PostsWatchServerApi
import com.module.posts.watch.UNSELECT_REASON_TO_OTHER_ACTIVITY
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap

abstract class BasePostsWatchView(val activity: FragmentActivity, val type: Int) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    val TAG = when (type) {
        TYPE_POST_FOLLOW -> "FollowPostsWatchView"
        TYPE_POST_RECOMMEND -> "RecommendPostsWatchView"
        TYPE_POST_LAST -> "LastPostsWatchView"
        TYPE_POST_PERSON -> "PersonPostsWatchView"
        TYPE_POST_TOPIC -> "TopicPostsWatchView"
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
    private val playCallback: SinglePlayerCallbackAdapter

    var isSeleted = false  // 是否选中
    var mHasInitData = false  //关注和推荐是否初始化过数据
    var hasMore = true // 是否可以加载更多
    var mOffset = 0   //偏移量
    val mCNT = 20  // 默认拉去的个数

    private val refreshLayout: SmartRefreshLayout
    private val classicsHeader: ClassicsHeader
    private val recyclerView: RecyclerView
    private var layoutManager: LinearLayoutManager

    var adapter: PostsWatchViewAdapter? = null

    var postsMoreDialogView: PostsMoreDialogView? = null
    var postsRedPkgDialogView: PostsRedPkgDialogView? = null
    var tipsDialogView: TipsDialogView? = null

    var mLoadService: LoadService<*>? = null

    fun dismissDialog() {
        tipsDialogView?.dismiss(false)
        postsMoreDialogView?.dismiss(false)
        postsRedPkgDialogView?.dismiss(false)
    }

    fun autoRefresh() {
        refreshLayout.autoRefresh()
    }

    init {
        View.inflate(context, R.layout.posts_watch_view_layout, this)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        refreshLayout = this.findViewById(R.id.refreshLayout)
        classicsHeader = this.findViewById(R.id.classics_header)
        recyclerView = this.findViewById(R.id.recycler_view)

        adapter = PostsWatchViewAdapter(type, object : PostsWatchListener {
            override fun onClickPostsDetail(position: Int, model: PostsWatchModel?) {
                if (model != null && model.isAudit()) {
                    recordClick(model)
                    // 停掉音乐吧
                    var pendingPlayingUrl:String? = null
                    if (adapter?.mCurrentPlayModel == model
                            && (adapter?.playStatus == PostsWatchViewAdapter.PLAY_POSTS_AUDIO || adapter?.playStatus == PostsWatchViewAdapter.PLAY_POSTS_SONG)) {
                        if (adapter?.playStatus == PostsWatchViewAdapter.PLAY_POSTS_AUDIO) {
                            pendingPlayingUrl = adapter?.mCurrentPlayModel?.posts?.audios?.getOrNull(0)?.url
                        } else if (adapter?.playStatus == PostsWatchViewAdapter.PLAY_POSTS_SONG) {
                            pendingPlayingUrl = adapter?.mCurrentPlayModel?.posts?.song?.playURL
                        }
                    } else {
                        SinglePlayer.stop(playerTag)
                        adapter?.stopPlay()
                    }
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_DETAIL)
                            .withInt("postsID", model.posts?.postsID?.toInt() ?: 0)
                            .withString("playingUrl", pendingPlayingUrl)
                            .navigation()
                } else {
                    U.getToastUtil().showShort("帖子审核完毕就可以互动啦～")
                }
            }

            override fun onClickPostsAvatar(position: Int, model: PostsWatchModel?) {
                model?.user?.let {
                    recordClick(model)
                    openOtherPersonCenter(it.userId)
                }
            }

            override fun onClickPostsMore(position: Int, model: PostsWatchModel?) {
                dismissDialog()
                model?.let {
                    recordClick(model)
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
                                    tipsDialogView?.dismiss(false)
                                    tipsDialogView = TipsDialogView.Builder(activity)
                                            .setMessageTip("确定删除帖子吗?")
                                            .setCancelTip("取消")
                                            .setCancelBtnClickListener {
                                                tipsDialogView?.dismiss()
                                            }
                                            .setConfirmTip("确认")
                                            .setConfirmBtnClickListener {
                                                tipsDialogView?.dismiss()
                                                deletePosts(position, model)
                                            }
                                            .build()
                                    tipsDialogView?.showByDialog()
                                }
                            })
                        }
                    }
                    postsMoreDialogView?.showByDialog(true)
                }
            }

            override fun onClickPostsAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                recordClick(model)
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
                recordClick(model)
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
                recordClick(model)
                goBigImageBrowse(index, model?.posts?.pictures)
            }

            override fun onClickPostsRedPkg(position: Int, model: PostsWatchModel?) {
                dismissDialog()
                recordClick(model)
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
                    StatisticsAdapter.recordCountEvent("posts", "topic_content_click", null)
                    recordClick(model)
                    model?.posts?.topicInfo?.let {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_TOPIC)
                                .withLong("topicID", it.topicID.toLong())
                                .navigation()
                    }
                }
            }

            override fun onClickPostsLike(position: Int, model: PostsWatchModel?) {
                if (model != null && model.isAudit()) {
                    recordClick(model)
                    postsLikeOrUnLike(position, model)
                } else {
                    U.getToastUtil().showShort("帖子审核完毕就可以互动啦～")
                }
            }

            override fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int) {
                // 审核过，且未投票
                if (model != null && model.isAudit()) {
                    if (model.posts?.voteInfo?.hasVoted == true) {
                        // 已投票，不让投了
                    } else {
                        recordClick(model)
                        votePosts(position, model, index)
                    }
                } else {
                    U.getToastUtil().showShort("帖子审核完毕就可以互动啦～")
                }
            }

            override fun onClickCommentAvatar(position: Int, model: PostsWatchModel?) {
                model?.bestComment?.user?.let {
                    recordClick(model)
                    openOtherPersonCenter(it.userId)
                }
            }

            override fun onClickCommentLike(position: Int, model: PostsWatchModel?) {
                if (model != null && model.isAudit()) {
                    recordClick(model)
                    postsCommentLikeOrUnLike(position, model)
                } else {
                    U.getToastUtil().showShort("帖子审核完毕就可以互动啦～")
                }
            }

            override fun onClickCommentAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean) {
                recordClick(model)
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
                recordClick(model)
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
                recordClick(model)
                goBigImageBrowse(index, model?.bestComment?.comment?.pictures)
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
                    if (type == TYPE_POST_RECOMMEND) {
                        StatisticsAdapter.recordCountEvent("posts", "hot_tab_refresh", null)
                    }
                    initPostsList(true)
                }
            })
        }


        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        playCallback = object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                adapter?.stopPlay()
            }

            override fun onPlaytagChange(oldPlayerTag: String?, newPlayerTag: String?) {
                if (newPlayerTag != playerTag) {
                    adapter?.stopPlay()
                }
            }
        }
        SinglePlayer.addCallback(playerTag, playCallback)

        if (type != TYPE_POST_PERSON) {
            val mLoadSir = LoadSir.Builder()
                    .addCallback(EmptyCallback(R.drawable.home_list_empty_icon, "暂无帖子发布", "#802F2F30"))
                    .build()
            mLoadService = mLoadSir.register(refreshLayout, com.kingja.loadsir.callback.Callback.OnReloadListener {
                initPostsList(true)
            })

            addOnScrollListenerToRv()
        }
    }

    private fun goBigImageBrowse(index: Int, list: List<String>?) {
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
                return list
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
        if (reason == UNSELECT_REASON_TO_OTHER_ACTIVITY)
        {
            // 跳到详情页或者看大图  不停止播放
            val topActivity = U.getActivityUtils().topActivity
            if (topActivity is PostsDetailActivity) {
                return
            }
            if (topActivity is BigImageBrowseActivity) {
                return
            }
        }
        SinglePlayer.stop(playerTag)
        adapter?.stopPlay()
    }

    open fun selected() {
        isSeleted = true
        if (!initPostsList(false)) {
            recordExposure("selected")
        }
    }

    fun addWatchPosts(list: List<PostsWatchModel>?, clear: Boolean) {
        if (clear) {
            // 数据更新，之前播放要停
            SinglePlayer.stop(playerTag)
            adapter?.stopPlay()
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
            recordExposure("addWatchPosts")
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter?.mDataList?.size!!
                adapter?.mDataList?.addAll(list)
                val newSize = adapter?.mDataList?.size!!
                adapter?.notifyItemRangeInserted(size, newSize - size)
            }
        }

        if (type != TYPE_POST_PERSON) {
            if (adapter?.mDataList.isNullOrEmpty()) {
                // 数据为空
                mLoadService?.showCallback(EmptyCallback::class.java)
            } else {
                mLoadService?.showSuccess()
            }
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

    private fun addOnScrollListenerToRv() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            //刚进入列表时统计当前屏幕可见views
            private var isFirstVisible = true

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isFirstVisible) {
                    recordExposure("onScrolled isFirstVisible")
                    isFirstVisible = false
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        recordExposure("SCROLL_STATE_IDLE")
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                }
            }
        })
    }

    fun recordExposure(from: String) {
        // 不需要个人中心的点
        if (type != TYPE_POST_PERSON) {
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (firstVisibleItem != RecyclerView.NO_POSITION && lastVisibleItem != RecyclerView.NO_POSITION) {
                for (i in firstVisibleItem..lastVisibleItem) {
                    if (adapter?.mDataList?.isNullOrEmpty() == false) {
                        adapter?.mDataList?.let {
                            if (i in 0 until it.size) {
                                it[i].posts?.postsID?.let { postsID ->
                                    MyLog.d(TAG, "recordExposure from = $from postsID = $postsID")
                                    PostsStatistics.addCurExpose(postsID.toInt())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun recordClick(model: PostsWatchModel?) {
        if (type != TYPE_POST_PERSON) {
            model?.posts?.postsID?.let {
                PostsStatistics.addCurClick(it.toInt())
            }
        }
    }

    // 帖子点赞
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

    // 评论点赞
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
                if (model == adapter?.mCurrentPlayModel) {
                    // 删除的正好是当前播放的
                    adapter?.stopPlay()
                    SinglePlayer.stop(playerTag)
                }
                adapter?.deletePosts(model)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                } else if (result.errno == 8302558) {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    // 投票
    fun votePosts(position: Int, model: PostsWatchModel, voteSeq: Int) {
        launch {
            val map = HashMap<String, Any>()
            map["postsID"] = model.posts?.postsID ?: 0
            map["voteID"] = model.posts?.voteInfo?.voteID ?: 0
            map["voteSeq"] = voteSeq
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

            val result = subscribe(RequestControl("votePosts", ControlType.CancelThis)) {
                postsWatchServerApi.votePosts(body)
            }
            if (result.errno == 0) {
                U.getToastUtil().showShort("投票成功")
                model.posts?.voteInfo?.hasVoted = true
                model.posts?.voteInfo?.voteSeq = voteSeq
                model.posts?.voteInfo?.voteList?.let {
                    if (voteSeq in 1..it.size) {
                        it[voteSeq - 1].voteCnt = it[voteSeq - 1].voteCnt + 1
                    }
                }
                adapter?.update(position, model, PostsWatchViewAdapter.REFRESH_POSTS_VOTE)

            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PostsDetailEvent) {
        event.model?.let {
            adapter?.updateModelFromDetail(it)
        }
    }

    open fun destory() {
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        cancel()
    }
}
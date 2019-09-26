package com.module.posts.detail.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.ObjectPlayControlTemplate
import com.common.base.BaseActivity
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.SkrProgressView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.event.AddSecondCommentEvent
import com.module.posts.detail.event.PostsDetailEvent
import com.module.posts.detail.inter.IPostsDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.presenter.PostsDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.detail.view.ReplyModel
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.publish.PostsPublishActivity
import com.module.posts.redpkg.PostsRedPkgDialogView
import com.module.posts.watch.model.PostsWatchModel
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_POSTS_DETAIL)
class PostsDetailActivity : BaseActivity(), IPostsDetailView {
    companion object {
        val playerTag = "PostsDetailActivity"
    }

    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var imageIv: ExImageView
    lateinit var audioIv: ExImageView
    lateinit var kgeIv: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    lateinit var smartRefreshLayout: SmartRefreshLayout
    var mPostsWatchModel: PostsWatchModel? = null
    var mPostsID: Int? = null
    var mPostsDetailPresenter: PostsDetailPresenter? = null
    var postsMoreDialogView: PostsMoreDialogView? = null
    var postsRedPkgDialogView: PostsRedPkgDialogView? = null
    lateinit var progressView: SkrProgressView
    lateinit var mImageTid: ExImageView
    lateinit var mKgeTid: ExImageView
    lateinit var mAudioTid: ExImageView

    var postsAdapter: PostsCommentAdapter? = null

    var mPlayingUrl = ""

    var mPlayingPosition = -1

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mPostsID = intent.getIntExtra("postsID", 0) as Int?
        if (mPostsID == null) {
            finish()
            return
        }

        SinglePlayer.addCallback(playerTag, object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                MyLog.d(TAG, "onCompletion url is $mPlayingUrl")
                super.onCompletion()
                stopPlayingState()
            }

            override fun onError(what: Int, extra: Int) {
                MyLog.d(TAG, "onError url is $mPlayingUrl")
                super.onError(what, extra)
                stopPlayingState()
            }
        })

        (intent.getSerializableExtra("playingUrl") as String?)?.let {
            mPlayingUrl = it
            mPlayingPosition = 0
            SinglePlayer.startPlay(playerTag, it)
        }

        titlebar = findViewById(R.id.titlebar)
        mImageTid = findViewById(R.id.image_tid)
        mKgeTid = findViewById(R.id.kge_tid)
        mAudioTid = findViewById(R.id.audio_tid)
        recyclerView = findViewById(R.id.recycler_view)
        commentTv = findViewById(R.id.comment_tv)
        imageIv = findViewById(R.id.image_iv)
        audioIv = findViewById(R.id.audio_iv)
        kgeIv = findViewById(R.id.kge_iv)
        progressView = findViewById(R.id.progress_view)
        feedsInputContainerView = findViewById(R.id.feeds_input_container_view)
        smartRefreshLayout = findViewById(R.id.smart_refresh)
        smartRefreshLayout.setEnableLoadMore(false)
        smartRefreshLayout.setEnableRefresh(false)

        smartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                mPostsDetailPresenter?.getPostsFirstLevelCommentList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        feedsInputContainerView?.mSendCallBack = { replyModel, obj ->
            progressView.visibility = View.VISIBLE
            beginUploadTask(replyModel, obj)
            feedsInputContainerView?.hideSoftInput()
            feedsInputContainerView?.visibility = View.GONE
        }

        mPostsDetailPresenter = PostsDetailPresenter(this)
        addPresent(mPostsDetailPresenter)

        postsAdapter = PostsCommentAdapter(this)
        postsAdapter?.mIDetailClickListener = object : PostsCommentAdapter.IDetailClickListener {
            override fun replayPosts(model: PostsWatchModel) {
                feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostsWatchModel)
            }

            override fun likePosts(model: PostsWatchModel) {
                mPostsDetailPresenter?.likePosts(!model.isLiked!!, model)
            }

            override fun showRedPkg(model: PostsWatchModel) {
                postsRedPkgDialogView?.dismiss(false)
                postsRedPkgDialogView = PostsRedPkgDialogView(this@PostsDetailActivity, model?.posts?.redpacketInfo!!)
                postsRedPkgDialogView?.showByDialog()
            }

            override fun clickFirstLevelComment() {

            }

            override fun likeFirstLevelComment(model: PostFirstLevelCommentModel) {
                mPostsDetailPresenter?.likeFirstLevelComment(!model.isLiked, model)
            }

            override fun getCurPlayingUrl(): String {
                return mPlayingUrl
            }

            override fun getCurPlayingPosition(): Int {
                return mPlayingPosition
            }

            override fun setCurPlayingUrl(url: String) {
                mPlayingUrl = url
            }

            override fun setCurPlayintPosition(pos: Int) {
                mPlayingPosition = pos
            }

            override fun playAnotherSong() {
                stopPlayingState()
            }

            override fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int) {
                // 审核过，且未投票
                if (model != null && model.isAudit()) {
                    if (model.posts?.voteInfo?.hasVoted == true) {
                        // 已投票，不让投了
                    } else {
//                        recordClick(model)
                        mPostsDetailPresenter?.votePosts(position, model, index)
                    }
                } else {
                    U.getToastUtil().showShort("帖子审核完毕就可以互动啦～")
                }
            }

            override fun getRelation(userID: Int) {
                mPostsDetailPresenter?.getRelation(userID)
            }

            override fun goSecondLevelCommetDetail(model: PostFirstLevelCommentModel, position: Int) {
                ToSecondLevelDetail.position = position
                var url: String? = null
                if (position == mPlayingPosition) {
                    url = mPlayingUrl
                }

                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_COMMENT_DETAIL)
                        .withSerializable("postFirstLevelCommentModel", model)
                        .withSerializable("postsWatchModel", postsAdapter?.dataList?.get(0) as PostsWatchModel)
                        .withSerializable("playingUrl", url)
                        .navigation()
            }
        }

        postsAdapter?.mClickContent = { postFirstLevelModel ->
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsMoreDialogView(this@PostsDetailActivity, PostsMoreDialogView.FROM_POSTS_DETAIL, mPostsWatchModel!!).apply {
                replayArea.visibility = View.VISIBLE
                replayTv.setDebounceViewClickListener {
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, postFirstLevelModel)
                    feedsInputContainerView?.setETHint("回复 ${postFirstLevelModel.commentUser?.nicknameRemark}")
                    dismiss()
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = postsAdapter

        feedsInputContainerView?.mHideCallBack = {
            mImageTid.visibility = View.GONE
            mKgeTid.visibility = View.GONE
            mAudioTid.visibility = View.GONE

            if (it == PostsInputContainerView.SHOW_TYPE.IMG) {
                mImageTid.visibility = View.VISIBLE
            }

            if (it == PostsInputContainerView.SHOW_TYPE.KEG) {
                mKgeTid.visibility = View.VISIBLE
            }

            if (it == PostsInputContainerView.SHOW_TYPE.AUDIO) {
                mAudioTid.visibility = View.VISIBLE
            }
        }

        mPostsDetailPresenter?.getPostsDetail(mPostsID!!)
    }

    private fun stopPlayingState() {
        if (!TextUtils.isEmpty(mPlayingUrl)) {
            mPlayingUrl = ""
            postsAdapter?.notifyItemChanged(mPlayingPosition, PostsCommentDetailAdapter.REFRESH_PLAY_STATE)
            mPlayingPosition = -1
        }
    }

    override fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>) {
        if (postsAdapter?.dataList?.size == 0) {
            postsAdapter?.dataList?.add(mPostsWatchModel!!)
        }
        val startIndex = postsAdapter?.dataList?.size ?: 0
        postsAdapter?.dataList?.addAll(list)
        postsAdapter?.notifyItemRangeChanged(startIndex, postsAdapter?.dataList?.size ?: 0)
        smartRefreshLayout.finishLoadMore()
    }

    override fun showLikePostsResulet() {
//        postsAdapter?.notifyDataSetChanged()
        postsAdapter?.notifyItemChanged(0, PostsCommentAdapter.REFRESH_LIKE)
    }

    override fun showLikeFirstLevelCommentResult(postFirstLevelCommentModel: PostFirstLevelCommentModel) {
        postsAdapter?.notifyItemChanged(getListPosition(postFirstLevelCommentModel), PostsCommentAdapter.REFRESH_LIKE)
    }

    private fun getListPosition(model: PostFirstLevelCommentModel): Int {
        postsAdapter?.dataList?.forEachIndexed { index, it ->
            if (it is PostFirstLevelCommentModel) {
                if (it.comment?.commentID == model.comment?.commentID) {
                    return index
                }
            }
        }

        return -1
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSecondCommentEvent) {
        postsAdapter?.dataList?.forEachIndexed { index, any ->
            if (any is PostFirstLevelCommentModel) {
                if (any.comment?.commentID == event.firstLevelCommentID) {
                    if (any.secondLevelComments == null) {
                        any.secondLevelComments = mutableListOf()
                    }
                    (postsAdapter!!.dataList[0] as PostsWatchModel).numeric?.let {
                        it.commentCnt++
                    }
                    any.secondLevelComments?.add(0, event.model!!)
                    any.comment?.let {
                        it.subCommentCnt++
                    }
                    postsAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    override fun loadDetailDelete() {
        U.getToastUtil().showShort("帖子已经删除")
        finish()
    }

    override fun loadDetailError() {
        finish()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    override fun addFirstLevelCommentSuccess(model: PostFirstLevelCommentModel) {
        progressView.visibility = View.GONE
        (postsAdapter!!.dataList[0] as PostsWatchModel).numeric?.let {
            it.commentCnt++
        }
        postsAdapter!!.dataList?.add(1, model)
        feedsInputContainerView.onCommentSuccess()
        recyclerView?.scrollToPosition(1)
        postsAdapter?.notifyItemRangeChanged(1, 2)
    }

    override fun addSecondLevelCommentSuccess() {
        progressView.visibility = View.GONE
        (postsAdapter!!.dataList[0] as PostsWatchModel).numeric?.let {
            it.commentCnt++
        }
        postsAdapter!!.notifyDataSetChanged()
        feedsInputContainerView.onCommentSuccess()
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun addCommetFaild() {
        progressView.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (feedsInputContainerView.visibility == View.VISIBLE) {
            feedsInputContainerView.hideSoftInput()
            return
        }

        return super.onBackPressed()
    }

    override fun voteSuccess(position: Int) {
        postsAdapter?.notifyItemChanged(position, PostsCommentAdapter.REFRESH_VOTE)
    }

    override fun showRelation(isBlack: Boolean, isFollow: Boolean, isFriend: Boolean) {
        if (mPostsWatchModel?.relationShip == null) {
            mPostsWatchModel?.relationShip = PostsWatchModel.RelationShip()
            mPostsWatchModel?.relationShip?.isBlack = isBlack
            mPostsWatchModel?.relationShip?.isFollow = isFollow
            mPostsWatchModel?.relationShip?.isFriend = isFriend
            postsAdapter?.notifyItemChanged(0, PostsCommentAdapter.REFRESH_FOLLOW_STATE)
        }
    }

    override fun hasMore(hasMore: Boolean) {
        smartRefreshLayout.setEnableLoadMore(hasMore)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        mPostsWatchModel?.posts?.userID?.let {
            if (it == event.useId) {
                mPostsWatchModel?.relationShip?.isFollow = event.isFollow
                mPostsWatchModel?.relationShip?.isFriend = event.isFriend
                postsAdapter?.notifyItemChanged(0, PostsCommentAdapter.REFRESH_FOLLOW_STATE)
            }
        }
    }

    override fun showPostsWatchModel(model: PostsWatchModel) {
        mPostsWatchModel = model

        commentTv?.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostsWatchModel)
            feedsInputContainerView?.setETHint("回复")
        }

        imageIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.IMG, mPostsWatchModel)
            feedsInputContainerView?.setETHint("回复")
        }

        audioIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.AUDIO, mPostsWatchModel)
            feedsInputContainerView?.setETHint("回复")
        }

        kgeIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEG, mPostsWatchModel)
            feedsInputContainerView?.setETHint("回复")
        }

        titlebar.rightImageButton.setDebounceViewClickListener {
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsMoreDialogView(this@PostsDetailActivity, PostsMoreDialogView.FROM_POSTS_DETAIL, mPostsWatchModel!!).apply {
                replayArea.visibility = View.VISIBLE
                replayTv.setDebounceViewClickListener {
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostsWatchModel)
                    feedsInputContainerView?.setETHint("回复")
                    dismiss()
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }

        postsAdapter?.dataList?.let {
            it.add(model)
        }

        postsAdapter?.notifyDataSetChanged()
        mPostsDetailPresenter?.getPostsFirstLevelCommentList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
                feedsInputContainerView.onSelectImgOk(ResPicker.getInstance().selectedImageList)
                return
            }
        }
        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        if (ToSecondLevelDetail.position == null || ToSecondLevelDetail.position!! != mPlayingPosition) {
            SinglePlayer.stop(playerTag)
        }
        stopPlayingState()
    }

    override fun onResume() {
        super.onResume()
        ToSecondLevelDetail.position = null
    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().post(PostsDetailEvent(mPostsWatchModel))
        SinglePlayer.removeCallback(playerTag)
        postsMoreDialogView?.dismiss()
        postsRedPkgDialogView?.dismiss()
    }

    /**
     * 帖子回复相关操作
     */
    var uploading = false
    var hasFailedTask = false
    var replyModel: ReplyModel? = null
    var mObj: Any? = null

    val uploadQueue = object : ObjectPlayControlTemplate<PostsPublishActivity.PostsUploadModel, PostsDetailActivity>() {
        override fun accept(cur: PostsPublishActivity.PostsUploadModel): PostsDetailActivity? {
            if (uploading) {
                return null
            }
            uploading = true
            return this@PostsDetailActivity
        }

        override fun onStart(model: PostsPublishActivity.PostsUploadModel, consumer: PostsDetailActivity) {
            uploadToOss(model)
        }

        override fun onEnd(model: PostsPublishActivity.PostsUploadModel?) {
            uploadToOssEnd(model)
        }

    }

    fun beginUploadTask(model: ReplyModel, obj: Any?) {
        this.replyModel = model
        this.mObj = obj
        var needUploadToOss = false
        hasFailedTask = false
        //音频上传
        if (model.recordVoicePath?.isNotEmpty() == true && model.recordDurationMs > 0) {
            if (model?.recordVoiceUrl.isNullOrEmpty()) {
                needUploadToOss = true
                uploadQueue.add(PostsPublishActivity.PostsUploadModel(1, model.recordVoicePath), true)
            }
        }
        //图片上传
        if (!model.imgLocalPathList.isNullOrEmpty()) {
            for (local in model.imgLocalPathList) {
                if (!model.imgUploadMap.containsKey(local.path)) {
                    //没有上传
                    needUploadToOss = true
                    uploadQueue.add(PostsPublishActivity.PostsUploadModel(2, local.path), true)
                }
            }
        }
        if (!needUploadToOss) {
            uploadToServer()
        } else {
            progressView.visibility = View.VISIBLE
        }
    }

    fun uploadToOss(m: PostsPublishActivity.PostsUploadModel) {
        UploadParams.newBuilder(m.localPath)
                .setFileType(UploadParams.FileType.posts)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                    }

                    override fun onSuccessNotInUiThread(url: String?) {
                        if (m.type == 1) {
                            replyModel?.recordVoiceUrl = url
                        } else if (m.type == 2) {
                            replyModel?.imgUploadMap?.put(m.localPath!!, url!!)
                        }
                        uploading = false
                        uploadQueue.endCurrent(m)
                    }

                    override fun onFailureNotInUiThread(msg: String?) {
                        uploading = false
                        uploadQueue.endCurrent(null)
                        hasFailedTask = true
                    }

                })
    }

    private fun uploadToOssEnd(model: PostsPublishActivity.PostsUploadModel?) {
        if (!uploadQueue.hasMoreData()) {
            if (hasFailedTask) {
                U.getToastUtil().showShort("部分资源上传失败，请尝试重新上传")
                progressView.visibility = View.GONE
            } else {
                uploadToServer()
            }
        }
    }

    /**
     * 执行服务器请求
     */
    fun uploadToServer() {
        var hasData = false
        val map = HashMap<String, Any?>()
        if (replyModel?.recordVoiceUrl?.isNotEmpty() == true) {
            map["audios"] = listOf(mapOf(
                    "URL" to replyModel?.recordVoiceUrl,
                    "durTimeMs" to replyModel?.recordDurationMs
            ))
            hasData = true
        }
        if (replyModel?.imgUploadMap?.isNotEmpty() == true) {
            val l = ArrayList<String>()
            replyModel?.imgUploadMap?.values?.forEach {
                l.add(it)
            }
            map["pictures"] = l
            hasData = true
        }


        if (replyModel?.contentStr?.isNotEmpty() == true) {
            map["content"] = replyModel?.contentStr
            hasData = true
        }
        replyModel?.songId?.let {
            if (it > 0) {
                map["songID"] = it
                hasData = true
            }
        }

        if (!hasData) {
            U.getToastUtil().showShort("内容为空")
            progressView.visibility = View.GONE
            return
        }

        map["postsID"] = mPostsWatchModel?.posts?.postsID


        mObj?.let {
            if (it is PostFirstLevelCommentModel) {
                map["firstLevelCommentID"] = it.comment?.commentID
                map["replyedCommentID"] = it.comment?.commentID
            }
        }

        progressView.visibility = View.VISIBLE
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        mPostsDetailPresenter?.addComment(body, mObj)
        feedsInputContainerView.hideSoftInput()
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    //去二级页的时候记录标记
    object ToSecondLevelDetail {
        var position: Int? = null
    }
}

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
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.SkrProgressView
import com.dialog.view.TipsDialogView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.adapter.PostsCommentDetailAdapter.Companion.REFRESH_COMMENT_CTN
import com.module.posts.detail.event.DeteleFirstCommentEvent
import com.module.posts.detail.event.DeteleSecondCommentEvent
import com.module.posts.detail.inter.IPostsCommentDetailView
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.detail.presenter.PostsCommentDetailPresenter
import com.module.posts.detail.view.PostsInputContainerView
import com.module.posts.detail.view.ReplyModel
import com.module.posts.more.PostsCommentMoreDialogView
import com.module.posts.publish.PostsPublishActivity
import com.module.posts.watch.model.PostsWatchModel
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConstants.ACTIVITY_POSTS_COMMENT_DETAIL)
class PostsCommentDetailActivity : BaseActivity(), IPostsCommentDetailView {
    companion object {
        val playerTag = "PostsCommentDetailActivity"
    }

    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var imageIv: ExImageView
    lateinit var audioIv: ExImageView
    lateinit var kgeIv: ExImageView
    lateinit var mImageTid: ExImageView
    lateinit var mKgeTid: ExImageView
    lateinit var mAudioTid: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    lateinit var smartRefreshLayout: SmartRefreshLayout

    var postsAdapter: PostsCommentDetailAdapter? = null
    var postsMoreDialogView: PostsCommentMoreDialogView? = null

    var progressView: SkrProgressView?? = null

    var postsCommentDetailPresenter: PostsCommentDetailPresenter? = null

    /**
     * 帖子回复相关操作
     */
    var uploading = false
    var hasFailedTask = false
    var replyModel: ReplyModel? = null
    var mObj: Any? = null

    var mPlayingUrl = ""

    var mPlayingPosition = -1

    val uploadQueue = object : ObjectPlayControlTemplate<PostsPublishActivity.PostsUploadModel, PostsCommentDetailActivity>() {
        override fun accept(cur: PostsPublishActivity.PostsUploadModel): PostsCommentDetailActivity? {
            if (uploading) {
                return null
            }
            uploading = true
            return this@PostsCommentDetailActivity
        }

        override fun onStart(model: PostsPublishActivity.PostsUploadModel, consumer: PostsCommentDetailActivity) {
            uploadToOss(model)
        }

        override fun onEnd(model: PostsPublishActivity.PostsUploadModel?) {
            uploadToOssEnd(model)
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

    var mPostsWatchModel: PostsWatchModel? = null
    var mPostFirstLevelCommentModel: PostFirstLevelCommentModel? = null


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mPostFirstLevelCommentModel = intent.getSerializableExtra("postFirstLevelCommentModel") as PostFirstLevelCommentModel?
        mPostsWatchModel = intent.getSerializableExtra("postsWatchModel") as PostsWatchModel?

        if (mPostsWatchModel == null || mPostFirstLevelCommentModel == null) {
            finish()
            return
        }

        SinglePlayer.addCallback(playerTag, object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                MyLog.d(TAG, "onCompletion url is $mPlayingUrl")
                stopPlayingState()
            }

            override fun onError(what: Int, extra: Int) {
                super.onError(what, extra)
                MyLog.d(TAG, "onError url is $mPlayingUrl")
                stopPlayingState()
            }
        })

        (intent.getSerializableExtra("playingUrl") as String?)?.let {
            mPlayingUrl = it
            mPlayingPosition = 0
            SinglePlayer.startPlay(playerTag, it)
        }

        titlebar = findViewById(com.module.posts.R.id.titlebar)
        recyclerView = findViewById(com.module.posts.R.id.recycler_view)
        commentTv = findViewById(R.id.comment_tv)
        imageIv = findViewById(R.id.image_iv)
        audioIv = findViewById(R.id.audio_iv)
        kgeIv = findViewById(R.id.kge_iv)
        mImageTid = findViewById(R.id.image_tid)
        mKgeTid = findViewById(R.id.kge_tid)
        mAudioTid = findViewById(R.id.audio_tid)
        progressView = findViewById(R.id.progress_view)
        feedsInputContainerView = findViewById(com.module.posts.R.id.feeds_input_container_view)
        smartRefreshLayout = findViewById(com.module.posts.R.id.smart_refresh)
        smartRefreshLayout.setEnableLoadMore(true)
        smartRefreshLayout.setEnableRefresh(false)

        smartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                postsCommentDetailPresenter?.getPostsSecondLevelCommentList(mPostFirstLevelCommentModel?.comment?.commentID
                        ?: 0)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        titlebar.rightImageButton.setDebounceViewClickListener {
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsCommentMoreDialogView(this).apply {
                reportTv.setDebounceViewClickListener {
                    dismiss(false)
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_REPORT)
                            .withInt("from", PostsCommentMoreDialogView.FROM_POSTS_COMMENT)
                            .withInt("targetID", mPostFirstLevelCommentModel?.commentUser?.userId
                                    ?: 0)
                            .withLong("postsID", mPostsWatchModel?.posts?.postsID ?: 0)
                            .withLong("commentID", mPostFirstLevelCommentModel?.comment?.commentID?.toLong()
                                    ?: 0)
                            .navigation()
                }

                deleteTv.visibility = View.GONE

                replyTv.setDebounceViewClickListener {
                    dismiss(false)
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostFirstLevelCommentModel)
                    feedsInputContainerView?.setETHint("回复")
                }

                if (mPostFirstLevelCommentModel?.commentUser?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                    deleteTv.visibility = View.VISIBLE
                    deleteTv.setDebounceViewClickListener {
                        dismiss(false)
                        deleteConfirm {
                            postsCommentDetailPresenter?.deleteComment(mPostFirstLevelCommentModel?.comment?.commentID
                                    ?: 0, mPostsWatchModel?.posts?.postsID?.toInt() ?: 0, 0, null)
                            progressView?.visibility = View.VISIBLE
                        }
                    }
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }

        commentTv?.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostFirstLevelCommentModel)
            feedsInputContainerView?.setETHint("回复")
        }

        imageIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.IMG, mPostFirstLevelCommentModel)
            feedsInputContainerView?.setETHint("回复")
        }

        audioIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.AUDIO, mPostFirstLevelCommentModel)
            feedsInputContainerView?.setETHint("回复")
        }

        kgeIv.setDebounceViewClickListener {
            feedsInputContainerView?.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEG, mPostFirstLevelCommentModel)
            feedsInputContainerView?.setETHint("回复")
        }

        postsCommentDetailPresenter = PostsCommentDetailPresenter(mPostsWatchModel!!.posts!!, this)

        postsAdapter = PostsCommentDetailAdapter(this, mPostsWatchModel?.user?.userId)
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

        postsAdapter?.mIDetailClickListener = object : PostsCommentDetailAdapter.ICommentDetailClickListener {
            override fun getCurPlayingUrl(): String {
                return mPlayingUrl
            }

            override fun setCurPlayingPosition(pos: Int) {
                mPlayingPosition = pos
            }

            override fun clickSecondLevelCommentContent(postsCommentModel: PostsSecondLevelCommentModel, pos: Int) {
                postsMoreDialogView?.dismiss(false)
                postsMoreDialogView = PostsCommentMoreDialogView(this@PostsCommentDetailActivity).apply {
                    reportTv.setDebounceViewClickListener {
                        dismiss(false)
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_REPORT)
                                .withInt("from", PostsCommentMoreDialogView.FROM_POSTS_COMMENT)
                                .withInt("targetID", postsCommentModel?.commentUser?.userId
                                        ?: 0)
                                .withLong("postsID", mPostsWatchModel?.posts?.postsID ?: 0)
                                .withLong("commentID", postsCommentModel?.comment?.commentID?.toLong()
                                        ?: 0)
                                .navigation()
                    }

                    deleteTv.visibility = View.GONE

                    replyTv.setDebounceViewClickListener {
                        dismiss(false)
                        feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, postsCommentModel)
                        feedsInputContainerView?.setETHint("回复 ${postsCommentModel.commentUser.nicknameRemark}")
                    }

                    if (postsCommentModel?.commentUser?.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                        deleteTv.visibility = View.VISIBLE
                        deleteTv.setDebounceViewClickListener {
                            dismiss(false)
                            deleteConfirm {
                                postsCommentDetailPresenter?.deleteComment(postsCommentModel?.comment?.commentID
                                        ?: 0, mPostsWatchModel?.posts?.postsID?.toInt()
                                        ?: 0, pos, postsCommentModel)
                                progressView?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                postsMoreDialogView?.showByDialog(true)
            }

            override fun goBigImageBrowse(index: Int, pictures: List<String>) {
                goBrowse(index, pictures)
            }

            override fun stopPlay() {
                mPlayingPosition = -1
                mPlayingUrl = ""
                SinglePlayer.stop(playerTag)
            }

            override fun startPlay(url: String, pos: Int) {
                stopPlayingState()
                mPlayingPosition = pos
                mPlayingUrl = url
                SinglePlayer.startPlay(playerTag, mPlayingUrl)
            }
        }

        feedsInputContainerView?.mSendCallBack = { replyModel, obj ->
            progressView?.visibility = View.VISIBLE
            beginUploadTask(replyModel, obj)
            feedsInputContainerView?.hideSoftInput()
            feedsInputContainerView?.visibility = View.GONE
        }

        feedsInputContainerView?.toStopPlayCall = {
            SinglePlayer.stop(playerTag)
            stopPlayingState()
        }

        postsAdapter?.dataList?.add(mPostFirstLevelCommentModel!!)
        postsAdapter?.notifyItemInserted(0)

        postsCommentDetailPresenter?.getPostsSecondLevelCommentList(mPostFirstLevelCommentModel?.comment?.commentID
                ?: 0)
    }

    private fun uploadToOssEnd(model: PostsPublishActivity.PostsUploadModel?) {
        if (!uploadQueue.hasMoreData()) {
            if (hasFailedTask) {
                U.getToastUtil().showShort("部分资源上传失败，请尝试重新上传")
                progressView?.visibility = View.GONE
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
            progressView?.visibility = View.GONE
            return
        }

        map["postsID"] = mPostsWatchModel?.posts?.postsID

        map["firstLevelCommentID"] = mPostFirstLevelCommentModel?.comment?.commentID

        mObj?.let {
            if (it is PostFirstLevelCommentModel) {
                map["replyedCommentID"] = it.comment?.commentID
            } else if (it is PostsSecondLevelCommentModel) {
                map["replyedCommentID"] = it.comment?.commentID
            }
        }

        progressView?.visibility = View.VISIBLE
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        postsCommentDetailPresenter?.addComment(body, mObj)
        feedsInputContainerView.hideSoftInput()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
                feedsInputContainerView.onSelectImgOk(ResPicker.getInstance().selectedImageList)
            }
        }
    }

    private fun stopPlayingState() {
        if (!TextUtils.isEmpty(mPlayingUrl)) {
            mPlayingUrl = ""
            postsAdapter?.notifyItemChanged(mPlayingPosition, PostsCommentDetailAdapter.REFRESH_PLAY_STATE)
            mPlayingPosition = -1
        }
    }

    override fun addCommetFaild() {
        progressView?.visibility = View.GONE
    }

    override fun getFirstLevelCommentID(): Int {
        return mPostFirstLevelCommentModel?.comment?.commentID ?: 0
    }

    override fun hasMore(hasMore: Boolean) {
        smartRefreshLayout.setEnableLoadMore(hasMore)
    }

    private fun goBrowse(index: Int, pictures: List<String>) {
        BigImageBrowseFragment.open(true, this, object : DefaultImageBrowserLoader<String>() {
            override fun init() {

            }

            override fun load(imageBrowseView: ImageBrowseView, position: Int, item: String) {
                imageBrowseView.load(item)
            }

            override fun getInitCurrentItemPostion(): Int {
                return index
            }

            override fun getInitList(): List<String>? {
                return pictures
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

        })
    }

    override fun deleteCommentSuccess(success: Boolean, pos: Int, model: PostsSecondLevelCommentModel?) {
        progressView?.visibility = View.GONE
        if (success) {
            if (pos == 0) {
                EventBus.getDefault().post(DeteleFirstCommentEvent(mPostFirstLevelCommentModel!!))
                finish()
            } else {
                EventBus.getDefault().post(DeteleSecondCommentEvent(model, mPostFirstLevelCommentModel?.comment?.commentID
                        ?: 0))
                (postsAdapter!!.dataList[0] as PostFirstLevelCommentModel).comment?.let {
                    if (it.subCommentCnt > 0) {
                        it.subCommentCnt--
                    }
                }

                if (pos == mPlayingPosition) {
                    stopPlayingState()
                    SinglePlayer.stop(playerTag)
                }

                postsAdapter?.dataList?.removeAt(pos)
                postsAdapter!!.notifyItemRemoved(pos)
                postsAdapter!!.notifyItemRangeChanged(pos, postsAdapter?.dataList?.size!! - pos)
                postsAdapter!!.notifyItemChanged(0, REFRESH_COMMENT_CTN)

            }
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
            progressView?.visibility = View.VISIBLE
        }
    }

    override fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>) {
        if (postsAdapter?.dataList?.size == 0) {
            postsAdapter?.dataList?.add(mPostFirstLevelCommentModel!!)
        }
        var startIndex = postsAdapter?.dataList?.size ?: 1
        postsAdapter?.dataList?.addAll(list)
        postsAdapter?.notifyItemRangeInserted(startIndex, list.size)
        if (mPostFirstLevelCommentModel?.comment?.subCommentCnt ?: 0 < list.size) {
            mPostFirstLevelCommentModel?.comment?.subCommentCnt = list.size
        }

        smartRefreshLayout.finishLoadMore()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    var mTipsDialogView: TipsDialogView? = null

    private fun deleteConfirm(call: (() -> Unit)?) {
        mTipsDialogView = TipsDialogView.Builder(this@PostsCommentDetailActivity)
                    .setMessageTip("是否确定删除该评论")
                    .setConfirmTip("确认删除")
                    .setCancelTip("取消")
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            mTipsDialogView?.dismiss()
                        }
                    })
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            mTipsDialogView?.dismiss(false)
                            call?.invoke()
                        }
                    })
                    .build()
            mTipsDialogView?.showByDialog()
    }

    override fun addSecondLevelCommentSuccess(model: PostsSecondLevelCommentModel) {
        mPostFirstLevelCommentModel?.comment?.let {
            it.subCommentCnt++
        }
        postsAdapter?.dataList?.add(1, model)
        postsAdapter?.notifyItemInserted(1)
        if (postsAdapter?.dataList?.size == 2) {
            postsAdapter?.notifyDataSetChanged()
        } else {
            postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
            recyclerView?.scrollToPosition(1)
            val count = postsAdapter?.dataList!!.size - 1
            postsAdapter?.notifyItemRangeChanged(1, count, PostsCommentDetailAdapter.REFRESH_POSITION)
        }
        progressView?.visibility = View.GONE
        feedsInputContainerView.onCommentSuccess()

        //这个需要手动更新位置
        if (!TextUtils.isEmpty(mPlayingUrl)) {
            if (mPlayingPosition >= 1) {
                mPlayingPosition++
            }
        }
    }

    private var beginContentTs = 0L

    override fun onPause() {
        super.onPause()
        SinglePlayer.stop(playerTag)
        stopPlayingState()
        StatisticsAdapter.recordCalculateEvent("posts", "commentpage_duration", System.currentTimeMillis() - beginContentTs, null)
    }

    override fun onResume() {
        super.onResume()
        beginContentTs = System.currentTimeMillis()
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.removeCallback(playerTag)
        postsMoreDialogView?.dismiss()
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

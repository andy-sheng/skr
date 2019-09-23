package com.module.posts.detail.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.ObjectPlayControlTemplate
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.player.SinglePlayer
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
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.adapter.PostsCommentDetailAdapter.Companion.DESTROY_HOLDER
import com.module.posts.detail.adapter.PostsCommentDetailAdapter.Companion.REFRESH_COMMENT_CTN
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
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody


class PostsCommentDetailFragment : BaseFragment(), IPostsCommentDetailView {
    lateinit var titlebar: CommonTitleBar
    lateinit var recyclerView: RecyclerView
    lateinit var commentTv: ExTextView
    lateinit var imageIv: ExImageView
    lateinit var audioIv: ExImageView
    lateinit var kgeIv: ExImageView
    lateinit var feedsInputContainerView: PostsInputContainerView
    lateinit var smartRefreshLayout: SmartRefreshLayout
    var mPostsWatchModel: PostsWatchModel? = null
    var mPostFirstLevelCommentModel: PostFirstLevelCommentModel? = null
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

    val uploadQueue = object : ObjectPlayControlTemplate<PostsPublishActivity.PostsUploadModel, PostsCommentDetailFragment>() {
        override fun accept(cur: PostsPublishActivity.PostsUploadModel): PostsCommentDetailFragment? {
            if (uploading) {
                return null
            }
            uploading = true
            return this@PostsCommentDetailFragment
        }

        override fun onStart(model: PostsPublishActivity.PostsUploadModel, consumer: PostsCommentDetailFragment) {
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

    override fun onActivityResultReal(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
                feedsInputContainerView.onSelectImgOk(ResPicker.getInstance().selectedImageList)
                return true
            }
        }
        return super.onActivityResultReal(requestCode, resultCode, data)
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
                    "duration" to replyModel?.recordDurationMs
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
        if (!hasData) {
            U.getToastUtil().showShort("内容为空")
            return
        }

        map["postsID"] = mPostsWatchModel?.posts?.postsID
//        map["songID"] = ""

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

    override fun initView(): Int {
        return R.layout.posts_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mPostsWatchModel == null || mPostFirstLevelCommentModel == null) {
            activity?.finish()
            return
        }

        titlebar = rootView.findViewById(com.module.posts.R.id.titlebar)
        recyclerView = rootView.findViewById(com.module.posts.R.id.recycler_view)
        commentTv = rootView.findViewById(R.id.comment_tv)
        imageIv = rootView.findViewById(R.id.image_iv)
        audioIv = rootView.findViewById(R.id.audio_iv)
        kgeIv = rootView.findViewById(R.id.kge_iv)
        progressView = rootView.findViewById(R.id.progress_view)
        feedsInputContainerView = rootView.findViewById(com.module.posts.R.id.feeds_input_container_view)
        smartRefreshLayout = rootView.findViewById(com.module.posts.R.id.smart_refresh)
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
            activity?.finish()
        }

        titlebar.rightImageButton.setDebounceViewClickListener {
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsCommentMoreDialogView(activity as FragmentActivity).apply {
                reportTv.setDebounceViewClickListener {
                    dismiss(false)
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_REPORT)
                            .withInt("from", PostsCommentMoreDialogView.FROM_POSTS_COMMENT)
                            .withInt("targetID", mPostsWatchModel?.user?.userId ?: 0)
                            .withLong("postsID", mPostsWatchModel?.posts?.postsID ?: 0)
                            .withLong("commentID", mPostFirstLevelCommentModel?.comment?.commentID?.toLong()
                                    ?: 0)
                            .navigation()
                }

                deleteTv.visibility = View.GONE

                replyTv.setDebounceViewClickListener {
                    dismiss(false)
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostFirstLevelCommentModel)
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }

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

        postsCommentDetailPresenter = PostsCommentDetailPresenter(mPostsWatchModel!!.posts!!, this)

        postsAdapter = PostsCommentDetailAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        postsAdapter?.mClickContentListener = { postsCommentModel ->
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsCommentMoreDialogView(activity as FragmentActivity).apply {
                reportTv.setDebounceViewClickListener {
                    dismiss(false)
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_REPORT)
                            .withInt("from", PostsCommentMoreDialogView.FROM_POSTS_COMMENT)
                            .withInt("targetID", mPostsWatchModel?.user?.userId ?: 0)
                            .withLong("postsID", mPostsWatchModel?.posts?.postsID ?: 0)
                            .withLong("commentID", postsCommentModel?.comment?.commentID?.toLong()
                                    ?: 0)
                            .navigation()
                }

                deleteTv.visibility = View.GONE

                replyTv.setDebounceViewClickListener {
                    dismiss(false)
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, postsCommentModel)
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }

        feedsInputContainerView?.mSendCallBack = { replyModel, obj ->
            progressView?.visibility = View.VISIBLE
            beginUploadTask(replyModel, obj)
            feedsInputContainerView?.hideSoftInput()
            feedsInputContainerView?.mInputContainer?.visibility = View.GONE
        }

        postsCommentDetailPresenter?.getPostsSecondLevelCommentList(mPostFirstLevelCommentModel?.comment?.commentID
                ?: 0)
    }

    override fun addCommetFaild() {
        progressView?.visibility = View.GONE
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

    override fun useEventBus(): Boolean {
        return false
    }

    override fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostFirstLevelCommentModel!!)
        modelList.addAll(list)
        postsAdapter?.mCommentCtn = list.size
        postsAdapter?.dataList = modelList
        launch {
            kotlinx.coroutines.delay(100)
            postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
        }
        smartRefreshLayout.setEnableLoadMore(hasMore)
        smartRefreshLayout.finishLoadMore()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    override fun addSecondLevelCommentSuccess() {
        postsAdapter!!.mCommentCtn++
        progressView?.visibility = View.GONE
        feedsInputContainerView.onCommentSuccess()
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun setData(type: Int, data: Any?) {
        if (type == 1) {
            mPostsWatchModel = data as PostsWatchModel?
        } else if (type == 0) {
            mPostFirstLevelCommentModel = data as PostFirstLevelCommentModel?
        }
    }

    override fun onPause() {
        super.onPause()
        SinglePlayer.stop(PostsCommentDetailAdapter.playerTag)
    }

    override fun destroy() {
        super.destroy()
        postsAdapter?.notifyItemChanged(0, DESTROY_HOLDER)
        SinglePlayer.removeCallback(PostsCommentDetailAdapter.playerTag)
        postsMoreDialogView?.dismiss()
    }
}
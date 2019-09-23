package com.module.posts.detail.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
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
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.detail.adapter.PostsCommentAdapter.Companion.DESTROY_HOLDER
import com.module.posts.detail.adapter.PostsCommentAdapter.Companion.REFRESH_COMMENT_CTN
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody


class PostsDetailFragment : BaseFragment(), IPostsDetailView {
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

    var postsAdapter: PostsCommentAdapter? = null

    override fun initView(): Int {
        return R.layout.posts_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mPostsID == null) {
            activity?.finish()
            return
        }

        titlebar = rootView.findViewById(R.id.titlebar)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        commentTv = rootView.findViewById(R.id.comment_tv)
        imageIv = rootView.findViewById(R.id.image_iv)
        audioIv = rootView.findViewById(R.id.audio_iv)
        kgeIv = rootView.findViewById(R.id.kge_iv)
        progressView = rootView.findViewById(R.id.progress_view)
        feedsInputContainerView = rootView.findViewById(R.id.feeds_input_container_view)
        smartRefreshLayout = rootView.findViewById(R.id.smart_refresh)
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
            activity?.finish()
        }

        feedsInputContainerView?.mSendCallBack = { replyModel, obj ->
            beginUploadTask(replyModel, obj)
        }

        mPostsDetailPresenter = PostsDetailPresenter(this)
        addPresent(mPostsDetailPresenter)

        postsAdapter = PostsCommentAdapter()
        postsAdapter?.mIDetailClickListener = object : PostsCommentAdapter.IDetailClickListener {
            override fun replayPosts(model: PostsWatchModel) {
                feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostsWatchModel)
            }

            override fun likePosts(model: PostsWatchModel) {
                mPostsDetailPresenter?.likePosts(!model.isLiked!!, model)
            }

            override fun showRedPkg(model: PostsWatchModel) {
                postsRedPkgDialogView?.dismiss(false)
                postsRedPkgDialogView = PostsRedPkgDialogView(activity as FragmentActivity, model?.posts?.redpacketInfo!!)
                postsRedPkgDialogView?.showByDialog()
            }

            override fun clickFirstLevelComment() {

            }

            override fun likeFirstLevelComment(model: PostFirstLevelCommentModel) {
                mPostsDetailPresenter?.likeFirstLevelComment(!model.isLiked, model)
            }
        }

        postsAdapter?.mClickContent = { postFirstLevelModel ->
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsMoreDialogView(activity, PostsMoreDialogView.FROM_POSTS_DETAIL, mPostsWatchModel!!).apply {
                replayArea.visibility = View.VISIBLE
                replayTv.setDebounceViewClickListener {
                    feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, postFirstLevelModel)
                    dismiss()
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = postsAdapter

        mPostsDetailPresenter?.getPostsDetail(mPostsID!!)
    }

    override fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>, hasMore: Boolean) {
        val modelList: MutableList<Any> = mutableListOf(mPostsWatchModel!!)
        modelList.addAll(list)
        postsAdapter?.mCommentCtn = list.size
        postsAdapter?.dataList = modelList
        launch {
            delay(100)
            postsAdapter?.notifyItemChanged(0, REFRESH_COMMENT_CTN)
        }
        smartRefreshLayout.setEnableLoadMore(hasMore)
        smartRefreshLayout.finishLoadMore()
    }

    override fun showLikePostsResulet() {
        postsAdapter?.notifyDataSetChanged()
    }

    override fun showLikeFirstLevelCommentResult(postFirstLevelCommentModel: PostFirstLevelCommentModel) {
        postsAdapter?.notifyDataSetChanged()
    }

    override fun loadMoreError() {
        smartRefreshLayout.finishLoadMore()
    }

    override fun addFirstLevelCommentSuccess() {
        progressView.visibility = View.GONE
        postsAdapter!!.mCommentCtn++
    }

    override fun addSecondLevelCommentSuccess() {
        progressView.visibility = View.GONE
        postsAdapter!!.notifyDataSetChanged()
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun useEventBus(): Boolean {
        return false
    }

    override fun addCommetFaild() {
        progressView.visibility = View.GONE
    }

    override fun onBackPressed(): Boolean {
        if (feedsInputContainerView.mInputContainer?.visibility == View.VISIBLE) {
            feedsInputContainerView.hideSoftInput()
            return true
        }

        return super.onBackPressed()
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
            activity?.let {
                postsMoreDialogView?.dismiss(false)
                postsMoreDialogView = PostsMoreDialogView(it, PostsMoreDialogView.FROM_POSTS_DETAIL, mPostsWatchModel!!).apply {
                    replayArea.visibility = View.VISIBLE
                    replayTv.setDebounceViewClickListener {
                        feedsInputContainerView.showSoftInput(PostsInputContainerView.SHOW_TYPE.KEY_BOARD, mPostsWatchModel)
                        dismiss()
                    }
                }
                postsMoreDialogView?.showByDialog(true)
            }
        }

        mPostsDetailPresenter?.getPostsFirstLevelCommentList()
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

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mPostsID = data as Int?
        }
    }

    override fun onPause() {
        super.onPause()
        SinglePlayer.stop(PostsCommentAdapter.playerTag)
    }

    override fun destroy() {
        super.destroy()
        postsAdapter?.notifyItemChanged(0, DESTROY_HOLDER)
        SinglePlayer.removeCallback(PostsCommentAdapter.playerTag)
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

    val uploadQueue = object : ObjectPlayControlTemplate<PostsPublishActivity.PostsUploadModel, PostsDetailFragment>() {
        override fun accept(cur: PostsPublishActivity.PostsUploadModel): PostsDetailFragment? {
            if (uploading) {
                return null
            }
            uploading = true
            return this@PostsDetailFragment
        }

        override fun onStart(model: PostsPublishActivity.PostsUploadModel, consumer: PostsDetailFragment) {
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
        map["songID"] = replyModel?.songId

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
}
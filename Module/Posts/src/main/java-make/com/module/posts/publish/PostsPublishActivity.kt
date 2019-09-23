package com.module.posts.publish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.ObjectPlayControlTemplate
import com.common.base.BaseActivity
import com.common.core.scheme.event.JumpHomeFromSchemeEvent
import com.common.core.view.setDebounceViewClickListener
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.event.FeedSongMakeSucessEvent
import com.component.busilib.event.PostsPublishSucessEvent
import com.component.busilib.view.SkrProgressView
import com.dialog.view.TipsDialogView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.img.PostsPublishImgAdapter
import com.module.posts.publish.redpkg.PostsRedPkgEditActivity
import com.module.posts.publish.redpkg.RedPkgModel
import com.module.posts.publish.topic.PostsTopicSelectActivity
import com.module.posts.publish.topic.Topic
import com.module.posts.publish.voice.PostsVoiceRecordActivity
import com.module.posts.publish.vote.PostsVoteEditActivity
import com.module.posts.view.PostsAudioView
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.respicker.model.ImageItem
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_POSTS_PUBLISH)
class PostsPublishActivity : BaseActivity() {

    var playerTag = TAG + hashCode()

    lateinit var mainActContainer: ConstraintLayout
    lateinit var titleBar: CommonTitleBar
    lateinit var topicTv: ExTextView
    lateinit var contentEt: NoLeakEditText
    lateinit var imageRecyclerView: RecyclerView
    lateinit var postsAudioView: PostsAudioView
    lateinit var audioDelIv: ImageView
    lateinit var redPkgVp: ConstraintLayout
    lateinit var redPkgIv: ImageView
    lateinit var redPkgTv: TextView
    lateinit var redPkgValidTv: TextView
    lateinit var redPkgDelIv: ImageView
    lateinit var voteVp: ExConstraintLayout
    lateinit var voteItem1Tv: ExTextView
    lateinit var voteItem2Tv: ExTextView
    lateinit var voteItem3Tv: ExTextView
    lateinit var voteItem4Tv: ExTextView
    lateinit var voteDelIv: ImageView
    lateinit var menuKtvIv: ImageView
    lateinit var menuDivideLine2: View
    lateinit var menuPicIv: ImageView
    lateinit var menuMicIv: ImageView
    lateinit var menuVoteIv: ImageView
    lateinit var menuRedPkgIv: ImageView
    lateinit var progressView: SkrProgressView

    lateinit var postsPublishImgAdapter: PostsPublishImgAdapter

    lateinit var model: PostsPublishModel

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_publish_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = PostsPublishModel()
        val topic = intent.getSerializableExtra("topic") as Topic?
        model.topic = topic
        mainActContainer = findViewById(R.id.main_act_container)
        titleBar = findViewById(R.id.title_bar)
        topicTv = findViewById(R.id.topic_tv)
        contentEt = findViewById(R.id.content_et)
        imageRecyclerView = findViewById(R.id.image_recycler_view)
        postsAudioView = findViewById(R.id.posts_audio_view)
        audioDelIv = findViewById(R.id.audio_del_iv)
        redPkgVp = findViewById(R.id.red_pkg_vp)
        redPkgIv = findViewById(R.id.red_pkg_iv)
        redPkgTv = findViewById(R.id.red_pkg_tv)
        redPkgValidTv = findViewById(R.id.red_pkg_valid_tv)
        redPkgDelIv = findViewById(R.id.red_pkg_del_iv)
        voteVp = findViewById(R.id.vote_vp)
        voteItem1Tv = findViewById(R.id.vote_item1_tv)
        voteItem2Tv = findViewById(R.id.vote_item2_tv)
        voteItem3Tv = findViewById(R.id.vote_item3_tv)
        voteItem4Tv = findViewById(R.id.vote_item4_tv)
        voteDelIv = findViewById(R.id.vote_del_iv)
        menuKtvIv = findViewById(R.id.menu_ktv_iv)
        menuDivideLine2 = findViewById(R.id.menu_divide_line2)
        menuPicIv = findViewById(R.id.menu_pic_iv)
        menuMicIv = findViewById(R.id.menu_mic_iv)
        menuVoteIv = findViewById(R.id.menu_vote_iv)
        menuRedPkgIv = findViewById(R.id.menu_red_pkg_iv)
        progressView = findViewById(R.id.progress_view)

        imageRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postsPublishImgAdapter = PostsPublishImgAdapter()
        imageRecyclerView.adapter = postsPublishImgAdapter
        postsPublishImgAdapter.delClickListener = { m, pos ->
            var index = 0
            for (v in postsPublishImgAdapter.dataList) {
                if (m!! == v) {
                    break
                }
                index++
            }
            model.imgUploadMap.remove(m?.path)
            postsPublishImgAdapter.dataList.removeAt(index)
            postsPublishImgAdapter.notifyItemRemoved(index)
            if (postsPublishImgAdapter.dataList.isEmpty()) {
                imageRecyclerView.visibility = View.GONE
            }
        }
        postsPublishImgAdapter.addClickListener = {
            goAddImagePage()
        }
        postsPublishImgAdapter.imgClickListener = { _, pos ->

            BigImageBrowseFragment.open(true, this@PostsPublishActivity, object : DefaultImageBrowserLoader<ImageItem>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: ImageItem) {
                    imageBrowseView.load(item.path)
                }

                override fun getInitCurrentItemPostion(): Int {
                    return pos
                }

                override fun getInitList(): List<ImageItem>? {
                    return postsPublishImgAdapter.dataList
                }
            })
        }
        menuPicIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                goAddImagePage()
            }
        })

        menuMicIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                goMicRecordPage()
            }
        })
        menuVoteIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_VOTE_EDIT)
                        .withSerializable("model", model)
                        .navigation(this@PostsPublishActivity, PostsVoteEditActivity.REQ_CODE_VOTE_EDIT)
            }
        })
        menuRedPkgIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_RED_PKG_EDIT)
                        .withSerializable("model", model)
                        .navigation(this@PostsPublishActivity, PostsRedPkgEditActivity.REQ_CODE_RED_PKG_EDIT)
            }
        })
        menuKtvIv.setDebounceViewClickListener {
            goKgeRecordPage()

        }
        topicTv.setDebounceViewClickListener {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_TOPIC_SELECT)
                    .withInt("from", 2)
                    .navigation(this@PostsPublishActivity, PostsTopicSelectActivity.REQ_CODE_TOPIC_SELECT)
        }
        audioDelIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                model.recordVoicePath = null
                model.recordDurationMs = 0
                model.recordVoiceUrl = null
                model.songId = 0
                SinglePlayer.stop(playerTag)
                postsAudioView.visibility = View.GONE
                audioDelIv.visibility = View.GONE
            }
        })

        postsAudioView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (postsAudioView.isPlaying) {
                    SinglePlayer.stop(playerTag)
                    postsAudioView.setPlay(false)
                } else {
                    SinglePlayer.startPlay(playerTag, model.recordVoicePath ?: "")
                    postsAudioView.setPlay(true)
                }
            }
        })
        SinglePlayer.addCallback(playerTag, object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                postsAudioView.setPlay(false)
            }
        })
        voteDelIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                voteVp.visibility = View.GONE
                voteDelIv.visibility = View.GONE
                model.voteList.clear()
            }
        })

        redPkgDelIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                redPkgDelIv.visibility = View.GONE
                redPkgVp.visibility = View.GONE
                model.redPkg = null
            }
        })
        titleBar.rightTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                beginUploadTask()
            }
        })
        titleBar.leftImageButton.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })
        if (model.topic != null) {
            topicTv.text = model.topic?.topicDesc
        }

    }

    var uploading = false
    var hasFailedTask = false

    val uploadQueue = object : ObjectPlayControlTemplate<PostsUploadModel, PostsPublishActivity>() {
        override fun accept(cur: PostsUploadModel): PostsPublishActivity? {
            if (uploading) {
                return null
            }
            uploading = true
            return this@PostsPublishActivity
        }

        override fun onStart(model: PostsUploadModel, consumer: PostsPublishActivity) {
            uploadToOss(model)
        }

        override fun onEnd(model: PostsUploadModel?) {
            uploadToOssEnd(model)
        }

    }

    class PostsUploadModel {
        constructor(type: Int, localPath: String?) {
            this.type = type
            this.localPath = localPath
        }

        var type = 0 // 1为音频 2为图片
        var localPath: String? = null
    }

    fun uploadToOss(m: PostsUploadModel) {
        UploadParams.newBuilder(m.localPath)
                .setFileType(UploadParams.FileType.posts)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                    }

                    override fun onSuccessNotInUiThread(url: String?) {
                        if (m.type == 1) {
                            model.recordVoiceUrl = url
                        } else if (m.type == 2) {
                            model.imgUploadMap.put(m.localPath!!, url!!)
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

    private fun uploadToOssEnd(model: PostsUploadModel?) {
        if (!uploadQueue.hasMoreData()) {
            if (hasFailedTask) {
                U.getToastUtil().showShort("部分资源上传失败，请尝试重新上传")
                progressView.visibility = View.GONE
            } else {
                uploadToServer()
            }
        }
    }

    fun beginUploadTask() {
        if (model.topic == null) {
            U.getToastUtil().showShort("请选择一个话题")
            return
        }
        if(contentEt.text.toString().isEmpty()){
            U.getToastUtil().showShort("标题不能为空")
            return
        }
        var needUploadToOss = false
        hasFailedTask = false
        //音频上传
        if (model.recordVoicePath?.isNotEmpty() == true && model.recordDurationMs > 0 && model.songId <= 0) {
            if (model?.recordVoiceUrl.isNullOrEmpty()) {
                needUploadToOss = true
                uploadQueue.add(PostsUploadModel(1, model.recordVoicePath), true)
            }
        }
        //图片上传
        if (!postsPublishImgAdapter.dataList.isNullOrEmpty()) {
            for (local in postsPublishImgAdapter.dataList) {
                if (!model.imgUploadMap.containsKey(local.path)) {
                    //没有上传
                    needUploadToOss = true
                    uploadQueue.add(PostsUploadModel(2, local.path), true)
                }
            }
        }
        if (!needUploadToOss) {
            uploadToServer()
        } else {
            progressView.visibility = View.VISIBLE
        }
    }

    fun uploadToServer() {
        var hasData = false
        val map = HashMap<String, Any>()
        val hasSong = model.songId > 0
        if (!hasSong && (model.recordVoiceUrl?.isNotEmpty() == true)) {
            map["audios"] = listOf(mapOf(
                    "URL" to model.recordVoiceUrl,
                    "durTimeMs" to model.recordDurationMs
            ))
            hasData = true
        }
        if (model.imgUploadMap.isNotEmpty()) {
            val l = ArrayList<String>()
            model.imgUploadMap.values.forEach {
                l.add(it)
            }
            map["pictures"] = l
            hasData = true
        }
        model.redPkg?.let {
            map["redpacket"] = mapOf(
                    "redpacketNum" to it.redpacketNum,
                    "redpacketType" to it.redpacketType
            )
        }
        if (model.voteList.isNotEmpty()) {
            map["vote"] = model.voteList
            hasData = true
        }

        map["topicID"] = model.topic?.topicID?:0

        map["songID"] = model.songId
        val content = contentEt.text.toString()
        if (content.isNotEmpty()) {
            map["title"] = content
            hasData = true
        }
        if (!hasData) {
            U.getToastUtil().showShort("内容为空")
            return
        }
        progressView.visibility = View.VISIBLE
        launch {
            val api = ApiManager.getInstance().createService(PostsPublishServerApi::class.java)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { api.uploadPosts(body) }

            progressView.visibility = View.GONE
            if (result.errno == 0) {
                U.getToastUtil().showShort("上传成功")
                finish()
                EventBus.getDefault().post(PostsPublishSucessEvent())
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    var tipsDialogView: TipsDialogView? = null

    fun goAddImagePage() {
        val hasSong = model.songId > 0
        val hasAudio = !hasSong && (model.recordVoicePath?.isNotEmpty() == true)
        val hasImg = postsPublishImgAdapter.dataList.isNotEmpty()

        if (hasAudio || hasSong) {
            var tips: String? = null
            if (hasAudio) {
                tips = "上传图片将清空语音,是否继续"
            } else if (hasSong) {
                tips = "上传图片将清空歌曲,是否继续"
            }
            //如果已经录入语音
            tipsDialogView = TipsDialogView.Builder(this)
                    .setMessageTip(tips)
                    .setConfirmTip("继续")
                    .setCancelTip("取消")
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            tipsDialogView?.dismiss()
                        }
                    })
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            audioDelIv.performClick()
                            tipsDialogView?.dismiss(false)
                            goAddImagePage()
                        }
                    })
                    .build()
            tipsDialogView?.showByDialog()
        } else {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
            ResPicker.getInstance().params = ResPicker.newParamsBuilder()
                    .setMultiMode(true)
                    .setShowCamera(true)
                    .setIncludeGif(true)
                    .setCrop(false)
                    .setSelectLimit(9)
                    .build()
            ResPickerActivity.open(this@PostsPublishActivity, ArrayList<ImageItem>(postsPublishImgAdapter.dataList))
        }
    }

    fun goMicRecordPage() {
        val hasSong = model.songId > 0
        val hasAudio = !hasSong && (model.recordVoicePath?.isNotEmpty() == true)
        val hasImg = postsPublishImgAdapter.dataList.isNotEmpty()

        if (hasSong || hasImg) {
            var tips: String? = null
            if (hasAudio) {
                tips = "录入语音将清空语音，是否继续"
            } else if (hasSong) {
                tips = "录入语音将清空歌曲，是否继续"
            }
            //如果已经录入语音
            tipsDialogView = TipsDialogView.Builder(PostsPublishActivity@ this)
                    .setMessageTip(tips)
                    .setConfirmTip("继续")
                    .setCancelTip("取消")
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            tipsDialogView?.dismiss()
                        }
                    })
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            postsPublishImgAdapter.dataList.clear()
                            postsPublishImgAdapter.notifyDataSetChanged()
                            model.imgUploadMap.clear()
                            model.songId = 0
                            imageRecyclerView.visibility = View.GONE
                            tipsDialogView?.dismiss(false)
                            goMicRecordPage()
                        }
                    })
                    .build()
            tipsDialogView?.showByDialog()
        } else {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_VOICE_RECORD)
                    .withSerializable("model", model)
                    .navigation(this@PostsPublishActivity, PostsVoiceRecordActivity.REQ_CODE_VOICE_RECORD)
        }
    }

    fun goKgeRecordPage() {
        val hasSong = model.songId > 0
        val hasAudio = !hasSong && (model.recordVoicePath?.isNotEmpty() == true)
        val hasImg = postsPublishImgAdapter.dataList.isNotEmpty()

        if (hasAudio || hasImg) {
            var tips: String? = null
            if (hasAudio) {
                tips = "录入歌曲将清空语音，是否继续"
            } else if (hasImg) {
                tips = "录入歌曲将清空图片，是否继续"
            }
            //如果已经录入语音
            tipsDialogView = TipsDialogView.Builder(PostsPublishActivity@ this)
                    .setMessageTip(tips)
                    .setConfirmTip("继续")
                    .setCancelTip("取消")
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            tipsDialogView?.dismiss()
                        }
                    })
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View?) {
                            postsPublishImgAdapter.dataList.clear()
                            postsPublishImgAdapter.notifyDataSetChanged()
                            model.imgUploadMap.clear()
                            if (!hasSong) {
                                audioDelIv.performClick()
                            }
                            imageRecyclerView.visibility = View.GONE
                            tipsDialogView?.dismiss(false)
                            goKgeRecordPage()
                        }
                    })
                    .build()
            tipsDialogView?.showByDialog()
        } else {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PostsPublishActivity)
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SONG_MANAGE)
                    .withInt("from", 9)
                    .navigation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
                val list = ResPicker.getInstance().selectedImageList
                postsPublishImgAdapter.dataList.clear()
                postsPublishImgAdapter.dataList.addAll(list)
                postsPublishImgAdapter.notifyDataSetChanged()
                if (postsPublishImgAdapter.dataList.isNotEmpty()) {
                    imageRecyclerView.visibility = View.VISIBLE
                }
            } else if (requestCode == PostsVoiceRecordActivity.REQ_CODE_VOICE_RECORD) {
                model.recordDurationMs = data?.getIntExtra("duration", 0) ?: 0
                model.recordVoicePath = PostsPublishModel.POSTS_PUBLISH_AUDIO_FILE_PATH
                postsAudioView.visibility = View.VISIBLE
                postsAudioView.bindData(model.recordDurationMs)
                audioDelIv.visibility = View.VISIBLE
            } else if (requestCode == PostsVoteEditActivity.REQ_CODE_VOTE_EDIT) {
                model.voteList.clear()
                model.voteList.addAll(data?.getStringArrayListExtra("vote_list") ?: ArrayList())
                voteVp.visibility = View.VISIBLE
                voteDelIv.visibility = View.VISIBLE
                if (model.voteList.size >= 1) {
                    voteItem1Tv.text = model.voteList[0]
                    voteItem1Tv.visibility = View.VISIBLE
                } else {
                    voteItem1Tv.visibility = View.GONE
                }
                if (model.voteList.size >= 2) {
                    voteItem2Tv.text = model.voteList[1]
                    voteItem2Tv.visibility = View.VISIBLE
                } else {
                    voteItem2Tv.visibility = View.GONE
                }
                if (model.voteList.size >= 3) {
                    voteItem3Tv.text = model.voteList[2]
                    voteItem3Tv.visibility = View.VISIBLE
                } else {
                    voteItem3Tv.visibility = View.GONE
                }
                if (model.voteList.size >= 4) {
                    voteItem4Tv.text = model.voteList[3]
                    voteItem4Tv.visibility = View.VISIBLE
                } else {
                    voteItem4Tv.visibility = View.GONE
                }
            } else if (requestCode == PostsRedPkgEditActivity.REQ_CODE_RED_PKG_EDIT) {
                model.redPkg = data?.getSerializableExtra("redPkg") as RedPkgModel
                redPkgDelIv.visibility = View.VISIBLE
                redPkgVp.visibility = View.VISIBLE
                redPkgTv.text = this.model.redPkg?.redpacketDesc
            } else if (requestCode == PostsTopicSelectActivity.REQ_CODE_TOPIC_SELECT) {
                model.topic = data?.getSerializableExtra("topic") as Topic
                topicTv.text = model.topic?.topicDesc
            }
        }
    }

    override fun destroy() {
        super.destroy()
        uploadQueue.destroy()
        SinglePlayer.removeCallback(playerTag)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedSongMakeSucessEvent) {
        model.recordDurationMs = event.duration ?: 0
        model.recordVoicePath = event.localPath
        postsAudioView.visibility = View.VISIBLE
        postsAudioView.bindData(model.recordDurationMs)
        audioDelIv.visibility = View.VISIBLE
        model.songId = event.songId ?: 0
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return true
    }
}

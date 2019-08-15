package com.module.feeds.make.publish

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.event.FeedPublishSucessEvent
import com.component.busilib.view.SkrProgressView
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.*
import com.module.feeds.make.editor.FeedsEditorActivity
import com.module.feeds.make.make.FeedsMakeActivity
import com.module.feeds.make.model.FeedsPublishTagModel
import com.module.feeds.watch.model.FeedTagModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Route(path = RouterConstants.ACTIVITY_FEEDS_PUBLISH)
class FeedsPublishActivity : BaseActivity() {


    lateinit var titleBar: CommonTitleBar
    lateinit var sayEdit: EditText
    lateinit var leftWordTipsTv: TextView
    lateinit var divideLine: View
    lateinit var worksNameTv: TextView
    lateinit var worksNameEt: EditText
    lateinit var tagClassifyTv: TextView
    lateinit var tagClassifyTf: TagFlowLayout
    //lateinit var uploadProgressbar: ProgressBar
    lateinit var progressSkr: SkrProgressView

    lateinit var tagClassifyAdapter: TagAdapter<FeedsPublishTagModel>

    var mFeedsMakeModel: FeedsMakeModel? = null

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    var rankList: List<FeedsPublishTagModel>? = null

    var customLrcUrl: String? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        MyLog.d(TAG, "initViewsavedInstanceState = $savedInstanceState")
        return R.layout.feeds_publish_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        MyLog.d(TAG, "initDatasavedInstanceState = $savedInstanceState")

        mFeedsMakeModel = sFeedsMakeModelHolder
        sFeedsMakeModelHolder = null

        titleBar = this.findViewById(R.id.title_bar)
        sayEdit = this.findViewById(R.id.say_edit)
        leftWordTipsTv = this.findViewById(R.id.left_word_tips_tv)
        divideLine = this.findViewById(R.id.divide_line)
        worksNameTv = this.findViewById(R.id.works_name_tv)
        worksNameEt = this.findViewById(R.id.works_name_et)
        tagClassifyTv = this.findViewById(R.id.tag_classify_tv)
        tagClassifyTf = this.findViewById(R.id.tag_classify_tf)
        progressSkr = this.findViewById(R.id.progress_skr)

        //填充标签
//        rankClassifyAdapter = object : TagAdapter<FeedsPublishTagModel>(ArrayList()) {
//            override fun getView(parent: FlowLayout, position: Int, tagModel: FeedsPublishTagModel): View {
//                val tv = LayoutInflater.from(parent.context).inflate(R.layout.feeds_tag_item_layout,
//                        parent, false) as ExTextView
//                tv.text = tagModel.rankName
//                return tv
//            }
//        }
//        rankClassifyTf.setMaxSelectCount(1)
//        rankClassifyTf.setMinSelectCount(1)
//        rankClassifyTf.adapter = rankClassifyAdapter
//        rankClassifyTf.setOnSelectListener {
//            it.take(1).getOrNull(0)?.let { it1 ->
//                rankList?.getOrNull(it1)?.tags.let { it2 ->
//                    tagClassifyAdapter.setTagDatas(it2)
//                    tagClassifyAdapter.setSelectedList()
//                }
//            }
//            radioView.play()
//        }

        tagClassifyAdapter = object : TagAdapter<FeedsPublishTagModel>(ArrayList()) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: FeedsPublishTagModel): View {
                val tv = LayoutInflater.from(parent.context).inflate(R.layout.feeds_tag_item_layout,
                        parent, false) as ExTextView
                tv.text = tagModel.tagDesc
                return tv
            }
        }
        tagClassifyTf.setMaxSelectCount(1)
        tagClassifyTf.adapter = tagClassifyAdapter

        launch {
            // 先看看发生异常会不会崩溃
            val result = subscribe { feedsMakeServerApi.getFeedLikeList() }
            if (result?.errno == 0) {
                rankList = JSON.parseArray(result.data.getString("tags"), FeedsPublishTagModel::class.java)
                tagClassifyAdapter.setTagDatas(rankList)
                val set = HashSet<Int>()
                rankList?.forEachIndexed { index, feedsPublishTagModel ->
                    mFeedsMakeModel?.songModel?.tags?.forEach { it ->
                        if (it?.tagID == feedsPublishTagModel.tagID) {
                            set.add(index)
                        }
                    }
                }
                tagClassifyAdapter.setSelectedList(set)
                tagClassifyAdapter.notifyDataChanged()
            }
        }

        titleBar.leftImageButton.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                //setResult(Activity.RESULT_OK)
                finishPage()
            }
        })

        titleBar.rightCustomView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("music_record", "publish_success", null)
                if (TextUtils.isEmpty(worksNameEt.text)) {
                    U.getToastUtil().showShort("作品名称为必填")
                    return
                }
                mFeedsMakeModel?.let {
                    progressSkr.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(mFeedsMakeModel?.audioUploadUrl)) {
                        step1()
                    } else {
                        step2()
                    }

                }
            }
        })
        sayEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                leftWordTipsTv.text = "${sayEdit.text.length}/300"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        worksNameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                titleBar.rightCustomView?.isSelected = TextUtils.isEmpty(worksNameEt.text)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        val from = intent.getIntExtra("from", 0)
        val displayName: String
        if (from == 3) {
            //从草稿箱进入的
            // 有作品名了,优先显示作品名字
            if (TextUtils.isEmpty(mFeedsMakeModel?.songModel?.workName)) {
                displayName = mFeedsMakeModel?.songModel?.getDisplayName() ?: ""
            } else {
                displayName = mFeedsMakeModel?.songModel?.workName ?: ""
            }
        } else {
            displayName = mFeedsMakeModel?.songModel?.getDisplayName() ?: ""
        }
        worksNameEt.setText(displayName)
        worksNameEt.setSelection(displayName.length)
        // 默认的心情 和标签
        sayEdit.setText(mFeedsMakeModel?.songModel?.title)
    }

    private suspend fun getReader(): LyricsReader {
        return suspendCancellableCoroutine<LyricsReader> { continuation ->
            LyricsManager.loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)
                    .subscribe({
                        it?.let {
                            createCustomZrce2ReaderByTxt(it, mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr)
                            mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader = it
                            continuation.resume(it)
                            step2()
                        }
                    }, {
                        MyLog.e(TAG, it)
                        continuation.resumeWithException(it)
                    })
        }
    }

    private fun step1() {
        uploadAudio {
            step2()
        }
    }

    private fun step2() {
        MyLog.d(TAG, "step2 mFeedsMakeModel?.hasChangeLyric=${mFeedsMakeModel?.hasChangeLyric}")
        if (mFeedsMakeModel?.hasChangeLyric == true) {
            if (!TextUtils.isEmpty(customLrcUrl)) {
                submitToServer()
            } else {
                launch(Dispatchers.IO) {
                    var content = ""
                    var filePath: String? = null
                    if (mFeedsMakeModel?.songModel?.songTpl?.lrcTs?.isNotEmpty() == true) {
                        val lrcTsReader = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader
                        if (lrcTsReader == null) {
                            LyricsManager.loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)
                                    .subscribe({
                                        it?.let {
                                            createCustomZrce2ReaderByTxt(it, mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr)
                                            mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader = it
                                            step2()
                                        }
                                    }, {
                                        MyLog.e(TAG, it)
                                    })
                            return@launch
                        } else {
                            filePath = U.getAppInfoUtils().getFilePathInSubDir("feeds", "feeds_custom_lyric_temp.zrce2")
                            content = LyricsManager.createZrce2ByReader(lrcTsReader)
                        }
                    } else {
                        content = mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr ?: ""
                        filePath = U.getAppInfoUtils().getFilePathInSubDir("feeds", "feeds_custom_lyric_temp.txt")
                    }
                    val file = File(filePath)
                    U.getIOUtils().writeFile(content, file)
                    if (file.exists()) {
                        UploadParams.newBuilder(filePath)
                                .setFileType(UploadParams.FileType.feed)
                                .startUploadAsync(object : UploadCallback {
                                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                                    }

                                    override fun onSuccessNotInUiThread(url: String?) {
                                        MyLog.d(TAG, "歌词上传 onSuccessNotInUiThreadurl = $url")
                                        customLrcUrl = url
                                        submitToServer()
                                    }

                                    override fun onFailureNotInUiThread(msg: String?) {
                                    }
                                })
                    }
                }
            }
        } else {
            submitToServer()
        }
    }

    private fun setValueFromUi() {
        mFeedsMakeModel?.songModel?.title = sayEdit.text.toString()
        /**
         * 这里的变动肯定只影响作品名，歌曲名不受影响
         */
        //mFeedsMakeModel?.songModel?.songTpl?.songNameChange = worksNameEt.text.toString()
        mFeedsMakeModel?.songModel?.workName = worksNameEt.text.toString()

        val tagsIds = ArrayList<FeedTagModel>()
        tagClassifyTf.selectedList.forEach {
            if (it < rankList?.size ?: 0) {
                rankList?.get(it)?.let {
                    val model = FeedTagModel()
                    model.tagID = it.tagID ?: 0
                    model.tagDesc = it.tagDesc
                    tagsIds.add(model)
                }
                mFeedsMakeModel?.songModel?.tags = tagsIds
            }

        }
    }

    private fun submitToServer() {
        //保存发布 服务器api
//                {
//                    "challengeID": 0,
//                    "feedID": 0,
//                    "playDurMs": 0,
//                    "playURL": "string",
//                    "rankType": "FRT_UNKNOWN",
//                    "tagIDs": [
//                    0
//                    ],
//                    "title": "string",
//                    "tplID": 0
//                }
        launch {
            setValueFromUi()
            val tagsIds = ArrayList<Int>()
            mFeedsMakeModel?.songModel?.tags?.forEach {
                it?.let {
                    tagsIds.add(it.tagID)
                }
            }
            val result: ApiResult
            if (mFeedsMakeModel?.songModel?.challengeID == 0L) {
                // 快唱
                val mutableSet1 = mapOf(
                        "hasChangeLRC" to (mFeedsMakeModel?.hasChangeLyric == true),
                        "lrcURL" to customLrcUrl,
                        "playDurMs" to mFeedsMakeModel?.recordDuration,
                        "playURL" to mFeedsMakeModel?.audioUploadUrl,
                        "songName" to mFeedsMakeModel?.songModel?.songTpl?.getDisplaySongName(),
                        "songType" to if (mFeedsMakeModel?.withBgm == true) 1 else 2,
                        "tagIDs" to tagsIds,
                        "title" to mFeedsMakeModel?.songModel?.title,
                        "tplID" to mFeedsMakeModel?.songModel?.songTpl?.tplID,
                        "workName" to mFeedsMakeModel?.songModel?.workName
                )

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
                result = subscribe { feedsMakeServerApi.uploadQuickFeeds(body) }
            } else {
                // 打榜
                val mutableSet1 = mapOf(
                        "challengeID" to mFeedsMakeModel?.songModel?.challengeID,
                        "hasChangeLRC" to (mFeedsMakeModel?.hasChangeLyric == true),
                        "lrcURL" to customLrcUrl,
                        "playDurMs" to mFeedsMakeModel?.recordDuration,
                        "playURL" to mFeedsMakeModel?.audioUploadUrl,
                        "songName" to mFeedsMakeModel?.songModel?.songTpl?.getDisplaySongName(),
                        "songType" to if (mFeedsMakeModel?.withBgm == true) 1 else 2,
                        "tagIDs" to tagsIds,
                        "title" to mFeedsMakeModel?.songModel?.title,
                        "tplID" to mFeedsMakeModel?.songModel?.songTpl?.tplID,
                        "workName" to mFeedsMakeModel?.songModel?.workName
                )

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
                result = subscribe { feedsMakeServerApi.uploadHitFeeds(body) }
            }

            progressSkr.visibility = View.GONE
            if (result?.errno == 0) {
                //上传成功
                U.getToastUtil().showShort("上传成功")
                EventBus.getDefault().post(FeedPublishSucessEvent())
                mFeedsMakeModel?.songModel?.playURL = mFeedsMakeModel?.audioUploadUrl
                mFeedsMakeModel?.songModel?.songID = result.data.getIntValue("songID")
                // 跳到分享页
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SHARE)
                        .withSerializable("feeds_make_model", mFeedsMakeModel)
                        .navigation()
                for (ac in U.getActivityUtils().activityList) {
                    if (ac is FeedsEditorActivity) {
                        ac.finish()
                    } else if (ac is FeedsMakeActivity) {
                        ac.finish()
                    }
                }
                finish()
                val draftId = mFeedsMakeModel?.draftID ?: 0L
                if (draftId != 0L) {
                    launch(Dispatchers.IO) {
                        FeedsMakeLocalApi.delete(draftId)
                    }
                }
            } else {
                U.getToastUtil().showShort(result?.errmsg)
            }
        }
    }

    override fun onBackPressed() {
        //setResult(Activity.RESULT_OK)
        finishPage()
    }

    private fun finishPage() {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(this)
        val tipsDialogView = TipsDialogView.Builder(this@FeedsPublishActivity)
                .setConfirmTip("保存")
                .setCancelTip("直接退出")
                .setCancelBtnClickListener {
                    finish()
                }
                .setMessageTip("是否将发布保存到草稿箱?")
                .setConfirmBtnClickListener {
                    setValueFromUi()
                    if (TextUtils.isEmpty(mFeedsMakeModel?.audioUploadUrl)) {
                        progressSkr.visibility = View.VISIBLE
                        progressSkr.setProgressText("保存中")
                        uploadAudio {
                            saveAndExit()
                        }
                    } else {
                        saveAndExit()
                    }
                }
                .build()
        tipsDialogView.showByDialog()
    }

    private fun saveAndExit() {
        launch {
            val j = launch(Dispatchers.IO) {
                mFeedsMakeModel?.let {
                    FeedsMakeLocalApi.insert(it)
                }
            }
            j.join()
            if (mFeedsMakeModel?.songModel?.challengeID == 0L) {
                U.getToastUtil().showShort("已存入翻唱草稿")
            } else {
                U.getToastUtil().showShort("已存入打榜草稿")
            }
            progressSkr.visibility = View.GONE
            finish()
        }
    }

    private fun uploadAudio(call: (String?) -> Unit) {
        UploadParams.newBuilder(mFeedsMakeModel?.composeSavePath)
                .setFileType(UploadParams.FileType.feed)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                    }

                    override fun onSuccessNotInUiThread(url: String?) {
                        mFeedsMakeModel?.audioUploadUrl = url
                        call.invoke(url)
                    }

                    override fun onFailureNotInUiThread(msg: String?) {
                        launch {
                            U.getToastUtil().showShort("上传失败，稍后重试")
                            progressSkr.visibility = View.GONE
                        }
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

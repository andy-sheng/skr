package com.module.feeds.make.publish

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeActivity
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.FeedsMakeServerApi
import com.module.feeds.make.editor.FeedsEditorActivity
import com.module.feeds.make.model.FeedsPublishTagModel
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody


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
    lateinit var uploadProgressbarContainer: ViewGroup
    lateinit var tagClassifyAdapter: TagAdapter<FeedsPublishTagModel>

    var mFeedsMakeModel: FeedsMakeModel? = null

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    var rankList: List<FeedsPublishTagModel>? = null

    var playUrl: String? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_publish_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = intent.getSerializableExtra("feeds_make_model") as FeedsMakeModel?

        titleBar = this.findViewById(R.id.title_bar)
        sayEdit = this.findViewById(R.id.say_edit)
        leftWordTipsTv = this.findViewById(R.id.left_word_tips_tv)
        divideLine = this.findViewById(R.id.divide_line)
        worksNameTv = this.findViewById(R.id.works_name_tv)
        worksNameEt = this.findViewById(R.id.works_name_et)
        tagClassifyTv = this.findViewById(R.id.tag_classify_tv)
        tagClassifyTf = this.findViewById(R.id.tag_classify_tf)
        uploadProgressbarContainer = this.findViewById(R.id.upload_progressbar_container)
        uploadProgressbarContainer?.setOnClickListener{}
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
                tagClassifyAdapter.notifyDataChanged()
            }
        }

        titleBar.leftImageButton.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        titleBar.rightCustomView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (TextUtils.isEmpty(worksNameEt.text)) {
                    U.getToastUtil().showShort("作品名称为必填")
                    return
                }
                mFeedsMakeModel?.let {
                    uploadProgressbarContainer.visibility = View.VISIBLE
                    if (TextUtils.isEmpty(playUrl)) {
                        UploadParams.newBuilder(it.composeSavePath)
                                .setFileType(UploadParams.FileType.feed)
                                .startUploadAsync(object : UploadCallback {
                                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                                    }

                                    override fun onSuccessNotInUiThread(url: String?) {
                                        playUrl = url
                                        submitToServer()
                                    }

                                    override fun onFailureNotInUiThread(msg: String?) {
                                        launch {
                                            U.getToastUtil().showShort("上传失败，稍后重试")
                                            uploadProgressbarContainer.visibility = View.GONE
                                        }
                                    }
                                })
                    } else {
                        submitToServer()
                    }

                }
            }
        })
        sayEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                leftWordTipsTv.text = "${worksNameEt.text.length}/300"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        worksNameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (TextUtils.isEmpty(worksNameEt.text)) {
                    titleBar.rightCustomView?.isSelected = true
                } else {
                    titleBar.rightCustomView?.isSelected = false
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
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
            val tagsIds = ArrayList<Int>()
            tagClassifyTf.selectedList.forEach {
                rankList?.get(it)?.tagID?.let { it2 ->
                    tagsIds.add(it2)
                }
            }

            val mutableSet1 = mapOf(
                    "title" to sayEdit.text.toString(),
                    "workName" to worksNameEt.text.toString(),
                    "tagIDs" to tagsIds,
                    "playDurMs" to mFeedsMakeModel?.recordDuration,
                    "playURL" to playUrl,
                    "challengeID" to mFeedsMakeModel?.songModel?.challengeID,
                    "tplID" to mFeedsMakeModel?.songModel?.songTpl?.tplID
//TODO                    "tplID": 0,
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
            val result = subscribe { feedsMakeServerApi.uploadFeeds(body) }
            uploadProgressbarContainer.visibility = View.GONE
            if (result?.errno == 0) {
                //上传成功
                U.getToastUtil().showShort("上传成功")
                mFeedsMakeModel?.songModel?.workName = worksNameEt.text.toString()
                mFeedsMakeModel?.songModel?.title = sayEdit.text.toString()
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
            } else {
                U.getToastUtil().showShort(result?.errmsg)
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
        return
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

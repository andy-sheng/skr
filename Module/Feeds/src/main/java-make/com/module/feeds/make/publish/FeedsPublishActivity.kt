package com.module.feeds.make.publish

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.image.fresco.BaseImageView
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.widget.ManyLyricsView
import com.engine.EngineEvent
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.FeedsMakeServerApi
import com.module.feeds.make.model.FeedsPublishTagModel
import com.module.feeds.watch.view.FeedsRecordAnimationView
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RouterConstants.ACTIVITY_FEEDS_PUBLISH)
class FeedsPublishActivity : BaseActivity() {


    lateinit var titleBar:CommonTitleBar
    lateinit var sayEdit:EditText
    lateinit var leftWordTipsTv:TextView
    lateinit var divideLine:View
    lateinit var worksNameTv:TextView
    lateinit var worksNameEt:EditText
    lateinit var tagClassifyTv:TextView
    lateinit var tagClassifyTf:TagFlowLayout
    lateinit var composeProgressbar:ProgressBar

    lateinit var tagClassifyAdapter: TagAdapter<FeedsPublishTagModel.Tag>

    var mFeedsMakeModel: FeedsMakeModel? = null

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    var rankList: List<FeedsPublishTagModel>? = null

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
        composeProgressbar = this.findViewById(R.id.compose_progressbar)

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

        tagClassifyAdapter = object : TagAdapter<FeedsPublishTagModel.Tag>(ArrayList()) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: FeedsPublishTagModel.Tag): View {
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
            val result = subscribe {  feedsMakeServerApi.getFeedLikeList()}
            if (result.errno == 0) {
                rankList = JSON.parseArray(result.data.getString("tags"), FeedsPublishTagModel::class.java)
//                rankClassifyAdapter.setTagDatas(rankList)
//                rankClassifyAdapter.setSelectedList(0)
//                rankList?.getOrNull(0)?.let {
//                    tagClassifyAdapter.setTagDatas(it.tags)
//                    tagClassifyAdapter.notifyDataChanged()
//                }
            }
        }

        titleBar.leftImageButton.setOnClickListener(object:DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                finish()
            }
        })

        titleBar.rightTextView.setOnClickListener(object:DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                //保存发布 服务器api

            }
        })
        worksNameEt.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                titleBar.rightTextView.isEnabled = !TextUtils.isEmpty(worksNameEt.text)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_START) {
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }
}

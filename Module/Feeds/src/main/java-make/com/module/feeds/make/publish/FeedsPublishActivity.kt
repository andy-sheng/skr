package com.module.feeds.make.publish

import android.os.Bundle
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


@Route(path = RouterConstants.ACTIVITY_FOR_TEST)
class FeedsPublishActivity : BaseActivity() {

    lateinit var titleBar: CommonTitleBar
    lateinit var sayEdit: EditText
    lateinit var avatarBg: BaseImageView
    lateinit var radioView: FeedsRecordAnimationView
    lateinit var manyLyricsView: ManyLyricsView
    lateinit var rankClassifyTv: TextView
    lateinit var rankClassifyTf: TagFlowLayout
    lateinit var tagClassifyTv: TextView
    lateinit var tagClassifyTf: TagFlowLayout
    lateinit var composeProgressbar: ProgressBar

    lateinit var rankClassifyAdapter: TagAdapter<FeedsPublishTagModel>
    lateinit var tagClassifyAdapter: TagAdapter<FeedsPublishTagModel.Tag>

    var mFeedsMakeModel: FeedsMakeModel? = null

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    var rankList: List<FeedsPublishTagModel>? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_publish_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = intent.getSerializableExtra("feeds_make_model") as FeedsMakeModel?

        titleBar = findViewById(R.id.title_bar)
        sayEdit = findViewById(R.id.say_edit)
        avatarBg = findViewById(R.id.avatar_bg)
        radioView = findViewById(R.id.radio_view)
        manyLyricsView = findViewById(R.id.many_lyrics_view)
        rankClassifyTv = findViewById(R.id.rank_classify_tv)
        rankClassifyTf = findViewById(R.id.rank_classify_tf)
        tagClassifyTv = findViewById(R.id.tag_classify_tv)
        tagClassifyTf = findViewById(R.id.tag_classify_tf)
        composeProgressbar = findViewById(R.id.compose_progressbar)

        //填充标签
        rankClassifyAdapter = object : TagAdapter<FeedsPublishTagModel>(ArrayList()) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: FeedsPublishTagModel): View {
                val tv = LayoutInflater.from(parent.context).inflate(R.layout.feeds_tag_item_layout,
                        parent, false) as ExTextView
                tv.text = tagModel.rankName
                return tv
            }
        }
        rankClassifyTf.setMaxSelectCount(1)
        rankClassifyTf.setMinSelectCount(1)
        rankClassifyTf.adapter = rankClassifyAdapter
        rankClassifyTf.setOnSelectListener {
            it.take(1).getOrNull(0)?.let { it1 ->
                rankList?.getOrNull(it1)?.tags.let { it2 ->
                    tagClassifyAdapter.setTagDatas(it2)
                    tagClassifyAdapter.setSelectedList()
                }
            }
            radioView.play()
        }

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
                rankClassifyAdapter.setTagDatas(rankList)
                rankClassifyAdapter.setSelectedList(0)
                rankList?.getOrNull(0)?.let {
                    tagClassifyAdapter.setTagDatas(it.tags)
                    tagClassifyAdapter.notifyDataChanged()
                }
            }
        }
        radioView.setAvatar(MyUserInfoManager.getInstance().avatar)
        startPlay()
    }

    fun startPlay(){
        radioView.play()
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

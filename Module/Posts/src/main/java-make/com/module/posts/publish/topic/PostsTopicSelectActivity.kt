package com.module.posts.publish.topic

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.SuccessCallback
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.publish.PostsPublishServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_POSTS_TOPIC_SELECT)
class PostsTopicSelectActivity : BaseActivity() {

    lateinit var mainActContainer: ConstraintLayout
    lateinit var titleBar: CommonTitleBar
    lateinit var classifyRv: RecyclerView
    lateinit var contentRv: RecyclerView
    lateinit var refreshLayout: SmartRefreshLayout

    lateinit var postsTopicClassifyAdapter: PostsTopicClassifyAdapter
    lateinit var postsTopicListAdapter: PostsTopicListAdapter

    val api = ApiManager.getInstance().createService(PostsPublishServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_topic_select_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mainActContainer = findViewById(R.id.main_act_container)
        titleBar = findViewById(R.id.title_bar)
        classifyRv = findViewById(R.id.classify_rv)
        contentRv = findViewById(R.id.content_rv)
        refreshLayout = findViewById(R.id.refreshLayout)

        titleBar.leftImageButton.setDebounceViewClickListener {
            finish()
        }
        postsTopicClassifyAdapter = PostsTopicClassifyAdapter()
        classifyRv.layoutManager = LinearLayoutManager(this)
        classifyRv.adapter = postsTopicClassifyAdapter
        postsTopicClassifyAdapter.selectListener = { model ->
            postsTopicClassifyAdapter.selectModel = model
            postsTopicClassifyAdapter.notifyDataSetChanged()
            offset = 0
            model?.let {
                getTopicListByCategory()
            }
        }

        postsTopicListAdapter = PostsTopicListAdapter()
        contentRv.layoutManager = LinearLayoutManager(this)
        contentRv.adapter = postsTopicListAdapter

        postsTopicListAdapter.selectListener = { model ->
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_POSTS_PUBLISH)
                    .withSerializable("topic", model)
                    .navigation()
        }


        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(false)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getTopicListByCategory()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        val loadService = LoadSir.beginBuilder()
                .addCallback(EmptyCallback(R.drawable.more_friend_empty_icon, "页面为空～", "#4cffffff"))
                .setDefaultCallback(EmptyCallback::class.java)
                .build().register(mainActContainer)

        launch {
            val result = subscribe { api.getTopicCategoryList(0, 1000, MyUserInfoManager.getInstance().uid.toInt()) }
            loadService.showSuccess()
            if (result.errno == 0) {
                val l = JSON.parseArray(result.data.getString("categorys"), Category::class.java)
                postsTopicClassifyAdapter.dataList.clear()
                postsTopicClassifyAdapter.dataList.addAll(l)
                postsTopicClassifyAdapter.selectModel = postsTopicClassifyAdapter.dataList.getOrNull(0)
                postsTopicClassifyAdapter.notifyDataSetChanged()
                if (postsTopicClassifyAdapter.selectModel != null) {
                    getTopicListByCategory()
                }
            }
        }
    }

    var offset = 0

    private fun getTopicListByCategory() {
        launch {
            val result = subscribe { api.getTopicList(offset, 50, MyUserInfoManager.getInstance().uid.toInt(), postsTopicClassifyAdapter.selectModel!!.categoryID!!) }
            if (result.errno == 0) {
                val l = JSON.parseArray(result.data.getString("topics"), Topic::class.java)
                val newOffset = result.data.getIntValue("offset")
                val hasMore = result.data.getBooleanValue("hasMore")
                if (offset == 0) {
                    postsTopicListAdapter.dataList.clear()
                }
                offset = newOffset
                postsTopicListAdapter.dataList.addAll(l)
                postsTopicListAdapter.notifyDataSetChanged()
                if (hasMore) {
                    refreshLayout.finishLoadMore()
                } else {
                    refreshLayout.finishLoadMoreWithNoMoreData()
                }
            }
        }
    }


    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

package com.module.posts.watch.view

import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.common.base.BaseFragment
import com.common.callback.Callback
import com.common.log.MyLog
import com.component.person.photo.model.PhotoModel
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseActivity
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.posts.R
import com.module.posts.watch.adapter.PostsWatchViewAdapter
import com.module.posts.watch.model.PostsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

abstract class BasePostsWatchView(val fragment: BaseFragment, val type: Int) : ConstraintLayout(fragment.context), CoroutineScope by MainScope() {
    val TAG = when (type) {
        TYPE_POST_FOLLOW -> "FollowPostsWatchView"
        TYPE_POST_RECOMMEND -> "FollowWatchView"
        TYPE_POST_LAST -> "RecommendPostsWatchView"
        TYPE_POST_PERSON -> "PersonPostsWatchView"
        else -> "BasePostsWatchView"
    }

    companion object {
        const val TYPE_POST_FOLLOW = 1   // 关注
        const val TYPE_POST_RECOMMEND = 2  // 推荐
        const val TYPE_POST_LAST = 3  // 最新
        const val TYPE_POST_PERSON = 4   // 个人中心
    }

    private val refreshLayout: SmartRefreshLayout
    private val classicsHeader: ClassicsHeader
    private val recyclerView: RecyclerView
    private val adapter: PostsWatchViewAdapter = PostsWatchViewAdapter()

    init {
        View.inflate(context, R.layout.posts_watch_view_layout, this)
        refreshLayout = this.findViewById(R.id.refreshLayout)
        classicsHeader = this.findViewById(R.id.classics_header)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableRefresh(false)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.imageClickListener = {pos,model,index,url->
            BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<String>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: String) {
                        imageBrowseView.load(item)
                }

                override fun getInitCurrentItemPostion(): Int {
                    return index
                }

                override fun getInitList(): List<String>? {
                    return model?.imageList
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

                override fun hasMenu(): Boolean {
                    return false
                }
            })
        }
    }

    open fun unselected(reason: Int) {

    }

    open fun selected() {
        // todo 做点假数据吧
        adapter.mDataList.clear()
        for (i in 0..10) {
            val model = PostsWatchModel()
            adapter.mDataList.add(model)
        }
        adapter.notifyDataSetChanged()
    }
}
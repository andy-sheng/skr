package com.module.feeds.songmanage.view


import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.component.busilib.callback.EmptyCallback
import com.dialog.view.TipsDialogView
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeLocalApi
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.make.openFeedsMakeActivity
import com.module.feeds.make.sFeedsMakeModelHolder
import com.module.feeds.songmanage.adapter.FeedSongDraftsAdapter
import com.module.feeds.songmanage.adapter.FeedSongDraftsListener
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.*

/**
 * 草稿箱view
 */
class FeedDraftsView(activity: BaseActivity) : ConstraintLayout(activity), CoroutineScope by MainScope() {

    val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView

    val adapter: FeedSongDraftsAdapter
    var mTipsDialogView: TipsDialogView? = null

    private val mLoadService: LoadService<*>

    init {
        View.inflate(context, R.layout.feed_song_drafts_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(false)

        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                // 加载更多
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        adapter = FeedSongDraftsAdapter(object : FeedSongDraftsListener {
            override fun onClickSing(position: Int, model: FeedsMakeModel?) {
                model?.let {
                    MyLog.d("FeedDraftsView", "FeedsMakeModel=$model")
                    if (TextUtils.isEmpty(model.audioUploadUrl)) {
                        // 演唱
                        openFeedsMakeActivity(model)
                    } else {
                        sFeedsMakeModelHolder = model
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_PUBLISH)
                                .navigation()
                    }
                }
            }

            override fun onLongClick(position: Int, model: FeedsMakeModel?) {
                model?.let {
                    mTipsDialogView?.dismiss()
                    mTipsDialogView = TipsDialogView.Builder(activity)
                            .setMessageTip("确定删除歌曲吗?")
                            .setCancelTip("取消")
                            .setCancelBtnClickListener {
                                mTipsDialogView?.dismiss()
                            }
                            .setConfirmTip("确认")
                            .setConfirmBtnClickListener {
                                mTipsDialogView?.dismiss()
                                deleteDrafts(model)
                            }
                            .build()
                    mTipsDialogView?.showByDialog()
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.feed_drafts_empty_icon, "暂无草稿", "#802F2F30"))
                .build()
        mLoadService = mLoadSir.register(refreshLayout, Callback.OnReloadListener {
            tryLoadData()
        })
    }

    fun tryLoadData() {
        (context as BaseActivity).launch {
            val list = async (Dispatchers.IO){
                FeedsMakeLocalApi.loadAll()
            }
            adapter.setData(list.await())

            if (!adapter.mDataList.isNullOrEmpty()) {
                mLoadService.showSuccess()
            } else {
                mLoadService.showCallback(EmptyCallback::class.java)
            }
        }
    }

    private fun deleteDrafts(model: FeedsMakeModel) {
        launch {
            launch(Dispatchers.IO) {
                FeedsMakeLocalApi.delete(model.draftID)
            }.join()
            adapter.delete(model)
        }
    }

    fun destroy() {
        mTipsDialogView?.dismiss(false)
        cancel()
    }

}
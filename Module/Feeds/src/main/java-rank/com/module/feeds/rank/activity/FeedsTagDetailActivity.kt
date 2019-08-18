package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.DateTimeUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedTagDetailAdapter
import com.module.feeds.rank.adapter.FeedTagListener
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*

@Route(path = RouterConstants.ACTIVITY_FEEDS_TAG_DETAIL)
class FeedsTagDetailActivity : BaseActivity() {

    lateinit var imageBg: SimpleDraweeView
    lateinit var timeTv: TextView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var appbar: AppBarLayout

    lateinit var toolbar: Toolbar
    lateinit var topAreaBg: SimpleDraweeView
    lateinit var topDesc: TextView

    lateinit var ivBack: ExImageView

    var model: FeedRecommendTagModel? = null
    var adapter: FeedTagDetailAdapter? = null

    var lastVerticalOffset = Int.MAX_VALUE
    var queryDate: String = ""

    var mOffset = 0
    val mCNT = 30
    var hasMore = true

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_tag_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = intent.getSerializableExtra("model") as FeedRecommendTagModel?
        if (model == null) {
            finish()
        }

        imageBg = findViewById(R.id.image_bg)
        timeTv = findViewById(R.id.time_tv)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)
        appbar = findViewById(R.id.appbar)
        toolbar = findViewById(R.id.toolbar)
        topAreaBg = findViewById(R.id.top_area_bg)
        topDesc = findViewById(R.id.top_desc)
        ivBack = findViewById(R.id.iv_back)

        smartRefresh.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    super.onRefresh(refreshLayout)
                    loadInitData()
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    loadMoreData()
                }

                var lastScale = 0f
                override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                    super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                    // 背景图片的高度是400
                    val scale = offset.toFloat() / 400.dp().toFloat() + 1
                    if (Math.abs(scale - lastScale) >= 0.01) {
                        lastScale = scale
                        imageBg.scaleX = scale
                        imageBg.scaleY = scale
                    }
                }
            })
        }

        appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                imageBg.translationY = verticalOffset.toFloat()
                if (verticalOffset == 0) {
                    // 展开状态
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    // 完全收缩状态
                    if (toolbar.visibility != View.VISIBLE) {
                        toolbar.visibility = View.VISIBLE
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                    }
                }
            }
        }

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })
        adapter = FeedTagDetailAdapter(object : FeedTagListener {
            override fun onClickItem(position: Int, model: FeedsWatchModel?) {

            }

            override fun onClickCollect(position: Int, model: FeedsWatchModel?) {
                model?.let {
                    collectOrUnCollectFeed(position, it)
                }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        FrescoWorker.loadImage(imageBg, ImageFactory.newPathImage(model?.bigImgURL)
                .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .build<BaseImage>())
        topDesc.text = model?.tagDesc
        AvatarUtils.loadAvatarByUrl(topAreaBg, AvatarUtils.newParamsBuilder(model?.bigImgURL)
                .setBlur(true)
                .build())
        queryDate = U.getDateTimeUtils().formatDateString(Date(model?.timeMs
                ?: System.currentTimeMillis()))
        timeTv.text = queryDate

        loadInitData()
    }

    private fun loadInitData() {
        getRecomendTagDetailList(0, true)
    }

    private fun loadMoreData() {
        if (hasMore) {
            getRecomendTagDetailList(mOffset, false)
        } else {
            U.getToastUtil().showShort("没有更多数据了")
        }
    }

    private fun changeDate() {

    }

    private fun addFeedList(list: List<FeedsWatchModel>, clean: Boolean) {
        smartRefresh.finishRefresh()
        smartRefresh.finishLoadMore()
        smartRefresh.setEnableLoadMore(hasMore)

        if (clean) {
            adapter?.mDataList?.clear()
        }

        if (!list.isNullOrEmpty()) {
            adapter?.mDataList?.addAll(list)
        }
        adapter?.notifyDataSetChanged()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    private fun getRecomendTagDetailList(offset: Int, isClean: Boolean) {
        launch {
            val obj = subscribe(RequestControl("getRecomendTagDetailList", ControlType.CancelThis)) {
                mFeedServerApi.getRecomendTagDetailList(offset, mCNT, model?.tagTypeID!!, queryDate)
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("rankInfos"), FeedsWatchModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBooleanValue("hasMore")
                addFeedList(list, isClean)
            } else {
                smartRefresh.finishRefresh()
                smartRefresh.finishLoadMore()
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    // 收藏和取消收藏
    private fun collectOrUnCollectFeed(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isCollected

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("collectFeed", ControlType.CancelThis)) { mFeedServerApi.collectFeed(body) }
            if (result.errno == 0) {
                model.isCollected = !model.isCollected
                EventBus.getDefault().post(FeedsCollectChangeEvent(model.feedID, model.isCollected))
                adapter?.update(position, model, FeedTagDetailAdapter.REFRESH_TYPE_COLLECT)
                if (model.isCollected) {
                    U.getToastUtil().showShort("收藏成功")
                } else {
                    U.getToastUtil().showShort("取消收藏成功")
                }
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                }
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("${result?.errmsg}")
                } else {
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }
}
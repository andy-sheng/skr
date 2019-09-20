package com.module.posts.watch.activity

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.ImageUtils
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.person.view.RequestCallBack
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.watch.PostsWatchServerApi
import com.module.posts.watch.model.PostsTopicDetailModel
import com.module.posts.watch.model.PostsTopicModel
import com.module.posts.watch.model.PostsTopicTabModel
import com.module.posts.watch.view.TopicPostsWatchView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_POSTS_TOPIC)
class PostsTopicActivity : BaseActivity(), RequestCallBack {

    private var imageBg: SimpleDraweeView? = null
    private var smartRefresh: SmartRefreshLayout? = null
    private var topicTab: SlidingTabLayout? = null
    private var topicVp: NestViewPager? = null
    private var appbar: AppBarLayout? = null
    private var topicCover: SimpleDraweeView? = null
    private var topicTitle: TextView? = null
    private var topicDesc: TextView? = null
    private var toolbar: Toolbar? = null
    private var toolbarLayout: ConstraintLayout? = null
    private var topAreaBg: SimpleDraweeView? = null
    private var topDesc: TextView? = null
    private var ivBack: ExImageView? = null

    var topicInfo: PostsTopicModel? = null
    val postsWatchServerApi = ApiManager.getInstance().createService(PostsWatchServerApi::class.java)

    var topicPostsViews: HashMap<Int, TopicPostsWatchView> = HashMap()
    var pagerAdapter: PagerAdapter? = null
    var lastVerticalOffset = Int.MAX_VALUE

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_topic_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        topicInfo = intent.getSerializableExtra("topicInfo") as PostsTopicModel?
        if (topicInfo == null) {
            finish()
            return
        }

        imageBg = findViewById(R.id.image_bg)
        smartRefresh = findViewById(R.id.smart_refresh)
        topicTab = findViewById(R.id.topic_tab)
        topicVp = findViewById(R.id.topic_vp)
        appbar = findViewById(R.id.appbar)
        topicCover = findViewById(R.id.topic_cover)
        topicTitle = findViewById(R.id.topic_title)
        topicDesc = findViewById(R.id.topic_desc)
        toolbar = findViewById(R.id.toolbar)
        toolbarLayout = findViewById(R.id.toolbar_layout)
        topAreaBg = findViewById(R.id.top_area_bg)
        topDesc = findViewById(R.id.top_desc)
        ivBack = findViewById(R.id.iv_back)

        smartRefresh?.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    super.onRefresh(refreshLayout)
                    // todo 待补全
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    // todo 待补全
                }

                var lastScale = 0f
                override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                    super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                    // 背景图片的高度是400
                    val scale = offset.toFloat() / 400.dp().toFloat() + 1
                    if (Math.abs(scale - lastScale) >= 0.01) {
                        lastScale = scale
                        imageBg?.scaleX = scale
                        imageBg?.scaleY = scale
                    }
                }
            })
        }

        appbar?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            imageBg?.translationY = verticalOffset.toFloat()
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (toolbar?.visibility != View.GONE) {
                        toolbar?.visibility = View.GONE
                        toolbarLayout?.visibility = View.GONE
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    // 完全收缩状态
                    if (toolbar?.visibility != View.VISIBLE) {
                        toolbar?.visibility = View.VISIBLE
                        toolbarLayout?.visibility = View.VISIBLE
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (toolbar?.visibility != View.GONE) {
                        toolbar?.visibility = View.GONE
                        toolbarLayout?.visibility = View.GONE
                    }
                }
            }
        }

        ivBack?.setOnClickListener(object : DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                finish()
            }
        })

        getTopicDetail()
        getTopicTabs()
    }

    private fun getTopicDetail() {
        launch {
            val result = subscribe(RequestControl("getTopicDetail", ControlType.CancelThis)) {
                postsWatchServerApi.getTopicDetail(MyUserInfoManager.getInstance().uid, topicInfo?.topicID
                        ?: 0)
            }
            if (result.errno == 0) {
                val detail = JSON.parseObject(result.data.toJSONString(), PostsTopicDetailModel::class.java)
                showDetail(detail)
            } else {

            }
        }
    }

    private fun showDetail(detail: PostsTopicDetailModel?) {
        detail?.let {
            topicTitle?.text = it.topicTitle
            topicDesc?.text = it.topicDesc
            topDesc?.text = it.topicTitle

            AvatarUtils.loadAvatarByUrl(imageBg, AvatarUtils.newParamsBuilder(it.topicURL)
                    .setSizeType(ImageUtils.SIZE.SIZE_160)
                    .setBlur(true)
                    .build())

            AvatarUtils.loadAvatarByUrl(topicCover, AvatarUtils.newParamsBuilder(it.topicURL)
                    .setSizeType(ImageUtils.SIZE.SIZE_160)
                    .setCornerRadius(8.dp().toFloat())
                    .build())

            AvatarUtils.loadAvatarByUrl(topAreaBg, AvatarUtils.newParamsBuilder(it.topicURL)
                    .setSizeType(ImageUtils.SIZE.SIZE_160)
                    .setBlur(true)
                    .build())

        }
    }

    override fun onRequestSucess(hasMore: Boolean) {
        smartRefresh?.finishLoadMore()
        smartRefresh?.finishRefresh()
    }

    private fun getTopicTabs() {
        launch {
            val result = subscribe(RequestControl("getTopicTabs", ControlType.CancelThis)) {
                postsWatchServerApi.getTopicTabs(topicInfo?.topicID ?: 0)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("tabs"), PostsTopicTabModel::class.java)
                showTopicTabs(list)
            } else {

            }
        }
    }

    private fun showTopicTabs(list: List<PostsTopicTabModel>?) {
        if (list == null || list.isEmpty()) {
            return
        }

        topicTab?.apply {
            setCustomTabView(R.layout.posts_topic_tab_view_layout, R.id.tab_tv)
            setSelectedIndicatorColors(Color.parseColor("#FFC15B"))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
            setIndicatorBottomMargin(3f.dp())
            setIndicatorWidth(22f.dp())
            setSelectedIndicatorThickness(4f.dp().toFloat())
            setIndicatorCornorRadius(2f.dp().toFloat())
        }
        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {

                val tabModel = list[position]
                if (!topicPostsViews.containsKey(tabModel.tabType)) {
                    topicPostsViews[tabModel.tabType] = TopicPostsWatchView(this@PostsTopicActivity, topicInfo, tabModel, this@PostsTopicActivity)
                }
                val view = topicPostsViews[tabModel.tabType]
                if (position == 0) {
                    view?.initPostsList(false)
                }
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return list.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return list[position].tabDesc

            }
        }

        topicTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                var tagModel = list[position]
                topicPostsViews[tagModel.tabType]?.initPostsList(false)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        topicVp?.adapter = pagerAdapter
        topicTab?.setViewPager(topicVp)
        pagerAdapter?.notifyDataSetChanged()
    }

    override fun destroy() {
        super.destroy()
        if (topicPostsViews != null) {
            for ((_, topicWatchView) in topicPostsViews) {
                topicWatchView.destory()
            }
        }
        topicPostsViews.clear()
    }

    override fun useEventBus(): Boolean {
        return false
    }

}
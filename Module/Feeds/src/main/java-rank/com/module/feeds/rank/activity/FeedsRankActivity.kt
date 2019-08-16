package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.module.feeds.R
import com.common.view.viewpager.SlidingTabLayout
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.make.FROM_CHALLENGE
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.model.FeedRankInfoModel
import com.module.feeds.rank.model.FeedRankTagModel
import com.module.feeds.rank.view.FeedsRankView
import com.module.feeds.songmanage.view.FeedDraftsView
import kotlinx.coroutines.launch

/**
 * 神曲打榜 排行榜
 */
@Route(path = RouterConstants.ACTIVITY_FEEDS_RANK)
class FeedsRankActivity : BaseActivity() {

    private lateinit var mTitlebar: CommonTitleBar
    private lateinit var mSearchFeedIv: ExTextView
    private lateinit var mTagTab: SlidingTabLayout
    private lateinit var mViewpager: ViewPager
    private lateinit var mPagerAdapter: PagerAdapter

    var mFeedRankViews: HashMap<Int, FeedsRankView> = HashMap()
    val mFeedDraftsView: FeedDraftsView by lazy { FeedDraftsView(this, FROM_CHALLENGE) }

    private val mFeedRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 避免不停地进主页又从神曲进主页
        var num = 0
        for (i in U.getActivityUtils().activityList.size - 1 downTo 0) {
            val ac = U.getActivityUtils().activityList[i]
            if (ac is FeedsRankActivity) {
                num++
                if (num >= 2) {
                    ac.finish()
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_rank_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = findViewById(R.id.titlebar)
        mSearchFeedIv = findViewById(R.id.search_feed_iv)
        mTagTab = findViewById(R.id.tag_tab)
        mViewpager = findViewById(R.id.viewpager)

        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        mSearchFeedIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 跳到搜索页面
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_SEARCH)
                        .navigation()
            }
        })

        getFeedsRankTags()
    }

    private fun getFeedsRankTags() {
        launch {
            val result = subscribe { mFeedRankServerApi.getFeedsRankTags() }
            if (result?.errno == 0) {
                val list = JSON.parseArray(result.data.getString("tags"), FeedRankTagModel::class.java)
                showFeedRankTag(list)
            } else {
                showFailed()
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    fun showFeedRankTag(list: List<FeedRankTagModel>?) {
        if (list == null || list.isEmpty()) {
            return
        }

        mTagTab.setCustomTabView(R.layout.feed_rank_tab_view, R.id.tab_tv)
        mTagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        mTagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER)
        mTagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mTagTab.setIndicatorWidth(80f.dp())
        mTagTab.setSelectedIndicatorThickness(24.dp().toFloat())
        mTagTab.setIndicatorCornorRadius(12.dp().toFloat())
        mPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position < list.size) {
                    val rankTagModel = list[position]
                    if (!mFeedRankViews.containsKey(rankTagModel.tagType)) {
                        mFeedRankViews[rankTagModel.tagType
                                ?: 0] = FeedsRankView(this@FeedsRankActivity, rankTagModel)
                    }
                    val view = mFeedRankViews[rankTagModel.tagType]
                    if (position == 0) {
                        view?.tryLoadData()
                    }
                    if (container.indexOfChild(view) == -1) {
                        container.addView(view)
                    }
                    return view!!
                } else {
                    if (container.indexOfChild(mFeedDraftsView) == -1) {
                        container.addView(mFeedDraftsView)
                    }
                    return mFeedDraftsView
                }

            }

            override fun getCount(): Int {
                return list.size + 1
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position < list.size) {
                    return list[position].tagDesc
                } else {
                    return "草稿箱"
                }
            }
        }

        mTagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position < list.size) {
                    var tagModel = list[position]
                    mFeedRankViews[tagModel.tagType]?.tryLoadData()
                } else {
                    mFeedDraftsView.tryLoadData()
                }

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mViewpager.adapter = mPagerAdapter
        mTagTab.setViewPager(mViewpager)
        mPagerAdapter.notifyDataSetChanged()
    }

    fun showFailed() {

    }

    override fun destroy() {
        super.destroy()
        if (mFeedRankViews.isNotEmpty()) {
            for ((_, feedRankView) in mFeedRankViews) {
                feedRankView.destory()
            }
        }
        mFeedRankViews.clear()
        mFeedDraftsView.destroy()
    }
}

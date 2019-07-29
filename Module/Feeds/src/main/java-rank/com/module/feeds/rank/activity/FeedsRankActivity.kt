package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.module.feeds.R
import com.common.view.viewpager.SlidingTabLayout
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.model.FeedRankTagModel
import com.module.feeds.rank.view.FeedsRankView
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

    private val mFeedRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

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
            }
        })
        
        getFeedsRankTags()
    }

    private fun getFeedsRankTags() {
        launch {
            val result = mFeedRankServerApi.getFeedsRankTags()
            if (result.errno == 0) {
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
            }

            override fun getCount(): Int {
                return list.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return list[position].tagDesc
            }
        }

        mTagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                var tagModel = list[position]
                mFeedRankViews[tagModel.tagType]?.tryLoadData()
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

}

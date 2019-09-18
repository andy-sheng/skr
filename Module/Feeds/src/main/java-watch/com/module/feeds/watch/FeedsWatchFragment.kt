package com.module.feeds.watch

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.base.INVISIBLE_REASON_IN_VIEWPAGER
import com.common.base.INVISIBLE_REASON_TO_DESKTOP
import com.common.base.INVISIBLE_REASON_TO_OTHER_ACTIVITY
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.busilib.event.FeedWatchTabRefreshEvent
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.view.FeedsCollectView
import com.module.feeds.watch.watchview.FollowWatchView
import com.module.feeds.watch.watchview.RecommendWatchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates

class FeedsWatchFragment : BaseFragment() {
    val BACKGROUNG_MSG = 0
    val PREF_KEY_SHOW_GUIDE = "need_show_watch_guide"
    private lateinit var mNavigationBgIv: ImageView
    private lateinit var mDivider: View
    private lateinit var mFeedChallengeTv: ExTextView
    private lateinit var mFeedTab: SlidingTabLayout
    private lateinit var mFeedVp: NestViewPager
    private lateinit var mTabPagerAdapter: PagerAdapter

    val mFollowFeedsView: FollowWatchView by lazy { FollowWatchView(this) }       //关注
    val mRecommendFeedsView: RecommendWatchView by lazy { RecommendWatchView(this) }   //推荐
    val mFeedsCollectView: FeedsCollectView by lazy { FeedsCollectView(this) } //喜欢
    val initPostion = 1
    // 保持 init Postion 一致
    var mPagerPosition: Int by Delegates.observable(initPostion, { _, oldPositon, newPosition ->
        // 为了解决滑动卡顿
        launch(Dispatchers.Main) {
            delay(400)
            when (oldPositon) {
                0 -> {
                    mFollowFeedsView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
                1 -> {
                    mRecommendFeedsView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
                2 -> {
                    mFeedsCollectView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
            }
            onViewSelected(newPosition)
        }
    })

    override fun initView(): Int {
        return R.layout.feeds_watch_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mNavigationBgIv = rootView.findViewById(R.id.navigation_bg_iv)
        mDivider = rootView.findViewById(R.id.divider)
        mFeedTab = rootView.findViewById(R.id.feed_tab)
        mFeedChallengeTv = rootView.findViewById(R.id.feed_challenge_tv)
        mFeedVp = rootView.findViewById(R.id.feed_vp)

        mFeedChallengeTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 打榜 神曲榜
                StatisticsAdapter.recordCountEvent("music_tab", "publish", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SELECT_MODE)
                        .navigation()
            }
        })

        mFeedTab.apply {
            setCustomTabView(R.layout.feed_tab_view_layout, R.id.tab_tv)
            setSelectedIndicatorColors(U.getColor(R.color.black_trans_80))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
            setTitleSize(14f)
            setSelectedTitleSize(24f)
            setIndicatorWidth(16f.dp())
            setSelectedIndicatorThickness(4f.dp().toFloat())
            setIndicatorCornorRadius(2f.dp().toFloat())
        }

        mTabPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                MyLog.d(TAG, "instantiateItem container=$container position=$position")
                var view: View? = when (position) {
                    0 -> mFollowFeedsView
                    1 -> mRecommendFeedsView
                    2 -> mFeedsCollectView
                    else -> null
                }
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "关注"
                    1 -> "推荐"
                    2 -> "收藏"
                    else -> super.getPageTitle(position)
                }
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        mFeedTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mFeedTab.notifyDataChange()
                mPagerPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mFeedVp.adapter = mTabPagerAdapter
        mFeedTab.setViewPager(mFeedVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mFeedVp?.setCurrentItem(initPostion, false)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        StatisticsAdapter.recordCountEvent("music_tab", "music_tab_expose", null)
        onViewSelected(mFeedVp.currentItem)
    }

    fun onViewSelected(pos: Int) {
        if (!this.fragmentVisible) {
            return
        }
        when (pos) {
            0 -> {
                StatisticsAdapter.recordCountEvent("music_tab", "follow_tab_expose", null)
                mFollowFeedsView.selected()
            }
            1 -> {
                StatisticsAdapter.recordCountEvent("music_tab", "recommend_tab_expose", null)
                mRecommendFeedsView.selected()
            }
            2 -> {
                StatisticsAdapter.recordCountEvent("music_tab", "like_tab_expose", null)
                mFeedsCollectView.selected()
            }
        }
    }

    override fun onFragmentInvisible(reason: Int) {
        super.onFragmentInvisible(reason)
        MyLog.d(TAG, "onFragmentInvisible reason=$reason")
        var r = UNSELECT_REASON_SLIDE_OUT
        if (reason == INVISIBLE_REASON_IN_VIEWPAGER) {
            r = UNSELECT_REASON_TO_OTHER_TAB
        } else if (reason == INVISIBLE_REASON_TO_OTHER_ACTIVITY) {
            r = UNSELECT_REASON_TO_OTHER_ACTIVITY
        } else if (reason == INVISIBLE_REASON_TO_DESKTOP) {
            r = UNSELECT_REASON_TO_DESKTOP
        }
        // 返回桌面
        when (mPagerPosition) {
            0 -> {
                mFollowFeedsView.unselected(r)
            }
            1 -> {
                mRecommendFeedsView.unselected(r)
            }
            2 -> {
                mFeedsCollectView.unselected(r)
            }
        }
        if (reason != INVISIBLE_REASON_TO_OTHER_ACTIVITY) {
            // 滑走导致的不可见
            FeedsPlayStatistics.setCurPlayMode(0, FeedPage.UNKNOW, 0)
            FeedsPlayStatistics.tryUpload(true)
        } else {

        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    override fun isBlackStatusBarText(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        mRecommendFeedsView.destroy()
        mFollowFeedsView.destroy()
        mFeedsCollectView.destory()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(refreshEvent: FeedWatchTabRefreshEvent) {
        // 首页重复点击了神曲，自动刷新一下吧
        if (mFeedVp.currentItem == 0) {
            // 关注自动刷新
            mFollowFeedsView.autoRefresh()
        } else if (mFeedVp.currentItem == 1) {
            // 推荐自动刷新
            mRecommendFeedsView.autoRefresh()
        }
    }
}

const val UNSELECT_REASON_SLIDE_OUT = 1 // tab滑走
const val UNSELECT_REASON_TO_DESKTOP = 2  // 到桌面
const val UNSELECT_REASON_TO_OTHER_ACTIVITY = 3 // 到别的activity
const val UNSELECT_REASON_TO_OTHER_TAB = 4//  feed tab滑走

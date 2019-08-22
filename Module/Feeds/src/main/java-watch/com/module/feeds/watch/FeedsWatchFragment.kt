package com.module.feeds.watch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.busilib.event.FeedWatchTabRefreshEvent
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.view.FeedsCollectView
import com.module.feeds.watch.watchview.FeedRecommendView
import com.module.feeds.watch.watchview.FollowWatchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates

class FeedsWatchFragment : BaseFragment() {
    val BACKGROUNG_MSG = 0
    private lateinit var mNavigationBgIv: ImageView
    private lateinit var mDivider: View
    private lateinit var mFeedChallengeTv: ExTextView
    private lateinit var mFeedPlaylistTv: ExTextView
    private lateinit var mFeedTab: SlidingTabLayout
    private lateinit var mFeedVp: NestViewPager
    private lateinit var mTabPagerAdapter: PagerAdapter
    private var isBackground = false   // 是否在后台

    val mFollowFeesView: FollowWatchView by lazy { FollowWatchView(this) }       //关注
    val mRecommendFeedsView: FeedRecommendView by lazy { FeedRecommendView(this) }   //推荐
    val mFeedsCollectView: FeedsCollectView by lazy { FeedsCollectView(this) } //喜欢

    val mUiHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == BACKGROUNG_MSG) {
                if (!isBackground) {
                    mFeedsCollectView.unselected()
                    mRecommendFeedsView.unselected()
                }
            }
        }
    }

    val initPostion = 1
    // 保持 init Postion 一致
    var mPagerPosition: Int by Delegates.observable(initPostion, { _, oldPositon, newPosition ->
        // 为了解决滑动卡顿
        launch(Dispatchers.Main) {
            delay(400)
            when (oldPositon) {
                0 -> {
                    mFollowFeesView.unselected()
                }
                1 -> {
                    mRecommendFeedsView.unselected()
                }
                2 -> {
                    mFeedsCollectView.unselected()
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
        mFeedPlaylistTv = rootView.findViewById(R.id.feed_playlist_tv)
        mFeedVp = rootView.findViewById(R.id.feed_vp)

        mFeedChallengeTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 打榜 神曲榜
                StatisticsAdapter.recordCountEvent("music_tab", "publish", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SELECT_MODE)
                        .navigation()
            }
        })

        mFeedPlaylistTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG)
                        .withInt("from", 1)
                        .navigation()
            }
        })

        mFeedTab.apply {
            setCustomTabView(R.layout.feed_tab_view_layout, R.id.tab_tv)
            setSelectedIndicatorColors(U.getColor(R.color.black_trans_80))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
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
                    0 -> mFollowFeesView
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
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        mUiHandler.removeMessages(BACKGROUNG_MSG)
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
                mDivider.visibility = View.VISIBLE
                mFollowFeesView.selected()
            }
            1 -> {
                StatisticsAdapter.recordCountEvent("music_tab", "recommend_tab_expose", null)
                mDivider.visibility = View.GONE
                mRecommendFeedsView.selected()
            }
            2 -> {
                StatisticsAdapter.recordCountEvent("music_tab", "like_tab_expose", null)
                mDivider.visibility = View.GONE
                mFeedsCollectView.selected()
            }
        }
    }

    override fun onFragmentInvisible(from: Int) {
        super.onFragmentInvisible(from)
        MyLog.d(TAG, "onFragmentInvisible from=$from")
        mFollowFeesView.unselected()
        //todo 因为切后台的事件会比不可见晚
        mUiHandler.sendEmptyMessageDelayed(BACKGROUNG_MSG, 200)
        if (from == 2) {
            FeedsPlayStatistics.setCurPlayMode(0)
            FeedsPlayStatistics.tryUpload(true)
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
        mFollowFeesView.destroy()
        mFeedsCollectView.destory()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        isBackground = !event.foreground
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(refreshEvent: FeedWatchTabRefreshEvent) {
        // 首页重复点击了神曲，自动刷新一下吧
        if (mFeedVp.currentItem == 0) {
            // 关注自动刷新
            mFollowFeesView.autoRefresh()
        } else if (mFeedVp.currentItem == 1) {
            // 推荐自动刷新
            mRecommendFeedsView.autoRefresh()
        }
    }
}

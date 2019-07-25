package com.module.feeds.watch

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.module.feeds.R
import com.module.feeds.watch.view.FeedsLikeView
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.common.view.ex.ExTextView
import com.component.feeds.view.FeedsWatchView

class FeedsWatchFragment : BaseFragment() {

    private lateinit var mNavigationBgIv: ImageView
    private lateinit var mFeedChallengeTv: ExTextView
    private lateinit var mFeedTab: SlidingTabLayout
    private lateinit var mFeedVp: NestViewPager
    private lateinit var mTabPagerAdapter: PagerAdapter

    val mRecommendFeedsView: FeedsWatchView by lazy { FeedsWatchView(this, FeedsWatchView.TYPE_RECOMMEND) }   //推荐
    val mFollowFeesView: FeedsWatchView by lazy { FeedsWatchView(this, FeedsWatchView.TYPE_FOLLOW) }       //关注
    val mFeedsCollectView: FeedsLikeView by lazy { FeedsLikeView(this) } //喜欢

    override fun initView(): Int {
        return R.layout.feeds_watch_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mNavigationBgIv = rootView.findViewById(R.id.navigation_bg_iv)
        mFeedTab = rootView.findViewById(R.id.feed_tab)
        mFeedChallengeTv = rootView.findViewById(R.id.feed_challenge_tv)
        mFeedVp = rootView.findViewById(R.id.feed_vp)

        mFeedChallengeTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 发起挑战
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
                    0 -> mRecommendFeedsView
                    1 -> mFollowFeesView
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
                    0 -> "推荐"
                    1 -> "关注"
                    2 -> "喜欢"
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
                onViewSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mFeedVp.adapter = mTabPagerAdapter
        mFeedTab.setViewPager(mFeedVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mFeedVp.setCurrentItem(1, false)
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        onViewSelected(mFeedVp.currentItem)
    }

    fun onViewSelected(pos:Int){
        if(!this.fragmentVisible){
            return
        }
        when (pos) {
            0 -> {
                mFollowFeesView.stopPlay()
                mFeedsCollectView.stopPlay()
                mRecommendFeedsView.initData(false)
            }
            1 -> {
                mRecommendFeedsView.stopPlay()
                mFeedsCollectView.stopPlay()
                mFollowFeesView.initData(false)
            }
            2 -> {
                mFollowFeesView.stopPlay()
                mRecommendFeedsView.stopPlay()
                mFeedsCollectView.initData(false)
            }
        }
    }
    override fun onFragmentInvisible() {
        super.onFragmentInvisible()
        mFollowFeesView.stopPlay()
        mRecommendFeedsView.stopPlay()
        mFeedsCollectView.stopPlay()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    override fun isBlackStatusBarText(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        mRecommendFeedsView.destory()
        mFollowFeesView.destory()
        mFeedsCollectView.destory()
    }
}

package com.module.posts.watch

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.base.BaseFragment
import com.common.base.INVISIBLE_REASON_IN_VIEWPAGER
import com.common.base.INVISIBLE_REASON_TO_DESKTOP
import com.common.base.INVISIBLE_REASON_TO_OTHER_ACTIVITY
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.posts.R
import com.module.posts.watch.view.FollowPostsWatchView
import com.module.posts.watch.view.LastPostsWatchView
import com.module.posts.watch.view.RecommendPostsWatchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class PostsWatchFragment : BaseFragment() {

    private var title: CommonTitleBar? = null
    private var navigationBgIv: ImageView? = null
    private var divider: View? = null
    private var postsTab: SlidingTabLayout? = null
    private var postsChallengeTv: ExTextView? = null
    private var postsVp: NestViewPager? = null

    private var tabPagerAdapter: PagerAdapter? = null

    val followPostsWatchView: FollowPostsWatchView by lazy { FollowPostsWatchView(this) }
    val recommendPostsWatchView: RecommendPostsWatchView by lazy { RecommendPostsWatchView(this) }
    val lastPostsWatchView: LastPostsWatchView by lazy { LastPostsWatchView(this) }

    val initPostion = 1  // 默认推荐
    // 保持 init Postion 一致
    var mPagerPosition: Int by Delegates.observable(initPostion, { _, oldPositon, newPosition ->
        // 为了解决滑动卡顿
        launch(Dispatchers.Main) {
            delay(400)
            when (oldPositon) {
                0 -> {
                    followPostsWatchView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
                1 -> {
                    recommendPostsWatchView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
                2 -> {
                    lastPostsWatchView.unselected(UNSELECT_REASON_SLIDE_OUT)
                }
            }
            onViewSelected(newPosition)
        }
    })

    override fun initView(): Int {
        return R.layout.posts_watch_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        title = rootView.findViewById(R.id.title)
        navigationBgIv = rootView.findViewById(R.id.navigation_bg_iv)
        divider = rootView.findViewById(R.id.divider)
        postsTab = rootView.findViewById(R.id.posts_tab)
        postsChallengeTv = rootView.findViewById(R.id.posts_challenge_tv)
        postsVp = rootView.findViewById(R.id.posts_vp)

        postsTab?.apply {
            setCustomTabView(R.layout.post_tab_view_layout, R.id.tab_tv)
            setSelectedIndicatorColors(U.getColor(R.color.black_trans_80))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
            setTitleSize(14f)
            setSelectedTitleSize(24f)
            setIndicatorWidth(16f.dp())
            setSelectedIndicatorThickness(4f.dp().toFloat())
            setIndicatorCornorRadius(2f.dp().toFloat())
        }

        tabPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                MyLog.d(TAG, "instantiateItem container=$container position=$position")
                var view: View? = when (position) {
                    0 -> followPostsWatchView
                    1 -> recommendPostsWatchView
                    2 -> lastPostsWatchView
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
                    2 -> "最新"
                    else -> super.getPageTitle(position)
                }
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        postsTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                postsTab?.notifyDataChange()
                mPagerPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        postsVp?.adapter = tabPagerAdapter
        postsTab?.setViewPager(postsVp)
        tabPagerAdapter?.notifyDataSetChanged()
        postsVp?.setCurrentItem(initPostion, false)
    }

    fun onViewSelected(pos: Int) {
        if (!this.fragmentVisible) {
            return
        }
        when (pos) {
            0 -> followPostsWatchView.selected()
            1 -> recommendPostsWatchView.selected()
            2 -> lastPostsWatchView.selected()
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        postsVp?.currentItem?.let { onViewSelected(it) }

    }

    override fun onFragmentInvisible(reason: Int) {
        super.onFragmentInvisible(reason)
        MyLog.d(TAG, "onFragmentInvisible reason=$reason")
        var r = UNSELECT_REASON_SLIDE_OUT
        // 滑走导致的不可见  返回桌面 返回桌面
        when (reason) {
            INVISIBLE_REASON_IN_VIEWPAGER -> r = UNSELECT_REASON_TO_OTHER_TAB
            INVISIBLE_REASON_TO_OTHER_ACTIVITY -> r = UNSELECT_REASON_TO_OTHER_ACTIVITY
            INVISIBLE_REASON_TO_DESKTOP -> r = UNSELECT_REASON_TO_DESKTOP
        }
        when (mPagerPosition) {
            0 -> followPostsWatchView.unselected(r)
            1 -> recommendPostsWatchView.unselected(r)
            2 -> lastPostsWatchView.unselected(r)
        }
    }

    override fun isBlackStatusBarText(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

const val UNSELECT_REASON_SLIDE_OUT = 1 // tab滑走
const val UNSELECT_REASON_TO_DESKTOP = 2  // 到桌面
const val UNSELECT_REASON_TO_OTHER_ACTIVITY = 3 // 到别的activity
const val UNSELECT_REASON_TO_OTHER_TAB = 4//  feed tab滑走

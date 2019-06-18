package com.module.home.game

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.home.R
import com.module.home.game.view.DoubleRoomGameView
import com.module.home.game.view.FriendRoomGameView
import com.module.home.game.view.QuickGameView

class GameFragment3 : BaseFragment() {

    lateinit var mContent: LinearLayout
    lateinit var mGameTab: SlidingTabLayout
    lateinit var mGameVp: NestViewPager

    lateinit var mTabPagerAdapter: PagerAdapter

    val mFriendRoomGameView: FriendRoomGameView by lazy { FriendRoomGameView(context!!) }
    val mQuickGameView: QuickGameView by lazy { QuickGameView(this) }
    val mDoubleRoomGameView: DoubleRoomGameView by lazy { DoubleRoomGameView(context!!) }

    override fun initView(): Int {
        return R.layout.game3_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mContent = mRootView.findViewById<View>(R.id.content) as LinearLayout
        mGameTab = mRootView.findViewById<View>(R.id.game_tab) as SlidingTabLayout
        mGameVp = mRootView.findViewById<View>(R.id.game_vp) as NestViewPager

        mGameTab?.setCustomTabView(R.layout.game_tab_view_layout, R.id.tab_tv)
        mGameTab?.setSelectedIndicatorColors(Color.WHITE)
        mGameTab?.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        mGameTab?.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mGameTab?.setTitleSize(14f)
        mGameTab?.setSelectedTilleSize(20f)
        mGameTab?.setIndicatorWidth(U.getDisplayUtils().dip2px(16f))
        mGameTab?.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(4f).toFloat())
        mGameTab?.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(2f).toFloat())

        mTabPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                MyLog.d(TAG, "instantiateItem container=$container position=$position")
                var view: View? = if (position == 0) {
                    mFriendRoomGameView
                } else if (position == 1) {
                    mQuickGameView
                } else if (position == 2) {
                    mDoubleRoomGameView
                } else {
                    null
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
                if (position == 0) {
                    return "好友房"
                } else if (position == 1) {
                    return "快速游戏"
                } else if (position == 2) {
                    return "双人唱聊"
                }
                return super.getPageTitle(position)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        mGameTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mGameTab.notifyDataChange()

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mGameVp.setAdapter(mTabPagerAdapter)
        mGameTab.setViewPager(mGameVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mGameVp.setCurrentItem(1, false)
        mQuickGameView?.initData()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun isInViewPager(): Boolean {
        return true
    }
}


package com.module.playways.party.room.activity

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.party.room.view.PartyGameListView
import com.module.playways.party.room.view.PartyHasSelectedGameListView

@Route(path = RouterConstants.ACTIVITY_PARTY_SELECT_GAME)
class PartySelectGameActivity : BaseActivity() {
    lateinit var titlebar: CommonTitleBar
    lateinit var viewPager: NestViewPager
    lateinit var tagTab: SlidingTabLayout
    lateinit var mPagerAdapter: PagerAdapter

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.party_select_game_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        tagTab = findViewById(R.id.sliding_tab_layout)
        viewPager = findViewById(R.id.viewPager)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        tagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        mPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                MyLog.d(TAG, "instantiateItem container=$container position=$position")

                var view: View? = when (position) {
                    0 -> PartyGameListView(this@PartySelectGameActivity)
                    1 -> PartyHasSelectedGameListView(this@PartySelectGameActivity)
                    else -> null
                }

                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }

                return view!!
            }

            override fun getCount(): Int {
                return 2
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "游戏"
                    1 -> "已点"
                    else -> ""
                }
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = viewPager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is PartyGameListView) {
                        view.selected()
                    } else if (view is PartyHasSelectedGameListView) {
                        view.selected()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewPager.adapter = mPagerAdapter
        tagTab.setViewPager(viewPager)
        mPagerAdapter.notifyDataSetChanged()

        viewPager.currentItem = 0
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
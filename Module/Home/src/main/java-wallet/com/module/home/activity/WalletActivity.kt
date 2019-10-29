package com.module.home.activity

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.R
import com.module.home.fragment.DiamondBallanceFragment
import com.module.home.fragment.InComeFragment

@Route(path = RouterConstants.ACTIVITY_WALLET)
class WalletActivity : BaseActivity() {
    lateinit var mainActContainer: ConstraintLayout
    lateinit var titlebar: CommonTitleBar
    lateinit var slidingTab: SlidingTabLayout
    lateinit var viewPager: NestViewPager
    private var tabPagerAdapter: FragmentPagerAdapter? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.wallet_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mainActContainer = findViewById(R.id.main_act_container)
        titlebar = findViewById(R.id.titlebar)
        slidingTab = findViewById(R.id.slidingTab)
        viewPager = findViewById(R.id.viewPager)

        slidingTab?.apply {
            setCustomTabView(R.layout.ranked_tab_view, R.id.tab_tv)
            setSelectedIndicatorColors(Color.parseColor("#596CCC"))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
            setIndicatorWidth(U.getDisplayUtils().dip2px(56f))
            setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
            setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
            setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        }

        titlebar?.leftTextView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                finish()
            }
        })

        tabPagerAdapter = object : FragmentPagerAdapter(getSupportFragmentManager()) {
            override fun getItem(position: Int): Fragment {
                MyLog.d(TAG, "getItem position=$position")
                if (position == 0) {
                    return DiamondBallanceFragment()
                } else if (position == 1) {
                    return InComeFragment()
                }
                return InComeFragment()
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "余额"
                    1 -> "收益"
                    else -> super.getPageTitle(position)
                }
            }
        }

        slidingTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                slidingTab?.notifyDataChange()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewPager?.adapter = tabPagerAdapter
        slidingTab?.setViewPager(viewPager)
        tabPagerAdapter?.notifyDataSetChanged()
        viewPager?.setCurrentItem(0, false)
    }

    override fun canSlide(): Boolean {
        return false
    }


    override fun useEventBus(): Boolean {
        return false
    }
}

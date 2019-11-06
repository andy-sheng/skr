package com.module.mall.activity

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.R
import com.module.mall.view.EffectView

@Route(path = RouterConstants.ACTIVITY_MALL_MALL)
class MallActivity : BaseActivity() {
    lateinit var title: CommonTitleBar
    lateinit var btnBack: ImageView
    lateinit var mallTv: ExTextView
    lateinit var effectView: EffectView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager

    var pagerAdapter: PagerAdapter? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mall_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        title = findViewById(R.id.title)
        btnBack = findViewById(R.id.btn_back)
        mallTv = findViewById(R.id.mall_tv)
        effectView = findViewById(R.id.effect_view)
        tagTab = findViewById(R.id.tag_tab)
        viewpager = findViewById(R.id.viewpager)

        tagTab.setCustomTabView(R.layout.mall_pager_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())

        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return ""
            }

            override fun getCount(): Int {
                return 4
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return ""
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = viewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is EffectView) {
                        view.tryLoad()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)
    }
}
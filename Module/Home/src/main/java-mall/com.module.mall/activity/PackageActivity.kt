package com.module.mall.activity

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.R
import com.module.mall.view.EffectView
import com.module.mall.view.PackageView
import com.module.mall.view.ProductView

@Route(path = RouterConstants.ACTIVITY_MALL_PACKAGE)
class PackageActivity : BaseActivity() {
    lateinit var title: CommonTitleBar
    lateinit var btnBack: ImageView
    lateinit var mallTv: ExTextView
    lateinit var effectView: EffectView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager

    var pagerAdapter: PagerAdapter? = null
    var viewList: ArrayList<PackageView>? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.package_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
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

        viewList = ArrayList()
        viewList?.add(PackageView(this))
        viewList?.add(PackageView(this))
        viewList?.add(PackageView(this))
        viewList?.add(PackageView(this))

        mallTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_MALL)
                    .navigation()
        }

        btnBack.setDebounceViewClickListener {
            finish()
        }

        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = viewList?.get(position)
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return 4
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "精选套装"
                    1 -> "演唱声纹"
                    2 -> "头像框"
                    3 -> "舞台"
                    else -> super.getPageTitle(position)
                }
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = viewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is ProductView) {
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

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
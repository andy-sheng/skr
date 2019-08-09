package com.module.msg.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.msg.follow.LastFollowFragment
import io.rong.imkit.R

//互动通知界面
@Route(path = RouterConstants.ACTIVITY_INTERACTION)
class InteractionActivity : BaseActivity() {
    var mRelationTab: SlidingTabLayout? = null
    var mRelationVp: NestViewPager? = null
    internal var mTabPagerAdapter: FragmentPagerAdapter? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.interaction_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mRelationTab = findViewById<View>(R.id.relation_tab) as SlidingTabLayout
        mRelationVp = findViewById<View>(R.id.relation_vp) as NestViewPager

        val mTitleBar = findViewById<CommonTitleBar>(R.id.titlebar)
        mTitleBar.leftTextView?.setOnClickListener {
            finish()
        }

        mRelationTab?.setCustomTabView(R.layout.interaction_tab_view, R.id.tab_tv)
        mRelationTab?.setSelectedIndicatorColors(U.getColor(com.component.busilib.R.color.black_trans_20))
        mRelationTab?.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        mRelationTab?.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mRelationTab?.setIndicatorWidth(U.getDisplayUtils().dip2px(87f))
        mRelationTab?.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12f))
        mRelationTab?.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28f).toFloat())
        mRelationTab?.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14f).toFloat())

        val service = ModuleServiceManager.getInstance().feedsService
        mTabPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                MyLog.d(TAG, "getItem position=$position")
                if (position == 0) {
                    return LastFollowFragment()
                } else if (position == 1) {
                    return service.refuseCommentFragment
                } else if (position == 2) {
                    return service.likeWorkFragment
                }
                return LastFollowFragment()
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "最新关注"
                    1 -> "评论"
                    2 -> "获赞"
                    else -> super.getPageTitle(position)
                }
            }

            override fun getCount(): Int = 3
        }

        mRelationVp?.setOffscreenPageLimit(3);
        mRelationVp?.setAdapter(mTabPagerAdapter)
        mRelationTab?.setViewPager(mRelationVp)
        mTabPagerAdapter?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}

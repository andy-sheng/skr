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
import io.rong.imkit.R

//评论和赞的页面
@Route(path = RouterConstants.ACTIVITY_COMMENT_LIKE)
class CommentAndLikeActivity : BaseActivity() {
    var mRelationTab: SlidingTabLayout? = null
    var mRelationVp: NestViewPager? = null
    internal var mTabPagerAdapter: FragmentPagerAdapter? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.comment_like_fragment_layout
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
                return if (position == 0) {
                    service.refuseCommentFragment
                } else {
                    service.likeWorkFragment
                }
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "评论"
                    1 -> "获赞"
                    else -> super.getPageTitle(position)
                }
            }

            override fun getCount(): Int = 2
        }

        mRelationVp?.setOffscreenPageLimit(3);
        mRelationVp?.setAdapter(mTabPagerAdapter)
        mRelationTab?.setViewPager(mRelationVp)
        mTabPagerAdapter?.notifyDataSetChanged()
    }

    override fun onBackPressedForActivity(): Boolean {
        finish()
        return true
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

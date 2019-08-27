package com.module.playways.race.room.view.actor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData

/**
 * 展示竞演者的view
 */
class RaceActorPanelView(viewStub: ViewStub, val mRoomData: RaceRoomData) : ExViewStub(viewStub) {

    lateinit var placeHolderView: View
    lateinit var raceTitleStl: SlidingTabLayout
    lateinit var raceVp: NestViewPager

    lateinit var pagerAdapter: PagerAdapter

    var actorView: RaceActorView? = null   // 竞演者

    var mShowOrHideAnimator: ObjectAnimator? = null

    override fun init(parentView: View?) {

        placeHolderView = mParentView.findViewById(R.id.place_holder_view)
        raceVp = mParentView.findViewById(R.id.race_vp)
        raceTitleStl = mParentView.findViewById(R.id.race_title_stl)

        raceTitleStl.setCustomTabView(R.layout.race_actor_tab_view, R.id.tab_tv)
        raceTitleStl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        raceTitleStl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        raceTitleStl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        raceTitleStl.setIndicatorWidth(64.dp())
        raceTitleStl.setIndicatorBottomMargin(10.dp())
        raceTitleStl.setSelectedIndicatorThickness(24.dp().toFloat())
        raceTitleStl.setIndicatorCornorRadius(12.dp().toFloat())

        pagerAdapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return 1
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position == 0) {
                    // 竞演者
                    if (actorView == null) {
                        actorView = RaceActorView(raceVp.context, mRoomData)
                    }
                    if (container.indexOfChild(actorView) == -1) {
                        container.addView(actorView)
                    }
                    return actorView!!
                }
                return super.instantiateItem(container, position)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "竞演者"
                }
                return ""
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }
        }

        raceVp.adapter = pagerAdapter
        raceTitleStl.setViewPager(raceVp)
        pagerAdapter.notifyDataSetChanged()

        raceVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    // 竞演者
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        placeHolderView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                hide()
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.race_actor_panel_view_stub_layout
    }

    fun show() {
        tryInflate()
        actorView?.initData()
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator?.removeAllListeners()
            mShowOrHideAnimator?.cancel()
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat<View>(mParentView, View.TRANSLATION_Y, mParentView.height.toFloat(), 0f)
        mShowOrHideAnimator?.duration = 300
        mShowOrHideAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mParentView.visibility = View.VISIBLE
            }
        })
        mShowOrHideAnimator?.start()
    }

    fun hide() {
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator?.removeAllListeners()
            mShowOrHideAnimator?.cancel()
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat<View>(mParentView, View.TRANSLATION_Y, 0f, mParentView.height.toFloat())
        mShowOrHideAnimator?.duration = 300
        mShowOrHideAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mParentView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mParentView.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                mParentView.visibility = View.GONE
            }

        })
        mShowOrHideAnimator?.start()
    }
}
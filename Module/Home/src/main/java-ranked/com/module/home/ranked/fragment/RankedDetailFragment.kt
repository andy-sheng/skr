package com.module.home.ranked.fragment

import android.graphics.Color
import android.os.Bundle

import com.common.base.BaseFragment
import com.module.home.R
import com.module.home.ranked.model.RankHomeCardModel
import com.module.home.ranked.model.RankHomeCardModel.Companion.DUAN_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.POPULAR_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.REWARD_RANK_TYPE
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.home.ranked.view.RankedDetailView
import com.component.person.event.ShowPersonCenterEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RankedDetailFragment : BaseFragment() {

    lateinit var mContainer: ConstraintLayout
    lateinit var mTopImage: ImageView
    lateinit var mIvBack: ImageView
    lateinit var mIvRule: ImageView

    lateinit var mTitleStl: SlidingTabLayout
    lateinit var mRankedVp: NestViewPager
    lateinit var mPagerAdapter: PagerAdapter
    lateinit var mRankedDetailViews: HashMap<Int, RankedDetailView>

    private var mModel: RankHomeCardModel? = null
    private var mDialogPlus: DialogPlus? = null

    override fun initView(): Int {
        return R.layout.ranked_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mContainer = rootView.findViewById<View>(R.id.container) as ConstraintLayout
        mTopImage = rootView.findViewById<View>(R.id.top_image) as ImageView
        mIvBack = rootView.findViewById<View>(R.id.iv_back) as ImageView
        mIvRule = rootView.findViewById<View>(R.id.iv_rule) as ImageView
        mTitleStl = rootView.findViewById<View>(R.id.title_stl) as SlidingTabLayout
        mRankedVp = rootView.findViewById<View>(R.id.ranked_vp) as NestViewPager


        when (mModel?.rankType) {
            POPULAR_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#FAA100"))
                mTopImage?.setBackgroundResource(R.drawable.renqi_bj)
            }
            DUAN_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#576FE3"))
                mTopImage?.setBackgroundResource(R.drawable.duanwei_bj)
            }
            REWARD_RANK_TYPE -> {
                mContainer?.setBackgroundColor(Color.parseColor("#67BC65"))
                mTopImage?.setBackgroundResource(R.drawable.dashang_bj)
            }
            else -> {
                MyLog.w(TAG, "unknown rankType")
            }
        }

        initRankTag()

        mIvBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getFragmentUtils().popFragment(this@RankedDetailFragment)
            }

        })

        mIvRule.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showRankedRule()
            }
        })
    }

    private fun initRankTag() {
        mRankedDetailViews = HashMap()
        mTitleStl.setCustomTabView(R.layout.ranked_tab_view, R.id.tab_tv)
        mTitleStl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        mTitleStl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        mTitleStl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mTitleStl.setIndicatorWidth(U.getDisplayUtils().dip2px(56f))
        mTitleStl.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
        mTitleStl.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        mTitleStl.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())

        mPagerAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                var tagModel = mModel?.tabs!![position]
                var mRankedDetailView: RankedDetailView?
                if (mRankedDetailViews.containsKey(tagModel.rankID)) {
                    mRankedDetailView = mRankedDetailViews[tagModel.rankID]
                } else {
                    mRankedDetailView = RankedDetailView(context, tagModel)
                    mRankedDetailViews[tagModel.rankID] = mRankedDetailView
                }

                if (container.indexOfChild(mRankedDetailView) == -1) {
                    container.addView(mRankedDetailView)
                }

                if (position == 0) {
                    mRankedDetailView?.initData()
                }
                return mRankedDetailView!!
            }

            override fun getCount(): Int {
                return mModel?.tabs!!.size
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mModel?.tabs!![position].title
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        mRankedVp.adapter = mPagerAdapter
        mTitleStl.setViewPager(mRankedVp)
        mPagerAdapter.notifyDataSetChanged()

        mTitleStl.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                var tagModel = mModel?.tabs!![position]
                mRankedDetailViews[tagModel.rankID]?.initData()
            }

        })
    }

    private fun showRankedRule() {
        var viewResourceId: Int
        when (mModel?.rankType) {
            POPULAR_RANK_TYPE -> {
                viewResourceId = R.layout.ranked_rule_renqi_view_layout
            }
            DUAN_RANK_TYPE -> {
                viewResourceId = R.layout.ranked_rule_duan_view_layout
            }
            else -> {
                viewResourceId = R.layout.ranked_rule_reward_view_layout
            }
        }
        mDialogPlus = (DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(viewResourceId))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false))
                .setGravity(Gravity.CENTER)
                .create()
        mDialogPlus?.show()
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mModel = data as RankHomeCardModel?
        }
    }

    override fun destroy() {
        super.destroy()
        for ((key, view) in mRankedDetailViews) {
            view.destory()
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCenterEvent) {
        val bundle = Bundle()
        bundle.putInt("bundle_user_id", event.uid)
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                .with(bundle)
                .navigation()
    }
}

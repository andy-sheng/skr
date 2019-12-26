package com.module.club.rank

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_LIST_CLUB_RANK)
class ClubRankListActivity : BaseActivity() {
    lateinit var ivBack: ExImageView
    lateinit var ruleTv: TextView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager

    lateinit var adapter: ClubRankAdapter

    private var pagerAdapter: PagerAdapter? = null

    private var clubID: Int = 0
    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var map: HashMap<Int, ClubRankView> = HashMap()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_rank_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        clubID = intent.getIntExtra("clubID", 0)
        if (clubID == 0) {
            finish()
        }

        ivBack = this.findViewById(R.id.iv_back)
        ruleTv = this.findViewById(R.id.rule_tv)
        tagTab = this.findViewById(R.id.tag_tab)
        viewpager = this.findViewById(R.id.viewpager)

        ivBack.setDebounceViewClickListener {
            finish()
        }

        ruleTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("http://dev.app.inframe.mobi/rules"))
                    .navigation()
        }

        getClubRankTags()
    }

    private fun getClubRankTags() {
        launch {
            val result = subscribe(RequestControl("getClubRankTags", ControlType.CancelThis)) {
                clubServerApi.getClubRankTags()
            }
            if (result.errno == 0) {
                val tagsList = JSON.parseArray(result.data.getString("tabs"), ClubTagModel::class.java)
                showRankTags(tagsList)
            }
        }
    }

    private fun showRankTags(list: List<ClubTagModel>?) {
        if (list.isNullOrEmpty()) {
            return
        }

        tagTab.setCustomTabView(R.layout.club_rank_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(Color.parseColor("#9680FF"))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        tagTab.setIndicatorWidth(U.getDisplayUtils().dip2px(80f))
        tagTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f))
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val tagModel = list[position]
                if (!map.containsKey(tagModel.type)) {
                    map[tagModel.type] = ClubRankView(this@ClubRankListActivity, clubID, tagModel)
                }
                val view = map[tagModel.type]
                if (position == 0) {
                    view?.tryLoad()
                }
                view?.tag = position
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return list.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return list[position].tabDesc
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = viewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is ClubRankView) {
                        view.tryLoad()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)
        pagerAdapter?.notifyDataSetChanged()
    }

    override fun destroy() {
        super.destroy()
        if (map.isNotEmpty()) {
            for ((_, rankView) in map) {
                rankView.destory()
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}
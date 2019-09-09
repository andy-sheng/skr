package com.module.playways.battle.songlist

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.battle.BattleServerApi
import com.module.playways.battle.songlist.model.BattleRankInfoModel
import com.module.playways.battle.songlist.model.BattleRankTagModel
import com.module.playways.battle.songlist.view.BattleRankView
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_BATTLE_RANK)
class BattleRankActivity : BaseActivity() {

    lateinit var title: CommonTitleBar
    lateinit var selfArea: ExImageView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: NestViewPager
    lateinit var ivBack: ImageView

    val battleServerApi = ApiManager.getInstance().createService(BattleServerApi::class.java)
    var battleRankViews = HashMap<Int, BattleRankView>()

    var tagID = 0

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.battle_rank_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        tagID = intent.getIntExtra("tagID", 0)

        title = findViewById(R.id.title)
        selfArea = findViewById(R.id.self_area)
        tagTab = findViewById(R.id.tag_tab)
        viewpager = findViewById(R.id.viewpager)
        ivBack = findViewById(R.id.iv_back)

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        getBattleRankTags()
    }

    private fun getBattleRankTags() {
        launch {
            val result = subscribe(RequestControl("getBattleRankTags", ControlType.CancelThis)) {
                battleServerApi.getStandRankTag()
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("tabs"), BattleRankTagModel::class.java)
                showBattleRankTags(list)
            } else {
                //todo 失败怎么处理
            }
        }
    }

    private fun getMineRank(tag: BattleRankTagModel) {
        launch {
            val result = subscribe(RequestControl("getMineRank", ControlType.CancelThis)) {
                battleServerApi.getStandRankMine(MyUserInfoManager.getInstance().uid, tagID)
            }
            if (result.errno == 0) {
                val mineRank = JSON.parseObject(result.data.toJSONString(), BattleRankInfoModel::class.java)
                showMineRank(mineRank)
            } else {
                //todo 失败怎么处理
            }
        }
    }

    private fun showMineRank(mineRank: BattleRankInfoModel?) {

    }

    private fun showBattleRankTags(list: List<BattleRankTagModel>?) {
        if (list.isNullOrEmpty()) {
            return
        }

        tagTab.apply {
            setCustomTabView(R.layout.battle_rank_tab_view, R.id.tab_tv)
            setSelectedIndicatorColors(U.getColor(R.color.black_trans_10))
            setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER)
            setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
            setIndicatorWidth(68f.dp())
            setSelectedIndicatorThickness(24.dp().toFloat())
            setIndicatorCornorRadius(12.dp().toFloat())
        }

        val pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val rankTagModel = list[position]
                if (!battleRankViews.containsKey(rankTagModel.tabType)) {
                    battleRankViews[rankTagModel.tabType] = BattleRankView(this@BattleRankActivity, rankTagModel,tagID)
                }
                val view = battleRankViews[rankTagModel.tabType]
                if (position == 0) {
                    view?.tryLoadData()
                    getMineRank(rankTagModel)
                }
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
                var tagModel = list[position]
                battleRankViews[tagModel.tabType]?.tryLoadData()
                getMineRank(tagModel)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)
        pagerAdapter.notifyDataSetChanged()
    }

    override fun destroy() {
        super.destroy()
        if (battleRankViews.isNotEmpty()) {
            for ((_, battleRankView) in battleRankViews) {
                battleRankView.destory()
            }
        }
        battleRankViews.clear()
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
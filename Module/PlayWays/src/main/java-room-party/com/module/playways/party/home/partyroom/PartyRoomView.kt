package com.module.playways.party.home.partyroom

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.playways.IPartyRoomView
import com.module.playways.R
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.PartyRoomServerApi
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody

// 首页的剧场(也包括一下主题房吧)
class PartyRoomView(context: Context, val type: Int) : ConstraintLayout(context), IPartyRoomView, CoroutineScope by MainScope() {

    companion object {
        const val TYPE_GAME_HOME = 1  //首页
        const val TYPE_PARTY_HOME = 2  //主题房首页
    }

    private val quickKtvIv: ImageView
    private val quickGameIv: ImageView
    private val tagTab: SlidingTabLayout
    private val viewpager: ViewPager

    private var pagerAdapter: PagerAdapter? = null

    private val skrAudioPermission = SkrAudioPermission()

    private var views = HashMap<PartyRoomTagMode, PartyRoomItemView>()
    private var tagList = ArrayList<PartyRoomTagMode>()

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    private var lastLoadDateTime: Long = 0    //记录上次获取接口的时间

    init {
        View.inflate(context, R.layout.party_room_view_layout, this)

        quickKtvIv = this.findViewById(R.id.quick_ktv_iv)
        quickGameIv = this.findViewById(R.id.quick_game_iv)
        tagTab = this.findViewById(R.id.tag_tab)
        viewpager = this.findViewById(R.id.viewpager)

        quickKtvIv.setDebounceViewClickListener {
            // 嗨唱KTV快速加入
            StatisticsAdapter.recordCountEvent("party", "sing_access", null)
            skrAudioPermission.ensurePermission({
                quickJoinParty(1)
            }, true)
        }

        quickGameIv.setDebounceViewClickListener {
            // 游戏PK快速加入
            StatisticsAdapter.recordCountEvent("party", "game_access", null)
            skrAudioPermission.ensurePermission({
                quickJoinParty(2)
            }, true)
        }

        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val tagType = tagList[position]
                if (!views.containsKey(tagType)) {
                    views[tagType] = PartyRoomItemView(type, tagType, context)
                }
                val view = views[tagType]
                if (position == 0) {
                    view?.initData(false)
                }
                view?.tag = position
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return tagList.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return tagList[position].des
            }

            override fun getItemPosition(`object`: Any): Int {
                return POSITION_NONE
            }
        }
    }

    private fun showViewPagerTags(list: List<PartyRoomTagMode>?) {
        // 只加一个试试请求
        if (!list.isNullOrEmpty()) {
            tagList.clear()
            tagList.addAll(list)
        }
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                stopTimer()
                val view = viewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is PartyRoomItemView) {
                        view.initData(false)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        tagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)
        pagerAdapter?.notifyDataSetChanged()
    }

    private fun getPartyRoomTag(flag: Boolean) {
        if (!flag) {
            val now = System.currentTimeMillis()
            if ((now - lastLoadDateTime) < 30 * 60 * 1000) {
                return
            }
        }

        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("getPartyRoomTag", ControlType.CancelThis)) {
                roomServerApi.getPartyRoomTag(body)
            }
            if (result.errno == 0) {
                lastLoadDateTime = System.currentTimeMillis()
                val list = JSON.parseArray(result.data.getString("tagList"), PartyRoomTagMode::class.java)
                showViewPagerTags(list)
            } else {

            }
        }
    }

    private fun quickJoinParty(gameMode: Int) {
        launch {
            val map = mutableMapOf(
                    "gameMode" to gameMode
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("quickJoinParty", ControlType.CancelThis)) {
                roomServerApi.quickJoinRoom(body)
            }
            if (result.errno == 0) {
                val rsp = JSON.parseObject(result.data.toString(), JoinPartyRoomRspModel::class.java)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_ROOM)
                        .withSerializable("JoinPartyRoomRspModel", rsp)
                        .navigation()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun stopTimer() {
        views.forEach {
            it.value.stopTimer()
        }
    }

    override fun initData(flag: Boolean) {
        getPartyRoomTag(flag)
        stopTimer()
        val view = viewpager.findViewWithTag<View>(viewpager.currentItem)
        if (view != null) {
            if (view is PartyRoomItemView) {
                view.initData(false)
            }
        }

    }

    override fun destroy() {
        cancel()
        views.forEach {
            it.value.destroy()
        }
        views.clear()
    }

}
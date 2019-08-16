package com.module.feeds.songmanage.activity

import android.os.Bundle
import android.os.Debug
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON

import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ERROR_NETWORK_BROKEN
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.songmanage.FeedSongManageServerApi
import com.module.feeds.songmanage.model.FeedSongTagModel
import com.module.feeds.songmanage.view.FeedDraftsView
import com.module.feeds.songmanage.view.FeedSongManageView
import kotlinx.coroutines.launch

/**
 * 歌曲管理页面
 */
@Route(path = RouterConstants.ACTIVITY_FEEDS_SONG_MANAGE)
class FeedSongManagerActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var searchSongIv: ExTextView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: NestViewPager
    var from = 1
    private lateinit var pagerAdapter: PagerAdapter

    private var songManageViews: HashMap<Int, FeedSongManageView> = HashMap()
    private val feedDraftsView: FeedDraftsView by lazy { FeedDraftsView(this, from) }

    val feedSongManageServerApi = ApiManager.getInstance().createService(FeedSongManageServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feed_song_manage_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        from = intent.getIntExtra("from",0)
        titlebar = findViewById(R.id.titlebar)
        searchSongIv = findViewById(R.id.search_song_iv)
        tagTab = findViewById(R.id.tag_tab)
        viewpager = findViewById(R.id.viewpager)

        searchSongIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SONG_SEARCH)
                        .withInt("from",from)
                        .navigation()
            }
        })

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        launch {
            val result = subscribe { feedSongManageServerApi.getFeedSongTagList() }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("tags"), FeedSongTagModel::class.java)
                showSongTagList(list)
            } else {
                if (result.errno == ERROR_NETWORK_BROKEN) {
                    U.getToastUtil().showShort("网络异常，请检查网络后重试～")
                }
            }
        }
    }

    fun showSongTagList(list: List<FeedSongTagModel>?) {
        if (list == null || list.isEmpty()) {
            return
        }

        tagTab.setCustomTabView(R.layout.feed_song_tab_view, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        tagTab.setIndicatorWidth(80f.dp())
        tagTab.setSelectedIndicatorThickness(24.dp().toFloat())
        tagTab.setIndicatorCornorRadius(12.dp().toFloat())

        pagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position < list.size) {
                    val songTagModel = list[position]
                    if (!songManageViews.containsKey(songTagModel.tagType)) {
                        songManageViews[songTagModel.tagType] = FeedSongManageView(this@FeedSongManagerActivity, songTagModel,from)
                    }
                    val view = songManageViews[songTagModel.tagType]
                    if (position == 0) {
                        view?.tryloadData()
                    }
                    if (container.indexOfChild(view) == -1) {
                        container.addView(view)
                    }
                    return view!!
                } else {
                    if (container.indexOfChild(feedDraftsView) == -1) {
                        container.addView(feedDraftsView)
                    }
                    return feedDraftsView
                }
            }

            override fun getCount(): Int {
                return list.size + 1
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position < list.size) {
                    list[position].tagDesc
                } else {
                    "草稿箱"
                }
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position < list.size) {
                    var tagModel = list[position]
                    songManageViews[tagModel.tagType]?.tryloadData()
                } else {
                    feedDraftsView.tryLoadData()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = pagerAdapter
        tagTab.setViewPager(viewpager)
        pagerAdapter.notifyDataSetChanged()

    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        if (songManageViews.isNotEmpty()) {
            for ((_, songView) in songManageViews) {
                songView.destory()
            }
        }
        songManageViews.clear()
        feedDraftsView.destroy()
    }

    override fun canSlide(): Boolean {
        return false
    }
}

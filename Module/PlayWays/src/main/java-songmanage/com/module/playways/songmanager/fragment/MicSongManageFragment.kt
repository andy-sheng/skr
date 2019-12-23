package com.module.playways.songmanager.fragment

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.base.FragmentDataListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.component.busilib.constans.GameModeType
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.room.song.fragment.GrabSearchSongFragment
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.SongManagerServerApi
import com.module.playways.songmanager.event.AddSongEvent
import com.module.playways.songmanager.model.RecommendTagModel
import com.module.playways.songmanager.view.ExistSongManageView
import com.module.playways.songmanager.view.RecommendSongView
import com.zq.live.proto.Common.StandPlayType
import com.zq.live.proto.MicRoom.EMWantSingType
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap

// 排麦房的点歌管理
class MicSongManageFragment : BaseFragment() {

    lateinit var titlebar: CommonTitleBar
    lateinit var searchSongIv: ExTextView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager

    lateinit var mPagerAdapter: PagerAdapter

    private var mRoomData: MicRoomData? = null
    private var micSongManageView: ExistSongManageView? = null

    val mSongManagerServerApi = ApiManager.getInstance().createService(SongManagerServerApi::class.java)
    var mTagModelList: List<RecommendTagModel>? = null

    override fun initView(): Int {
        return R.layout.mic_song_manage_fragment_layout
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mRoomData = data as MicRoomData?
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mRoomData == null) {
            if (activity != null) {
                activity!!.finish()
            }
            return
        }

        titlebar = rootView.findViewById(R.id.titlebar)
        searchSongIv = rootView.findViewById(R.id.search_song_iv)
        tagTab = rootView.findViewById(R.id.tag_tab)
        viewpager = rootView.findViewById(R.id.viewpager)

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity is SongManagerActivity) {
                    if (activity != null) {
                        activity!!.finish()
                    }
                } else {
                    finish()
                }
            }
        })

        searchSongIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(context as BaseActivity?, GrabSearchSongFragment::class.java)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_MIC)
                        .addDataBeforeAdd(1, mRoomData!!.isOwner)
                        .setFragmentDataListener(object : FragmentDataListener {
                            override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                                if (requestCode == 0 && resultCode == 0 && obj != null) {
                                    val model = obj as SongModel
                                    MyLog.d(TAG, "onFragmentResult model=$model")
                                    EventBus.getDefault().post(AddSongEvent(model, SongManagerActivity.TYPE_FROM_MIC))
                                }
                            }
                        })
                        .build())
            }
        })

        getSongManagerTag()
    }

    private fun getSongManagerTag() {
        launch {
            val result = subscribe(RequestControl("getMicSongTagList", ControlType.CancelThis)) {
                mSongManagerServerApi.getMicSongTagList()
            }
            if (result.errno == 0) {
                val tagsList = JSON.parseArray(result.data.getString("tabs"), RecommendTagModel::class.java)
                showRecommendSong(tagsList)
            } else {
                //todo 失败怎么处理
            }
        }
    }

    private fun showRecommendSong(recommendTagModelList: MutableList<RecommendTagModel>?) {
        if (recommendTagModelList.isNullOrEmpty()) {
            return
        }

        val recommendTagModel = RecommendTagModel()
        recommendTagModel.type = -1
        recommendTagModel.name = "已点"
        recommendTagModelList.add(0, recommendTagModel)

        mTagModelList = recommendTagModelList
        tagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv)
        tagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        tagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        tagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        tagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        tagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        mPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return instantiateItemTag(container, position, mTagModelList!!)
            }

            override fun getCount(): Int {
                return mTagModelList!!.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mTagModelList!![position].name
            }
        }

        tagTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val view = viewpager.findViewWithTag<View>(position)
                if (view != null) {
                    if (view is RecommendSongView) {
                        view.tryLoad()
                    } else if (view is ExistSongManageView) {
                        view.tryLoad()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        viewpager.adapter = mPagerAdapter
        tagTab.setViewPager(viewpager)
        mPagerAdapter.notifyDataSetChanged()
    }

    fun instantiateItemTag(container: ViewGroup, position: Int, recommendTagModelList: List<RecommendTagModel>): Any {
        MyLog.d(TAG, "instantiateItem container=$container position=$position")
        var view: View

        if (position == 0) {
            if (micSongManageView == null) {
                micSongManageView = ExistSongManageView(context!!, mRoomData?.gameId
                        ?: 0, GameModeType.GAME_MODE_MIC)
            }
            micSongManageView?.tag = position
            view = micSongManageView!!
        } else {
            val recommendTagModel = recommendTagModelList[position]
            val recommendSongView = RecommendSongView(activity!!, SongManagerActivity.TYPE_FROM_MIC,
                    mRoomData!!.isOwner, mRoomData!!.gameId, recommendTagModel)
            recommendSongView.tag = position
            view = recommendSongView
        }

        if (container.indexOfChild(view) == -1) {
            container.addView(view)
        }

        return view
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        if (event.newRound?.singBySelf() == true || event.lastRound?.singBySelf() == true) {
            // TODO 当前页面被选中
            micSongManageView?.isSongChange = true
            if (viewpager.currentItem == 0) {
                // 当前页面就是已点，直接去更新吧
                micSongManageView?.tryLoad()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        // 想唱或者发起邀请
        val map = HashMap<String, Any>()
        map["itemID"] = event.songModel.itemID
        map["roomID"] = mRoomData?.gameId ?: 0
        // pk的歌曲
        if (event.songModel.playType == StandPlayType.PT_SPK_TYPE.value) {
            map["wantSingType"] = EMWantSingType.MWST_SPK.value
        } else if (event.songModel.playType == StandPlayType.PT_CHO_TYPE.value) {
            map["wantSingType"] = EMWantSingType.MWST_CHORUS.value
        } else {
            if (mRoomData?.isAccEnable == true) {
                map["wantSingType"] = EMWantSingType.MWST_ACCOMPANY.value
            } else {
                map["wantSingType"] = EMWantSingType.MWST_COMMON.value
            }
        }
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe { mSongManagerServerApi.addWantMicSong(body) }
            if (result.errno == 0) {
                // todo 需不需要通知其它页面
                U.getToastUtil().showShort("点歌请求发送成功")
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }


    override fun useEventBus(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        micSongManageView?.destory()
    }
}
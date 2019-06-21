package com.module.home.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup

import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.home.R
import com.module.home.game.view.DoubleRoomGameView
import com.module.home.game.view.FriendRoomGameView
import com.module.home.game.view.QuickGameView
import android.widget.ImageView
import android.view.animation.AlphaAnimation
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.notification.event.FollowNotifyEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.home.event.ShowConfirmInfoEvent
import com.module.home.game.view.DoubleRoomGameView.Companion.SP_HAS_CONFIRM_INFO
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.dialog.BusinessCardDialogView
import com.zq.dialog.ConfirmMatchInfoView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap


class GameFragment3 : BaseFragment() {

    lateinit var mNavigationBgIv: ImageView
    lateinit var mGameTab: SlidingTabLayout
    lateinit var mGameVp: NestViewPager
    lateinit var mTabPagerAdapter: PagerAdapter

    lateinit var mDialogPlus: DialogPlus

    val mFriendRoomGameView: FriendRoomGameView by lazy { FriendRoomGameView(context!!) }
    val mQuickGameView: QuickGameView by lazy { QuickGameView(this) }
    val mDoubleRoomGameView: DoubleRoomGameView by lazy { DoubleRoomGameView(context!!) }

    private var alphaAnimation: AlphaAnimation? = null

    override fun initView(): Int {
        return R.layout.game3_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mNavigationBgIv = mRootView.findViewById<View>(R.id.navigation_bg_iv) as ImageView
        mGameTab = mRootView.findViewById<View>(R.id.game_tab) as SlidingTabLayout
        mGameVp = mRootView.findViewById<View>(R.id.game_vp) as NestViewPager

        mGameTab?.setCustomTabView(R.layout.game_tab_view_layout, R.id.tab_tv)
        mGameTab?.setSelectedIndicatorColors(Color.WHITE)
        mGameTab?.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER)
        mGameTab?.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mGameTab?.setTitleSize(14f)
        mGameTab?.setSelectedTilleSize(20f)
        mGameTab?.setIndicatorWidth(U.getDisplayUtils().dip2px(16f))
        mGameTab?.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(4f).toFloat())
        mGameTab?.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(2f).toFloat())

        mTabPagerAdapter = object : PagerAdapter() {

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                MyLog.d(TAG, "destroyItem container=$container position=$position object=$`object`")
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                MyLog.d(TAG, "instantiateItem container=$container position=$position")
                var view: View? = if (position == 0) {
                    mFriendRoomGameView
                } else if (position == 1) {
                    mQuickGameView
                } else if (position == 2) {
                    mDoubleRoomGameView
                } else {
                    null
                }
                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "好友房"
                } else if (position == 1) {
                    return "快速游戏"
                } else if (position == 2) {
                    return "邂逅好声音"
                }
                return super.getPageTitle(position)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }

        mGameTab?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mGameTab.notifyDataChange()
                val drawable = mNavigationBgIv.getBackground() as ColorDrawable
                val color: Int = drawable.color
                if (position == 0) {
                    animation(color, Color.parseColor("#7088FF"))
                    mFriendRoomGameView?.initData()
                } else if (position == 1) {
                    animation(color, Color.parseColor("#7088FF"))
                    mQuickGameView?.initData()
                } else if (position == 2) {
                    animation(color, Color.parseColor("#1f0e26"))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mGameVp.setAdapter(mTabPagerAdapter)
        mGameTab.setViewPager(mGameVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mGameVp.setCurrentItem(1, false)
    }

    fun animation(startColor: Int, endColor: Int) {
        if (startColor == endColor) {
            return
        }
        mNavigationBgIv.setBackgroundColor(endColor)

        if (alphaAnimation == null) {
            alphaAnimation = AlphaAnimation(0.9f, 1f)
            alphaAnimation?.duration = 1000
        }
        mNavigationBgIv.startAnimation(alphaAnimation)
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        if (mGameVp.currentItem == 0) {
            mFriendRoomGameView?.initData()
        } else if (mGameVp.currentItem == 1) {
            mQuickGameView?.initData()
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowConfirmInfoEvent) {
        if (event != null) {
            val confirmMatchInfoView = ConfirmMatchInfoView(context, object : ConfirmMatchInfoView.Listener {
                override fun onSelect(sex: Int, ageTag: Int) {
                    var userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
                    val map = HashMap<String, Any>()
                    map["sex"] = sex
                    map["stage"] = ageTag
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    ApiMethods.subscribe(userInfoServerApi.modifyDoubleUserInfo(body), object : ApiObserver<ApiResult>() {
                        override fun process(obj: ApiResult?) {
                            if (obj?.errno == 0) {
                                U.getPreferenceUtils().setSettingBoolean(SP_HAS_CONFIRM_INFO, true)
                                //TODO 是否需要更新本地资料，服务器确认
                                mDialogPlus.dismiss(false)
                            } else {
                                MyLog.w(TAG, "modifyDoubleUserInfo erro = " + obj?.errmsg)
                            }
                        }

                    }, this@GameFragment3)
                }
            })
            mDialogPlus = DialogPlus.newDialog(activity!!)
                    .setContentHolder(ViewHolder(confirmMatchInfoView))
                    .setGravity(Gravity.BOTTOM)
                    .setMargin(U.getDisplayUtils().dip2px(10f), -1, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(65f))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.transparent)
                    .setExpanded(false)
                    .create()
            mDialogPlus.show()
        }
    }

    override fun destroy() {
        super.destroy()
        mQuickGameView?.destory()
        mFriendRoomGameView?.destory()
        mDoubleRoomGameView?.destory()
        alphaAnimation?.cancel()
    }
}


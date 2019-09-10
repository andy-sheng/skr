package com.module.home.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import com.common.base.BaseFragment
import com.common.core.account.event.AccountEvent
import com.common.core.scheme.event.JumpHomeDoubleChatPageEvent
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.home.R
import com.module.home.game.presenter.GamePresenter3
import com.module.home.game.view.*
import com.module.home.model.GameKConfigModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


// 主页
class GameFragment3 : BaseFragment(), IGameView3 {
    val PREF_KEY_BALLTLE_DIALOG = "battle_dialog"

    lateinit var mTitle: CommonTitleBar
    lateinit var mNavigationBgIv: ImageView
    lateinit var mGameTab: SlidingTabLayout
    lateinit var mGameVp: NestViewPager
    lateinit var mTabPagerAdapter: PagerAdapter
    lateinit var mPresenter: GamePresenter3

    //    val mFriendRoomGameView: FriendRoomGameView by lazy { FriendRoomGameView(context!!) }
    val mGrabGameView: GrabGameView by lazy { GrabGameView(context!!) }
    val mQuickGameView: QuickGameView by lazy { QuickGameView(this) }
    val mDoubleRoomGameView: DoubleRoomGameView by lazy { DoubleRoomGameView(context!!) }
    val mPkGameView: PKGameView by lazy { PKGameView(this) }

    private var alphaAnimation: AlphaAnimation? = null

    var mWaitingDialogPlus: DialogPlus? = null

    override fun initView(): Int {
        return R.layout.game3_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitle = rootView.findViewById(R.id.title) as CommonTitleBar
        mNavigationBgIv = rootView.findViewById<View>(R.id.navigation_bg_iv) as ImageView
        mGameTab = rootView.findViewById<View>(R.id.game_tab) as SlidingTabLayout
        mGameVp = rootView.findViewById<View>(R.id.game_vp) as NestViewPager

        mGameTab?.setCustomTabView(R.layout.game_tab_view_layout, R.id.tab_tv)
        mGameTab?.setSelectedIndicatorColors(Color.WHITE)
        mGameTab?.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        mGameTab?.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL)
        mGameTab?.setTitleSize(14f)
        mGameTab?.setSelectedTitleSize(24f)
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
                var view: View? = when (position) {
                    0 -> mGrabGameView
                    1 -> mQuickGameView
                    2 -> mPkGameView
                    3 -> mDoubleRoomGameView
                    else -> null
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
                return 4
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "抢唱"
                    1 -> "首页"
                    2 -> "排位"
                    3 -> "唱聊"
                    else -> super.getPageTitle(position)
                }
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
                when (position) {
                    0 -> {
                        animation(color, Color.parseColor("#7088FF"))
                        mGrabGameView.initData(false)
                        mQuickGameView.stopTimer()
//                        mFriendRoomGameView?.initData(false)
                        StatisticsAdapter.recordCountEvent("grab", "1.1expose", null)
                    }
                    1 -> {
                        animation(color, Color.parseColor("#7088FF"))
//                        mFriendRoomGameView?.stopTimer()
                        mQuickGameView.initData(false)
                        StatisticsAdapter.recordCountEvent("grab", "1.2expose", null)
                    }
                    2 -> {
                        mQuickGameView.stopTimer()
                        animation(color, Color.parseColor("#7088FF"))
//                        mFriendRoomGameView?.stopTimer()
                        mPkGameView.initData(false)
                    }
                    3 -> {
                        animation(color, Color.parseColor("#122042"))
                        mQuickGameView.stopTimer()
//                        mFriendRoomGameView?.stopTimer()
                        mDoubleRoomGameView?.initData()
                        StatisticsAdapter.recordCountEvent("grab", "1.3expose", null)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mGameVp.offscreenPageLimit = 3
        mGameVp.setAdapter(mTabPagerAdapter)
        mGameTab.setViewPager(mGameVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mGameVp.setCurrentItem(1, false)

        mPresenter = GamePresenter3(this)
        addPresent(mPresenter)
    }

    private fun showSongListBattleDialog() {
        if (!U.getPreferenceUtils().getSettingBoolean(PREF_KEY_BALLTLE_DIALOG, false)) {
            if (mWaitingDialogPlus == null) {
                activity?.let {
                    mWaitingDialogPlus = DialogPlus.newDialog(it)
                            .setContentHolder(ViewHolder(R.layout.battle_first_tip_layout))
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_50)
                            .setExpanded(false)
                            .setCancelable(true)
                            .setGravity(Gravity.CENTER)
                            .create()

                    mWaitingDialogPlus?.findViewById(R.id.bg_iv)?.setDebounceViewClickListener {
                        mWaitingDialogPlus?.dismiss()
                    }
                }
            }

            mWaitingDialogPlus?.show()
            U.getPreferenceUtils().setSettingBoolean(PREF_KEY_BALLTLE_DIALOG, true)
        }
    }

    fun animation(startColor: Int, endColor: Int) {
        if (startColor == endColor) {
            return
        }
        mTitle.setStatusBarColor(endColor)
        mNavigationBgIv.setBackgroundColor(endColor)

        if (alphaAnimation == null) {
            alphaAnimation = AlphaAnimation(0.9f, 1f)
            alphaAnimation?.duration = 1000
        }
        mNavigationBgIv.startAnimation(alphaAnimation)
        mTitle.startAnimation(alphaAnimation)
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        mPresenter.initGameKConfig()
        showSongListBattleDialog()
        when {
            mGameVp.currentItem == 0 -> {
                mGrabGameView.initData(false)
                mQuickGameView.stopTimer()
//                mFriendRoomGameView?.initData(false)
                StatisticsAdapter.recordCountEvent("game", "grab_expose", null)
            }
            mGameVp.currentItem == 1 -> {
//                mFriendRoomGameView?.stopTimer()
                mQuickGameView.initData(false)
                StatisticsAdapter.recordCountEvent("game", "express_expose", null)
            }
            mGameVp.currentItem == 2 -> {
                mQuickGameView.stopTimer()
//                mFriendRoomGameView?.stopTimer()
                mPkGameView.initData(false)
                StatisticsAdapter.recordCountEvent("game", "rank_expose", null)
            }
            mGameVp.currentItem == 3 -> {
                mQuickGameView.stopTimer()
//                mFriendRoomGameView?.stopTimer()
                mDoubleRoomGameView.initData()
                StatisticsAdapter.recordCountEvent("game", "cp_expose", null)
            }
        }
        StatisticsAdapter.recordCountEvent("game", "all_expose", null)
    }

    override fun onFragmentInvisible(from: Int) {
        super.onFragmentInvisible(from)
//        mFriendRoomGameView.stopTimer()
        mQuickGameView.stopTimer()
    }

    override fun setGameConfig(gameKConfigModel: GameKConfigModel) {
//        mFriendRoomGameView.mRecommendInterval = gameKConfigModel!!.homepagetickerinterval
//        if (mGameVp.currentItem == 0) {
//            mFriendRoomGameView.initData(true)
//        }

        mQuickGameView.mRecommendInterval = gameKConfigModel!!.homepagetickerinterval
        if (mGameVp.currentItem == 1 && this.fragmentVisible) {
            mQuickGameView.initData(true)
        }
    }

    override fun showRedOperationView(homepagesitefirstBean: GameKConfigModel.HomepagesitefirstBean) {
        mQuickGameView.showRedOperationView(homepagesitefirstBean)
    }

    override fun hideRedOperationView() {
        mQuickGameView.hideRedOperationView()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        mPresenter.initGameKConfig()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: JumpHomeDoubleChatPageEvent) {
        mGameVp.setCurrentItem(3, false)
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        mGrabGameView.destory()
        mQuickGameView.destory()
//        mFriendRoomGameView?.destory()
        mDoubleRoomGameView.destory()
        mPkGameView.destory()
        alphaAnimation?.cancel()
    }
}


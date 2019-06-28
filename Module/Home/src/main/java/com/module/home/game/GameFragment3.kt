package com.module.home.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
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
import com.common.core.account.event.AccountEvent
import com.common.statistics.StatisticsAdapter
import com.module.home.game.presenter.GamePresenter3
import com.module.home.game.view.IGameView3
import com.module.home.model.GameKConfigModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class GameFragment3 : BaseFragment(), IGameView3 {

    lateinit var mNavigationBgIv: ImageView
    lateinit var mGameTab: SlidingTabLayout
    lateinit var mGameVp: NestViewPager
    lateinit var mTabPagerAdapter: PagerAdapter
    lateinit var mPresenter: GamePresenter3

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
                    return "多人抢唱"
                } else if (position == 2) {
                    return "双人唱聊"
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
                    StatisticsAdapter.recordCountEvent("grab", "1.1expose", null)
                } else if (position == 1) {
                    animation(color, Color.parseColor("#7088FF"))
                    mFriendRoomGameView?.stopTimer()
                    mQuickGameView?.initData()
                    StatisticsAdapter.recordCountEvent("grab", "1.2expose", null)
                } else if (position == 2) {
                    animation(color, Color.parseColor("#261127"))
                    mFriendRoomGameView?.stopTimer()
                    mDoubleRoomGameView?.initData()
                    StatisticsAdapter.recordCountEvent("grab", "1.3expose", null)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mGameVp.setAdapter(mTabPagerAdapter)
        mGameTab.setViewPager(mGameVp)
        mTabPagerAdapter.notifyDataSetChanged()
        mGameVp.setCurrentItem(1, false)

        mPresenter = GamePresenter3(this)
        addPresent(mPresenter)
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
        mPresenter.initGameKConfig()
        if (mGameVp.currentItem == 0) {
            mFriendRoomGameView?.initData()
        } else if (mGameVp.currentItem == 1) {
            mFriendRoomGameView?.stopTimer()
            mQuickGameView?.initData()
        } else if (mGameVp.currentItem == 2) {
            mFriendRoomGameView?.stopTimer()
            mDoubleRoomGameView?.initData()
        }
        StatisticsAdapter.recordCountEvent("grab", "expose", null)
    }

    override fun onFragmentInvisible() {
        super.onFragmentInvisible()
        mFriendRoomGameView.stopTimer()
    }

    override fun setGameConfig(gameKConfigModel: GameKConfigModel) {
        mFriendRoomGameView.mRecommendInterval = gameKConfigModel!!.homepagetickerinterval
        if (mGameVp.currentItem == 0) {
            mFriendRoomGameView.initData()
        }
    }

    override fun showRedOperationView(homepagesitefirstBean: GameKConfigModel.HomepagesitefirstBean) {
        mQuickGameView?.showRedOperationView(homepagesitefirstBean)
    }

    override fun hideRedOperationView() {
        mQuickGameView?.hideRedOperationView()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        mPresenter.initGameKConfig()
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        mQuickGameView?.destory()
        mFriendRoomGameView?.destory()
        mDoubleRoomGameView?.destory()
        alphaAnimation?.cancel()
    }
}


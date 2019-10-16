package com.module.playways.race.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.busilib.view.pagetransformerhelp.cardtransformer.AlphaAndScalePageTransformer
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.adapter.RaceSelectSongAdapter
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RacePagerSelectSongView : ExConstraintLayout {
    val TAG = "RacePagerSelectSongView"
    var closeIv: ExImageView
    var hideClickArea: View
    var mPagerRootView: View
    var bannerPager: ViewPager
    var mRoomData: RaceRoomData? = null
    var mPagerAdapter: RaceSelectSongAdapter? = null
    var mHasSignUpChoiceID = -1
    var mSignUpMethed: ((Int, RaceGamePlayInfo?) -> Unit)? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            } else if (msg.what == PAGER_BUG) {
                fakeDrag()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun fakeDrag() {
        try {
            bannerPager.beginFakeDrag()
            bannerPager.fakeDragBy(1.0f)
            bannerPager.endFakeDrag()
        } catch (e: Exception) {
            MyLog.w(TAG, e.toString())
        }
    }

    init {
        View.inflate(context, com.module.playways.R.layout.race_pager_select_song_view_layout, this)
        closeIv = rootView.findViewById(com.module.playways.R.id.close_iv)
        mPagerRootView = rootView.findViewById(com.module.playways.R.id.pager_root_view)
        hideClickArea = rootView.findViewById(com.module.playways.R.id.hide_click_area)
        bannerPager = rootView.findViewById(com.module.playways.R.id.banner_pager)
        bannerPager.offscreenPageLimit = 2
        bannerPager.setPageMargin(U.getDisplayUtils().dip2px(15f))
        bannerPager.setPageTransformer(true, AlphaAndScalePageTransformer())

        mPagerAdapter = RaceSelectSongAdapter(context, object : RaceSelectSongAdapter.IRaceSelectListener {
            override fun onSignUp(choiceID: Int, model: RaceGamePlayInfo?) {
                mSignUpMethed?.invoke(choiceID, model)
            }

            override fun getSignUpChoiceID(): Int {
                return mHasSignUpChoiceID
            }
        })

        bannerPager.adapter = mPagerAdapter

        closeIv.setDebounceViewClickListener {
            hideView()
        }

        mPagerRootView.setDebounceViewClickListener {
            //拦截
        }

        hideClickArea.setDebounceViewClickListener {
            hideView()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
        if (event.userID == MyUserInfoManager.getInstance().uid.toInt()) {
            mHasSignUpChoiceID = event.choiceID
        }
    }

    fun showView() {
        if (visibility == View.VISIBLE) {
            return
        }

        if (mRoomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value
                && mRoomData?.realRoundInfo?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
            MyLog.w(TAG, "中途进来的，而且是选歌阶段，不展示选歌界面")
            return
        }

        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
        mUiHandler.sendEmptyMessage(PAGER_BUG)
    }

    fun hideView() {
        if (mUiHandler.hasMessages(HIDE_PANEL) || View.GONE == visibility) {
            return
        }

        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        val HIDE_PANEL = 1
        val PAGER_BUG = 2
        val ANIMATION_DURATION = 300
    }
}
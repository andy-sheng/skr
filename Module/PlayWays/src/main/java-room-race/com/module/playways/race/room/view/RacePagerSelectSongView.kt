package com.module.playways.race.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.gift.view.GiftPanelView

class RacePagerSelectSongView : ConstraintLayout {
    var closeIv: ExImageView
    var hideClickArea: View
    var bannerPager: ViewPager
    var raceRoomData: RaceRoomData? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == GiftPanelView.HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            }
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_pager_select_song_view_layout, this)
        closeIv = rootView.findViewById(R.id.close_iv)
        hideClickArea = rootView.findViewById(R.id.hide_click_area)
        bannerPager = rootView.findViewById(R.id.banner_pager)

        closeIv.setDebounceViewClickListener {
            hideView()
        }

        hideClickArea.setDebounceViewClickListener {
            hideView()
        }
    }

    //只有在唱歌的阶段就用到RaceRoomData couldChoiceGames的数据，别的时候都用到之前的歌曲的数据
    fun updateData() {

    }

    fun showView() {
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
    }

    fun hideView() {
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

    companion object {
        val HIDE_PANEL = 1
        val ANIMATION_DURATION = 300
    }
}
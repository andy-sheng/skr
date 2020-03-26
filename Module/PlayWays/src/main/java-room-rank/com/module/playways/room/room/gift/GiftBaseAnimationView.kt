package com.module.playways.room.room.gift

import android.view.ViewGroup
import com.module.playways.room.room.gift.model.GiftPlayModel

/**
 *
 */
interface GiftBaseAnimationView {

    fun init()
    fun play(parentView:ViewGroup, giftPlayModel: GiftPlayModel)
    fun isIdle():Boolean
    fun destroy()
    fun reset()
    /**
     * 是否支持该礼物动画
     */
    fun isSupport(giftPlayModel: GiftPlayModel):Boolean

    fun setListener(listener: Listener)

    interface Listener {
        fun onFinished(animationView: GiftBaseAnimationView, giftPlayModel: GiftPlayModel)
    }
}
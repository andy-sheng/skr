package com.module.playways.room.room.gift

import android.content.Context
import android.view.TextureView
import com.module.playways.room.room.gift.model.GiftPlayModel
import com.zq.mediaengine.kit.ZqAnimatedVideoPlayer

class GiftBigVideoView (context:Context) {

    private val mPlayer:ZqAnimatedVideoPlayer = ZqAnimatedVideoPlayer()

    fun play(textureView: TextureView, giftPlayModel: GiftPlayModel){
        textureView.isOpaque = true

        mPlayer.setEnableLoop(true)
        mPlayer.setDisplay(textureView)
    }

    fun load(textureView: TextureView, giftPlayModel: GiftPlayModel){
        onLoadComplete(textureView, giftPlayModel.giftAnimationUrl)
    }

    fun onLoadComplete(textureView: TextureView, url:String){
        mPlayer.start(url)
    }
}
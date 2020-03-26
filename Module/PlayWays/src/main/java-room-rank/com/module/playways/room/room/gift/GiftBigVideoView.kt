package com.module.playways.room.room.gift

import android.content.Context
import android.view.TextureView
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.common.log.MyLog
import com.common.utils.U
import com.module.playways.room.gift.model.AnimationGift
import com.module.playways.room.room.gift.model.GiftPlayModel
import com.zq.mediaengine.kit.ZqAnimatedVideoPlayer

class GiftBigVideoView (val context:Context) : GiftBaseAnimationView{

    private var mListener: GiftBaseAnimationView.Listener? = null
    private val TAG: String = "GiftBigVideoView"
    private lateinit var mGiftPlayModel: GiftPlayModel
    private var mTextureView:TextureView = TextureView(context)
    private val mPlayer:ZqAnimatedVideoPlayer = ZqAnimatedVideoPlayer()
    internal val STATUS_IDLE = 1
    internal val STATUS_PLAYING = 2
    internal var mStatus = STATUS_IDLE

    override fun init() {
        mPlayer.setDisplay(mTextureView)
        mPlayer.setEnableLoop(false)
        mTextureView.isOpaque = true
        mPlayer.setOnCompletionListener {
            mStatus = STATUS_IDLE
            mListener?.onFinished(this, mGiftPlayModel)
        }
    }

    override fun play(parentView: ViewGroup, giftPlayModel: GiftPlayModel) {
        mGiftPlayModel = giftPlayModel
        val baseGift = mGiftPlayModel.getGift()
        if (baseGift is AnimationGift) {
            val animationGift = baseGift as AnimationGift
            val giftParamModel = animationGift.animationPrams
            mStatus = STATUS_PLAYING
            load(parentView, animationGift.sourceURL, giftParamModel)
//            mUiHanlder.removeMessages(MSG_ENSURE_FINISH)
//            // TODO: 2019/5/8 时间可以根据动画加上一点做保护
//            mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, giftParamModel.duration + 5000)
        }
    }

    override fun isIdle(): Boolean {
        return mStatus == STATUS_IDLE
    }

    override fun destroy() {
        mPlayer.release()
    }

    override fun reset() {
        mPlayer.release()
    }


    private fun load(parent: ViewGroup, url:String, animationPrams: AnimationGift.AnimationPrams){
        onLoadComplete(parent, url, animationPrams)
    }

    private fun onLoadComplete(parent: ViewGroup, url:String, animationPrams: AnimationGift.AnimationPrams) {
        if (parent.indexOfChild(mTextureView) < 0) {
            // 确定尺寸和位置
            if (animationPrams.isFullScreen()) {
                // 全屏
                val realWidth = 640.toDouble()
                val realHeight = 500.toDouble()
                if (animationPrams.isFullX()) {
                    // 横向平铺
                    if (realWidth != 0.0) {
                        val width = U.getDisplayUtils().screenWidth
                        val height = (realHeight * width / realWidth).toInt()
                        val lp = RelativeLayout.LayoutParams(width, height)
                        // 确定位置
                        if (animationPrams.getBottom() != -1) {
                            // 距离底部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                            lp.bottomMargin = U.getDisplayUtils().dip2px(animationPrams.getBottom().toFloat())
                            parent.addView(mTextureView, lp)
                        } else if (animationPrams.getTop() != -1) {
                            // 距离顶部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                            lp.topMargin = U.getDisplayUtils().dip2px(animationPrams.getTop().toFloat())
                            parent.addView(mTextureView, lp)
                        } else {
                            // 顶部和底部无要求，则居中
                            lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                            parent.addView(mTextureView, lp)
                        }
                    } else {
//                        MyLog.w(TAG, "onLoadComplete parent=$parent animationPrams=$animationPrams videoItem=$videoItem realWidth = 0")
                    }
                } else {
                    // 纵向平铺
                    if (realHeight != 0.0) {
                        val height = U.getDisplayUtils().screenHeight
                        val width = (realWidth * height / realHeight).toInt()
                        val lp = RelativeLayout.LayoutParams(width, height)
                        // 确定位置
                        if (animationPrams.getLeft() != -1) {
                            // 距离左边多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                            lp.leftMargin = U.getDisplayUtils().dip2px(animationPrams.getLeft().toFloat())
                            parent.addView(mTextureView, lp)
                        } else if (animationPrams.getRight() != -1) {
                            // 距离右边多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                            lp.rightMargin = U.getDisplayUtils().dip2px(animationPrams.getRight().toFloat())
                            parent.addView(mTextureView, lp)
                        } else {
                            // 左部和右部无要求，则居中
                            lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                            parent.addView(mTextureView, lp)
                        }
                    } else {
//                        MyLog.w(TAG, "onLoadComplete" + " parent=" + parent + " animationPrams=" + animationPrams + " videoItem=" + videoItem + "realHeight = 0")
                    }
                }
            } else {
                // 非全屏幕
                val lp = RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(animationPrams.getWidth().toFloat()),
                        U.getDisplayUtils().dip2px(animationPrams.getHeight().toFloat()))
                lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                parent.addView(mTextureView, lp)
            }
            mPlayer.start("/sdcard/animated.mp4")
        }
    }

    override fun setListener(listener: GiftBaseAnimationView.Listener) {
        this.mListener = listener
    }
}
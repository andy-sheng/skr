package com.module.playways.room.room.gift

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.TextureView
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.common.log.MyLog
import com.common.utils.U
import com.common.videocache.MediaCacheManager
import com.module.playways.room.gift.model.AnimationGift
import com.module.playways.room.room.gift.model.GiftPlayModel
import com.zq.mediaengine.kit.ZqAnimatedVideoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 视频动画礼物
 */
class GiftBigVideoAnimationView (val context:Context) : GiftBaseAnimationView{

    private var mListener: GiftBaseAnimationView.Listener? = null
    private val TAG: String = "GiftBigVideoView"
    private lateinit var mGiftPlayModel: GiftPlayModel
    private var mTextureView:TextureView = TextureView(context)
    private val mPlayer:ZqAnimatedVideoPlayer = ZqAnimatedVideoPlayer()

    internal val TRANSPARENT_VIDEO_SIZE_RATE = 2

    internal val STATUS_IDLE = 1
    internal val STATUS_PLAYING = 2
    internal val MSG_ENSURE_FINISH = 101

    internal var mStatus = STATUS_IDLE

    internal var mUiHanlder: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if (msg.what == MSG_ENSURE_FINISH) {
                onFinish()
            }
        }
    }

    override fun init() {
        mTextureView = TextureView(context)

        mPlayer.setOnCompletionListener {
            avp -> MyLog.e(TAG, "Animation completion")
            onFinish()
        }

        mPlayer.setOnErrorListener { mp, what, extra ->
            onFinish()
            return@setOnErrorListener false
        }
    }

    override fun play(parentView: ViewGroup, giftPlayModel: GiftPlayModel) {
        mGiftPlayModel = giftPlayModel
        val baseGift = mGiftPlayModel.gift

        if (baseGift is AnimationGift) {
            val giftParamModel = baseGift.animationPrams
            mStatus = STATUS_PLAYING
            load(parentView, "${baseGift.sourceBaseURL}${baseGift.sourceMp4}", giftParamModel)
            mUiHanlder.removeMessages(MSG_ENSURE_FINISH)
            //视频结束时间容错，防止一直不结束
            mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, giftParamModel.duration + 5000)
        }
    }

    override fun isSupport(giftPlayModel: GiftPlayModel): Boolean {
        val source = giftPlayModel.gift.sourceMp4
        return source != null && !TextUtils.isEmpty(source)
    }

    override fun isIdle(): Boolean {
        return mStatus == STATUS_IDLE
    }

    override fun destroy() {
        mPlayer.release()
        mUiHanlder.removeCallbacksAndMessages(null)
    }

    override fun reset() {
        mPlayer.release()
    }


    private fun load(parent: ViewGroup, url:String, animationPrams: AnimationGift.AnimationPrams){

        MyLog.d(TAG, "获取视频礼物动画: $url")

        MainScope().launch (Dispatchers.IO){
//            val retriever = MediaMetadataRetriever()
//
//            retriever.setDataSource(context, Uri.parse("/sdcard/animated.mp4"))

//            val wVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
//            val hVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

            MainScope().launch (Dispatchers.Main){
                // 测试可使用此地址
//                val urlProxy = MediaCacheManager.getProxyUrl("http://res-static.inframe.mobi/pkgs/android/animated.mp4",true)
//                animationPrams.width = 640
//                animationPrams.height = 496

                 val urlProxy = MediaCacheManager.getProxyUrl(url, true)
                onLoadComplete(parent, urlProxy, animationPrams, Pair(animationPrams.width.toDouble(), animationPrams.height.toDouble()))
            }
        }
    }

    private fun onLoadComplete(parent: ViewGroup, url:String, animationPrams: AnimationGift.AnimationPrams, videoItem:Pair<Double, Double>) {
        if (parent.indexOfChild(mTextureView) < 0) {
            // 确定尺寸和位置
            if (animationPrams.isFullScreen) {
                // 全屏
                val realWidth = videoItem.first
                val realHeight = videoItem.second

                if (animationPrams.isFullX) {
                    // 横向平铺
                    if (realWidth != 0.0) {
                        //透明视频分为两部分 各存储一部分信息 非全屏状态 分辨率要乘二倍
                        val width = U.getDisplayUtils().screenWidth * TRANSPARENT_VIDEO_SIZE_RATE
                        val height = (realHeight * (width / realWidth)).toInt()
                        val lp = RelativeLayout.LayoutParams(width, height)

                        // 确定位置
                        if (animationPrams.bottom != -1) {
                            // 距离底部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                            lp.bottomMargin = U.getDisplayUtils().dip2px(animationPrams.bottom.toFloat())
                            parent.addView(mTextureView, lp)
                        } else if (animationPrams.top != -1) {
                            // 距离顶部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                            lp.topMargin = U.getDisplayUtils().dip2px(animationPrams.top.toFloat())
                            parent.addView(mTextureView, lp)
                        } else {
                            // 顶部和底部无要求，则居中
                            lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                            parent.addView(mTextureView, lp)
                        }
                    } else {
                        MyLog.w(TAG, "onLoadComplete parent=$parent animationPrams=$animationPrams videoItem=$videoItem realWidth = 0")
                    }
                } else {
                    // 纵向平铺
                    if (realHeight != 0.0) {
                        val height = U.getDisplayUtils().screenHeight * 2
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
                        MyLog.w(TAG, "onLoadComplete" + " parent=" + parent + " animationPrams=" + animationPrams + " videoItem=" + videoItem + "realHeight = 0")
                    }
                }
            } else {
                // 非全屏幕
                val lp = RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(animationPrams.getWidth().toFloat()),
                        U.getDisplayUtils().dip2px(animationPrams.getHeight().toFloat()))

                lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                parent.addView(mTextureView, lp)
            }
        }

        MyLog.d(TAG, "mTextureView " + " width: " + mTextureView.width + " height: " + mTextureView.height +
                " measuredWidth: " + mTextureView.measuredWidth + " measuredHeight: " + mTextureView.measuredHeight)

        mTextureView.isOpaque = false
        mPlayer.setEnableLoop(false)
        mPlayer.setDisplay(mTextureView)
        mPlayer.start(url)
    }

    private fun onFinish() {
        if (mStatus == STATUS_PLAYING) {
            mUiHanlder.post {
                MyLog.d(TAG, "onFinish " + " videoWidth: " + mPlayer.mediaPlayer.videoWidth + " videoHeight: " + mPlayer.mediaPlayer.videoHeight)
                if (mListener != null) {
                    mListener?.onFinished(this@GiftBigVideoAnimationView, mGiftPlayModel)
                }
                mStatus = STATUS_IDLE
                val vg = mTextureView.parent as? ViewGroup
                vg?.removeView(mTextureView)
                mUiHanlder.removeCallbacksAndMessages(MSG_ENSURE_FINISH)
            }
        }
    }
    override fun setListener(listener: GiftBaseAnimationView.Listener) {
        this.mListener = listener
    }
}
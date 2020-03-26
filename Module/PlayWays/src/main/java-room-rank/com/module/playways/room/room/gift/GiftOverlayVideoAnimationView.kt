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
import com.module.playways.room.gift.model.AnimationGift
import com.module.playways.room.room.gift.model.GiftPlayModel
import com.zq.mediaengine.kit.ZqAnimatedVideoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class GiftOverlayVideoAnimationView(val context:Context): GiftBaseAnimationView {

    private var mListener: GiftBaseAnimationView.Listener? = null
    private val TAG: String = "GiftBigVideoView"
    private lateinit var mGiftPlayModel: GiftPlayModel
    private var mTextureView: TextureView = TextureView(context)
    private val mPlayer: ZqAnimatedVideoPlayer = ZqAnimatedVideoPlayer()

    internal val TRANSPARENT_VIDEO_SIZE_RATE = 2

    internal val STATUS_IDLE = 1
    internal val STATUS_PLAYING = 2
    internal val MSG_ENSURE_FINISH = 101

    internal var mStatus = STATUS_IDLE
    internal var mRandom = Random()


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
        val baseGift = mGiftPlayModel.getGift()

        if (baseGift is AnimationGift) {
            val animationGift = baseGift as AnimationGift
            val giftParamModel = animationGift.animationPrams
            mStatus = STATUS_PLAYING
            load(parentView, "${animationGift.sourceBaseURL}${animationGift.sourceMp4}", giftPlayModel)
            mUiHanlder.removeMessages(MSG_ENSURE_FINISH)
            //视频结束时间容错，防止一直不结束
            mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, giftParamModel.duration + 5000)
        }
    }

    override fun isSupport(giftPlayModel: GiftPlayModel): Boolean {
        val source = giftPlayModel.gift.sourceMp4
        // TODO 先强制使用视频动画测试
        return true || source != null && !TextUtils.isEmpty(source)
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


    private fun load(parent: ViewGroup, url:String?, giftPlayModel: GiftPlayModel){

        MyLog.d(TAG, "获取视频礼物动画: $url")

        MainScope().launch (Dispatchers.IO){
            //            val retriever = MediaMetadataRetriever()
//
//            retriever.setDataSource(context, Uri.parse("/sdcard/animated.mp4"))

//            val wVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
//            val hVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

            MainScope().launch (Dispatchers.Main){
                // TODO 临时使用本地固定视频地址，服务端修改后使用服务端地址
                val baseGift = giftPlayModel.gift
                if (baseGift is AnimationGift) {
                    onLoadComplete(parent, "/sdcard/animated.mp4", giftPlayModel, Pair(baseGift.animationPrams.width.toDouble(), baseGift.animationPrams.height.toDouble()))
                }

            }
        }
    }

    private fun onLoadComplete(parent: ViewGroup, url:String, giftPlayModel:GiftPlayModel, videoItem:Pair<Double, Double>) {
        if (parent.indexOfChild(mTextureView) < 0) {
            val translateX = U.getDisplayUtils().dip2px((mRandom.nextInt(200) - 100).toFloat())
            val translateY = U.getDisplayUtils().dip2px((mRandom.nextInt(200) - 100).toFloat())
            val l = GiftOverlayAnimationView.SLocation(0.67f + mRandom.nextFloat() / 3f, translateX.toFloat(), translateY.toFloat())
            if (giftPlayModel.gift is AnimationGift) {
                val animationGift = giftPlayModel.gift as AnimationGift
                val lp = RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(animationGift.animationPrams.width.toFloat() * TRANSPARENT_VIDEO_SIZE_RATE),
                        U.getDisplayUtils().dip2px(animationGift.animationPrams.height.toFloat() * TRANSPARENT_VIDEO_SIZE_RATE))
                lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                parent.addView(mTextureView, lp)
            } else {
                val lp = RelativeLayout.LayoutParams(videoItem.first.toInt() * TRANSPARENT_VIDEO_SIZE_RATE, videoItem.second.toInt() * TRANSPARENT_VIDEO_SIZE_RATE)
                lp.addRule(RelativeLayout.CENTER_IN_PARENT)
                parent.addView(mTextureView, lp)
            }

            // 变化一下位置
            mTextureView.setScaleX(l.scale)
            mTextureView.setScaleY(l.scale)
            mTextureView.setTranslationX(l.translateX)
            mTextureView.setTranslationY(l.translateY)
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
                    mListener?.onFinished(this@GiftOverlayVideoAnimationView, mGiftPlayModel)
                }

                val vg = mTextureView.parent as? ViewGroup
                vg?.removeView(mTextureView)
                mUiHanlder.removeCallbacksAndMessages(MSG_ENSURE_FINISH)


                mStatus = STATUS_IDLE
            }
        }
    }
    override fun setListener(listener: GiftBaseAnimationView.Listener) {
        this.mListener = listener
    }
}
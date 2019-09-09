package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.anim.ObjectPlayControlTemplate
import com.common.anim.svga.SvgaParserAdapter
import com.common.log.MyLog
import com.common.utils.U
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

import java.util.ArrayList

/**
 * 灯的全屏效果
 */
class GrabDengBigAnimationView : RelativeLayout {
    val TAG = "GrabDengBigAnimationView"

    internal var mDengSvgaViewList: MutableList<SVGAImageViewEx> = ArrayList()

    internal var mViewObjectPlayControlTemplate: ObjectPlayControlTemplate<PlayData, SVGAImageViewEx>? = object : ObjectPlayControlTemplate<PlayData, SVGAImageViewEx>() {
        override fun accept(cur: PlayData): SVGAImageViewEx? {
            return isIdle
        }

        override fun onStart(playData: PlayData, svgaImageView: SVGAImageViewEx) {
            playBurstAnimationInner(playData, svgaImageView)
        }

        override fun onEnd(playData: PlayData?) {

        }
    }

    private// 先让只有一个消费者
    val isIdle: SVGAImageViewEx?
        get() {
            for (svgaImageViewEx in mDengSvgaViewList) {
                if (!svgaImageViewEx.playing) {
                    svgaImageViewEx.playing = true
                    return svgaImageViewEx
                }
            }
            if (mDengSvgaViewList.size < 1) {
                val svgaImageViewEx = SVGAImageViewEx(SVGAImageView(context))
                mDengSvgaViewList.add(svgaImageViewEx)
                return svgaImageViewEx
            }
            return null
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_deng_big_animation_view, this)
        U.getSoundUtils().preLoad(TAG, R.raw.grab_olight)
    }

    fun playBurstAnimation(flag: Boolean) {
        MyLog.d(TAG, "playBurstAnimation")
        mViewObjectPlayControlTemplate!!.add(PlayData(flag), true)
    }

    // 爆灯
    private fun playBurstAnimationInner(playData: PlayData, dengSvgaEx: SVGAImageViewEx) {
        MyLog.d(TAG, "playBurstAnimationInner playData=$playData dengSvgaEx=$dengSvgaEx")
        if (playData.isFromSelf) {
            U.getSoundUtils().play(TAG, R.raw.grab_olight)
        } else {
            //U.getSoundUtils().play(TAG, R.raw.grab_olight_lowervolume);
        }

        val dengSvga = dengSvgaEx.mSVGAImageView
        if (this.indexOfChild(dengSvga) == -1) {
            MyLog.d(TAG, "视图未添加，添加")
            dengSvgaEx.add(this)
        } else {
            MyLog.d(TAG, "视图已添加")
        }
        dengSvga!!.callback = null
        dengSvga.stopAnimation(true)
        visibility = View.VISIBLE

        dengSvga.visibility = View.VISIBLE
        dengSvga.loops = 1
        SvgaParserAdapter.parse(BaseRoomData.GRAB_BURST_BIG_SVGA, object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                dengSvga.setImageDrawable(drawable)
                dengSvga.startAnimation()
            }

            override fun onError() {
                MyLog.d(TAG, "playBurstAnimationInner onError")
            }
        })

        dengSvga.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                if (dengSvga != null) {
                    dengSvga.callback = null
                    dengSvga.stopAnimation(true)
                    visibility = View.GONE
                }
                dengSvgaEx.playing = false
                mViewObjectPlayControlTemplate!!.endCurrent(playData)
            }

            override fun onRepeat() {
                if (dengSvga != null && dengSvga.isAnimating) {
                    dengSvga.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        for (svgaImageViewEx in mDengSvgaViewList) {
            svgaImageViewEx.destroy()
        }
        if (mViewObjectPlayControlTemplate != null) {
            mViewObjectPlayControlTemplate!!.destroy()
        }
        U.getSoundUtils().release(TAG)
    }

    class PlayData(internal var isFromSelf: Boolean   //标记爆灯的发送者自己
    )

    class SVGAImageViewEx(var mSVGAImageView: SVGAImageView?) {
        var playing = false

        fun add(parent: GrabDengBigAnimationView) {
            val lp = RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(375f), U.getDisplayUtils().dip2px(400f))
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            lp.topMargin = U.getDisplayUtils().dip2px(50f)
            parent.addView(mSVGAImageView, lp)
        }

        fun destroy() {
            if (mSVGAImageView != null) {
                mSVGAImageView!!.callback = null
                mSVGAImageView!!.stopAnimation(true)
            }
        }
    }

}

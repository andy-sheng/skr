package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.anim.svga.SvgaParserAdapter
import com.common.log.MyLog
import com.module.playways.listener.SVGAListener
import com.module.playways.R
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity


class TurnInfoCardView : RelativeLayout {

    val mTag = "TurnInfoCardView"

    private val mFirstSvga: SVGAImageView
    private val mNextSvga: SVGAImageView

    internal var mSVGAListener: SVGAListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_turn_info_card_layout, this)
        mFirstSvga = findViewById(R.id.first_svga)
        mNextSvga = findViewById(R.id.next_svga)
    }

    fun setModeSongSeq(first: Boolean, listener: SVGAListener) {
        MyLog.d(mTag, "setModeSongSeq first=$first listener=$listener")
        this.mSVGAListener = listener
        visibility = View.VISIBLE
        if (first) {
            firstBegin()
        } else {
            nextBegin()
        }
    }

    // 对战开始连着第一首是同一个动画
    private fun firstBegin() {
        mFirstSvga.clearAnimation()
        mFirstSvga.visibility = View.VISIBLE
        mFirstSvga.loops = 1
        SvgaParserAdapter.parse("grab_battle_start.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mFirstSvga.setImageDrawable(drawable)
                mFirstSvga.startAnimation()
            }

            override fun onError() {

            }
        })

        mFirstSvga.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                mFirstSvga.callback = null
                mFirstSvga.stopAnimation(true)
                mFirstSvga.visibility = View.GONE
                if (mSVGAListener != null) {
                    mSVGAListener!!.onFinished()
                }
            }

            override fun onRepeat() {
                if (mFirstSvga.isAnimating) {
                    mFirstSvga.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    private fun nextBegin() {
        mNextSvga.clearAnimation()
        mNextSvga.visibility = View.VISIBLE
        mNextSvga.loops = 1
        SvgaParserAdapter.parse("grab_battle_next.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mNextSvga.setImageDrawable(drawable)
                mNextSvga.startAnimation()
            }

            override fun onError() {

            }
        })

        mNextSvga.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                mNextSvga.callback = null
                mNextSvga.stopAnimation(true)
                mNextSvga.visibility = View.GONE
                mSVGAListener?.onFinished()
            }

            override fun onRepeat() {
                if (mNextSvga.isAnimating) {
                    mNextSvga.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            this.mSVGAListener = null
            mFirstSvga.callback = null
            mFirstSvga.stopAnimation(true)


            this.mFirstSvga.callback = null
            mNextSvga.stopAnimation(true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.mSVGAListener = null
        mFirstSvga.callback = null
        mFirstSvga.stopAnimation(true)
        mNextSvga.callback = null
        mNextSvga.stopAnimation(true)
    }
}

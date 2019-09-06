package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.anim.svga.SvgaParserAdapter
import com.module.playways.listener.SVGAListener
import com.module.playways.R
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * 游戏结束，对战结束的动画
 */
class GrabGameOverView : RelativeLayout {

    val mTag = "GrabGameOverView"

    private val mEndGameIv: SVGAImageView
    private var mSVGAListener: SVGAListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_game_end_card_layout, this)
        mEndGameIv = findViewById(R.id.end_game_iv)
    }

    fun starSVGAAnimation(listener: SVGAListener) {
        this.mSVGAListener = listener
        mEndGameIv.clearAnimation()
        mEndGameIv.visibility = View.VISIBLE
        mEndGameIv.loops = 1
        SvgaParserAdapter.parse("grab_game_over.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mEndGameIv.setImageDrawable(drawable)
                mEndGameIv.startAnimation()
            }

            override fun onError() {

            }
        })

        mEndGameIv.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                mEndGameIv.callback = null
                mEndGameIv.stopAnimation(true)
                mEndGameIv.visibility = View.GONE
                mSVGAListener?.onFinished()
            }

            override fun onRepeat() {
                if (mEndGameIv.isAnimating) {
                    mEndGameIv.stopAnimation(false)
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
            mEndGameIv.callback = null
            mEndGameIv.stopAnimation(true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.mSVGAListener = null
        mEndGameIv.callback = null
        mEndGameIv.stopAnimation(true)
    }
}

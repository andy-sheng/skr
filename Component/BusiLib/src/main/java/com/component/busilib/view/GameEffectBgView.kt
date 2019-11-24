package com.component.busilib.view

import android.graphics.Color
import android.view.View
import android.view.ViewStub
import com.common.anim.svga.SvgaParserAdapter
import com.common.utils.U
import com.common.view.ExViewStub
import com.component.busilib.R
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import org.greenrobot.greendao.annotation.NotNull

class GameEffectBgView : ExViewStub {
    lateinit var bg: View
    lateinit var effectSvga: SVGAImageView

    constructor(mViewStub: ViewStub?) : super(mViewStub)

    override fun init(parentView: View) {
        bg = parentView.findViewById(R.id.bg)
        effectSvga = parentView.findViewById(R.id.effect_svga)
    }

    fun showBgEffect(sourceURL: String, color: String) {
        setVisibility(View.VISIBLE)
        effectSvga?.loops = 1

        SvgaParserAdapter.parse(sourceURL, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                effectSvga!!.loops = -1
                effectSvga!!.setImageDrawable(drawable)
                effectSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })

        try {
            bg.setBackgroundColor(Color.parseColor(color))
        } catch (color: IllegalArgumentException) {
            bg.setBackgroundColor(U.getColor(R.color.black))
        }
    }

    fun hideBg() {
        setVisibility(View.GONE)
        effectSvga?.let {
            it!!.callback = null
            it!!.stopAnimation(true)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.game_effect_bg_view_layout
    }
}
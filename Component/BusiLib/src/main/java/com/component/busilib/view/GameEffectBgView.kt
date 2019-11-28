package com.component.busilib.view

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import com.common.anim.svga.SvgaParserAdapter
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.component.busilib.R
import com.component.busilib.model.EffectModel
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.greendao.annotation.NotNull

class GameEffectBgView : ExViewStub {
    var bg: View? = null
    var effectSvga: SVGAImageView? = null
    var dynamicEffectSvga: SVGAImageView? = null

    var dynamicJob: Job? = null

    constructor(mViewStub: ViewStub?) : super(mViewStub)

    override fun init(parentView: View) {
        bg = parentView.findViewById(R.id.bg)
        effectSvga = parentView.findViewById(R.id.effect_svga)
        dynamicEffectSvga = parentView.findViewById(R.id.dynamic_effect_svga)

        effectSvga?.layoutParams?.height = U.getDisplayUtils().phoneWidth * 490 / 375
        dynamicEffectSvga?.layoutParams?.height = U.getDisplayUtils().phoneWidth * 490 / 375
    }

    fun showBgEffect(backgroundEffectModel: EffectModel?) {
        hideBg()
        tryInflate()
        bg?.setBackgroundColor(U.getColor(R.color.transparent))
        if (backgroundEffectModel == null || backgroundEffectModel?.items == null || backgroundEffectModel?.items?.size == 0) {
            return
        }

        val color: String = backgroundEffectModel.bgColor
        try {
            bg?.setBackgroundColor(Color.parseColor(color))
        } catch (color: Exception) {
            MyLog.e("GameEffectBgView", "IllegalArgumentException")
            bg?.setBackgroundColor(U.getColor(R.color.black))
        }

        effectSvga?.visibility = View.GONE
        dynamicEffectSvga?.visibility = View.GONE

        backgroundEffectModel?.items?.forEach {
            if (it.type == 1) {
                showFirstLevelEffect(it)
            } else if (it.type == 0) {
                showSecondLevelEffect(it)
            }
        }

        setVisibility(View.VISIBLE)
    }

    //背景的话直接循环播就可以了
    private fun showFirstLevelEffect(effectObg: EffectModel.EffectObg) {
        if (!TextUtils.isEmpty(effectObg.sourceUrl)) {
            effectSvga?.visibility = View.VISIBLE

            SvgaParserAdapter.parse(effectObg.sourceUrl, object : SVGAParser.ParseCompletion {
                override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                    if (mParentView?.visibility == View.VISIBLE) {
                        val drawable = SVGADrawable(videoItem)
                        effectSvga!!.loops = -1
                        effectSvga!!.setImageDrawable(drawable)
                        effectSvga!!.startAnimation()
                    }
                }

                override fun onError() {

                }
            })
        }
    }

    private fun showSecondLevelEffect(effectObg: EffectModel.EffectObg) {

        if (effectObg.interval > 0) {
            dynamicJob = launch {
                if (effectObg.startTs > 0) {
                    delay(effectObg.startTs.toLong())
                }

                dynamicEffectSvga?.visibility = View.VISIBLE

                repeat(Int.MAX_VALUE) {
                    SvgaParserAdapter.parse(effectObg.sourceUrl, object : SVGAParser.ParseCompletion {
                        override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                            if (mParentView?.visibility == View.VISIBLE) {
                                val drawable = SVGADrawable(videoItem)
                                dynamicEffectSvga!!.loops = 1
                                dynamicEffectSvga!!.setImageDrawable(drawable)
                                dynamicEffectSvga!!.startAnimation()
                            }
                        }

                        override fun onError() {

                        }
                    })

                    delay(effectObg.interval.toLong())
                }
            }
        } else {
            if (!TextUtils.isEmpty(effectObg.sourceUrl)) {
                dynamicEffectSvga?.visibility = View.VISIBLE

                SvgaParserAdapter.parse(effectObg.sourceUrl, object : SVGAParser.ParseCompletion {
                    override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                        if (mParentView?.visibility == View.VISIBLE) {
                            val drawable = SVGADrawable(videoItem)
                            dynamicEffectSvga!!.loops = -1
                            dynamicEffectSvga!!.setImageDrawable(drawable)
                            dynamicEffectSvga!!.startAnimation()
                        }
                    }

                    override fun onError() {

                    }
                })
            }
        }
    }

    fun hideBg() {
        setVisibility(View.GONE)
        effectSvga?.let {
            it!!.callback = null
            it!!.stopAnimation(true)
        }
        dynamicJob?.cancel()
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        dynamicJob?.cancel()
    }

    override fun layoutDesc(): Int {
        return R.layout.game_effect_bg_view_layout
    }
}
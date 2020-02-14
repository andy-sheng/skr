package com.module.mall.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.component.level.utils.LevelConfigUtils
import com.module.home.R
import com.module.mall.model.ProductModel
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import org.greenrobot.greendao.annotation.NotNull

class EffectView : ConstraintLayout {
    var bgSvga: SVGAImageView
    var avatarIv: BaseImageView
    var avatarBox: BaseImageView
    var voiceprintSvga: SVGAImageView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.all_effect_view_layout, this)

        voiceprintSvga = rootView.findViewById(R.id.voiceprint_svga)
        bgSvga = rootView.findViewById(R.id.bg_svga)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        avatarBox = rootView.findViewById(R.id.avatar_box)

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .setBorderColor(U.getColor(R.color.white))
                .build())

        if (LevelConfigUtils.getRaceCenterAvatarBg(MyUserInfoManager.myUserInfo?.ranking?.mainRanking
                        ?: 0) != 0) {
            avatarBox.setImageResource(LevelConfigUtils.getRaceCenterAvatarBg(MyUserInfoManager.myUserInfo?.ranking?.mainRanking
                    ?: 0))
        }

        showVoiceprint()
    }

    fun showVoiceprint() {
        voiceprintSvga?.visibility = View.VISIBLE
        voiceprintSvga?.loops = 1

        SvgaParserAdapter.parse("grab_main_stage.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                voiceprintSvga!!.loops = -1
                voiceprintSvga!!.setImageDrawable(drawable)
                voiceprintSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    fun showBgEffect(productModel: ProductModel) {
        reset()

        avatarIv.visibility = View.VISIBLE
        avatarBox.visibility = View.VISIBLE

        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        showVoiceprint()

        SvgaParserAdapter.parse(getFirstLevelBgEffect(productModel), object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    private fun getFirstLevelBgEffect(productModel: ProductModel): String {
        productModel.effectModel?.items?.forEach {
            if (it.type == 1) {
                return it.sourceUrl
            }
        }

        return ""
    }

    fun showCoinEffect(productModel: ProductModel) {
        reset()
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(productModel?.effectModel?.items?.get(0)?.sourceUrl, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    fun showLightEffect(productModel: ProductModel) {
        reset()
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(productModel?.effectModel?.items?.get(0)?.sourceUrl, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    fun showDefaultBgEffect() {
        reset()

        avatarIv.visibility = View.VISIBLE
        avatarBox.visibility = View.VISIBLE
        showVoiceprint()
    }

    fun showDefaultLightEffect() {
        reset()
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(GRAB_BURST_BIG_SVGA, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    fun showDefaultCoinEffect() {
        reset()
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(COMMON_COIN_BIG_SVGA, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    fun showDefaultCardEffect() {
        reset()
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(COMMON_CARD_BIG_SVGA, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                bgSvga!!.loops = -1
                bgSvga!!.setImageDrawable(drawable)
                bgSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    private fun reset() {
        if (bgSvga != null) {
            bgSvga!!.callback = null
            bgSvga!!.stopAnimation(true)
            bgSvga.visibility = View.GONE
        }

        avatarIv.visibility = View.GONE
        avatarBox.visibility = View.GONE
        voiceprintSvga.visibility = View.GONE
        voiceprintSvga?.stopAnimation()
    }

    companion object {
        val GRAB_BURST_BIG_SVGA = "http://res-static.inframe.mobi/app/grab_burst_big_animation.svga"
        val COMMON_COIN_BIG_SVGA = "http://res-static.inframe.mobi/mall/libao/coin_500.svga"
        val COMMON_CARD_BIG_SVGA = "http://res-static.inframe.mobi/mall/libao/coin_500.svga"
    }
}
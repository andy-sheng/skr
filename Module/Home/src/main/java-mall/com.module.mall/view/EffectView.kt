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
    var lightSvga: SVGAImageView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.all_effect_view_layout, this)

        bgSvga = rootView.findViewById(R.id.bg_svga)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        avatarBox = rootView.findViewById(R.id.avatar_box)
        lightSvga = rootView.findViewById(R.id.light_svga)

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
    }

    fun showBgEffect(productModel: ProductModel) {
        bgSvga?.visibility = View.VISIBLE
        bgSvga?.loops = 1

        SvgaParserAdapter.parse(productModel.sourceURL, object : SVGAParser.ParseCompletion {
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
        lightSvga?.visibility = View.VISIBLE
        lightSvga?.loops = 1

        SvgaParserAdapter.parse(productModel.sourceURL, object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                lightSvga!!.loops = -1
                lightSvga!!.setImageDrawable(drawable)
                lightSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (lightSvga != null) {
            lightSvga!!.callback = null
            lightSvga!!.stopAnimation(true)
        }

        if (bgSvga != null) {
            bgSvga!!.callback = null
            bgSvga!!.stopAnimation(true)
        }
    }
}
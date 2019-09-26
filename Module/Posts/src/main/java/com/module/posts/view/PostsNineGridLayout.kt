package com.module.posts.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Handler
import android.util.AttributeSet

import com.common.image.fresco.FrescoWorker
import com.common.image.fresco.IFrescoCallBack
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.dp
import com.common.view.ninegrid.NineGridLayout
import com.common.view.ninegrid.RatioImageView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.imagepipeline.image.ImageInfo

/**
 * 描述：
 * 作者：HMY
 * 时间：2016/5/12
 */
class PostsNineGridLayout : NineGridLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    var clickListener: ((i: Int, url: String, urlList: List<String>) -> Unit)? = null

    override fun displayOneImage(imageView: RatioImageView, url: String, parentWidth: Int): Boolean {
        imageView.load(ImageFactory.newPathImage(url)
                .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_640)
                .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setCornerRadius(8.dp().toFloat())
                .setCallBack(object : IFrescoCallBack {
                    override fun processWithInfo(imageInfo: ImageInfo, animatable: Animatable?) {
                        val w = imageInfo.width
                        val h = imageInfo.height
                        val newW: Int
                        val newH: Int
                        when {
                            h > w * MAX_W_H_RATIO -> {//h:w = 5:3
                                newW = parentWidth / 2
                                newH = newW * 5 / 3
                            }
                            h < w -> {//h:w = 2:3
                                newW = parentWidth * 2 / 3
                                newH = newW * 2 / 3
                            }
                            else -> {//newH:h = newW :w
                                newW = parentWidth / 2
                                newH = h * newW / w
                            }
                        }
                        // 针对一个图片的 动态调整下宽高
                        setOneImageLayoutParams(imageView, newW, newH,url)
                    }

                    override fun processWithFailure() {
                    }

                })
                .build<BaseImage>())

        return false
    }

    override fun displayImage(imageView: RatioImageView, url: String) {
        imageView.load(ImageFactory.newPathImage(url)
                .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_320)
                .setCornerRadius(8.dp().toFloat())
                .build<BaseImage>())
    }

    override fun onClickImage(i: Int, url: String, urlList: List<String>) {
        clickListener?.invoke(i, url, urlList)
    }

    companion object {

        protected val MAX_W_H_RATIO = 3
    }
}

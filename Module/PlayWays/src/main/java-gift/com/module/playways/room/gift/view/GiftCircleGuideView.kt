package com.module.playways.room.gift.view

import android.content.Context
import android.graphics.Color
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.widget.LinearLayout
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R

class GiftCircleGuideView : LinearLayout {
    val TAG = "GiftCircleGuideView"

    val imageViewList = ArrayList<ExImageView>()

    var currentSelectedImageView: ExImageView? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {

        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

        }

        override fun onPageSelected(p0: Int) {
            currentSelectedImageView?.isSelected = false
            currentSelectedImageView = imageViewList.get(p0)
            currentSelectedImageView?.isSelected = true
        }
    }

    fun setViewPager(viewPager: ViewPager, count: Int) {
        viewPager.addOnPageChangeListener(onPageChangeListener)
        repeat(count) {
            val imageView = ExImageView(context)
            imageViewList.add(imageView)
            val param = LayoutParams(U.getDisplayUtils().dip2px(6f), U.getDisplayUtils().dip2px(6f))
            imageView.layoutParams = param
            val mDrawable = DrawableCreator.Builder()
                    .setCornersRadius(U.getDisplayUtils().dip2px(3f).toFloat())
                    .setSelectedDrawable(DrawableCreator.Builder()
                            .setCornersRadius(U.getDisplayUtils().dip2px(3f).toFloat())
                            .setSolidColor(Color.parseColor("#93BEFF")).build())
                    .setUnSelectedDrawable(DrawableCreator.Builder()
                            .setCornersRadius(U.getDisplayUtils().dip2px(3f).toFloat())
                            .setSolidColor(U.getColor(R.color.black_trans_30)).build())
                    .build()

            imageView.background = mDrawable

            if (it == 0) {
                currentSelectedImageView = imageView
                currentSelectedImageView?.isSelected = true
            } else {
                param.leftMargin = U.getDisplayUtils().dip2px(10f)
                imageView.isSelected = false
            }

            addView(imageView)
        }
    }
}

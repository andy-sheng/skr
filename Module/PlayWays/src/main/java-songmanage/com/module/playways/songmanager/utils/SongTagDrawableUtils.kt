package com.module.playways.songmanager.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.common.utils.U
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R

object SongTagDrawableUtils {

    // 红色按钮背景
    val redDrawable: Drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45f).toFloat())
            .setSolidColor(Color.parseColor("#FF8AB6"))
            .setStrokeColor(Color.parseColor("#3B4E79"))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
            .build()

    // 灰色按钮背景
    val grayDrawable: Drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45f).toFloat())
            .setSolidColor(Color.parseColor("#B1AC99"))
            .setStrokeColor(Color.parseColor("#3B4E79"))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
            .build()

    // 合唱标签背景
    val chorusDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#7088FF"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    // pk标签背景
    val pkDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#E55088"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    // 小游戏标签背景
    val miniGameDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    // 自由麦标签背景
    val freeMicDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#C856E0"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()
}
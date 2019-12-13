package com.module.club.homepage.utils

import android.graphics.Color
import com.common.utils.dp
import com.common.view.ex.drawable.DrawableCreator

object ClubRoleUtils {

    val leaderDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFB02C"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    val leaderTextColor = Color.parseColor("#8B572A")

    val secondLeaderDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFD287"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    val secondLeaderTextColor = Color.parseColor("#8B572A")

    val hostDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#B8E986"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    val hostTextColor = Color.parseColor("#417505")
}
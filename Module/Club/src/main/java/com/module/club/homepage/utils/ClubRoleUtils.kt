package com.module.club.homepage.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.common.utils.dp
import com.common.view.ex.drawable.DrawableCreator
import com.zq.live.proto.Common.EClubMemberRoleType

object ClubRoleUtils {

    fun getClubRoleBackground(roleType: Int): Drawable? {
        return when (roleType) {
            EClubMemberRoleType.ECMRT_Founder.value -> leaderDrawable
            EClubMemberRoleType.ECMRT_CoFounder.value -> secondLeaderDrawable
            EClubMemberRoleType.ECMRT_Hostman.value -> hostDrawable
            else -> null
        }
    }

    fun getClubRoleTextColor(roleType: Int): Int? {
        return when (roleType) {
            EClubMemberRoleType.ECMRT_Founder.value -> leaderTextColor
            EClubMemberRoleType.ECMRT_CoFounder.value -> secondLeaderTextColor
            EClubMemberRoleType.ECMRT_Hostman.value -> hostTextColor
            else -> null
        }
    }

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
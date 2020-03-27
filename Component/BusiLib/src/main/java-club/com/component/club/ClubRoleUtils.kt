package com.component.club

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
            else -> memberDrawable
        }
    }

    fun getClubRoleTextColor(roleType: Int): Int? {
        return when (roleType) {
            EClubMemberRoleType.ECMRT_Founder.value -> leaderTextColor
            EClubMemberRoleType.ECMRT_CoFounder.value -> secondLeaderTextColor
            EClubMemberRoleType.ECMRT_Hostman.value -> hostTextColor
            else -> memberTextColor
        }
    }

    private val leaderDrawable: Drawable = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#FF9C72"), Color.parseColor("#FF7939"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    private val leaderTextColor = Color.parseColor("#ccffffff")

    private val secondLeaderDrawable: Drawable = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#FFD382"), Color.parseColor("#FFAB39"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    private val secondLeaderTextColor = Color.parseColor("#ccffffff")

    private val hostDrawable: Drawable = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#A6E6AC"), Color.parseColor("#86D07E"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    private val hostTextColor = Color.parseColor("#ccffffff")

    private val memberDrawable: Drawable = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#C9B4FF"), Color.parseColor("#AC95E6"))
            .setCornersRadius(4.dp().toFloat())
            .build()
    private val memberTextColor = Color.parseColor("#ccffffff")
}
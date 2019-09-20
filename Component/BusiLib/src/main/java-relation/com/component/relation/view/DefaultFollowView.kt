package com.component.relation.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.R

class DefaultFollowView : FollowActionView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun isFriendState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(U.getColor(R.color.white))
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .build()

        text = "已互关"
        background = followState
        setTextColor(Color.parseColor("#AD6C00"))
    }

    override fun isFollowState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(U.getColor(R.color.white))
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .build()

        text = "已关注"
        background = followState
        setTextColor(Color.parseColor("#AD6C00"))
    }

    override fun isStrangerState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(Color.parseColor("#FFC15B"))
                .build()

        text = "+关注"
        background = followState
        setTextColor(Color.parseColor("#AD6C00"))
        setDebounceViewClickListener {
            userID?.let {
                UserInfoManager.getInstance().mateRelation(it, UserInfoManager.RA_BUILD, false, 0, null)
            }
        }
    }

    override fun useEventBus(): Boolean = true
}
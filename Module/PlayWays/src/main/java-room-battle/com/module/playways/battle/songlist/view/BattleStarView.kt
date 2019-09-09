package com.module.playways.battle.songlist.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.common.utils.dp
import com.module.playways.R

/**
 * 评分控件 星星
 */
class BattleStarView : LinearLayout {

    val mTag = "BattleStarView"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        orientation = HORIZONTAL
    }

    fun bindData(cur: Int, max: Int) {
        removeAllViews()
        for (i in 1..max) {
            val imageView = ImageView(context)
            if (i <= cur) {
                imageView.setImageResource(R.drawable.battle_star_light)
            } else {
                imageView.setImageResource(R.drawable.battle_star_normal)
            }
            val layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            layoutParams.leftMargin = 1.dp()
            layoutParams.rightMargin = 1.dp()
            addView(imageView, layoutParams)
        }
    }
}
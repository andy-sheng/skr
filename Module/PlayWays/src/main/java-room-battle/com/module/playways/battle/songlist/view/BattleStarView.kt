package com.module.playways.battle.songlist.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.common.utils.U
import com.common.utils.dp
import com.module.playways.R

/**
 * 评分控件 星星
 */
class BattleStarView : LinearLayout {

    val mTag = "BattleStarView"

    private var starWidth = 0
    private var starHeight = 0
    private var normalDrawable: Drawable = U.getDrawable(R.drawable.battle_star_normal)
    private var selectedDrawable: Drawable = U.getDrawable(R.drawable.battle_star_light)

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    init {
        orientation = HORIZONTAL
    }

    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.battleStar)
        starWidth = typedArray.getDimensionPixelSize(R.styleable.battleStar_width, 15.dp())
        starHeight = typedArray.getDimensionPixelSize(R.styleable.battleStar_height, 14.dp())
        normalDrawable = typedArray.getDrawable(R.styleable.battleStar_normalDrawable)
                ?: U.getDrawable(R.drawable.battle_star_normal)
        selectedDrawable = typedArray.getDrawable(R.styleable.battleStar_selectedDrawable)
                ?: U.getDrawable(R.drawable.battle_star_light)
        typedArray.recycle()
    }

    fun bindData(cur: Int, max: Int) {
        removeAllViews()
        for (i in 1..max) {
            val imageView = ImageView(context)
            if (i <= cur) {
                imageView.background = selectedDrawable
            } else {
                imageView.background = normalDrawable
            }
            val layoutParams = LayoutParams(starWidth, starHeight)
            layoutParams.leftMargin = 1.dp()
            layoutParams.rightMargin = 1.dp()
            addView(imageView, layoutParams)
        }
    }
}
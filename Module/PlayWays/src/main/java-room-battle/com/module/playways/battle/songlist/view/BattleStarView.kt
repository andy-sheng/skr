package com.module.playways.battle.songlist.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet

/**
 * 评分控件 星星
 */
class BattleStarView : ConstraintLayout {

    val mTag = "BattleStarView"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

    }

    fun bindData(cur: Int, max: Int) {

    }
}
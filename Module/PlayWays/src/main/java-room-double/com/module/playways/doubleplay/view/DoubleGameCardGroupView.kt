package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet


class DoubleGameCardGroupView : ConstraintLayout {
    val mCard1: DoubleGameSelectCardView
    val mCard2: DoubleGameSelectCardView
    val mCard3: DoubleGameSelectCardView
    val mCard4: DoubleGameSelectCardView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, com.module.playways.R.layout.double_game_cardgroup_layout, this)
        mCard1 = findViewById(com.module.playways.R.id.card_1)
        mCard2 = findViewById(com.module.playways.R.id.card_2)
        mCard3 = findViewById(com.module.playways.R.id.card_3)
        mCard4 = findViewById(com.module.playways.R.id.card_4)
    }

    fun setData() {

    }
}
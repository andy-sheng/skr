package com.module.playways.race.room.view.matchview

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.module.playways.R


// 匹配中类似赌博机的效果
class RaceMatchView : ConstraintLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val leftView: RaceMatchItemView
    private val rightView: RaceMatchItemView


    init {
        View.inflate(context, R.layout.race_match_view_layout, this)

        leftView = this.findViewById(R.id.left_view)
        rightView = this.findViewById(R.id.right_view)

        leftView.setData()
        rightView.setData()
    }
}


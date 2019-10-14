package com.module.playways.room.room.matchview

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.module.playways.R


// 匹配中类似赌博机的效果
class RankMatchView : ConstraintLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val leftView: RankMatchItemView
    private val rightView: RankMatchItemView


    init {
        View.inflate(context, R.layout.rank_match_view_layout, this)

        leftView = this.findViewById(R.id.left_view)
        rightView = this.findViewById(R.id.right_view)

        leftView.setData()
        rightView.setData()
    }
}


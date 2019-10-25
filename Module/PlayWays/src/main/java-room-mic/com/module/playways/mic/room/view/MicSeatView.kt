package com.module.playways.mic.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.playways.R

// 右边操作区域，投票
class MicSeatView : ConstraintLayout {

    val TAG = "MicSeatView"

    var bg: ExImageView
    var recyclerView: RecyclerView

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.mic_seat_view_layout, this)
        bg = rootView.findViewById(R.id.bg)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        setDebounceViewClickListener {
            hide()
        }

        bg.setDebounceViewClickListener {
            //拦截
        }
    }

    private fun hide() {

    }
}

package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R

/**
 * 切换房间过场UI
 */
class GrabChangeRoomTransitionView : RelativeLayout {

    private val mChangeRoomTipTv: ExTextView
    private val mChangeRoomIv: ExImageView

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_change_room_transition_view, this)
        mChangeRoomTipTv = this.findViewById<View>(R.id.change_room_tip_tv) as ExTextView
        mChangeRoomIv = this.findViewById<View>(R.id.change_room_iv) as ExImageView
        setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {

            }
        })
    }


}

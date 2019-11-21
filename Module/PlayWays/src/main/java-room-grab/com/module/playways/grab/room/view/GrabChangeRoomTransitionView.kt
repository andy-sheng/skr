package com.module.playways.grab.room.view

import android.view.View
import android.view.ViewStub
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R

/**
 * 切换房间过场UI
 */
class GrabChangeRoomTransitionView(viewStub: ViewStub) : ExViewStub(viewStub) {
    private var mChangeRoomTipTv: ExTextView?=null
    private var mChangeRoomIv: ExImageView?=null

    override fun init(parentView: View) {
        mChangeRoomTipTv = parentView.findViewById(R.id.change_room_tip_tv)
        mChangeRoomIv = parentView.findViewById(R.id.change_room_iv)
        mParentView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {

            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_change_room_transition_view
    }

}

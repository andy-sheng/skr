package com.module.playways.party.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.room.data.H

class PartyRightOpView : ConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val applyList: ExTextView
    private val opMicTv: ExTextView

    var listener: Listener? = null

    val mic_status_unapply = 1 // 未申请
    val mic_status_wating = 2  // 申请中
    val mic_status_online = 3  // 在麦上

    var micStatus = mic_status_online  // 默认

    init {
        View.inflate(context, R.layout.party_right_op_view_layout, this)

        applyList = this.findViewById(R.id.apply_list)
        opMicTv = this.findViewById(R.id.op_mic_tv)

        applyList.setDebounceViewClickListener {
            listener?.onClickApplyList()
        }
        opMicTv.setDebounceViewClickListener {
            // 申请 取消 下麦
            when (micStatus) {
                mic_status_unapply -> {
                    // todo 去申请上麦
                }
                mic_status_wating -> {
                    // todo 等待上麦中，取消申请上麦
                }
                else -> {
                    // todo 在麦上，下麦
                }
            }
        }
    }

    // 身份改变需要重置
    fun bindData() {
        val myInfo = H.partyRoomData?.getMyInfoInParty()
        when {
            myInfo?.isHost() == true -> {
                applyList.visibility = View.VISIBLE
                opMicTv.visibility = View.GONE
                applyList.text = "申请${H.partyRoomData?.applyUserCnt}人"
                micStatus = mic_status_online
            }
            myInfo?.isGuest() == true -> {
                applyList.visibility = View.GONE
                opMicTv.visibility = View.VISIBLE
                opMicTv.text = "下麦"
                micStatus = mic_status_online
            }
            else -> {
                applyList.visibility = View.GONE
                opMicTv.visibility = View.VISIBLE
                opMicTv.text = "申请上麦"
                micStatus = mic_status_unapply
            }
        }
    }

    interface Listener {
        fun onClickApplyList()
    }
}
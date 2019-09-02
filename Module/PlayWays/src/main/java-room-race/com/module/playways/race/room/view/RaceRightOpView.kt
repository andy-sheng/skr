package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.playways.R

// 右边操作区域，投票
class RaceRightOpView : ConstraintLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    val voteIv: ExImageView
    val giveUpIv: ExImageView

    var mListener: RightOpListener? = null

    init {
        View.inflate(context, R.layout.race_right_op_view, this)

        voteIv = this.findViewById(R.id.vote_iv)
        giveUpIv = this.findViewById(R.id.give_up_iv)

        voteIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (voteIv.isSelected) {
                    U.getToastUtil().showShort("已经投过票了")
                } else {
                    // 投票
                    mListener?.onClickVote()
                }
            }
        })

        giveUpIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (giveUpIv.isSelected) {
                    U.getToastUtil().showShort("已经放弃演唱了")
                } else {
                    // 放弃演唱
                    mListener?.onClickGiveUp()
                }
            }
        })
    }

    fun showVote(isSelected: Boolean) {
        visibility = View.VISIBLE
        giveUpIv.visibility = View.GONE
        voteIv.visibility = View.VISIBLE
        voteIv.isSelected = isSelected
        voteIv.isClickable = !isSelected
    }

    fun showGiveUp(isSelected: Boolean) {
        visibility = View.VISIBLE
        voteIv.visibility = View.GONE
        giveUpIv.visibility = View.VISIBLE
        giveUpIv.isSelected = isSelected
        giveUpIv.isClickable = !isSelected
    }

    fun setListener(listener: RightOpListener) {
        mListener = listener
    }
}

interface RightOpListener {
    fun onClickVote()
    fun onClickGiveUp()
}
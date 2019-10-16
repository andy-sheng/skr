package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.model.FuncationModel

class FuncationAreaViewHolder(itemView: View,
                              listener: ClickGameListener) : RecyclerView.ViewHolder(itemView) {
    val mTaskIv: ExTextView = itemView.findViewById(R.id.task_iv)
    val mTaskRedIv: ExImageView = itemView.findViewById(R.id.task_red_iv)
    val mRankIv: ExTextView = itemView.findViewById(R.id.rank_iv)
    val mPracticeIv: ExTextView = itemView.findViewById(R.id.practice_iv)

    init {
        mTaskIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                listener.onClickTaskListener()
            }
        })

        mRankIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                listener.onClickRankListener()
            }
        })

        mPracticeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                listener.onClickPracticeListener()
            }
        })

    }

    fun bindData(model: FuncationModel) {
        if (model.isTaskHasRed) {
            mTaskRedIv.visibility = View.VISIBLE
        } else {
            mTaskRedIv.visibility = View.GONE
        }
    }
}

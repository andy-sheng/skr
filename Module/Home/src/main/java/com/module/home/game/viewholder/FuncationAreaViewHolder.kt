package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.model.FuncationModel

class FuncationAreaViewHolder(itemView: View,
                              listener: ClickGameListener) : RecyclerView.ViewHolder(itemView) {
    private val mTaskIv: ExImageView = itemView.findViewById(R.id.task_iv)
    private val mTaskRedIv: ExImageView = itemView.findViewById(R.id.task_red_iv)
    private val mRankIv: ExImageView = itemView.findViewById(R.id.rank_iv)
    private val mPracticeIv: ExImageView = itemView.findViewById(R.id.practice_iv)
    private val mMallIv: ExImageView = itemView.findViewById(R.id.mall_iv)


    init {
        mTaskIv.setDebounceViewClickListener { listener.onClickTaskListener() }
        mRankIv.setDebounceViewClickListener { listener.onClickRankListener() }
        mPracticeIv.setDebounceViewClickListener { listener.onClickPracticeListener() }
        mMallIv.setDebounceViewClickListener { listener.onClickMallListner() }
    }

    fun bindData(model: FuncationModel) {
        if (model.isTaskHasRed) {
            mTaskRedIv.visibility = View.VISIBLE
        } else {
            mTaskRedIv.visibility = View.GONE
        }
    }
}

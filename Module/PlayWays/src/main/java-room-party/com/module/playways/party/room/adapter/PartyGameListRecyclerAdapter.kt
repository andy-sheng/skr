package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyRule
import com.zq.live.proto.PartyRoom.EPGameType

class PartyGameListRecyclerAdapter : RecyclerView.Adapter<PartyGameListRecyclerAdapter.PartyGameListHolder>() {
    val mPartyRuleList = ArrayList<PartyRule>()
    var mMoreMethod: ((PartyRule) -> Unit)? = null
    var mDetailMethod: ((PartyRule) -> Unit)? = null
    var mAddMethod: ((PartyRule) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyGameListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_game_list_item_layout, parent, false)
        return PartyGameListHolder(view)
    }

    override fun getItemCount(): Int {
        return mPartyRuleList.size
    }

    override fun onBindViewHolder(holder: PartyGameListHolder, position: Int) {
        holder.bindData(position, mPartyRuleList.get(position))
    }

    fun addData(list: List<PartyRule>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (mPartyRuleList.size > 0) mPartyRuleList.size - 1 else 0
                mPartyRuleList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, mPartyRuleList.size - startNotifyIndex)
            }
        }
    }

    inner class PartyGameListHolder : RecyclerView.ViewHolder {
        val TAG = "PartyGameListHolder"
        var gameNameTv: ExTextView
        var detailIv: ExImageView
        var addTv: ExTextView
        var moreTv: ExImageView
        var pos = -1
        var model: PartyRule? = null

        constructor(itemView: View) : super(itemView) {
            gameNameTv = itemView.findViewById(R.id.game_name_tv)
            detailIv = itemView.findViewById(R.id.detail_iv)
            addTv = itemView.findViewById(R.id.add_tv)
            moreTv = itemView.findViewById(R.id.more_tv)

            gameNameTv.maxWidth = U.getDisplayUtils().phoneWidth - U.getDisplayUtils().dip2px(170f)

            addTv.setDebounceViewClickListener {
                mAddMethod?.invoke(model!!)
            }

            itemView.setDebounceViewClickListener {
                if (model?.ruleType == EPGameType.PGT_Play.ordinal) {
                    mMoreMethod?.invoke(model!!)
                }
            }

            detailIv.setDebounceViewClickListener {
                mDetailMethod?.invoke(model!!)
            }
        }

        fun bindData(position: Int, model: PartyRule) {
            pos = position
            this.model = model

            gameNameTv.text = model.ruleName
            if (model.ruleType == EPGameType.PGT_Play.ordinal) {
                moreTv.visibility = View.VISIBLE
                addTv.visibility = View.GONE
            } else {
                moreTv.visibility = View.GONE
                addTv.visibility = View.VISIBLE
            }
        }
    }
}
package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.module.playways.R
import com.module.playways.party.room.model.PartySelectedGameModel

class PartyHasSelectedGameListRecyclerAdapter : DiffAdapter<PartySelectedGameModel, PartyHasSelectedGameListRecyclerAdapter.ModelHolder>() {
    var mDelMethod: ((Int, PartySelectedGameModel) -> Unit)? = null
    var mUpMethod: ((Int, PartySelectedGameModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_has_selected_game_list_item_layout, parent, false)
        return ModelHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ModelHolder, position: Int) {
        holder.bindData(dataList.get(position), position)
    }

    fun addData(list: List<PartySelectedGameModel>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (dataList.size > 0) dataList.size - 1 else 0
                dataList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, dataList.size - startNotifyIndex)
            }
        }
    }

    fun upModel(pos: Int) {
        val model = dataList.removeAt(pos)
        dataList.add(0, model)
        notifyDataSetChanged()
    }

    inner class ModelHolder : RecyclerView.ViewHolder {
        val TAG = "ModelHolder"
        var gameNameTv: ExTextView
        var detailIv: ExImageView
        var upIv: ExImageView
        var delTv: ExTextView
        var model: PartySelectedGameModel? = null

        constructor(itemView: View) : super(itemView) {
            gameNameTv = itemView.findViewById(R.id.game_name_tv)
            detailIv = itemView.findViewById(R.id.detail_iv)
            upIv = itemView.findViewById(R.id.up_iv)
            delTv = itemView.findViewById(R.id.del_tv)

            delTv.setDebounceViewClickListener {
                mDelMethod?.invoke(dataList.indexOf(model!!), model!!)
            }

            upIv.setDebounceViewClickListener {
                mUpMethod?.invoke(dataList.indexOf(model!!), model!!)
            }
        }

        fun bindData(model: PartySelectedGameModel, position: Int) {
            this.model = model
            gameNameTv.text = model.name

            if (position == 0) {
                upIv.visibility = View.GONE
                delTv.visibility = View.GONE
            } else {
                upIv.visibility = View.VISIBLE
                delTv.visibility = View.VISIBLE
            }
        }
    }
}
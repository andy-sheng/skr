package com.component.person.producation.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.R
import com.component.person.producation.model.ProducationModel
import com.component.person.producation.holder.EmptyProducationHolder
import com.component.person.producation.holder.ProducationHolder

/**
 * 用recycleview做一个空页面
 */
class ProducationAdapter(private var mIsSelf: Boolean) : DiffAdapter<ProducationModel, RecyclerView.ViewHolder>() {

    var playingWorksIdPosition = -1
        internal set  //选中播放的id

    var mOnClickDeleListener: ((position: Int, model: ProducationModel?) -> Unit)? = null
    var mOnClickShareListener: ((position: Int, model: ProducationModel?) -> Unit)? = null
    var mOnClickPlayListener: ((view: View, play: Boolean, position: Int, model: ProducationModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.producation_item_view_layout, parent, false)
            return ProducationHolder(view, mIsSelf, mOnClickDeleListener, mOnClickShareListener, mOnClickPlayListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.produacation_empty_view_layout, parent, false)
            return EmptyProducationHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mDataList != null && mDataList.size > 0) {
            val model = mDataList[position]
            val viewHolder = holder as ProducationHolder
            if (playingWorksIdPosition == model.worksID) {
                viewHolder.bindData(position, model, true)
            } else {
                viewHolder.bindData(position, model, false)
            }
        } else {
            val viewHolder = holder as EmptyProducationHolder
            viewHolder.bindData(mIsSelf)
        }
    }

    override fun getItemCount(): Int {
        return if (mDataList != null && mDataList.size > 0) {
            mDataList.size
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList != null && mDataList.size > 0) {
            TYPE_NORMAL
        } else {
            TYPE_EMPTY
        }
    }

    fun setPlayPosition(workId: Int) {
        playingWorksIdPosition = workId
        notifyDataSetChanged()
    }

    companion object {

        private val TYPE_NORMAL = 1
        private val TYPE_EMPTY = 2
    }
}

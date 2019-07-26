package com.module.feeds.report.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.feeds.R
import com.module.feeds.report.model.FeedReportModel

class FeedReportAdapter : RecyclerView.Adapter<FeedReportAdapter.FeedReportViewHolder>() {

    var mDataList = ArrayList<FeedReportModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_report_item_layout, parent, false)
        return FeedReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedReportViewHolder, position: Int) {
    }

    override fun onBindViewHolder(holder: FeedReportViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            holder.bindData(position, mDataList[position])
        } else {
            holder.refreshSelected(position, mDataList[position])
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun getSelectedList(): ArrayList<FeedReportModel> {
        var result = ArrayList<FeedReportModel>()
        for (model in mDataList) {
            if (model.isSelected) {
                result.add(model)
            }
        }
        return result
    }

    inner class FeedReportViewHolder(var item: View) : RecyclerView.ViewHolder(item) {

        var mPosition = 0
        var mModel: FeedReportModel? = null

        val mContentTv: TextView = item.findViewById(R.id.content_tv)
        val mSelectIv: ExImageView = item.findViewById(R.id.select_iv)

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    clickItem()
                }
            })

            mSelectIv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    clickItem()
                }
            })
        }

        fun bindData(position: Int, model: FeedReportModel) {
            this.mPosition = position
            this.mModel = model

            mContentTv.text = model.reportDesc
            refreshSelected(position, model)
        }

        fun refreshSelected(position: Int, model: FeedReportModel) {
            this.mPosition = position
            this.mModel = model

            mSelectIv.isSelected = model.isSelected
        }

        fun clickItem() {
            mModel?.let {
                it.isSelected = !it.isSelected
                mDataList[mPosition] = it
                this@FeedReportAdapter.notifyItemChanged(mPosition, 0)
            }
        }
    }
}


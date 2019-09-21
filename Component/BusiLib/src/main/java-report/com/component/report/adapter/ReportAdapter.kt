package com.component.report.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.component.busilib.R

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.FeedReportViewHolder>() {

    var mDataList = ArrayList<ReportModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_item_layout, parent, false)
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

    // 获得所有选中的类型
    fun getSelectedList(): ArrayList<Int> {
        var result = ArrayList<Int>()
        for (model in mDataList) {
            if (model.isSelected) {
                result.add(model.type)
            }
        }
        return result
    }

    inner class FeedReportViewHolder(var item: View) : RecyclerView.ViewHolder(item) {

        var mPosition = 0
        var mModel: ReportModel? = null

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

        fun bindData(position: Int, model: ReportModel) {
            this.mPosition = position
            this.mModel = model

            mContentTv.text = model.reportDesc
            refreshSelected(position, model)
        }

        fun refreshSelected(position: Int, model: ReportModel) {
            this.mPosition = position
            this.mModel = model

            mSelectIv.isSelected = model.isSelected
        }

        fun clickItem() {
            mModel?.let {
                it.isSelected = !it.isSelected
                mDataList[mPosition] = it
                this@ReportAdapter.notifyItemChanged(mPosition, 0)
            }
        }
    }
}


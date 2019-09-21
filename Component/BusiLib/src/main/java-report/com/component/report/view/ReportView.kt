package com.component.report.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.component.busilib.R
import com.component.report.adapter.ReportAdapter
import com.component.report.adapter.ReportModel

class ReportView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val adapter: ReportAdapter = ReportAdapter()
    val recyclerView: RecyclerView

    init {
        View.inflate(context, R.layout.report_view_layout, this)

        recyclerView = this.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    fun setDataList(list: ArrayList<ReportModel>) {
        adapter.mDataList.clear()
        adapter.mDataList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    // 获得所有选中的类型
    fun getSelectedList(): ArrayList<Int> {
        return adapter.getSelectedList()
    }
}
package com.module.posts.activity

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.report.adapter.ReportModel
import com.component.report.view.ReportView
import com.module.RouterConstants
import com.module.posts.R

@Route(path = RouterConstants.ACTIVITY_POSTS_REPORT)
class PostsReportActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var textHintTv: TextView
    lateinit var reportView: ReportView
    lateinit var contentEdit: NoLeakEditText
    lateinit var sumbitTv: ExTextView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_report_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        textHintTv = findViewById(R.id.text_hint_tv)
        reportView = findViewById(R.id.report_view)
        contentEdit = findViewById(R.id.content_edit)
        sumbitTv = findViewById(R.id.sumbit_tv)

        contentEdit.hint = SpannedString("请详细描述你的问题")

        reportView.setDataList(getReportPosts())

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        sumbitTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                val list = reportView.getSelectedList()
                val content = contentEdit.text.toString()
                //具体的举报
                // todo 待补全
            }
        })
    }

    private fun getReportPosts(): ArrayList<ReportModel> {
        var list = ArrayList<ReportModel>()
        list.add(ReportModel(11, "垃圾广告", false))
        list.add(ReportModel(3, "色情低俗", false))
        list.add(ReportModel(2, "攻击谩骂", false))
        list.add(ReportModel(1, "诈骗信息", false))
        list.add(ReportModel(8, "政治敏感", false))
        list.add(ReportModel(4, "血腥暴力", false))
        list.add(ReportModel(13, "抄袭、非原创", false))
        list.add(ReportModel(10, "其它问题", false))
        return list
    }

    override fun useEventBus(): Boolean {
        return super.useEventBus()
    }
}
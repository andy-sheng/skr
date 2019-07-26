package com.module.feeds.report.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.common.base.BaseFragment
import com.module.feeds.R
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.component.dialog.FeedsMoreDialogView
import com.module.feeds.report.adapter.FeedReportAdapter
import com.module.feeds.report.model.FeedReportModel

class FeedReportFragment : BaseFragment() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mTextHintTv: TextView
    lateinit var mRecyclerView: RecyclerView
    lateinit var mContentEdit: NoLeakEditText
    lateinit var mSumbitTv: ExTextView

    lateinit var mAdapter: FeedReportAdapter

    var mFrom = FeedsMoreDialogView.FROM_FEED

    override fun initView(): Int {
        return R.layout.feeds_report_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = rootView.findViewById(R.id.titlebar)
        mTextHintTv = rootView.findViewById(R.id.text_hint_tv)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)
        mContentEdit = rootView.findViewById(R.id.content_edit)
        mSumbitTv = rootView.findViewById(R.id.sumbit_tv)

        mAdapter = FeedReportAdapter()
        if (mFrom == FeedsMoreDialogView.FROM_FEED) {
            mAdapter.mDataList = getReportFeed()
        } else if (mFrom == FeedsMoreDialogView.FROM_COMMENT) {
            mAdapter.mDataList = getReportComment()
        }
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                activity?.finish()
            }
        })

        mSumbitTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                val list = mAdapter.getSelectedList()
                val content = mContentEdit.text.toString()
                when (mFrom) {
                    FeedsMoreDialogView.FROM_FEED -> {
                        // 举报作品
                    }
                    FeedsMoreDialogView.FROM_COMMENT -> {
                        // 举报评论
                    }
                    else -> {

                    }
                }
            }
        })
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mFrom = data as Int
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun isBlackStatusBarText(): Boolean {
        return true
    }

    private fun getReportFeed(): ArrayList<FeedReportModel> {
        var list = ArrayList<FeedReportModel>()
        list.add(FeedReportModel(11, "垃圾广告", false))
        list.add(FeedReportModel(3, "色情低俗", false))
        list.add(FeedReportModel(2, "攻击谩骂", false))
        list.add(FeedReportModel(1, "诈骗信息", false))
        list.add(FeedReportModel(8, "政治敏感", false))
        list.add(FeedReportModel(4, "血腥暴力", false))
        list.add(FeedReportModel(13, "抄袭、非原创", false))
        list.add(FeedReportModel(12, "歌词有误", false))
        list.add(FeedReportModel(10, "其它问题", false))
        return list
    }

    private fun getReportComment(): ArrayList<FeedReportModel> {
        var list = ArrayList<FeedReportModel>()
        list.add(FeedReportModel(11, "垃圾广告", false))
        list.add(FeedReportModel(3, "色情低俗", false))
        list.add(FeedReportModel(2, "攻击谩骂", false))
        list.add(FeedReportModel(1, "诈骗信息", false))
        list.add(FeedReportModel(8, "政治敏感", false))
        list.add(FeedReportModel(4, "血腥暴力", false))
        list.add(FeedReportModel(10, "其它问题", false))
        return list
    }
}
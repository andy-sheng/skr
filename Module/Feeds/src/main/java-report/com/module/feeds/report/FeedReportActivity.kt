package com.module.feeds.report

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.UserInfoServerApi
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.dialog.FeedsMoreDialogView
import com.component.toast.CommonToastView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.report.adapter.FeedReportAdapter
import com.module.feeds.report.model.FeedReportModel
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

@Route(path = RouterConstants.ACTIVITY_FEEDS_REPORT)
class FeedReportActivity : BaseActivity() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mTextHintTv: TextView
    lateinit var mRecyclerView: RecyclerView
    lateinit var mContentEdit: NoLeakEditText
    lateinit var mSumbitTv: ExTextView

    lateinit var mAdapter: FeedReportAdapter

    var mFrom = FeedsMoreDialogView.FROM_FEED

    var mTargetID = 0  // 被举报人ID
    var mSongID = 0    // 神曲的songID
    var mCommentID = 0  // 评论ID

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_report_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFrom = intent.getIntExtra("from", FeedsMoreDialogView.FROM_FEED)
        mTargetID = intent.getIntExtra("targetID", 0)
        mSongID = intent.getIntExtra("songID", 0)
        mCommentID = intent.getIntExtra("commentID", 0)

        mTitlebar = findViewById(R.id.titlebar)
        mTextHintTv = findViewById(R.id.text_hint_tv)
        mRecyclerView = findViewById(R.id.recycler_view)
        mContentEdit = findViewById(R.id.content_edit)
        mSumbitTv = findViewById(R.id.sumbit_tv)

        mAdapter = FeedReportAdapter()
        if (mFrom == FeedsMoreDialogView.FROM_COMMENT) {
            // 只有这个是举报评论的
            mAdapter.mDataList = getReportComment()
        } else {
            mAdapter.mDataList = getReportFeed()
        }

        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        mSumbitTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                val list = mAdapter.getSelectedList()
                val content = mContentEdit.text.toString()
                when (mFrom) {
                    FeedsMoreDialogView.FROM_FEED -> {
                        // 举报作品,来源神曲
                        reportFeed(content, list, 4)
                    }
                    FeedsMoreDialogView.FROM_FEED_DETAIL -> {
                        // 举报作品，来源详情
                        reportFeed(content, list, 6)
                    }
                    FeedsMoreDialogView.FROM_OTHER_PERSON -> {
                        // 举报作品，来源他人主页
                        reportFeed(content, list, 2)
                    }
                    FeedsMoreDialogView.FROM_COMMENT -> {
                        // 举报评论，来源评论
                        reportComment(content, list)
                    }
                    else -> {
                        MyLog.w(TAG, "no from")
                    }
                }
            }
        })
    }

    override fun useEventBus(): Boolean {
        return false
    }

    fun reportFeed(content: String, typeList: List<Int>, source: Int) {
        val map = HashMap<String, Any>()
        map["targetID"] = mTargetID
        map["songID"] = mSongID
        map["content"] = content
        map["type"] = typeList
        map["source"] = source

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userInfoServerApi.report(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    U.getToastUtil().showSkrCustomShort(CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhichenggong_icon)
                            .setText("举报成功")
                            .build())
                    finish()
                } else {
                    U.getToastUtil().showSkrCustomShort(CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                            .setText("举报失败")
                            .build())
                    finish()
                }
            }
        }, this, ApiMethods.RequestControl("feedback", ApiMethods.ControlType.CancelThis))
    }

    fun reportComment(content: String, typeList: List<Int>) {
        val map = HashMap<String, Any>()
        map["targetID"] = mTargetID
        map["commentID"] = mCommentID
        map["content"] = content
        map["type"] = typeList
        map["source"] = 5

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userInfoServerApi.report(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    U.getToastUtil().showSkrCustomShort(CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhichenggong_icon)
                            .setText("举报成功")
                            .build())
                    finish()
                } else {
                    U.getToastUtil().showSkrCustomShort(CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                            .setText("举报失败")
                            .build())
                    finish()
                }
            }
        }, this, ApiMethods.RequestControl("feedback", ApiMethods.ControlType.CancelThis))
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
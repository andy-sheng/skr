package com.module.posts.activity

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.report.adapter.ReportModel
import com.component.report.view.ReportView
import com.component.toast.CommonToastView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.more.PostsMoreDialogView
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

@Route(path = RouterConstants.ACTIVITY_POSTS_REPORT)
class PostsReportActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var textHintTv: TextView
    lateinit var reportView: ReportView
    lateinit var contentEdit: NoLeakEditText
    lateinit var sumbitTv: ExTextView

    var mFrom = PostsMoreDialogView.FROM_POSTS_HOME  // 来源

    var targetID = 0  // 被举报人ID
    var postsID = 0L   // 被举报的帖子ID
    var commentID = 0L  // 被举报的帖子评论ID

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_report_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFrom = intent.getIntExtra("from", PostsMoreDialogView.FROM_POSTS_HOME)
        targetID = intent.getIntExtra("targetID", 0)
        postsID = intent.getLongExtra("postsID", 0L)
        commentID = intent.getLongExtra("commentID", 0L)

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
                when (mFrom) {
                    PostsMoreDialogView.FROM_POSTS_HOME -> reportPosts(content, list, 9)
                    PostsMoreDialogView.FROM_POSTS_TOPIC -> reportPosts(content, list, 9)
                    PostsMoreDialogView.FROM_POSTS_DETAIL -> reportPosts(content, list, 9)
                    PostsMoreDialogView.FROM_POSTS_PERSON -> reportPosts(content, list, 2)

                    else -> {
                        //todo donothing
                    }
                }
            }
        })
    }

    fun reportPosts(content: String, typeList: List<Int>, source: Int) {
        val map = HashMap<String, Any>()
        map["targetID"] = targetID
        map["postsID"] = postsID
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
        }, this, RequestControl("feedback", ControlType.CancelThis))
    }

    fun reportPostsComment(content: String, typeList: List<Int>, source: Int) {
        val map = HashMap<String, Any>()
        map["targetID"] = targetID
        map["postsID"] = postsID
        map["commentID"] = commentID
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
        }, this, RequestControl("feedback", ControlType.CancelThis))
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
        return false
    }
}
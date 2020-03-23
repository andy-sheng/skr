package com.module.club.homepage.view

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.member.ClubMemberInfoModel
import com.zq.live.proto.Common.EClubMemberRoleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// 家族简介
class ClubIntroView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var clubMemberInfo: ClubMemberInfo? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    val clubNoticeArea: ConstraintLayout
    val noticeTitleTv: TextView
    val noticeContentTv: TextView
    val clubIntroArea: ConstraintLayout
    val introTitleTv: TextView
    val introContentTv: TextView
    val memberTitleTv: TextView
    val memberAllTv: TextView
    val recyclerView: RecyclerView

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private val cnt = 15

    private val adapter: ClubMemberAdapter

    init {
        View.inflate(context, R.layout.club_tab_intro_view_layout, this)

        clubNoticeArea = this.findViewById(R.id.club_notice_area)
        noticeTitleTv = this.findViewById(R.id.notice_title_tv)
        noticeContentTv = this.findViewById(R.id.notice_content_tv)
        clubIntroArea = this.findViewById(R.id.club_intro_area)
        introTitleTv = this.findViewById(R.id.intro_title_tv)
        introContentTv = this.findViewById(R.id.intro_content_tv)
        memberTitleTv = this.findViewById(R.id.member_title_tv)
        memberAllTv = this.findViewById(R.id.member_all_tv)
        recyclerView = this.findViewById(R.id.recycler_view)

        adapter = ClubMemberAdapter()
        recyclerView.layoutManager = GridLayoutManager(context, 6)
        recyclerView.adapter = adapter

        adapter.listener = { position, model ->
            model?.userInfoModel?.userId?.let {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", it)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        memberAllTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_MEMBER)
                    .withSerializable("clubMemberInfo", clubMemberInfo)
                    .navigation()
        }

    }

    fun loadData(flag: Boolean, callback: () -> Unit?) {
        loadData(callback)
    }

    fun loadMoreData(callback: () -> Unit?) {
        loadData(callback)
    }

    private fun loadData(callback: () -> Unit?) {
        launch {
            val result = subscribe(RequestControl("initData", ControlType.CancelThis)) {
                clubServerApi.getClubMemberList(clubMemberInfo?.club?.clubID ?: 0, 0, cnt)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("items"), ClubMemberInfoModel::class.java)
                adapter.mTotal = clubMemberInfo?.club?.memberCnt ?: 0
                adapter.mDataList.clear()
                if (!list.isNullOrEmpty()) {
                    adapter.mDataList.addAll(list)
                }
                adapter.notifyDataSetChanged()
            }
            refreshUI()
            callback.invoke()
        }
    }

    private fun refreshUI() {
        if (clubMemberInfo?.isMyClub() == true) {
            clubNoticeArea.visibility = View.VISIBLE
        } else {
            clubNoticeArea.visibility = View.GONE
        }

        noticeContentTv.text = clubMemberInfo?.club?.notice
        introContentTv.text = clubMemberInfo?.club?.desc
    }

    fun destroy() {
        cancel()
    }
}
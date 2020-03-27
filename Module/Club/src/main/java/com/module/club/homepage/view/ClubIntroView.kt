package com.module.club.homepage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.component.club.view.ClubMemberView
import com.module.RouterConstants
import com.module.club.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

// 家族简介
class ClubIntroView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    private var clubMemberInfo: ClubMemberInfo? = null

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
    val memberView: ClubMemberView

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

        memberView = this.findViewById(R.id.member_view)

        memberAllTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_MEMBER)
                    .withSerializable("clubMemberInfo", clubMemberInfo)
                    .navigation()
        }

    }

    fun setData(clubMemberInfo: ClubMemberInfo?) {
        this.clubMemberInfo = clubMemberInfo
        memberView.clubID = clubMemberInfo?.club?.clubID ?: 0
        memberView.memberCnt = clubMemberInfo?.club?.memberCnt ?: 0
    }

    fun loadData(flag: Boolean, callback: () -> Unit?) {
        loadData(callback)
    }

    fun loadMoreData(callback: () -> Unit?) {
        loadData(callback)
    }

    private fun loadData(callback: () -> Unit?) {
        refreshUI()
        memberView.loadData {
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
        memberView.destroy()
        cancel()
    }
}
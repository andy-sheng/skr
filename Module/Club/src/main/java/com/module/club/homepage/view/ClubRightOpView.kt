package com.module.club.homepage.view

import android.view.View
import android.view.ViewStub
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.view.ExViewStub
import com.common.view.ex.ExButton
import com.common.view.ex.ExTextView
import com.module.ModuleServiceManager
import com.module.club.R

/**
 * 家族首页底部操作按钮
 */
class ClubRightOpView(viewStub: ViewStub): ExViewStub(viewStub) {

    private val TAG = "ClubRightOpView"
    private var mApplyTv:ExTextView? = null
    private var mConversationTv:ExTextView? = null
    private var mPostTv: ExTextView? = null

    override fun init(parentView: View) {
        mApplyTv = parentView.findViewById(R.id.club_right_apply_tv)
        mConversationTv = parentView.findViewById(R.id.club_right_conversation_tv)
        mPostTv = parentView.findViewById(R.id.club_right_post_tv)
    }

    fun bindData(clubMemberInfo:ClubMemberInfo?){


        mApplyTv?.setDebounceViewClickListener{

        }

        mConversationTv?.setDebounceViewClickListener {view->
            view?:return@setDebounceViewClickListener
            val club = clubMemberInfo?.club?: MyLog.e(TAG, "未获取到家族信息").let {
                return@setDebounceViewClickListener
            }

            club.name.let {
                ModuleServiceManager.getInstance().msgService.startClubChat(view.context, club.clubID.toString(), it)
            }
        }

        mPostTv?.setDebounceViewClickListener { view->


        }
    }

    override fun layoutDesc(): Int {
        return R.layout.club_home_page_right_op_view_layout
    }

    fun show(){
        setVisibility(View.VISIBLE)
    }

    fun hide(){
        setVisibility(View.GONE)
    }
}
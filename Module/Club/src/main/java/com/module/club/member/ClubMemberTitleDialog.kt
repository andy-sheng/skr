package com.module.club.member

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.club.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EClubMemberRoleType

// 家族头衔授予 副族长 主持人 取消
// 取消头衔授予 取消头衔 取消
class ClubMemberTitleDialog(context: Context, model: UserInfoModel?, listener: Listener) : ConstraintLayout(context) {

    private val function1: ExTextView
    private val function2: ExTextView
    private val cancel: ExTextView

    private var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.club_member_title_dialog_layout, this)

        function1 = this.findViewById(R.id.function_1)
        function2 = this.findViewById(R.id.function_2)
        cancel = this.findViewById(R.id.cancel)

        cancel.setDebounceViewClickListener { mDialogPlus?.dismiss() }

        if (model?.clubInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value
                || model?.clubInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value
                || model?.clubInfo?.roleType == EClubMemberRoleType.ECMRT_Hostman.value) {
            // 有身份得先撤销身份
            function1.visibility = View.GONE
            function2.visibility = View.VISIBLE
            function2.text = "撤销头衔"
            function2.setDebounceViewClickListener { listener.onClickCommon() }
        } else {
            function1.visibility = View.VISIBLE
            function1.text = "设为副族长"
            function1.setDebounceViewClickListener { listener.onClickCoFounder() }
            function2.visibility = View.VISIBLE
            function2.text = "设为主持人"
            function2.setDebounceViewClickListener { listener.onClickHost() }
        }
    }

    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }


    interface Listener {
        fun onClickCoFounder()  // 设为副族长
        fun onClickHost()       // 设为主持人
        fun onClickCommon()     // 撤销头衔，即设未普通团员
    }
}
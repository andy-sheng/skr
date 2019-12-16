package com.module.club.member

import android.content.Context
import android.view.Gravity
import android.view.View
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.module.club.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EClubMemberRoleType

// 家族头衔授予 副族长 主持人 取消
// 取消头衔授予 取消头衔 取消
class ClubMemberTitleDialog(context: Context, model: UserInfoModel?) : ExConstraintLayout(context) {

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

        } else {

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
}
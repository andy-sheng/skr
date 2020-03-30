package com.common.core.policy

import android.app.Activity
import android.view.Gravity
import com.common.core.R
import com.common.utils.U
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

/**
 * 个人信息保护引导
 */
class PrivacyPolicyDialog(private val mActivity:Activity){


    private var mDialogPlus:DialogPlus? = null
    private val SP_KEY_SHOWED_PRIVACY_POLICY = "SHOWED_PRIVACY_POLICY"

    private fun createPrivacyPolicyDialog() {
        mDialogPlus?.dismiss()

        val privacyPolicyDialogView = PrivacyPolicyDialogView(mActivity)

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(ViewHolder(privacyPolicyDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_60)
                .setMargin(U.getDisplayUtils().dip2px(12f), -1, U.getDisplayUtils().dip2px(12f), U.getDisplayUtils().dip2px(10f))
                .setExpanded(false)
                .setCancelable(false)
                .create()

        privacyPolicyDialogView.setOkClikListener {
            U.getPreferenceUtils().setSettingBoolean(SP_KEY_SHOWED_PRIVACY_POLICY, true)
            dismiss()
        }
    }


    fun show(){
        if (U.getChannelUtils().channel.startsWith("YYB_SHOP") && !U.getPreferenceUtils().getSettingBoolean(SP_KEY_SHOWED_PRIVACY_POLICY, false)) {
            createPrivacyPolicyDialog()
            mDialogPlus?.show()
        }
    }

    fun dismiss(){
        mDialogPlus?.dismiss()
    }

    fun isShowing(): Boolean {
        return mDialogPlus?.isShowing == true
    }
}
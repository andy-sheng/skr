package com.component.dialog

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.level.utils.LevelConfigUtils
import com.component.person.view.PersonTagView
import com.facebook.drawee.view.SimpleDraweeView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

// 名片页，已经是第二次加进来了
class BusinessCardDialogView(context: Context, userInfoModel: UserInfoModel, meiLiCntTotal: Int, fansNum: Int) : ConstraintLayout(context) {

    private var mDialogPlus: DialogPlus? = null

    private val avatarIv: SimpleDraweeView
    private val levelBg: ImageView
    private val nameTv: ExTextView
    private val honorIv: ImageView
    private val verifyTv: TextView
    private val personTagView: PersonTagView
    private val divider: View
    private val signTv: ExTextView

    init {
        View.inflate(context, R.layout.business_card_dialog_view_layout, this)

        avatarIv = this.findViewById(R.id.avatar_iv)
        levelBg = this.findViewById(R.id.level_bg)
        nameTv = this.findViewById(R.id.name_tv)
        honorIv = this.findViewById(R.id.honor_iv)
        verifyTv = this.findViewById(R.id.verify_tv)
        personTagView = this.findViewById(R.id.person_tag_view)
        divider = this.findViewById(R.id.divider)
        signTv = this.findViewById(R.id.sign_tv)

        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(userInfoModel.avatar)
                .setCircle(true)
                .build())
        val ranking = userInfoModel.ranking
        if (ranking != null && LevelConfigUtils.getRaceCenterAvatarBg(ranking.mainRanking) != 0) {
            levelBg.visibility = View.VISIBLE
            levelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(ranking.mainRanking))
        } else {
            levelBg.visibility = View.GONE
        }
        nameTv.text = userInfoModel.nicknameRemark
        if (userInfoModel.honorInfo != null && userInfoModel.honorInfo?.isHonor() == true) {
            honorIv.visibility = View.VISIBLE
        } else {
            honorIv.visibility = View.GONE
        }
        if (userInfoModel.vipInfo != null && userInfoModel.vipInfo.vipType > 0) {
            // 加V 撕歌认证
            verifyTv.visibility = View.VISIBLE
            verifyTv.text = userInfoModel.vipInfo.vipDesc
        } else {
            verifyTv.visibility = View.GONE
        }
        personTagView.setUserID(userInfoModel.userId)
        personTagView.setSex(userInfoModel.sex)
        personTagView.setLocation(userInfoModel.location)
        personTagView.setCharmTotal(meiLiCntTotal)
        personTagView.setFansNum(fansNum)

        signTv.text = userInfoModel.signature
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
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
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
package com.component.dialog

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.view.PersonTagView
import com.facebook.drawee.view.SimpleDraweeView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import okhttp3.MediaType
import okhttp3.RequestBody

// 家族卡片页面
class ClubCardDialogView(val mContext: Context, val clubID: Int) : ConstraintLayout(mContext) {

    private var mDialogPlus: DialogPlus? = null

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val clubLogoSdv: SimpleDraweeView
    private val clubNameTv: ExTextView
    private val contentTv: TextView
    private val clubTagView: PersonTagView
    private val divider: View
    private val applyTv: ExTextView

    init {
        View.inflate(context, R.layout.club_card_dialog_view_layout, this)

        clubLogoSdv = this.findViewById(R.id.club_logo_sdv)
        clubNameTv = this.findViewById(R.id.club_name_tv)
        contentTv = this.findViewById(R.id.content_tv)
        clubTagView = this.findViewById(R.id.club_tag_view)
        divider = this.findViewById(R.id.divider)
        applyTv = this.findViewById(R.id.apply_tv)

        applyTv.setDebounceViewClickListener {
            applyJoinClub()
        }

        getClubMemberInfo()
    }


    private fun getClubMemberInfo() {
        ApiMethods.subscribe(userInfoServerApi.getClubMemberInfo(MyUserInfoManager.uid.toInt(), clubID), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val clubMemberInfo = JSON.parseObject(result.data.getString("info"), ClubMemberInfo::class.java)
                    showClubMemberInfo(clubMemberInfo)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        }, mContext as BaseActivity)
    }

    private fun applyJoinClub() {
        val map = mapOf(
                "clubID" to clubID,
                "text" to ""
        )
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(userInfoServerApi.applyJoinClub(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    U.getToastUtil().showShort("申请成功")
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        }, mContext as BaseActivity)
    }

    private fun showClubMemberInfo(clubMemberInfo: ClubMemberInfo?) {
        AvatarUtils.loadAvatarByUrl(clubLogoSdv, AvatarUtils.newParamsBuilder(clubMemberInfo?.club?.logo)
                .setCircle(false)
                .setCornerRadius(8.dp().toFloat())
                .build())
        clubNameTv.text = clubMemberInfo?.club?.name
        contentTv.text = clubMemberInfo?.club?.desc
        clubTagView.setClubID(clubID)
        clubTagView.setClubHot(clubMemberInfo?.club?.hot ?: 0)
        if (clubMemberInfo?.roleType != 0) {
            divider.visibility = View.GONE
            applyTv.visibility = View.GONE
        } else {
            divider.visibility = View.VISIBLE
            applyTv.visibility = View.VISIBLE
        }
    }

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
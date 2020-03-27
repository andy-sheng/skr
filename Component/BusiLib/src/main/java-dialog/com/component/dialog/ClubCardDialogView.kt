package com.component.dialog

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubInfo
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.R
import com.component.club.ClubMemberServerApi
import com.component.club.view.ClubMemberView
import com.component.person.utils.StringFromatUtils
import com.component.person.view.PhotoHorizView
import com.facebook.drawee.view.SimpleDraweeView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

// 家族卡片页面
class ClubCardDialogView(val mContext: Context, val clubInfo: ClubInfo?) : ConstraintLayout(mContext), CoroutineScope by MainScope() {

    private var mDialogPlus: DialogPlus? = null

    private val clubServerApi = ApiManager.getInstance().createService(ClubMemberServerApi::class.java)
    private var userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val topBgSdv: SimpleDraweeView
    private val clubLogoSdv: SimpleDraweeView
    private val clubHotTv: ExTextView
    private val clubNameTv: ExTextView
    private val contentTv: TextView
    private val memberView: ClubMemberView
    private val photoHorizView: PhotoHorizView
    private val applyTv: ExTextView

    private var hasApplied = false

    init {
        View.inflate(context, R.layout.club_card_dialog_view_layout, this)

        topBgSdv = this.findViewById(R.id.top_bg_sdv)
        clubLogoSdv = this.findViewById(R.id.club_logo_sdv)
        clubHotTv = this.findViewById(R.id.club_hot_tv)
        clubNameTv = this.findViewById(R.id.club_name_tv)
        contentTv = this.findViewById(R.id.content_tv)
        memberView = this.findViewById(R.id.member_view)
        photoHorizView = this.findViewById(R.id.photo_horiz_view)
        applyTv = this.findViewById(R.id.apply_tv)

        applyTv.setDebounceViewClickListener {
            if (hasApplied) {
                cancelJoinApply()
            } else {
                applyJoinClub()
            }
        }

        getClubMemberInfo()
        memberView.clubID = clubInfo?.clubID ?: 0
        memberView.memberCnt = clubInfo?.memberCnt ?: 0
        memberView.loadData { }
        photoHorizView.clubID = clubInfo?.clubID ?: 0
        photoHorizView.getPhotos(0)
    }


    private fun hasAppliedJoin() {
        launch {
            val result = subscribe(RequestControl("hasAppliedJoin", ControlType.CancelThis)) {
                clubServerApi.hasAppliedJoin(clubInfo?.clubID ?: 0)
            }
            if (result.errno == 0) {
                hasApplied = result.data.getBooleanValue("yes")
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun cancelJoinApply() {
        launch {
            val map = mapOf(
                    "clubID" to clubInfo?.clubID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("cancelJoinApply", ControlType.CancelThis)) {
                clubServerApi.cancelApplyJoin(body)
            }
            if (result.errno == 0) {
                hasApplied = false
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun applyJoinClub() {
        launch {
            val map = mapOf(
                    "clubID" to clubInfo?.clubID,
                    "text" to ""
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("applyJoinClub", ControlType.CancelThis)) {
                clubServerApi.applyJoinClub(body)
            }
            if (result.errno == 0) {
                hasApplied = true
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun refreshApplyStatus() {
        applyTv.visibility = View.VISIBLE
        if (hasApplied) {
            applyTv.text = "取消申请"
            applyTv.background = DrawableCreator.Builder()
                    .setSolidColor(U.getColor(R.color.black_trans_10))
                    .setStrokeColor(Color.WHITE)
                    .setStrokeWidth(1.dp().toFloat())
                    .setCornersRadius(21.dp().toFloat())
                    .build()
        } else {
            applyTv.text = "申请加入"
            applyTv.background = DrawableCreator.Builder()
                    .setSolidColor(Color.parseColor("#FF9B9B"))
                    .setCornersRadius(21.dp().toFloat())
                    .build()
        }
    }

    private fun getClubMemberInfo() {
        ApiMethods.subscribe(userInfoServerApi.getClubMemberInfo(MyUserInfoManager.uid.toInt(), clubInfo?.clubID
                ?: 0), object : ApiObserver<ApiResult>() {
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

    private fun showClubMemberInfo(clubMemberInfo: ClubMemberInfo?) {
        FrescoWorker.loadImage(topBgSdv, ImageFactory.newPathImage(clubMemberInfo?.club?.logo)
                .setCornerRadii(floatArrayOf(16.dp().toFloat(), 16.dp().toFloat(), 16.dp().toFloat(), 16.dp().toFloat(), 0f, 0f, 0f, 0f))
                .build())
        AvatarUtils.loadAvatarByUrl(clubLogoSdv, AvatarUtils.newParamsBuilder(clubMemberInfo?.club?.logo)
                .setCircle(false)
                .setCornerRadius(8.dp().toFloat())
                .build())
        clubNameTv.text = clubMemberInfo?.club?.name
        contentTv.text = clubMemberInfo?.club?.desc
        clubHotTv.text = StringFromatUtils.formatTenThousand( clubMemberInfo?.club?.hot ?: 0)
        if (clubMemberInfo?.isMyClub() == true) {
            photoHorizView.setIsMyClub(true)
            applyTv.visibility = View.GONE
        } else {
            photoHorizView.setIsMyClub(false)
            applyTv.visibility = View.VISIBLE
            hasAppliedJoin()
        }
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setMargin(16.dp(), -1, 16.dp(), 16.dp())
                .setExpanded(false)
                .setCancelable(true)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        cancel()
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        cancel()
        mDialogPlus?.dismiss(isAnimation)
    }
}
package com.module.club.manage.setting

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.homepage.ClubHomepageActivity2
import com.zq.live.proto.Common.EClubMemberRoleType
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

class ClubManageActivity : BaseActivity() {

    var mainActContainer: ConstraintLayout?=null
    var titlebar:CommonTitleBar?=null
    var bgIv: ExImageView?=null
    var clubInfoSettingTv:ExTextView?=null
    var clubInfoSettingLine:View?=null
    var clubNoticeTv:ExTextView?=null
    var clubNoticeLine:View?=null
    var clubTransferTv:ExTextView?=null
    var clubTransferLine:View?=null
    var clubDissolveTv:ExTextView?=null
    var clubDissolveLine:View?=null
    var clubExitTv:ExTextView?=null
    var clubExitLine:View?=null


    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)

    private var clubMemberInfo: ClubMemberInfo? = null

    private var mTipsDialogView: TipsDialogView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_manage_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        clubMemberInfo = intent.getSerializableExtra("clubMemberInfo") as ClubMemberInfo?
        if (clubMemberInfo == null) {
            finish()
        }

        mainActContainer = this.findViewById(R.id.main_act_container)
        titlebar = this.findViewById(R.id.titlebar)
        bgIv = this.findViewById(R.id.bg_iv)
        clubInfoSettingTv = this.findViewById(R.id.club_info_setting_tv)
        clubInfoSettingLine = this.findViewById(R.id.club_info_setting_line)
        clubNoticeTv = this.findViewById(R.id.club_notice_tv)
        clubNoticeLine = this.findViewById(R.id.club_notice_line)
        clubTransferTv = this.findViewById(R.id.club_transfer_tv)
        clubTransferLine = this.findViewById(R.id.club_transfer_line)
        clubDissolveTv = this.findViewById(R.id.club_dissolve_tv)
        clubDissolveLine = this.findViewById(R.id.club_dissolve_line)
        clubExitTv = this.findViewById(R.id.club_exit_tv)
        clubExitLine = this.findViewById(R.id.club_exit_line)


        when {
            clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value -> {
                clubInfoSettingTv?.visibility = View.VISIBLE
                clubInfoSettingLine?.visibility = View.VISIBLE
                clubNoticeTv?.visibility = View.VISIBLE
                clubNoticeLine?.visibility = View.VISIBLE
                clubDissolveTv?.visibility = View.VISIBLE
                clubDissolveLine?.visibility = View.VISIBLE
                clubTransferTv?.visibility = View.VISIBLE
                clubTransferLine?.visibility = View.VISIBLE
                clubExitTv?.visibility = View.GONE
                clubExitLine?.visibility = View.GONE
            }
            clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value -> {
                clubInfoSettingTv?.visibility = View.VISIBLE
                clubInfoSettingLine?.visibility = View.VISIBLE
                clubNoticeTv?.visibility = View.VISIBLE
                clubNoticeLine?.visibility = View.VISIBLE
                clubDissolveTv?.visibility = View.GONE
                clubDissolveLine?.visibility = View.VISIBLE
                clubTransferTv?.visibility = View.GONE
                clubTransferLine?.visibility = View.VISIBLE
                clubExitTv?.visibility = View.VISIBLE
                clubExitLine?.visibility = View.VISIBLE
            }
            else -> {
                clubInfoSettingTv?.visibility = View.GONE
                clubInfoSettingLine?.visibility = View.GONE
                clubNoticeTv?.visibility = View.GONE
                clubNoticeLine?.visibility = View.GONE
                clubDissolveTv?.visibility = View.GONE
                clubDissolveLine?.visibility = View.GONE
                clubTransferTv?.visibility = View.GONE
                clubTransferLine?.visibility = View.GONE
                clubExitTv?.visibility = View.VISIBLE
                clubExitLine?.visibility = View.VISIBLE
            }
        }

        titlebar?.leftTextView?.setDebounceViewClickListener {
            finish()
        }

        clubInfoSettingTv?.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_CLUB)
                    .withString("from", "change")
                    .withSerializable("clubMemberInfo", clubMemberInfo)
                    .navigation()
        }

        clubNoticeTv?.setDebounceViewClickListener {
            // 跳到公告设置
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, ClubNoticeSettingFragment::class.java)
                    .addDataBeforeAdd(1, clubMemberInfo)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .build())
        }

        clubDissolveTv?.setDebounceViewClickListener {
            mTipsDialogView?.dismiss(false)
            mTipsDialogView = TipsDialogView.Builder(this)
                    .setMessageTip("确定解散家族么")
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        mTipsDialogView?.dismiss()
                        dissolveClub()
                    }
                    .setCancelBtnClickListener {
                        mTipsDialogView?.dismiss()
                    }
                    .build()
            mTipsDialogView?.showByDialog()
        }

        clubExitTv?.setDebounceViewClickListener {
            // 退出家族
            mTipsDialogView?.dismiss(false)
            mTipsDialogView = TipsDialogView.Builder(this)
                    .setMessageTip("确定退出家族么")
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        mTipsDialogView?.dismiss()
                        existClub()
                    }
                    .setCancelBtnClickListener {
                        mTipsDialogView?.dismiss()
                    }
                    .build()
            mTipsDialogView?.showByDialog()
        }

        clubTransferTv?.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_MEMBER)
                    .withSerializable("clubMemberInfo", clubMemberInfo)
                    .withInt("clubMemberType", 2)
                    .navigation()
        }

    }

    private fun dissolveClub() {
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("dissolveClub", ControlType.CancelThis)) {
                clubServerApi.dismissClub(body)
            }
            if (result.errno == 0) {
                // 解散成功
                U.getToastUtil().showShort("家族解散成功")
                for (activity in U.getActivityUtils().activityList) {
                    if (activity is ClubHomepageActivity2) {
                        activity.finish()
                    }
                }
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun existClub() {
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("existClub", ControlType.CancelThis)) {
                clubServerApi.existClub(body)
            }
            if (result.errno == 0) {
                // 退出成功
                U.getToastUtil().showShort("家族退出成功")
                MyUserInfoManager.myUserInfo?.clubInfo = null
                EventBus.getDefault().post(MyUserInfoEvent.UserInfoChangeEvent())
                for (activity in U.getActivityUtils().activityList) {
                    if (activity is ClubHomepageActivity2) {
                        activity.finish()
                    }
                }
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        mTipsDialogView?.dismiss(false)
    }
}
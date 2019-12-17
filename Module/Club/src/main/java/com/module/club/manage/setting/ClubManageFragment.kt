package com.module.club.manage.setting

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.zq.live.proto.Common.ClubInfo
import com.zq.live.proto.Common.EClubMemberRoleType
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class ClubManageFragment : BaseFragment() {

    private var titlebar: CommonTitleBar? = null

    private var clubInfoSettingTv: ExTextView? = null
    private var clubNoticeTv: ExTextView? = null
    private var clubTransferTv: ExTextView? = null
    private var clubDissolveTv: ExTextView? = null
    private var clubExitTv: ExTextView? = null

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)

    private var clubMemberInfo: ClubMemberInfo? = null

    override fun initView(): Int {
        return R.layout.club_manage_fragment_layout
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 1) {
            clubMemberInfo = data as ClubMemberInfo?
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (clubMemberInfo == null) {
            finish()
        }

        titlebar = rootView.findViewById(R.id.titlebar)
        clubInfoSettingTv = rootView.findViewById(R.id.club_info_setting_tv)
        clubNoticeTv = rootView.findViewById(R.id.club_notice_tv)
        clubTransferTv = rootView.findViewById(R.id.club_transfer_tv)
        clubDissolveTv = rootView.findViewById(R.id.club_dissolve_tv)
        clubExitTv = rootView.findViewById(R.id.club_exit_tv)

        clubTransferTv?.visibility = View.GONE
        when {
            clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value -> {
                clubInfoSettingTv?.visibility = View.VISIBLE
                clubNoticeTv?.visibility = View.VISIBLE
                clubDissolveTv?.visibility = View.VISIBLE
                clubExitTv?.visibility = View.GONE
            }
            clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value -> {
                clubInfoSettingTv?.visibility = View.VISIBLE
                clubNoticeTv?.visibility = View.VISIBLE
                clubDissolveTv?.visibility = View.GONE
                clubExitTv?.visibility = View.VISIBLE
            }
            else -> {
                clubInfoSettingTv?.visibility = View.GONE
                clubNoticeTv?.visibility = View.GONE
                clubDissolveTv?.visibility = View.GONE
                clubExitTv?.visibility = View.VISIBLE
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
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, ClubNoticeSettingFragment::class.java)
                    .addDataBeforeAdd(1, clubMemberInfo)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .build())
        }

        clubDissolveTv?.setDebounceViewClickListener {
            dissolveClub()
        }

        clubExitTv?.setDebounceViewClickListener {
            // 退出家族
            existClub()
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
                activity?.finish()
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
                activity?.finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
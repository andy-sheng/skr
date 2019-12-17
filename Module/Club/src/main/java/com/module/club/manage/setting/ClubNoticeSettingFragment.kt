package com.module.club.manage.setting

import android.os.Bundle
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.homepage.event.ClubInfoChangeEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

// 家族公告
class ClubNoticeSettingFragment : BaseFragment() {

    lateinit var titlebar: CommonTitleBar
    lateinit var editText: NoLeakEditText
    lateinit var saveTv: ExTextView

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var clubMemberInfo: ClubMemberInfo? = null

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 1) {
            clubMemberInfo = data as ClubMemberInfo?
        }
    }

    override fun initView(): Int {
        return R.layout.club_manage_edit_notice_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (clubMemberInfo == null) {
            finish()
        }

        titlebar = rootView.findViewById(R.id.titlebar)
        editText = rootView.findViewById(R.id.edit_text)
        saveTv = rootView.findViewById(R.id.save_tv)

        editText.setText(clubMemberInfo?.club?.notice)
        editText.setSelection(clubMemberInfo?.club?.notice?.length ?: 0)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        saveTv.setDebounceViewClickListener {
            setNotice(editText.text.toString().trim())
        }
    }

    private fun setNotice(notice: String) {
        if (TextUtils.isEmpty(notice)) {
            U.getToastUtil().showShort("公告不能为空")
            return
        }

        launch {
            val map = mutableMapOf(
                    "notice" to notice,
                    "clubID" to (clubMemberInfo?.club?.clubID ?: 0)
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("setNotice", ControlType.CancelThis)) {
                clubServerApi.editClubInfo(body)
            }

            if (result.errno == 0) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
                U.getToastUtil().showShort("公告设置成功")
                EventBus.getDefault().post(ClubInfoChangeEvent())
                U.getFragmentUtils().popFragment(this@ClubNoticeSettingFragment)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
package com.module.club.manage

import android.os.Bundle
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.club.R

class ClubManageActivity : BaseActivity() {
    private var titlebar: CommonTitleBar? = null

    private var clubInfoSettingTv: ExTextView? = null
    private var clubNoticeTv: ExTextView? = null
    private var clubTransferTv: ExTextView? = null
    private var clubDissolveTv: ExTextView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_manage_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {


        titlebar = findViewById(R.id.titlebar)

        clubInfoSettingTv = findViewById(R.id.club_info_setting_tv)
        clubNoticeTv = findViewById(R.id.club_notice_tv)
        clubTransferTv = findViewById(R.id.club_transfer_tv)
        clubDissolveTv = findViewById(R.id.club_dissolve_tv)

        titlebar?.leftTextView?.setDebounceViewClickListener {
            finish()
        }

        clubInfoSettingTv?.setDebounceViewClickListener {
            // todo 编辑家族资料
        }

        clubInfoSettingTv?.setDebounceViewClickListener {
            // todo 转让家族
        }

        clubDissolveTv?.setDebounceViewClickListener {
            // todo 解散家族
        }

    }
}
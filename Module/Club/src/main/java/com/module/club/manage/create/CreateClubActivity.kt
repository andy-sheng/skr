package com.module.club.manage.create

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExImageView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.club.R

@Route(path = RouterConstants.ACTIVITY_CREATE_CLUB)
class CreateClubActivity : BaseActivity() {
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ImageView
    lateinit var iconIvBg: ExImageView
    lateinit var iconIv: BaseImageView
    lateinit var divider: View
    lateinit var clubNameEt: NoLeakEditText
    lateinit var clubIntroductionEt: NoLeakEditText
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_create_ss
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        bgIv = findViewById(R.id.bg_iv)
        iconIvBg = findViewById(R.id.icon_iv_bg)
        iconIv = findViewById(R.id.icon_iv)
        divider = findViewById(R.id.divider)
        clubNameEt = findViewById(R.id.club_name_et)
        clubIntroductionEt = findViewById(R.id.club_introduction_et)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        titlebar.rightTextView.setDebounceViewClickListener {
            editFinish()
        }


    }

    private fun editFinish() {
        finish()
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
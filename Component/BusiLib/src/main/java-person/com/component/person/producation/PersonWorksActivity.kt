package com.component.person.producation

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.component.busilib.R
import com.component.person.producation.view.ProducationWallView
import com.module.RouterConstants

// 本地作品
@Route(path = RouterConstants.ACTIVITY_PERSON_WORKS)
class PersonWorksActivity : BaseActivity() {

    lateinit var backIv: ImageView
    lateinit var divider: View
    lateinit var content: RelativeLayout

    var userInfoModel: UserInfoModel? = null
    var watchView: ProducationWallView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_works_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        userInfoModel = intent.getSerializableExtra("userInfoModel") as UserInfoModel?
        if (userInfoModel == null) {
            MyLog.w(TAG, "PersonWorksActivity userInfoModel = null")
            finish()
            return
        }

        backIv = findViewById(R.id.back_iv)
        divider = findViewById(R.id.divider)

        backIv.setDebounceViewClickListener {
            finish()
        }

        content = findViewById(R.id.content)
        if (watchView == null) {
            watchView = ProducationWallView(this@PersonWorksActivity, userInfoModel!!, null)
        }
        watchView?.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        content.addView(watchView)
    }

    override fun destroy() {
        super.destroy()
        watchView?.destory()
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
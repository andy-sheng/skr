package com.moudule.playways.beauty


import android.content.Intent
import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.beauty.FROM_MATCH
import com.engine.Params
import com.module.RouterConstants
import com.module.playways.R
import com.moudule.playways.beauty.fragment.BeautyPreviewFragment
import com.component.mediaengine.kit.ZqEngineKit

@Route(path = RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
class BeautyPreviewActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val bundle = intent.extras
        var mFrom = bundle.getInt("mFrom")
        if (mFrom == FROM_FRIEND_RECOMMEND || mFrom == FROM_MATCH) {
            for (activity in U.getActivityUtils().activityList) {
                if (activity === this) {
                    continue
                }
                if (U.getActivityUtils().isHomeActivity(activity)) {
                    continue
                }
                activity.finish()
            }
        }
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, BeautyPreviewFragment::class.java)
                .setBundle(bundle)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build())
        hasCreate = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var fragment = U.getFragmentUtils().findFragment(this,BeautyPreviewFragment::class.java)
        fragment?.arguments = intent?.extras
    }

    override fun finish() {
        super.finish()
        hasCreate = false
        val config = ZqEngineKit.getInstance().params
        if (config != null) {
            Params.save2Pref(config)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    companion object {
        @JvmField
        var hasCreate: Boolean = false
    }
}

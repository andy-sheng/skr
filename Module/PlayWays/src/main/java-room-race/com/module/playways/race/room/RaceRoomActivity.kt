package com.module.playways.race.room

import android.os.Bundle
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.room.ui.RaceRoomFragment

@Route(path = RouterConstants.ACTIVITY_RACE_ROOM)
class RaceRoomActivity : BaseActivity() {

    /**
     * 存起该房间一些状态信息
     */
    internal var mRoomData = RaceRoomData()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.race_room_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val joinRaceRoomRspModel = intent.getSerializableExtra("JoinRaceRoomRspModel") as JoinRaceRoomRspModel?
        joinRaceRoomRspModel?.let {
            mRoomData.loadFromRsp(it)
        }
        go()
        U.getStatusBarUtil().setTransparentBar(this, false)
    }

    internal fun go() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, RaceRoomFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, mRoomData)
                        .build())
        // 销毁其他的除一唱到底页面所有界面
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

    override fun useEventBus(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun destroy() {
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        super.destroy()
    }

    override fun finish() {
        super.finish()
        MyLog.w(TAG, "finish")
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}

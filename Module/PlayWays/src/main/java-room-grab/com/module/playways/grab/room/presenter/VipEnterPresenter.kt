package com.module.playways.grab.room.presenter

import com.common.anim.ObjectPlayControlTemplate
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.UserLevelType.SKRER_LEVEL_PLATINUM
import com.common.core.userinfo.model.UserLevelType.SKRER_LEVEL_POTENTIAL
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.utils.U
import com.module.playways.BaseRoomData
import com.module.playways.grab.room.inter.IGrabVipView

class VipEnterPresenter(val view: IGrabVipView, roomData: BaseRoomData<*>) : RxLifeCyclePresenter() {
    val mTag = "VipEnterPresenter"
    var canAccept = true

    internal val mVipEnterObjectPlayControlTemplate: ObjectPlayControlTemplate<UserInfoModel, VipEnterPresenter> = object : ObjectPlayControlTemplate<UserInfoModel, VipEnterPresenter>() {
        override fun accept(cur: UserInfoModel): VipEnterPresenter? {
            if (!U.getActivityUtils().isAppForeground) {
                MyLog.d(TAG, "在后台，不弹出通知")
                return null
            }

            if (!canAccept) {
                MyLog.d(TAG, "不可接受")
                return null
            }
            return this@VipEnterPresenter
        }

        override fun onStart(playerInfoModel: UserInfoModel, floatWindow: VipEnterPresenter) {
            MyLog.d(mTag, "onStart playerInfoModel = $playerInfoModel, floatWindow = $floatWindow")
            canAccept = false
            if (roomData.inPlayerOrWaiterInfoList(playerInfoModel.userId)) {
                view.startEnterAnimation(playerInfoModel) {
                    canAccept = true
                    endCurrent(playerInfoModel)
                }
            } else {
                canAccept = true
                endCurrent(playerInfoModel)
            }
        }

        override fun onEnd(floatWindowData: UserInfoModel?) {

        }
    }

    fun switchRoom() {
        mVipEnterObjectPlayControlTemplate.reset()
    }

    fun addNotice(playerInfoModel: UserInfoModel) {
        if (playerInfoModel.ranking.mainRanking >= SKRER_LEVEL_PLATINUM) {
            mVipEnterObjectPlayControlTemplate.add(playerInfoModel, true)
        }
    }

    override fun destroy() {
        super.destroy()
        mVipEnterObjectPlayControlTemplate.destroy()
    }
}
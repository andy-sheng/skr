package com.module.playways.doubleplay.fragment

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent
import com.common.view.ex.ExImageView
import com.module.RouterConstants
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IMatchView
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.module.playways.doubleplay.presenter.DoubleMatchPresenter


class DoubleGameMatchFragment : BaseFragment(), IMatchView {
    val mTag = "DoubleGameMatchFragment"
    lateinit var mExImageView: ExImageView
    lateinit var doubleMatchPresenter: DoubleMatchPresenter

    override fun initView(): Int {
        return com.module.playways.R.layout.double_game_match_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mExImageView = mRootView.findViewById<View>(com.module.playways.R.id.exImageView) as ExImageView

        doubleMatchPresenter = DoubleMatchPresenter(this)
        addPresent(doubleMatchPresenter)
        doubleMatchPresenter.startMatch()
    }

    override fun matchSuccessFromHttp(doubleRoomData: DoubleRoomData) {
        MyLog.d(mTag, "matchSuccessFromHttp")

        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation()
        activity?.finish()
    }

    override fun matchSuccessFromPush(doubleStartCombineRoomByMatchPushEvent: DoubleStartCombineRoomByMatchPushEvent) {
        MyLog.d(mTag, "matchSuccessFromPush")
        val doubleRoomData = DoubleRoomData()
        doubleRoomData.gameId = doubleStartCombineRoomByMatchPushEvent.roomID
        doubleRoomData.enableNoLimitDuration = true
        doubleRoomData.passedTimeMs = doubleStartCombineRoomByMatchPushEvent.passedTimeMs
        doubleRoomData.config = doubleStartCombineRoomByMatchPushEvent.config

        val hashMap = HashMap<Int, UserInfoModel>()
        for (userInfoModel in doubleStartCombineRoomByMatchPushEvent.users) {
            hashMap.put(userInfoModel.userId, userInfoModel)
        }
        doubleRoomData.userInfoListMap = hashMap

        val list = ArrayList<LocalAgoraTokenInfo>();
        for (value in doubleStartCombineRoomByMatchPushEvent.tokens) {
            list.add(LocalAgoraTokenInfo(value.key, value.value))
        }
        doubleRoomData.tokens = list
        doubleRoomData.needMaskUserInfo = doubleStartCombineRoomByMatchPushEvent.isNeedMaskUserInfo

        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation()

        activity?.finish()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun toDoubleRoomByPush() {
        activity?.finish()
    }
}
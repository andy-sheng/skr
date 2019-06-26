package com.module.playways.doubleplay.fragment

import android.os.Bundle
import android.support.constraint.Group
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IMatchView
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.module.playways.doubleplay.presenter.DoubleMatchPresenter


class DoubleGameMatchFragment : BaseFragment(), IMatchView {
    val mTag = "DoubleGameMatchFragment"
    lateinit var mCalcelMatchTv: ExTextView
    lateinit var doubleMatchPresenter: DoubleMatchPresenter
    lateinit var mSelectArea: Group
    lateinit var mSelectSexTipTv: ExTextView
    lateinit var mManTv: ExTextView
    lateinit var mWomanTv: ExTextView


    override fun initView(): Int {
        return com.module.playways.R.layout.double_game_match_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCalcelMatchTv = mRootView.findViewById<View>(com.module.playways.R.id.calcel_match_tv) as ExTextView
        mSelectArea = mRootView.findViewById<View>(com.module.playways.R.id.select_area) as Group
        mSelectSexTipTv = mRootView.findViewById<View>(com.module.playways.R.id.select_sex_tip_tv) as ExTextView
        mManTv = mRootView.findViewById<View>(com.module.playways.R.id.man_tv) as ExTextView
        mWomanTv = mRootView.findViewById<View>(com.module.playways.R.id.woman_tv) as ExTextView

        mCalcelMatchTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                activity?.finish()
            }
        })

        mManTv.setOnClickListener {
            doubleMatchPresenter.startMatch(true)
            mSelectArea.visibility = View.GONE
            mCalcelMatchTv.visibility = View.VISIBLE
        }

        mWomanTv.setOnClickListener {
            doubleMatchPresenter.startMatch(false)
            mSelectArea.visibility = View.GONE
            mCalcelMatchTv.visibility = View.VISIBLE
        }

        doubleMatchPresenter = DoubleMatchPresenter(this)
        addPresent(doubleMatchPresenter)
    }

//    private fun exitLogin() {
//        val tipsDialogView = TipsDialogView.Builder(context)
//                .setMessageTip("确定取消匹配吗？")
//                .setConfirmTip("确定")
//                .setCancelTip("取消")
//                .setConfirmBtnClickListener(object : AnimateClickListener() {
//                    override fun click(view: View) {
//                        mDialogPlus?.dismiss()
//                        activity?.finish()
//                    }
//                })
//                .setCancelBtnClickListener(object : AnimateClickListener() {
//                    override fun click(view: View) {
//                        mDialogPlus?.dismiss()
//                    }
//                })
//                .build()
//
//        mDialogPlus = DialogPlus.newDialog(context!!)
//                .setContentHolder(ViewHolder(tipsDialogView))
//                .setGravity(Gravity.BOTTOM)
//                .setContentBackgroundResource(R.color.transparent)
//                .setOverlayBackgroundResource(R.color.black_trans_80)
//                .setExpanded(false)
//                .create()
//        mDialogPlus?.show()
//    }

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
        doubleRoomData.passedTimeMs = doubleStartCombineRoomByMatchPushEvent.passedTimeMs
        doubleRoomData.config = doubleStartCombineRoomByMatchPushEvent.config
        doubleRoomData.enableNoLimitDuration = doubleRoomData.config?.durationTimeMs == -1

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
        doubleRoomData.doubleRoomOri = DoubleRoomData.DoubleRoomOri.MATCH

        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation()

        activity?.finish()
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun toDoubleRoomByPush() {
        activity?.finish()
    }

    override fun destroy() {
        super.destroy()
    }
}
package com.module.playways.doubleplay.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.manager.BgMusicManager
import com.module.RouterConstants
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IMatchView
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.module.playways.doubleplay.pbLocalModel.LocalEnterRoomModel
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel
import com.module.playways.doubleplay.presenter.DoubleMatchPresenter


class DoubleGameMatchFragment : BaseFragment(), IMatchView {
    val mTag = "DoubleGameMatchFragment"
    lateinit var mCalcelMatchTv: ExTextView
    lateinit var doubleMatchPresenter: DoubleMatchPresenter

    //是否想找男的
    var mIsFindMale: Boolean? = null
    //自己是否是男的
    var mMeIsMale: Boolean? = null

    override fun initView(): Int {
        return com.module.playways.R.layout.double_game_match_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCalcelMatchTv = rootView.findViewById<View>(com.module.playways.R.id.calcel_match_tv) as ExTextView

        mCalcelMatchTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                activity?.finish()
            }
        })

        doubleMatchPresenter = DoubleMatchPresenter(this)
        addPresent(doubleMatchPresenter)
        doubleMatchPresenter.getBgMusic()

        doubleMatchPresenter.startMatch(mMeIsMale ?: true, mIsFindMale ?: true)
    }

    override fun playBgMusic(musicUrl: String) {
        if (!TextUtils.isEmpty(musicUrl)) {
            BgMusicManager.getInstance().starPlay(musicUrl, 0, "DoubleGameMatchFragment")
        }
    }

    override fun matchSuccessFromHttp(doubleRoomData: DoubleRoomData) {
        MyLog.d(mTag, "matchSuccessFromHttp")
        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation()
        activity?.finish()
        StatisticsAdapter.recordCountEvent("cp", "pairing_success", null)
    }

    override fun finishActivity() {
        activity?.finish()
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            val bundle = data as Bundle
            mIsFindMale = bundle.getBoolean("is_find_male")
            mMeIsMale = bundle.getBoolean("is_me_male")
        }
    }

    override fun matchSuccessFromPush(localEnterRoomModel: LocalEnterRoomModel) {
        MyLog.d(mTag, "matchSuccessFromPush")
        val doubleRoomData = DoubleRoomData()
        doubleRoomData.gameId = localEnterRoomModel.roomID
        doubleRoomData.passedTimeMs = localEnterRoomModel.passedTimeMs
        doubleRoomData.config = localEnterRoomModel.config
        doubleRoomData.enableNoLimitDuration = doubleRoomData.config?.durationTimeMs == -1
        doubleRoomData.localGamePanelInfo = localEnterRoomModel.gamePanelInfo
        doubleRoomData.sceneType = localEnterRoomModel.currentSceneType
        doubleRoomData.gameSenceDataModel = LocalGameSenceDataModel(localEnterRoomModel.gamePanelInfo.panelSeq)

        val hashMap = HashMap<Int, UserInfoModel>()
        for (userInfoModel in localEnterRoomModel.users) {
            hashMap.put(userInfoModel.userId, userInfoModel)
        }
        doubleRoomData.userInfoListMap = hashMap

        val list = ArrayList<LocalAgoraTokenInfo>();
        for (value in localEnterRoomModel.tokens) {
            list.add(LocalAgoraTokenInfo(value.key, value.value))
        }
        doubleRoomData.tokens = list
        doubleRoomData.needMaskUserInfo = localEnterRoomModel.isNeedMaskUserInfo
        doubleRoomData.doubleRoomOri = DoubleRoomData.DoubleRoomOri.MATCH

        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation()

        activity?.finish()
        StatisticsAdapter.recordCountEvent("cp", "pairing_success", null)
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
        BgMusicManager.getInstance().destory()
    }
}
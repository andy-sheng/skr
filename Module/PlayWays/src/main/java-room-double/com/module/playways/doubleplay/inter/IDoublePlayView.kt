package com.module.playways.doubleplay.inter

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent

interface IDoublePlayView {
    /**
     * @param pre  之前的，如果刚开始是null
     * @param mCur 当前的，是现在的轮次信息
     */
    fun changeRound(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean)

    fun picked(count: Int)

    fun gameEnd(doubleEndCombineRoomPushEvent: DoubleEndCombineRoomPushEvent)

    fun showLockState(userID: Int, lockState: Boolean)

    fun showNoLimitDurationState(noLimit: Boolean)

    fun startSing(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean)

    fun finishActivityWithError()

    fun updateNextSongDec(mNext: String, hasNext: Boolean)

    fun finishActivity()

    fun unLockSelfSuccess()

    fun noMusic()

    fun joinAgora()

    fun askSceneChange(sceneType: Int, str: String)

    fun updateGameSenceData(localGameSenceDataModel: LocalGameSenceDataModel)

    fun showGameSceneGamePanel(localGamePanelInfo: LocalGamePanelInfo)

    fun showGameSceneGameCard(localGameItemInfo: LocalGameItemInfo)

    fun updateGameSceneSelectState(userInfoModel: UserInfoModel, panelSeq: Int, itemID: Int)

    fun updateGameScene(sceneType: Int)
}

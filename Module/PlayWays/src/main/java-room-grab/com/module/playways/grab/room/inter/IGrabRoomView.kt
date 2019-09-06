package com.module.playways.grab.room.inter

import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.room.prepare.model.OnlineInfoModel
import com.module.playways.room.song.model.SongModel

interface IGrabRoomView {
    /**
     * 抢唱阶段开始
     * 展示要唱的歌儿,《下一首》《东西》
     *
     * @param seq       当前轮次的序号
     * @param songModel 要唱的歌信息
     */
    fun grabBegin(seq: Int, songModel: SongModel)

    /**
     * 自己抢到了
     * 演唱阶段开始
     */
    fun singBySelf()

    /**
     * 别人抢到了
     * 演唱阶段开始
     */
    fun singByOthers()

    /**
     * 轮次结束
     */
    fun roundOver(lastRoundInfo: GrabRoundInfoModel?, playNextSongInfoCard: Boolean, now: GrabRoundInfoModel?)

    fun updateUserState(jsonOnLineInfoList: List<OnlineInfoModel>)

    /**
     * 中途跑了
     */
    fun exitInRound()


    fun gameFinish()


    fun onGetGameResult(success: Boolean)

    fun onChangeRoomResult(success: Boolean, errorMsg: String?)

    fun giveUpSuccess(seq: Int)

    fun updateScrollBarProgress(score: Int, songLineNum: Int)

    fun showKickVoteDialog(kickUserID: Int, sourceUserID: Int)

    /**
     * 是否被房主踢出去
     *
     * @param isOwner
     */
    fun kickBySomeOne(isOwner: Boolean)

    /**
     * 把别人踢出去
     */
    fun dimissKickDialog()

    fun showPracticeFlag(flag: Boolean)

    fun hideInviteTipView()

    fun hideManageTipView()

    //切换房间了，把所有的板子Gone掉，重新显示
    //void hideAllCardView();

    //开始实名认证
    fun beginOuath()

    fun changeRoomMode(isVideo: Boolean)
}

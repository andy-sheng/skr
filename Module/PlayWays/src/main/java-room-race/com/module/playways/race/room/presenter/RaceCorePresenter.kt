package com.module.playways.race.room.presenter

import android.support.annotation.CallSuper
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.event.RaceRoundStatusChangeEvent
import com.module.playways.race.room.inter.IRaceRoomView
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.model.parseFromRoundInfoPB
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.event.raceroom.RBLightEvent
import com.module.playways.room.msg.event.raceroom.RExitGameEvent
import com.module.playways.room.msg.event.raceroom.RJoinNoticeEvent
import com.module.playways.room.msg.event.raceroom.RWantSingChanceEvent
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.RaceRoom.RGetSingChanceMsg
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceCorePresenter(var mRoomData: RaceRoomData, var mIRaceRoomView: IRaceRoomView) : RxLifeCyclePresenter() {

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        val commentSysModel = CommentSysModel("欢迎来到Race房间", CommentSysModel.TYPE_ENTER_ROOM)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
    }

    fun onOpeningAnimationOver() {
        mRoomData.checkRoundInEachMode()
    }

    /**
     * 选择歌曲
     */
    fun wantSingChance(choiceID: Int, roomID: Int, roundSeq: Int) {
        launch {
            val map = mutableMapOf(
                    "choiceID" to choiceID,
                    "roomID" to roomID,
                    "roundSeq" to roundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.queryMatch(body) }
            if (result.errno == 0) {
                if (roundSeq == mRoomData?.realRoundSeq) {
                    mRoomData?.realRoundInfo?.addWantSingChange(choiceID, MyUserInfoManager.getInstance().uid.toInt())
                }
            } else {

            }
        }
    }

    /**
     * 轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundChangeEvent) {
        processStatusChange(event.thisRound)
    }

    /**
     * 轮次内 状态切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundStatusChangeEvent) {
        processStatusChange(event.thisRound)
    }

    private fun processStatusChange(thisRound: RaceRoundInfoModel?) {
        if (thisRound?.status == ERaceRoundStatus.ERRS_WAITING.value) {
            mIRaceRoomView.showWaiting()
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
            mIRaceRoomView.showChoicing()
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {

            // 演唱阶段
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_END.value) {
            // 结束
        }
    }

    /**
     * 用户选择了某个歌曲
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RWantSingChanceEvent) {
        if (event.pb.roundSeq == mRoomData.realRoundSeq) {
            mRoomData?.realRoundInfo?.addWantSingChange(event.pb.choiceID, event.pb.userID)
        }
    }

    /**
     * 用户得到了演唱机会
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RGetSingChanceMsg) {
        val roundInfoModel = parseFromRoundInfoPB(event.currentRound)
        if (roundInfoModel.roundSeq == mRoomData.realRoundSeq) {
            // 轮次符合，切换
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(roundInfoModel, true)
        }
    }

    /**
     * 有人加入了房间
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RJoinNoticeEvent) {
    }

    /**
     * 有人退出了房间
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RExitGameEvent) {
    }

    /**
     * 用户爆灯投票
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RBLightEvent) {
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(giftPresentEvent: GiftPresentEvent) {
        MyLog.d(TAG, "onEvent giftPresentEvent=$giftPresentEvent")
        EventBus.getDefault().post(GiftBrushMsgEvent(giftPresentEvent.mGPrensentGiftMsgModel))

        if (giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList != null) {
            for (property in giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList) {
                if (property.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                    if (property.coinBalance != -1f) {
                        UpdateCoinEvent.sendEvent(property.coinBalance.toInt(), property.lastChangeMs)
                    }
                    if (property.hongZuanBalance != -1f) {
                        //mRoomData.setHzCount(property.hongZuanBalance, property.lastChangeMs)
                    }
                }
                if (property.curRoundSeqMeiliTotal > 0) {
                    // 他人的只关心魅力值的变化
                    EventBus.getDefault().post(UpdateMeiliEvent(property.userID, property.curRoundSeqMeiliTotal.toInt(), property.lastChangeMs))
                }
            }
        }

        if (giftPresentEvent.mGPrensentGiftMsgModel.receiveUserInfo.userId.toLong() == MyUserInfoManager.getInstance().uid) {
            if (giftPresentEvent.mGPrensentGiftMsgModel.giftInfo.price <= 0) {
                StatisticsAdapter.recordCountEvent("grab", "game_getflower", null)
            } else {
                StatisticsAdapter.recordCountEvent("grab", "game_getgift", null)
            }
        }
    }

    @CallSuper
    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

}
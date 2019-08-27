package com.module.playways.race.room.presenter

import android.support.annotation.CallSuper
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.statistics.StatisticsAdapter
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.inter.IRaceRoomView
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceCorePresenter(var mRoomData: RaceRoomData, var mIRaceRoomView: IRaceRoomView) : RxLifeCyclePresenter() {

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        val commentSysModel = CommentSysModel("欢迎来到Race房间", CommentSysModel.TYPE_ENTER_ROOM)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
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
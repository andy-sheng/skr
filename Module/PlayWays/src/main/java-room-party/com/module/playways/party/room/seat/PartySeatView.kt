package com.module.playways.party.room.seat

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import com.module.playways.R
import android.support.v7.widget.RecyclerView
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.engine.EngineEvent
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyPopularityUpdateEvent
import com.module.playways.party.room.event.PartyQuickAnswerResultEvent
import com.module.playways.party.room.event.PartySeatInfoChangeEvent
import com.module.playways.party.room.event.PartySendEmojiEvent
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyEmojiInfoModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.QuickAnswerUiModel
import com.module.playways.room.data.H
import com.zq.live.proto.PartyRoom.EMicStatus
import com.zq.live.proto.PartyRoom.PDynamicEmojiMsg
import com.zq.live.proto.PartyRoom.PResponseQuickAnswer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


// 席位view
class PartySeatView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val recyclerView: RecyclerView
    val adapter: PartySeatAdapter
    var listener: Listener? = null

    init {
        View.inflate(context, R.layout.party_seat_view_layout, this)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = PartySeatAdapter(object : PartySeatAdapter.Listener {
            override fun onClickItem(position: Int, model: PartyActorInfoModel?) {
                listener?.onClickAvatar(position, model)
            }
        })
        recyclerView.adapter = adapter
    }

    fun bindData(roomData: PartyRoomData) {
        adapter.mDataList.clear()
        adapter.mDataList = roomData.getSeatInfoMap()
        adapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PDynamicEmojiMsg) {
        // 房间内收到的发送表情信息
        val user = PartyPlayerInfoModel.parseFromPb(event.user)
        if (user.userID == MyUserInfoManager.uid.toInt()) {
            // 把自己的过滤掉 donothing
        } else {
            val seatInfo = H.partyRoomData?.getSeatInfoByUserId(user.userID)
            seatInfo?.let {
                adapter.showUserEmoji(it, PartyEmojiInfoModel.parseFromPB(event.emoji))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartySendEmojiEvent) {
        // 自己发的 直接先更新
        val seatInfo = H.partyRoomData?.getSeatInfoByUserId(MyUserInfoManager.uid.toInt())
        seatInfo?.let {
            adapter.showUserEmoji(it, event.model)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyPopularityUpdateEvent) {
        // 单个刷新人气
        var p = PartyActorInfoModel()
        p.player = H.partyRoomData?.getPlayerInfoById(event.userID)
        p.seat = H.partyRoomData?.getSeatInfoByUserId(event.userID)
        p.seat?.let {
            adapter.mDataList[it.seatSeq] = p
            adapter.notifyItemChanged(it.seatSeq - 1, PartySeatAdapter.REFRESH_HOT)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartySeatInfoChangeEvent) {
        if (event.seatSeq <= 0) {
            // 全部刷新
            adapter.mDataList = H.partyRoomData?.getSeatInfoMap() ?: hashMapOf()
            adapter.notifyDataSetChanged()
        } else {
            // 单个刷新
            var p = PartyActorInfoModel()
            p.player = H.partyRoomData?.getPlayerInfoBySeq(event.seatSeq)
            p.seat = H.partyRoomData?.getSeatInfoBySeq(event.seatSeq)
            adapter.mDataList[event.seatSeq] = p
            adapter.notifyItemChanged(event.seatSeq - 1, null)
        }
    }

    // 有人抢答
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PResponseQuickAnswer) {
        val seatInfo = H.partyRoomData?.getSeatInfoByUserId(event.answer.user.userInfo.userID)
        seatInfo?.let {
            var model = QuickAnswerUiModel()
            model.seq = event.answer.seq
            model.durationTime = 5000
            adapter.notifyItemChanged(it.seatSeq - 1, model)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyQuickAnswerResultEvent) {
        event.list?.let {
            for (an in it) {
                val seatInfo = H.partyRoomData?.getSeatInfoByUserId(an.user?.userID ?: 0)
                seatInfo?.let { seat ->
                    var model = QuickAnswerUiModel()
                    model.seq = an.seq
                    model.durationTime = 3000
                    adapter.notifyItemChanged(seat.seatSeq - 1, model)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            var list = event.getObj<List<EngineEvent.UserVolumeInfo>>()
            for (uv in list) {
                //    MyLog.d(TAG, "UserVolumeInfo uv=" + uv);
                if (uv != null) {
                    var uid = uv.uid
                    if (uid == 0) {
                        uid = MyUserInfoManager.uid.toInt()
                    }
                    var volume = uv.volume
                    if (volume > 20) {
                        val seatInfo = H.partyRoomData?.getSeatInfoByUserId(uid)
                        if (seatInfo?.micStatus == EMicStatus.MS_OPEN.value) {
                            // 麦是开着的
                            seatInfo?.seatSeq?.let {
                                adapter.notifyItemChanged(it - 1, PartySeatAdapter.REFRESH_PLAY_VOLUME)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    //接收麦 和 表情 换位置 上下席位 的信息

    interface Listener {
        fun onClickAvatar(position: Int, model: PartyActorInfoModel?)
    }
}
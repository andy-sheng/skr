package com.module.playways.party.room.seat

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import com.module.playways.R
import android.support.v7.widget.RecyclerView
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.model.PartyActorInfoModel


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
                listener?.onClikAvatar(position, model)
            }
        })
        recyclerView.adapter = adapter
    }

    fun bindData(roomData: PartyRoomData) {
        adapter.mDataList.clear()
        adapter.mDataList = roomData.getSeatInfoMap()
        adapter.notifyDataSetChanged()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this)
//        }
    }

    //接收麦 和 表情 换位置 上下席位 的信息

    interface Listener {
        fun onClikAvatar(position: Int, model: PartyActorInfoModel?)
    }
}
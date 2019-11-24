package com.module.playways.relay.room.top

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.log.MyLog
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.event.MicHomeOwnerChangeEvent
import com.module.playways.mic.room.event.MicPlaySeatUpdateEvent
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.mic.room.event.MicWantInviteEvent
import com.module.playways.relay.room.RelayRoomData
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RelayTopContentView : ConstraintLayout {
    val TAG = "RelayTopContentView"
    val REFRESH_DATA = 1

    val arrowIv: ImageView
    var emptyIv: ImageView

    internal var mIsOpen = true
    private var mRoomData: RelayRoomData? = null

    internal var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.relay_top_content_view_layout, this)

        arrowIv = rootView.findViewById(R.id.arrow_iv)
        emptyIv = rootView.findViewById(R.id.empty_iv)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }


        arrowIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.clickArrow(!mIsOpen)
            }
        })

        emptyIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                EventBus.getDefault().post(MicWantInviteEvent())
            }
        })
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            arrowIv.setImageResource(R.drawable.race_expand_icon)
        } else {
            // 折叠状态
            mIsOpen = false
            arrowIv.setImageResource(R.drawable.race_shrink_icon)
        }
    }

    //只有轮次切换的时候调用
    private fun initData(from: String) {
        val list = mRoomData?.getPlayerAndWaiterInfoList()
    }

    fun setRoomData(roomData: RelayRoomData) {
        mRoomData = roomData
        initData("setRoomData")

        if (mRoomData?.isOwner == true) {
            emptyIv.visibility = View.VISIBLE
        } else {
            emptyIv.visibility = View.GONE
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        MyLog.d(TAG, "onEvent event = $event")
        initData("MicRoundChangeEvent")
    }


    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
    }
}
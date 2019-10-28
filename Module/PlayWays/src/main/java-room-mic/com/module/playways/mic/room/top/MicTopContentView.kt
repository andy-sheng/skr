package com.module.playways.mic.room.top

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.log.MyLog
import com.common.view.DebounceViewClickListener
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.adapter.MicTopContentAdapter
import com.module.playways.race.room.event.RacePlaySeatUpdateEvent
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.event.RaceWaitSeatUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MicTopContentView : ConstraintLayout {
    val TAG = "MicTopContentView"

    val arrowIv: ImageView
    val recyclerView: RecyclerView

    val adapter: MicTopContentAdapter = MicTopContentAdapter()

    internal var mIsOpen = true
    private var mRoomData: MicRoomData? = null

    internal var mListener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.mic_top_content_view_layout, this)

        arrowIv = rootView.findViewById(R.id.arrow_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        arrowIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mListener?.clickArrow(!mIsOpen)
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
        if (!list.isNullOrEmpty()) {
            MyLog.d(TAG, "initData list.size=${list.size} from=$from")
            adapter.mDataList.clear()
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()
        } else {
            MyLog.e(TAG, "initData 没人？？？？")
        }
    }

    fun setRoomData(roomData: MicRoomData) {
        mRoomData = roomData
        adapter.maxUserCount = roomData.configModel.maxUserCnt
        initData("setRoomData")
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
    fun onEvent(event: RaceRoundChangeEvent) {
        initData("RaceRoundChangeEvent")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RacePlaySeatUpdateEvent) {
        initData("RacePlaySeatUpdateEvent")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWaitSeatUpdateEvent) {
        initData("RaceWaitSeatUpdateEvent")
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
    }
}
package com.module.playways.mic.room.top

import android.content.Context
import android.os.Handler
import android.os.Message
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MicTopContentView : ConstraintLayout {
    val TAG = "MicTopContentView"
    val REFRESH_DATA = 1

    val arrowIv: ImageView
    val recyclerView: RecyclerView

    val adapter: MicTopContentAdapter = MicTopContentAdapter()

    internal var mIsOpen = true
    private var mRoomData: MicRoomData? = null

    internal var mListener: Listener? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            if (REFRESH_DATA == msg?.what) {
                initData(msg.obj as String)
            }
        }
    }

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
            MyLog.e(TAG, "initData 没人？？？？from=$from")
        }
    }

    fun setRoomData(roomData: MicRoomData) {
        mRoomData = roomData
        adapter.maxUserCount = roomData.configModel.maxUserCnt
        adapter.mRoomData = mRoomData
        initData("setRoomData")
    }

    fun getViewLeft(userID: Int): Int {
        var targetPosition = -1
        adapter.mDataList.forEachIndexed { index, model ->
            if (model.userID == userID) {
                targetPosition = index
                return@forEachIndexed
            }
        }
        if (targetPosition >= 0) {
            val width = recyclerView.findViewHolderForAdapterPosition(targetPosition).itemView.measuredHeight
            return (targetPosition * width + width * 0.5).toInt()
        }
        return 26.dp()
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

        mUiHandler.removeCallbacksAndMessages(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        MyLog.d(TAG, "onEvent event = $event")
        mUiHandler.removeMessages(REFRESH_DATA)
        val msg = mUiHandler.obtainMessage()
        msg.what = REFRESH_DATA
        msg.obj = "MicRoundChangeEvent"
        mUiHandler.sendMessageDelayed(msg, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicPlaySeatUpdateEvent) {
        MyLog.d(TAG, "onEvent event = $event")
        mUiHandler.removeMessages(REFRESH_DATA)
        val msg = mUiHandler.obtainMessage()
        msg.what = REFRESH_DATA
        msg.obj = "MicPlaySeatUpdateEvent"
        mUiHandler.sendMessageDelayed(msg, 500)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicHomeOwnerChangeEvent) {
        MyLog.d(TAG, "onEvent event = $event")
        mUiHandler.removeMessages(REFRESH_DATA)
        val msg = mUiHandler.obtainMessage()
        msg.what = REFRESH_DATA
        msg.obj = "MicHomeOwnerChangeEvent"
        mUiHandler.sendMessageDelayed(msg, 500)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun clickArrow(open: Boolean)
    }
}
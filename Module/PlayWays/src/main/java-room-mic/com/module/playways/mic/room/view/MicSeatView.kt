package com.module.playways.mic.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.engine.EngineEvent
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.MicRoomServerApi
import com.module.playways.mic.room.adapter.MicSeatRecyclerAdapter
import com.module.playways.mic.room.model.MicSeatModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 右边操作区域，投票
class MicSeatView : ExConstraintLayout {

    val TAG = "MicSeatView"

    var bg: ExImageView
    var recyclerView: RecyclerView

    var mRoomData: MicRoomData? = null
    var adapter: MicSeatRecyclerAdapter? = null

    val raceRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            }
        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.mic_seat_view_layout, this)
        bg = rootView.findViewById(R.id.bg)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MicSeatRecyclerAdapter()
        recyclerView.adapter = adapter

        setDebounceViewClickListener {
            hide()
        }

        bg.setDebounceViewClickListener {
            //拦截
        }

        getUserList()
    }

    private fun getUserList() {
        launch {
            val result = subscribe(RequestControl("MicSeatView getUserList", ControlType.CancelThis)) {
                raceRoomServerApi.getMicSeatUserList(MyUserInfoManager.getInstance().uid.toInt(), mRoomData?.gameId!!)
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("userLists"), MicSeatModel::class.java)
                list?.let {
                    adapter?.mDataList?.clear()
                    adapter?.mDataList?.addAll(list)
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (EngineEvent.TYPE_USER_SELF_JOIN_SUCCESS == event.getType()) {

        }
    }

    private fun hide() {
        if (mUiHandler.hasMessages(HIDE_PANEL) || View.GONE == visibility) {
            return
        }

        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
    }

    fun show() {
        if (visibility == View.VISIBLE) {
            return
        }

        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        val HIDE_PANEL = 1
        val ANIMATION_DURATION = 300
    }
}

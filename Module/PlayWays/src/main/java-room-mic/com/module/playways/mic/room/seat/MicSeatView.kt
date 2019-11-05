package com.module.playways.mic.room.seat

import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.MicRoomServerApi
import com.module.playways.mic.room.event.MicHomeOwnerChangeEvent
import com.module.playways.mic.room.event.MicPlaySeatUpdateEvent
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.mic.room.model.MicSeatModel
import com.umeng.socialize.utils.DeviceConfig.context
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 右边操作区域，投票
class MicSeatView : ExViewStub {

    val TAG = "MicSeatView"

    var bg: ExImageView? = null
    var recyclerView: RecyclerView? = null

    var mRoomData: MicRoomData? = null
    var adapter: MicSeatRecyclerAdapter? = null

    var callWhenVisible: (() -> Unit)? = null

    val raceRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    var hasSelectSongNumTv: ExTextView? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                mParentView?.clearAnimation()
                mParentView?.visibility = View.GONE
            } else if (REFRESH_DATA == msg?.what) {
                getUserList()
            }
        }
    }

    constructor(mViewStub: ViewStub?) : super(mViewStub)

    override fun init(parentView: View) {
        bg = parentView.findViewById(R.id.bg)
        recyclerView = parentView.findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = object : LinearLayoutManager(context) {

            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                try {
                    //RecyclerView内部崩溃，保护一下
                    super.onLayoutChildren(recycler, state);
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace();
                }

            }

        }
        adapter = MicSeatRecyclerAdapter()
        recyclerView?.adapter = adapter

        parentView.setDebounceViewClickListener {
            hide()
        }

        bg?.setDebounceViewClickListener {
            //拦截
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.mic_seat_view_layout
    }

    private fun getUserList() {
        if (hasInflate()) {
            launch {
                val result = subscribe(RequestControl("MicSeatView getUserList", ControlType.CancelThis)) {
                    raceRoomServerApi.getMicSeatUserList(MyUserInfoManager.uid.toInt(), mRoomData?.gameId!!)
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
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        EventBus.getDefault().register(this)
    }

    private fun hasInflate(): Boolean {
        return mViewStub == null
    }

    fun onBackPressed(): Boolean {
        if (hasInflate() && mParentView?.visibility == View.VISIBLE) {
            hide()
            return true
        }

        return false
    }

    private fun hide() {
        if (!hasInflate()) {
            return
        }

        if (mUiHandler.hasMessages(HIDE_PANEL) || View.GONE == mParentView?.visibility) {
            return
        }

        mUiHandler.removeMessages(HIDE_PANEL)
        mParentView?.clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        mParentView?.startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
    }

    fun show() {
        if (!hasInflate()) {
            tryInflate()
        } else {
            if (mParentView?.visibility == View.VISIBLE) {
                return
            }
        }

        callWhenVisible?.invoke()
        callWhenVisible = null

        if (adapter?.mDataList?.size == 0) {
            getUserList()
        }

        mParentView?.clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        mParentView?.startAnimation(animation)
        mParentView?.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        callUpdate {
            getUserList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicHomeOwnerChangeEvent) {
        callUpdate {
            getUserList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicPlaySeatUpdateEvent) {
        callUpdate {
            getUserList()
        }
    }

    private fun callUpdate(call: (() -> Unit)) {
        if (View.VISIBLE == mParentView?.visibility) {
            if (!mUiHandler.hasMessages(REFRESH_DATA)) {
                mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(REFRESH_DATA), 500)
            }
        } else {
            callWhenVisible = call
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        EventBus.getDefault().unregister(this)
    }

    companion object {
        val HIDE_PANEL = 1
        val REFRESH_DATA = 2
        val ANIMATION_DURATION = 300
    }
}

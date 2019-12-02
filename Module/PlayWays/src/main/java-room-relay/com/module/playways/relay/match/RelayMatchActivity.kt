package com.module.playways.relay.match

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.common.ICallback
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.relay.match.adapter.RelayRoomAdapter
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.match.model.RelayRecommendRoomInfo
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.room.prepare.presenter.GrabMatchPresenter
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RelayRoom.RUserEnterMsg
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_RELAY_MATCH)
class RelayMatchActivity : BaseActivity() {

    private var titlebar: CommonTitleBar? = null
    private var joinTipsTv: ExTextView? = null
    private var speedRecyclerView: SpeedRecyclerView? = null

    var adapter: RelayRoomAdapter = RelayRoomAdapter()
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    val cnt = 15

    //在滑动到最后的时候自动加载更多
    var loadMore: Boolean = false
    var currentPosition = -1

    var model: SongModel? = null
    var matchJob: Job? = null

    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_match_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = intent.getSerializableExtra("songModel") as SongModel?
        if (model == null) {
            finish()
        }

        titlebar = findViewById(R.id.titlebar)
        joinTipsTv = findViewById(R.id.join_tips_tv)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

        speedRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        speedRecyclerView?.adapter = adapter

        cardScaleHelper = CardScaleHelper(8, 12)
        cardScaleHelper?.attachToRecyclerView(speedRecyclerView)
        speedRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = cardScaleHelper?.currentItemPos ?: 0
                    if (!loadMore && currentPosition > (adapter.mDataList.size - 3)) {
                        loadMore = true
                        getRecommendRoomList(offset, false)
                    }
                }
            }
        })

        adapter.listener = object : RelayRoomAdapter.RelayRoomListener {
            override fun selectRoom(position: Int, model: RelayRecommendRoomInfo?) {
                model?.let {
                    choiceRoom(it)
                }
            }

            override fun getRecyclerViewPosition(): Int {
                return currentPosition
            }
        }

        titlebar?.leftTextView?.setDebounceViewClickListener {
            cancelMatch()
            finish()
        }

        titlebar?.centerTextView?.text = "《${model?.itemName}》"

        getRecommendRoomList(0, true)
        startMatch()
        RelayRoomData.syncServerTs()
    }

    // 开始匹配
    private fun startMatch() {
        model?.let {
            matchJob?.cancel()
            matchJob = launch {
                val map = mutableMapOf(
                        "itemID" to it.itemID,
                        "platform" to 20)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                repeat(Int.MAX_VALUE) {
                    if (it % 2 == 0) {
                        joinTipsTv?.text = "ta正在赶来的路上..."
                    } else {
                        joinTipsTv?.text = "等太久，试试加入别人等合唱吧～"
                    }
                    val result = subscribe(RequestControl("startMatch", ControlType.CancelThis)) {
                        relayMatchServerApi.queryMatch(body)
                    }
                    if (result.errno == 0) {
                        val hasMatchedRoom = result.data.getBoolean("hasMatchedRoom")
                        if (hasMatchedRoom) {
                            val joinRelayRoomRspModel = JSON.parseObject(result.data.toJSONString(), JoinRelayRoomRspModel::class.java)
                            matchJob?.cancel()
                            tryGoRelayRoom(joinRelayRoomRspModel)
                        } else {
                            // 没匹配到 donothing
                        }
                    }
                    delay(10 * 1000)
                }
            }
        }
    }

    // 取消匹配
    private fun cancelMatch() {
        matchJob?.cancel()
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("cancelMatch", ControlType.CancelThis)) {
                relayMatchServerApi.cancelMatch(body)
            }
            if (result.errno == 0) {

            }
        }
    }

    fun getRecommendRoomList(off: Int, clean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getRecommendRoomList", ControlType.CancelThis)) {
                relayMatchServerApi.getMatchRoomList(off, cnt, MyUserInfoManager.uid.toInt())
            }
            loadMore = false
            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("items"), RelayRecommendRoomInfo::class.java)
                addRoomList(list, clean)
            }
        }
    }

    private fun addRoomList(list: List<RelayRecommendRoomInfo>?, clean: Boolean) {
        if (clean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter.addData(list)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RUserEnterMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        // 进入房间的信令 直接加入融云的房间
        matchJob?.cancel()
        tryGoRelayRoom(JoinRelayRoomRspModel.parseFromPB(event))
    }

    private fun choiceRoom(model: RelayRecommendRoomInfo) {
        launch {
            val map = mutableMapOf(
                    "itemID" to (model.item?.itemID ?: 0),
                    "peerUserID" to (model.user?.userId ?: 0))
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("selectRoom", ControlType.CancelThis)) {
                relayMatchServerApi.choiceRoom(body)
            }
            if (result.errno == 0) {
                val joinRelayRoomRspModel = JSON.parseObject(result.data.toJSONString(), JoinRelayRoomRspModel::class.java)
                tryGoRelayRoom(joinRelayRoomRspModel)
            } else {
                U.getToastUtil().showShort(result.errmsg)
                // todo 补充一个UI更新的逻辑
            }
        }
    }

    private fun tryGoRelayRoom(model: JoinRelayRoomRspModel) {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RELAY_ROOM)
                .withSerializable("JoinRelayRoomRspModel", model)
                .navigation()

//        ModuleServiceManager.getInstance().msgService.joinChatRoom(model.roomID.toString(), 10, object : ICallback {
//            override fun onSucess(obj: Any?) {
//                // todo 补全加融云成功直接
//
//            }
//
//            override fun onFailed(obj: Any?, errcode: Int, message: String?) {
//                // 加入失败
//                reportEnterFail(model)
//            }
//        })
    }

    private fun reportEnterFail(model: JoinRelayRoomRspModel) {
        launch {
            val map = mutableMapOf("roomID" to (model.roomID))
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("reportEnterFail", ControlType.CancelThis)) {
                relayMatchServerApi.enterRoomFailed(body)
            }
            if (result.errno == 0) {
                // 进入失败，重新开始匹配
                startMatch()
            }
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun destroy() {
        super.destroy()
        matchJob?.cancel()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onBackPressedForActivity(): Boolean {
        cancelMatch()
        return super.onBackPressedForActivity()
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}

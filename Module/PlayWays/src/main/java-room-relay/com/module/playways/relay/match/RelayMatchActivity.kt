package com.module.playways.relay.match

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.manager.BgMusicManager
import com.component.busilib.view.CircleCountDownView
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.component.lyrics.utils.SongResUtils
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayRoomAdapter
import com.module.playways.relay.match.adapter.RelayRoomAdapter.Companion.REFRESH_TYPE_RESET_VOICE_ANIMATION
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.match.model.RelaySelectItemInfo
import com.module.playways.relay.match.view.RelayEmptyRoomCallback
import com.module.playways.relay.room.RelayRoomActivity
import com.module.playways.room.song.model.SongModel
import com.opensource.svgaplayer.SVGAImageView
import com.zq.live.proto.RelayRoom.RUserEnterMsg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.random.Random

@Route(path = RouterConstants.ACTIVITY_RELAY_MATCH)
class RelayMatchActivity : BaseActivity() {

    private var titlebar: CommonTitleBar? = null
    private var joinTipsTv: ExTextView? = null
    private var quickTipsTv: TextView? = null
    private var speedRecyclerView: SpeedRecyclerView? = null
    private var circleCountDownView: CircleCountDownView? = null
    private var inviteAvatar: SimpleDraweeView? = null

    var adapter: RelayRoomAdapter = RelayRoomAdapter()
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    val cnt = 10    // 房间列表实质上并不是一个分页的接口，客户端当成每次刷新的接口来做 offset和hasMore 都没有用

    //在滑动到最后的时候自动加载更多
    var loadMore: Boolean = false
    var currentPosition = -1

    var model: SongModel? = null
    var matchJob: Job? = null

    var roomJob: Job? = null
    var roomInterval = 6 * 1000L

    private var inviteIntervalTimeMs = 6 * 1000L // 邀请倒计时

    var mLoadService: LoadService<*>? = null

    var todayResTimes: Int = 0
    var needAlert: Boolean = false  // 是否需要在次数不足显示充值弹窗

    var mTipsDialogView: TipsDialogView? = null

    var playingVoiceIndex = -1

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

        U.getStatusBarUtil().setTransparentBar(this, false)
        titlebar = findViewById(R.id.titlebar)
        joinTipsTv = findViewById(R.id.join_tips_tv)
        quickTipsTv = findViewById(R.id.quick_tips_tv)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

        circleCountDownView = findViewById(R.id.circle_count_down_view)
        inviteAvatar = findViewById(R.id.invite_avatar)


        findViewById<SVGAImageView>(R.id.match_avga).layoutParams.height = U.getDisplayUtils().phoneWidth * 230 / 375

        // 获取时间间隔
        roomInterval = U.getPreferenceUtils().getSettingLong("relay-ticker-interval-ms", 6000L)

        speedRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        speedRecyclerView?.adapter = adapter

        cardScaleHelper = CardScaleHelper(8, 12)
        cardScaleHelper?.attachToRecyclerView(speedRecyclerView)
        speedRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = cardScaleHelper?.currentItemPos ?: 0
                    if (currentPosition < adapter.mDataList.size && currentPosition >= 0) {
                        if (adapter.mDataList[currentPosition].type == RelaySelectItemInfo.ST_MATCH_ITEM) {
                            StatisticsAdapter.recordCountEvent("chorus", "common_expose", null)
                        } else if (adapter.mDataList[currentPosition].type == RelaySelectItemInfo.ST_REDPACKET_ITEM) {
                            StatisticsAdapter.recordCountEvent("chorus", "redpacket_expose", null)
                        }
                    }
                    stopVoicePlay("onScrollStateChanged")
                } else {
                    cancelTimeRoom()
                }
            }
        })

        SinglePlayer.addCallback(TAG, object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                stopVoicePlay("onCompletion")
            }
        })

        adapter.listener = object : RelayRoomAdapter.RelayRoomListener {
            // 点击声音标签
            override fun clickVoiceInfo(position: Int, model: RelaySelectItemInfo?): Boolean {
                MyLog.d(TAG, "SinglePlayer.isPlaying=${SinglePlayer.isPlaying} SinglePlayer.startFrom=${SinglePlayer.startFrom}")
                if (SinglePlayer.isPlaying && SinglePlayer.startFrom == TAG) {
                    stopVoicePlay("clickVoiceInfo")
                    return false
                } else {
                    playingVoiceIndex = position
                    var url = model?.redpacketItem?.voiceInfo?.voiceURL
                    SinglePlayer.setVolume(1.0f)
                    BgMusicManager.getInstance().destory()
                    SinglePlayer.startPlay(TAG, url ?: "")
                    // 停止刷新列表
                    cancelTimeRoom()
                    return true
                }
            }

            override fun selectRedPacket(position: Int, model: RelaySelectItemInfo?) {
                StatisticsAdapter.recordCountEvent("chorus", "redpacket_invite", null)
                model?.let {
                    sendRedPacketInvite(position, it)
                }
            }

            override fun selectRoom(position: Int, model: RelaySelectItemInfo?) {
                StatisticsAdapter.recordCountEvent("chorus", "join", null)
                if (todayResTimes <= 0) {
                    if (needAlert) {
                        // 提示充值
                        mTipsDialogView?.dismiss(false)
                        mTipsDialogView = TipsDialogView.Builder(this@RelayMatchActivity)
                                .setMessageTip("今日加入次数用完啦～开通VIP立享每日25次机会")
                                .setCancelTip("取消")
                                .setConfirmTip("开通VIP")
                                .setConfirmBtnClickListener {
                                    mTipsDialogView?.dismiss(false)
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                            .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                                            .greenChannel().navigation()
                                }
                                .setCancelBtnClickListener {
                                    mTipsDialogView?.dismiss()
                                }
                                .build()
                        mTipsDialogView?.showByDialog()
                    } else {
                        U.getToastUtil().showShort("今日加入次数用完啦")
                    }
                } else {
                    model?.let {
                        choiceRoom(position, it)
                    }
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


        startMatch()
        startTimerRoom(0)
        checkResTime()
        BaseRoomData.syncServerTs()

        val mLoadSir = LoadSir.Builder()
                .addCallback(RelayEmptyRoomCallback())
                .build()
        mLoadService = mLoadSir.register(speedRecyclerView, Callback.OnReloadListener {
            startTimerRoom(0)
        })

        val accFile = SongResUtils.getAccFileByUrl(model?.acc)
        if (!accFile.exists()) {
            // 先下载这个伴奏备用
            U.getHttpUtils().downloadFileAsync(model?.acc, accFile, true, null)
        }

        BgMusicManager.getInstance().starPlay(model?.acc, 0, "RelayMatchActivity")
    }

    private fun stopVoicePlay(from: String) {
        MyLog.d(TAG, "stopVoicePlay from = $from")
        SinglePlayer.stop(TAG)
        startTimerRoom(roomInterval)
        if (!BgMusicManager.getInstance().isPlaying) {
            BgMusicManager.getInstance().starPlay(model?.acc, Random(System.currentTimeMillis()).nextInt(1 * 60 * 1000).toLong(), "RelayMatchActivity")
        }
        if (playingVoiceIndex >= 0) {
            adapter.notifyItemChanged(playingVoiceIndex, REFRESH_TYPE_RESET_VOICE_ANIMATION)
            playingVoiceIndex = -1
        }
    }

    private fun sendRedPacketInvite(position: Int, itemInfo: RelaySelectItemInfo) {
        launch {
            val map = mutableMapOf(
                    "itemID" to model?.itemID,
                    "inviteUserID" to itemInfo.redpacketItem?.user?.userId)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("checkResTime", ControlType.CancelThis)) {
                relayMatchServerApi.sendRedPacketInvite(body)
            }
            if (result.errno == 0) {
                inviteIntervalTimeMs = result.data.getLongValue("inviteIntervalTimeMs")
                // 更新到已邀请
                adapter.updateInviteStatus(itemInfo, true)
                // 停止发送匹配
                matchJob?.cancel()
                // 开始邀请等待倒计时
                if (inviteIntervalTimeMs <= 0) {
                    inviteIntervalTimeMs = 6 * 1000L
                }
                showInviteCountDown(itemInfo)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun showInviteCountDown(itemInfo: RelaySelectItemInfo) {
        // 邀请倒计时
        circleCountDownView?.visibility = View.VISIBLE
        inviteAvatar?.visibility = View.VISIBLE
        AvatarUtils.loadAvatarByUrl(inviteAvatar, AvatarUtils.newParamsBuilder(itemInfo.redpacketItem?.user?.avatar)
                .setCircle(true)
                .build())
        circleCountDownView?.cancelAnim()
        circleCountDownView?.go(0, inviteIntervalTimeMs.toInt()) {
            // 更新恢复到未邀请状态
            circleCountDownView?.visibility = View.GONE
            inviteAvatar?.visibility = View.GONE
            adapter.updateInviteStatus(itemInfo, false)
            // 还没人响应, 重新开始匹配
            startMatch()
        }

    }

    private fun checkResTime() {
        launch {
            val result = subscribe(RequestControl("checkResTime", ControlType.CancelThis)) {
                relayMatchServerApi.getTotalResTimes()
            }
            if (result.errno == 0) {
                todayResTimes = result.data.getIntValue("todayResTimes")
                needAlert = result.data.getBooleanValue("needAlert")
                showTodayResTimes()
            }
        }
    }

    private fun showTodayResTimes() {
        val ss = SpanUtils()
                .append("今日剩余").setForegroundColor(U.getColor(R.color.white_trans_30))
                .append("${todayResTimes}次").setForegroundColor(U.getColor(R.color.white))
                .append("快速合唱").setForegroundColor(U.getColor(R.color.white_trans_30))
                .create()
        quickTipsTv?.text = ss
    }

    // 定时自动刷新房间列表
    private fun startTimerRoom(delayTime: Long) {
        roomJob?.cancel()
        roomJob = launch {
            delay(delayTime)
            repeat(Int.MAX_VALUE) {
                getRecommendRoomList()
                delay(roomInterval)
            }
        }
    }

    private fun cancelTimeRoom() {
        roomJob?.cancel()
    }

    // 开始匹配
    private fun startMatch() {
        model?.let { song ->
            matchJob?.cancel()
            matchJob = launch {

                repeat(Int.MAX_VALUE) {
                    when (it % 20) {
                        0 -> {
                            joinTipsTv?.text = "正在为你匹配合拍好声音.  "
                            queryMatch(song.itemID)
                        }
                        1, 4, 7 -> {
                            joinTipsTv?.text = "正在为你匹配合拍好声音.. "
                        }
                        2, 5, 8 -> {
                            joinTipsTv?.text = "正在为你匹配合拍好声音..."
                        }
                        3, 6 -> {
                            joinTipsTv?.text = "正在为你匹配合拍好声音.  "
                        }
                        10 -> {
                            joinTipsTv?.text = "等太久，试试加入别人的合唱吧～"
                            queryMatch(song.itemID)
                        }
                        else -> {
                            joinTipsTv?.text = "等太久，试试加入别人的合唱吧～"
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    private fun queryMatch(songID: Int) {
        val map = mutableMapOf(
                "itemID" to songID,
                "platform" to 20)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
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
        }
    }

    // 取消匹配
    private fun cancelMatch() {
        matchJob?.cancel()
        GlobalScope.launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("cancelMatch", ControlType.CancelThis)) {
                ApiManager.getInstance().createService(RelayMatchServerApi::class.java).cancelMatch(body)
            }
            if (result.errno == 0) {

            }
        }
    }

    private fun getRecommendRoomList() {
        launch {
            val result = subscribe(RequestControl("getRecommendRoomList", ControlType.CancelThis)) {
                relayMatchServerApi.getMatchRoomList(0, cnt, MyUserInfoManager.uid.toInt())
            }
            loadMore = false
            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("items"), RelaySelectItemInfo::class.java)
                addRoomList(list, true)
            }
        }
    }

    private fun addRoomList(list: List<RelaySelectItemInfo>?, clean: Boolean) {
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

        checkIsEmpty()
    }

    private fun checkIsEmpty() {
        if (adapter.mDataList.isNullOrEmpty()) {
            mLoadService?.showCallback(RelayEmptyRoomCallback::class.java)
            quickTipsTv?.visibility = View.GONE
        } else {
            mLoadService?.showSuccess()
            quickTipsTv?.visibility = View.VISIBLE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RUserEnterMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        // 进入房间的信令 直接加入融云的房间
        matchJob?.cancel()
        circleCountDownView?.cancelAnim()
        adapter.inviteModel = null
        tryGoRelayRoom(JoinRelayRoomRspModel.parseFromPB(event))
    }

    private fun choiceRoom(position: Int, model: RelaySelectItemInfo) {
        launch {
            val map = mutableMapOf(
                    "itemID" to (model.matchItem?.item?.itemID ?: 0),
                    "peerUserID" to (model.matchItem?.user?.userId ?: 0))
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("selectRoom", ControlType.CancelThis)) {
                relayMatchServerApi.choiceRoom(body)
            }
            if (result.errno == 0) {
                // 选择成功，即取消匹配
                cancelMatch()
                val joinRelayRoomRspModel = JSON.parseObject(result.data.toJSONString(), JoinRelayRoomRspModel::class.java)
                tryGoRelayRoom(joinRelayRoomRspModel)
            } else {
                U.getToastUtil().showShort(result.errmsg)
                if (result.errno == 8343064 || result.errno == 8343065) {
                    // 在等待邀请响应中，8343064
                    // 对方在繁忙中
                } else {
                    adapter.mDataList.remove(model)
                    adapter.notifyItemRemoved(position)//注意这里
                    if (position != adapter.mDataList.size) {
                        adapter.notifyItemRangeChanged(position, adapter.mDataList.size - position)
                    }
                    checkIsEmpty()
                }
            }
        }
    }

    private var hasMatched = false

    private fun tryGoRelayRoom(model: JoinRelayRoomRspModel) {
        MyLog.d(TAG, "tryGoRelayRoom model = $model hasMatched=$hasMatched")
        if (!hasMatched) {
            hasMatched = true
            val intent = Intent(this, RelayRoomActivity::class.java)
            intent.putExtra("JoinRelayRoomRspModel", model)
            this.startActivity(intent)
            finish()
        }
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

    override fun finish() {
        super.finish()
        BgMusicManager.getInstance().destory()
    }

    override fun destroy() {
        super.destroy()
        roomJob?.cancel()
        matchJob?.cancel()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        BgMusicManager.getInstance().destory()
        SinglePlayer.stop(TAG)
        SinglePlayer.removeCallback(TAG)
    }

    override fun onBackPressedForActivity(): Boolean {
        cancelMatch()
        return super.onBackPressedForActivity()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        if (!event.foreground) {
            cancelMatch()
            finish()
        }
    }


    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}

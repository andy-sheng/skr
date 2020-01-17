package com.module.playways.relay.match

import android.content.Intent
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongCardAdapter
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.match.view.ClickRedPacketListener
import com.module.playways.relay.match.view.RelayRedPacketConfirmView
import com.module.playways.relay.room.RelayRoomActivity
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.room.song.SongSelectServerApi
import com.module.playways.room.song.fragment.HistorySongFragment
import com.module.playways.room.song.fragment.SearchSongFragment
import com.module.playways.room.song.model.SongModel
import com.module.playways.room.song.view.RelaySongInfoDialogView
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.AddSongEvent
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap
import kotlin.math.max

@Route(path = RouterConstants.ACTIVITY_RELAY_HOME)
class RelayHomeActivity : BaseActivity() {

    private var title: CommonTitleBar? = null
    private var selectBack: ExImageView? = null
    private var titleTv: TextView? = null
    private var relayRedPacketTv: TextView? = null
    private var searchSongTv: ExTextView? = null
    private var songListTv: ExTextView? = null
    private var inviteFriendTv: ExTextView? = null
    private var bottomArea: Barrier? = null
    private var speedRecyclerView: SpeedRecyclerView? = null

    var adapter: RelayHomeSongCardAdapter? = null
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    var cnt = 15
    val firstRequestPage = 4 // 第一次请求多少页数据

    //在滑动到最后的时候自动加载更多
    var loadMore: Boolean = false
    var currentPosition = -1
    var skrAudioPermission = SkrAudioPermission()

    var mTipsDialogView: TipsDialogView? = null
    var mRelaySongInfoDialogView: RelaySongInfoDialogView? = null

    var status = 1 // 红包状态
    var mRelayRedPacketConfirmView: RelayRedPacketConfirmView? = null
    val KEY_PREFRE_RED_PACKET_SHOW = "has_show_redpacket_dialog"

    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        title = findViewById(R.id.title)
        selectBack = findViewById(R.id.select_back)
        titleTv = findViewById(R.id.title_tv)
        relayRedPacketTv = findViewById(R.id.relay_red_packet_tv)

        searchSongTv = findViewById(R.id.search_song_tv)
        songListTv = findViewById(R.id.song_list_tv)
        inviteFriendTv = this.findViewById(R.id.invite_friend_tv)

        bottomArea = findViewById(R.id.bottom_area)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

        selectBack?.setAnimateDebounceViewClickListener { finish() }

        inviteFriendTv?.setDebounceViewClickListener {
            inviteFriendTv?.isClickable = false
            createRelayRoom()
        }

        searchSongTv?.setDebounceViewClickListener {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, SearchSongFragment::class.java)
                    .setAddToBackStack(true)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_RELAY_HOME)
                    .addDataBeforeAdd(1, false)
                    .setFragmentDataListener(object : FragmentDataListener {
                        override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                            if (requestCode == 0 && resultCode == 0 && obj != null) {
                                val model = obj as SongModel?
                                EventBus.getDefault().post(AddSongEvent(model!!, SongManagerActivity.TYPE_FROM_RELAY_HOME))
                            }
                        }
                    })
                    .build())
        }

        songListTv?.setDebounceViewClickListener {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, RelayHistorySongFragment::class.java)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_RELAY_HOME)
                    .setEnterAnim(R.anim.slide_in_bottom)
                    .setExitAnim(R.anim.slide_out_bottom)
                    .build())
        }

        relayRedPacketTv?.setDebounceViewClickListener {
            showRedPacketDialog()
        }

        val songCardMaxHeight = (U.getDisplayUtils().screenHeight - 62.dp() - 50.dp() - 36.dp() - 20.dp()) -
                (if (U.getDeviceUtils().hasNotch(this)) U.getStatusBarUtil().getStatusBarHeight(this) else 0)
        var maxSize = songCardMaxHeight / 72.dp()
        var songCardHeight = 72.dp() * maxSize + 36.dp()
        if ((songCardMaxHeight - songCardHeight) >= 50.dp()) {
            // 只是把判断条件放宽一点，毕竟有上下的距离
            maxSize += 1
            songCardHeight = 72.dp() * maxSize + 36.dp()
        }
        // 直接用cnt表示每一页的熟练
        cnt = maxSize

        val layoutParams = speedRecyclerView?.layoutParams as ConstraintLayout.LayoutParams?
        layoutParams?.height = songCardHeight
        speedRecyclerView?.layoutParams = layoutParams

        adapter = RelayHomeSongCardAdapter(maxSize)
        speedRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        speedRecyclerView?.adapter = adapter

        speedRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = cardScaleHelper?.currentItemPos ?: 0
                    if (!loadMore && currentPosition >= ((adapter?.mDataList?.size
                                    ?: 0) - 3)) {
                        loadMore = true
                        if ((adapter?.mDataList?.size ?: 0) > 0) {
                            val lastItemSize = adapter?.mDataList?.get((adapter?.mDataList?.size
                                    ?: 0) - 1)?.list?.size ?: 0
                            val diff = (adapter?.maxSize ?: 0) - lastItemSize
                            // 尽量保障拉回来的数据和之前数据能组成满页
                            getPlayBookList(offset, 3 * cnt + diff, false)
                        } else {
                            getPlayBookList(offset, 3 * cnt, false)
                        }
                    }
                }
            }
        })

        cardScaleHelper = CardScaleHelper(8, 12)
        cardScaleHelper?.attachToRecyclerView(speedRecyclerView)

        adapter?.listener = object : RelayHomeSongCardAdapter.RelayHomeListener {
            override fun selectSong(position: Int, childPosition: Int, model: SongModel?) {
                // 跳到匹配中到页面
                goMatch(model)
            }

            override fun selectSongDetail(position: Int, childPosition: Int, model: SongModel?) {
                model?.let {
                    mRelaySongInfoDialogView?.dismiss(false)
                    mRelaySongInfoDialogView = RelaySongInfoDialogView(model, this@RelayHomeActivity)
                    mRelaySongInfoDialogView?.showByDialog(U.getDisplayUtils().screenHeight - 2 * U.getDisplayUtils().dip2px(60f))
                }

            }

            override fun getRecyclerViewPosition(): Int {
                return currentPosition
            }
        }

        getPlayBookList(0, firstRequestPage * cnt, true)
        getRedPacketStatus()
    }

    private fun showRedPacketDialog() {
        mRelayRedPacketConfirmView?.dismiss(false)
        var isOpen = false  // 默认是未开启
        if (status == 1) {
            isOpen = true
        }
        mRelayRedPacketConfirmView = RelayRedPacketConfirmView(this, isOpen, object : ClickRedPacketListener {
            override fun onClickRedPacket() {
                mRelayRedPacketConfirmView?.dismiss(false)
                updateRedPacketStatus(!isOpen)
            }
        })
        mRelayRedPacketConfirmView?.showByDialog()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AddSongEvent) {
        // 过滤一下点歌的来源
        if (event.from == SongManagerActivity.TYPE_FROM_RELAY_HOME) {
            StatisticsAdapter.recordCountEvent("chorus", "start", null)
            goMatch(event.songModel)
        }
    }

    fun goMatch(model: SongModel?) {
        // 先跳到匹配页面发起匹配
        model?.let {
            launch {
                val result = subscribe(RequestControl("checkEnterPermission", ControlType.CancelThis)) {
                    relayMatchServerApi.checkEnterPermission()
                }
                if (result.errno == 0) {
                    skrAudioPermission.ensurePermission({
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_RELAY_MATCH)
                                .withSerializable("songModel", model)
                                .navigation()
                    }, true)
                } else {
                    if (result.errno == 8343059) {
                        // 多次恶意退出
                        mTipsDialogView?.dismiss(false)
                        mTipsDialogView = TipsDialogView.Builder(this@RelayHomeActivity)
                                .setMessageTip(result.errmsg)
                                .setOkBtnTip("我知道了")
                                .setOkBtnClickListener {
                                    mTipsDialogView?.dismiss(false)
                                }
                                .build()
                        mTipsDialogView?.showByDialog()
                    } else {
                        U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }

        }
    }

    private fun createRelayRoom() {
        launch {
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(null))
            val result = subscribe(RequestControl("createRelayRoom", ControlType.CancelThis)) {
                relayMatchServerApi.createRelayRoom(body)
            }
            if (result.errno == 0) {
                val rsp = JSON.parseObject(result.data!!.toJSONString(), JoinRelayRoomRspModel::class.java)
                rsp.enterType = RelayRoomData.EnterType.INVITE
                createSuccess(rsp)
                inviteFriendTv?.isClickable = true
            } else {
                U.getToastUtil().showShort(result.errmsg)
                inviteFriendTv?.isClickable = true
            }
        }
    }

    private fun createSuccess(joinRelayRoomRspModel: JoinRelayRoomRspModel) {
        val intent = Intent(this, RelayRoomActivity::class.java)
        intent.putExtra("JoinRelayRoomRspModel", joinRelayRoomRspModel)
        startActivity(intent)
    }

    private fun getRedPacketStatus() {
        launch {
            val result = subscribe(RequestControl("getRedPacketStatus", ControlType.CancelThis)) {
                relayMatchServerApi.getRedPacketStatus()
            }

            if (result.errno == 0) {
                status = result.data.getIntValue("status")
                refreshRedPacketStatus()
                if (status == 1) {
                    // 红包已开启了
                    U.getPreferenceUtils().setSettingBoolean(KEY_PREFRE_RED_PACKET_SHOW, true)
                } else {
                    if (!U.getPreferenceUtils().getSettingBoolean(KEY_PREFRE_RED_PACKET_SHOW, false)) {
                        U.getPreferenceUtils().setSettingBoolean(KEY_PREFRE_RED_PACKET_SHOW, true)
                        showRedPacketDialog()
                    }
                }
            }
        }
    }

    private fun updateRedPacketStatus(isOpen: Boolean) {
        launch {
            val result = subscribe(RequestControl("updateRedPacketStatus", ControlType.CancelThis)) {
                relayMatchServerApi.updateRedPacketStatus(isOpen)
            }

            if (result.errno == 0) {
                if (isOpen) {
                    U.getToastUtil().showShort("开启红包合唱成功")
                    StatisticsAdapter.recordCountEvent("chorus", "redpacket_open", null)
                } else {
                    U.getToastUtil().showShort("关闭红包合唱成功")
                    StatisticsAdapter.recordCountEvent("chorus", "redpacket_close", null)
                }
                status = result.data.getIntValue("status")
                refreshRedPacketStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun refreshRedPacketStatus() {
        when (status) {
            1 -> {
                relayRedPacketTv?.visibility = View.VISIBLE
                relayRedPacketTv?.text = "已开启"
            }
            2 -> {
                relayRedPacketTv?.visibility = View.VISIBLE
                relayRedPacketTv?.text = "红包合唱"
            }
            else -> relayRedPacketTv?.visibility = View.GONE
        }
    }

    fun getPlayBookList(off: Int, limit: Int, clean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getPlayBookList", ControlType.CancelThis)) {
                relayMatchServerApi.getPlayBookList(off, limit, MyUserInfoManager.uid.toInt())
            }
            loadMore = false

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("items"), SongModel::class.java)
                addSongList(list, clean)
            }
        }
    }

    private fun addSongList(list: List<SongModel>?, clean: Boolean) {
        if (clean) {
            adapter?.mDataList?.clear()
            adapter?.addData(list)
            speedRecyclerView?.post {
                speedRecyclerView?.smoothScrollBy(1, 0)
            }
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.addData(list)
                speedRecyclerView?.post {
                    speedRecyclerView?.smoothScrollBy(1, 0)
                }
            }
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        skrAudioPermission.onBackFromPermisionManagerMaybe(this)
    }


    override fun destroy() {
        super.destroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}

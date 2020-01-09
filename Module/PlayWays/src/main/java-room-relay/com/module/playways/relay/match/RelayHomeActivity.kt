package com.module.playways.relay.match

import android.os.Bundle
import android.support.constraint.Barrier
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongCardAdapter
import com.module.playways.room.song.model.SongModel
import com.module.playways.room.song.view.RelaySongInfoDialogView
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.AddSongEvent
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.max

@Route(path = RouterConstants.ACTIVITY_RELAY_HOME)
class RelayHomeActivity : BaseActivity() {

    private var title: CommonTitleBar? = null
    private var selectBack: ExImageView? = null
    private var titleTv: TextView? = null
    private var inviteFriendIv: ImageView? = null
    private var searchSongTv: ExTextView? = null
    private var songListTv: ExTextView? = null
    private var bottomArea: Barrier? = null
    private var speedRecyclerView: SpeedRecyclerView? = null

    var adapter: RelayHomeSongCardAdapter? = null
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    val cnt = 15

    //在滑动到最后的时候自动加载更多
    var loadMore: Boolean = false
    var currentPosition = -1
    var skrAudioPermission = SkrAudioPermission()

    var mTipsDialogView: TipsDialogView? = null
    var mRelaySongInfoDialogView: RelaySongInfoDialogView? = null

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
        inviteFriendIv = findViewById(R.id.invite_friend_iv)
        searchSongTv = findViewById(R.id.search_song_tv)
        songListTv = findViewById(R.id.song_list_tv)
        bottomArea = findViewById(R.id.bottom_area)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

        val songCardMaxHeight = (U.getDisplayUtils().screenHeight - 32.dp() - 30.dp() - 50.dp() - 2 * 16.dp()) -
                (if (U.getDeviceUtils().hasNotch(this)) U.getStatusBarUtil().getStatusBarHeight(this) else 0)
        var maxSize = songCardMaxHeight / 72.dp()
        var songCardHeight = 72.dp() * maxSize
        MyLog.d(TAG, "initData songCardMaxHeight = $songCardMaxHeight songCardHeight = $songCardHeight maxSize = $maxSize")
        if ((songCardMaxHeight - songCardHeight) >= 66.dp()) {
            // 只是把判断条件放宽一点，毕竟有上下的距离
            maxSize += 1
            songCardHeight = 72.dp() * maxSize
        }

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
                    if (!loadMore && currentPosition > (adapter?.mDataList?.size ?: 0 - 3)) {
                        loadMore = true
                        getPlayBookList(offset, false)
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

        getPlayBookList(0, true)
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

    fun getPlayBookList(off: Int, clean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getPlayBookList", ControlType.CancelThis)) {
                relayMatchServerApi.getPlayBookList(off, cnt, MyUserInfoManager.uid.toInt())
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
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.addData(list)
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

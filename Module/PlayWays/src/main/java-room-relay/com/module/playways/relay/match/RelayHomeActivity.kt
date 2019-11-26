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
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.CardScaleHelper
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongAdapter
import com.module.playways.room.song.model.SongModel
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_RELAY_HOME)
class RelayHomeActivity : BaseActivity() {

    var titlebar: CommonTitleBar? = null
    var speedRecyclerView: SpeedRecyclerView? = null

    val adapter: RelayHomeSongAdapter = RelayHomeSongAdapter()
    private var cardScaleHelper: CardScaleHelper? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    val cnt = 15

    //在滑动到最后的时候自动加载更多
    var loadMore: Boolean = false
    var currentPosition = -1


    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        for (activity in U.getActivityUtils().activityList) {
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue
            }
            if (activity === this) {
                continue
            }
            activity.finish()
        }

        titlebar = findViewById(R.id.titlebar)
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
                        getPlayBookList(offset, false)
                    }
                }
            }
        })

        titlebar?.leftTextView?.setDebounceViewClickListener { finish() }
        titlebar?.rightTextView?.setDebounceViewClickListener {
            // todo 去搜歌
        }
        adapter.listener = object : RelayHomeSongAdapter.RelayHomeListener {
            override fun selectSong(position: Int, model: SongModel?) {
                // 跳到匹配中到页面
                model?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_RELAY_MATCH)
                            .withSerializable("songModel", model)
                            .navigation()
                }
            }

            override fun getRecyclerViewPosition(): Int {
                return currentPosition
            }
        }

        getPlayBookList(0, true)
    }

    fun getPlayBookList(off: Int, clean: Boolean) {
        launch {
            val result = subscribe (RequestControl("getPlayBookList", ControlType.CancelThis)){
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

    override fun useEventBus(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

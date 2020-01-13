package com.module.playways.relay.match

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongItemAdapter
import com.module.playways.room.song.model.SongModel
import com.module.playways.room.song.view.RelaySongInfoDialogView
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.AddSongEvent
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class RelayHistorySongFragment : BaseFragment() {

    lateinit var title: CommonTitleBar
    lateinit var selectBack: ExImageView
    lateinit var titleTv: TextView
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView

    private var adapter: RelayHomeSongItemAdapter? = null

    var mRelaySongInfoDialogView: RelaySongInfoDialogView? = null

    private val relayMatchServerApi = ApiManager.getInstance().createService(RelayMatchServerApi::class.java)
    var offset: Int = 0
    var hasMore: Boolean = false
    val cnt = 15

    private var mLoadService: LoadService<*>? = null

    override fun initView(): Int {
        return R.layout.relay_history_song_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(activity, false)
        title = rootView.findViewById(R.id.title)
        selectBack = rootView.findViewById(R.id.select_back)
        titleTv = rootView.findViewById(R.id.title_tv)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        selectBack.setDebounceViewClickListener { finish() }

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getPlayBookList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RelayHomeSongItemAdapter(object : RelayHomeSongItemAdapter.RelaySongListener {
            override fun selectSong(position: Int, model: SongModel?) {
                model?.let {
                    EventBus.getDefault().post(AddSongEvent(it, SongManagerActivity.TYPE_FROM_RELAY_HOME))
                }
            }

            override fun selectSongDetail(position: Int, model: SongModel?) {
                model?.let {
                    mRelaySongInfoDialogView?.dismiss(false)
                    mRelaySongInfoDialogView = RelaySongInfoDialogView(model, context!!)
                    mRelaySongInfoDialogView?.showByDialog(U.getDisplayUtils().screenHeight - 2 * U.getDisplayUtils().dip2px(60f))
                }
            }

        }, null, null)
        recyclerView.adapter = adapter

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.wulishigedan, "你敢不敢唱首歌？", null))
                .build()
        mLoadService = mLoadSir.register(refreshLayout, Callback.OnReloadListener {
            getPlayBookList(0, true)
        })

        getPlayBookList(0, true)
    }

    fun getPlayBookList(off: Int, clean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getPlayBookList", ControlType.CancelThis)) {
                relayMatchServerApi.getRelayClickedMusicItmes(off, cnt, MyUserInfoManager.uid.toInt())
            }

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("items"), SongModel::class.java)
                addSongList(list, clean)
            }

            refreshLayout.finishLoadMore()
            refreshLayout.finishRefresh()
            refreshLayout.setEnableLoadMore(hasMore)
        }
    }

    private fun addSongList(list: List<SongModel>?, clean: Boolean) {
        if (clean) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
                adapter?.notifyDataSetChanged()
            }
        }

        if (adapter?.mDataList.isNullOrEmpty()) {
            mLoadService?.showCallback(EmptyCallback::class.java)
        } else {
            mLoadService?.showSuccess()
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        mRelaySongInfoDialogView?.dismiss(false)
    }
}
package com.module.posts.watch.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnItemTouchListener
import android.view.MotionEvent
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.model.PartyRoomInfoModel
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.common.IBooleanCallback
import com.module.playways.IPlaywaysModeService
import com.module.posts.R
import com.module.posts.watch.PostsWatchServerApi
import com.module.posts.watch.adapter.DynamicPostsRoomAdapter
import kotlinx.coroutines.*

/**
 * horizon
 */
class DynamicRoomView(context: Context) : ConstraintLayout(context), CoroutineScope by MainScope() {
    private var clubID: Int = 0


    var adapter: DynamicPostsRoomAdapter


    private val recyclerView: RecyclerView

    private val mLoadService: LoadService<*>

    private val postsWatchServerApi = ApiManager.getInstance().createService(PostsWatchServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val cnt = 20

    private var roomJob: Job? = null
    private var lastLoadDateTime: Long = 0    //记录上次获取接口的时间
    private var recommendInterval: Long = 15 * 1000   // 自动更新的时间间隔


    init {
        View.inflate(context, R.layout.posts_wall_view_dynamic_layout, this)
        recyclerView = findViewById(R.id.recycler_view)
        adapter = DynamicPostsRoomAdapter()

        adapter.clickListener = { position, model ->
            model?.roomID?.let {
                /*if (type == PartyRoomView.TYPE_PARTY_HOME) {
                    stopTimer()
                }*/
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoPartyRoom(it, 1, model.roomtype ?: 0)

                /* val map = HashMap<String, String>()
                 map["roomType"] = model.roomtype.toString()
                 StatisticsAdapter.recordCountEvent("party", "recommend", map)*/
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    starTimer(recommendInterval, IBooleanCallback { })
                } else {
                    stopTimer()
                }
            }
        })



        U.getCommonUtils().setSupportsChangeAnimations(recyclerView, false)
        recyclerView.addOnItemTouchListener(object : OnItemTouchListener {
            var initX = 0f
            var initY = 0f
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = e.x
                        initY = e.y
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> if (Math.abs(e.y - initY) > Math.abs(e.x - initX)) {
                        rv.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })


        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.loading_empty2, "暂无房间", "#99FFFFFF"))
                .build()
        mLoadService = mLoadSir.register(recyclerView, Callback.OnReloadListener {
            initData(true, IBooleanCallback { })
        })
        initData(true, IBooleanCallback { })
    }


    fun loadRoomListData(off: Int, isClean: Boolean, callback: IBooleanCallback) {

        MyLog.d("lijianqun DynamicRoomView loadRoomListData() ")


        launch {
            val result = subscribe(RequestControl("loadRoomListData1", ControlType.CancelThis)) {
                //                postsWatchServerApi.getPartyRoomList(off, cnt, model.gameMode) //gameMode 从来

                postsWatchServerApi.getClubMemberPartyDetail(clubID, off, cnt)

            }
            if (result.errno == 0) {
                lastLoadDateTime = System.currentTimeMillis()
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("roomInfo"), PartyRoomInfoModel::class.java)
                MyLog.d("lijianqun DynamicRoomView loadRoomListData() list = " + list?.size)
                addRoomList(list, isClean)
/*
                val list: MutableList<PartyRoomInfoModel> = ArrayList()
                for (i in 0..19) {
                    val model = PartyRoomInfoModel()
                    model.gameName = i.toString() + "1test1"
                    model.ownerName = "$i 1test1 "
                    model.topicName = "$i 1test1 "
                    model.roomName = "$i 1test1 "
                    model.avatarUrl= "http://res-static.inframe.mobi/app/skr-redpacket-20190304.png"
                    list.add(model)
                }
                MyLog.d("lijianqun DynamicRoomView loadRoomListData() list = " + list?.size)
                addRoomList(list, isClean)

*/
            }

            if (result.errno == -2) {
                U.getToastUtil().showShort("网络出错了，请检查网络后重试")
            }


            callback.result(hasMore)
            if (!isClean) {
                // 是加载更多
                starTimer(recommendInterval, callback)
            }
        }

    }

    private fun addRoomList(list: List<PartyRoomInfoModel>?, isClean: Boolean) {
        MyLog.d("lijianqun DynamicRoomView addRoomList() list = " + list?.size + " : isClean = " + isClean)

        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                // todo 可能需要补一个去重 (简单的去重，只去掉后面加入的重复)
                val size = adapter.mDataList.size
                for (room in list) {
                    if (!adapter.mDataList.contains(room)) {
                        adapter.mDataList.add(room)
                    } else {
                        MyLog.w("PartyRoomView", "重复的房间 = $room, isClean = $isClean")
                    }
                }
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, newSize - size)
            }
        }

        //列表空显示
        if (adapter.mDataList.isNullOrEmpty()) {
            mLoadService.showCallback(EmptyCallback::class.java)
        } else {
            mLoadService.showSuccess()
        }
    }

    private fun starTimer(delayTime: Long, callback: IBooleanCallback) {
        roomJob?.cancel()
        roomJob = launch {
            delay(delayTime)
            repeat(Int.MAX_VALUE) {
                loadRoomListData(0, true, callback)
//                if (type == TYPE_GAME_HOME) {
//                    loadClubListData()
//                }
                delay(recommendInterval)
            }
        }
    }

    fun stopTimer() {
        roomJob?.cancel()
    }

    fun initData(flag: Boolean, callback: IBooleanCallback) {
        if (!flag) {
            var now = System.currentTimeMillis();
            if ((now - lastLoadDateTime) > recommendInterval) {
                starTimer(0, callback)
            } else {
                var delayTime = recommendInterval - (now - lastLoadDateTime)
                starTimer(delayTime, callback)
            }
        } else {
            starTimer(0, callback)
        }
    }

    fun destroy() {
        cancel()
        roomJob?.cancel()
    }


    fun loadData(clubId: Int, callback: IBooleanCallback) {
        this.clubID = clubId
        initData(true, callback)
    }

    fun loadMoreData(callback: IBooleanCallback) {
        loadRoomListData(offset, false, callback)
    }


}
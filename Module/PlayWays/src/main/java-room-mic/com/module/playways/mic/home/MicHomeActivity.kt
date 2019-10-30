package com.module.playways.mic.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.recommend.RA
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.mic.room.MicRoomServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_MIC_HOME)
class MicHomeActivity : BaseActivity() {

    val playTag = "MicHomeActivity" + hashCode()
    lateinit var playCallback: SinglePlayerCallbackAdapter

    lateinit var titlebar: CommonTitleBar
    lateinit var quickBegin: ExImageView
    lateinit var createRoom: ExImageView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView

    var adapter: RecommendMicAdapter? = null

    val micRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    var offset = 0

    val skrVerifyUtils = SkrVerifyUtils()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mic_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        U.getStatusBarUtil().setTransparentBar(this, false)

        titlebar = findViewById(R.id.titlebar)
        quickBegin = findViewById(R.id.quick_begin)
        createRoom = findViewById(R.id.create_room)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }
        quickBegin.setAnimateDebounceViewClickListener {
            skrVerifyUtils.checkHasMicAudioPermission {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_MATCH)
                        .navigation()
            }
        }
        createRoom.setAnimateDebounceViewClickListener {
            skrVerifyUtils.checkHasMicAudioPermission {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_MIC_ROOM)
                        .navigation()
            }
        }

        smartRefresh.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    // 加载更多
                    getRecomRoomList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        adapter = RecommendMicAdapter(object : RecommendMicListener {
            override fun onClickEnterRoom(model: RecommendMicInfoModel?, position: Int) {
                skrVerifyUtils.checkHasMicAudioPermission {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    model?.roomInfo?.roomID?.let {
                        iRankingModeService?.jumpMicRoomBySuggest(it)
                    }
                }
            }

            override fun onClickUserVoice(model: RecommendMicInfoModel?, position: Int, recomUserInfo: RecommendUserInfo?, childPos: Int) {
                MyLog.d(TAG, "onClickUserVoice model = $model, position = $position, recomUserInfo = $recomUserInfo, childPos = $childPos")
                if (adapter?.isPlay == true && adapter?.playPosition == position && adapter?.playChildPosition == childPos) {
                    // 对同一个的声音的重复点击
                    MyLog.d(TAG, "onClickUserVoice stopPlay")
                    SinglePlayer.stop(playTag)
                    adapter?.stopPlay()
                } else {
                    MyLog.d(TAG, "onClickUserVoice startPlay")
                    SinglePlayer.stop(playTag)
                    recomUserInfo?.voiceInfo?.let {
                        SinglePlayer.startPlay(playTag, it.voiceURL)
                    }
                    adapter?.startPlay(model, position, recomUserInfo, childPos)
                }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        playCallback = object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                SinglePlayer.stop(playTag)
                adapter?.stopPlay()
            }

            override fun onPlaytagChange(oldPlayerTag: String?, newPlayerTag: String?) {
                if (newPlayerTag != playTag) {
                    SinglePlayer.stop(playTag)
                    adapter?.stopPlay()
                }
            }
        }
        SinglePlayer.addCallback(playTag, playCallback)

        getRecomRoomList(0, true)
    }

    private fun getRecomRoomList(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getMicHomeRoomList", ControlType.CancelThis)) {
                micRoomServerApi.getMicHomeRoomList(off, RA.getTestList(), RA.getVars())
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("rooms"), RecommendMicInfoModel::class.java)
                addRoomList(list, isClear)
            } else {
                smartRefresh.finishLoadMore()
                smartRefresh.finishRefresh()
            }
        }
    }

    private fun addRoomList(list: List<RecommendMicInfoModel>?, isClear: Boolean) {
        if (isClear) {
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
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.release(playTag)
        SinglePlayer.removeCallback(playTag)
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}
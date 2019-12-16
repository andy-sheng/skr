package com.module.club.homepage.room

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.ClubInfo
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.component.busilib.model.PartyRoomInfoModel
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.playways.IPlaywaysModeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ClubPartyRoomView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val recyclerView: RecyclerView
    private val adapter: ClubPartyRoomAdapter

    var clubID: Int = 0
    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val cnt = 15

    init {
        View.inflate(context, R.layout.club_party_room_view_layout, this)

        recyclerView = this.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClubPartyRoomAdapter(object : ClubPartyRoomAdapter.Listener {
            override fun onClickClubParty(position: Int, model: PartyRoomInfoModel?) {
                model?.roomID?.let {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService.tryGoPartyRoom(it, 1)
                }

            }

            override fun onClickClubMemberParty(position: Int, model: PartyRoomInfoModel?) {
                model?.roomID?.let {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService.tryGoPartyRoom(it, 1)
                }
            }
        })
        recyclerView.adapter = adapter
    }

    fun initData() {
        loadClubPartyDetail()
        loadClubMemberPartyList(0, true, null)
    }

    fun loadMoreData(callBack: ((hasMore: Boolean) -> Unit)?) {
        loadClubMemberPartyList(offset, false, callBack)
    }

    // 家族剧场
    private fun loadClubPartyDetail() {
        launch {
            val result = subscribe(RequestControl("loadClubPartyDetail", ControlType.CancelThis)) {
                clubServerApi.getClubPartyDetail(clubID)
            }
            if (result.errno == 0) {
                adapter.clubParty = JSON.parseObject(result.data.getString("info"), PartyRoomInfoModel::class.java)
                adapter.notifyItemChanged(0)
            }
        }
    }

    // 成员派对
    private fun loadClubMemberPartyList(off: Int, isClean: Boolean, callBack: ((hasMore: Boolean) -> Unit)?) {
        launch {
            val result = subscribe(RequestControl("loadClubMemberPartyList", ControlType.CancelThis)) {
                clubServerApi.getClubMemberPartyDetail(clubID, off, cnt)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("items"), PartyRoomInfoModel::class.java)
                addClubMemberList(list, isClean)
                callBack?.invoke(hasMore)
            }
        }
    }

    private fun addClubMemberList(list: List<PartyRoomInfoModel>?, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter.mDataList.size
                adapter.mDataList.addAll(list)
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted((size + 1), 1 + newSize - size)
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}
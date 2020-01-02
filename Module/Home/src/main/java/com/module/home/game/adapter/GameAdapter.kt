package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.common.base.BaseFragment
import com.component.busilib.model.PartyRoomInfoModel
import com.component.person.model.UserRankModel
import com.module.home.R
import com.module.home.game.viewholder.*
import com.module.home.model.SlideShowModel

class GameAdapter(internal var mBaseFragment: BaseFragment, val listener: ClickGameListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "GameAdapter"

    companion object {
        const val TYPE_BANNER_HOLDER = 0       // 广告
        const val TYPE_FUNCTION_HOLDER = 1    // 功能区域（做任务，排行榜，练歌房）
        const val TYPE_GAMETYPE_HOLDER = 2     // 玩法区域

        // 局部更新
        const val REFRESH_TYPE_BANNER = 1
        const val REFRESH_TYPE_RED = 2
        const val REFRESH_TYPE_REGION = 3
        const val REFRESH_TYPE_PARTY = 4
    }


    var slideShowModelList: List<SlideShowModel>? = null  //广告的数据
    var isTaskHasRed = false  // 功能区域，任务红点数据源
    var regionDiff: UserRankModel? = null  // 玩法区域 排名信息
    var partyList: List<PartyRoomInfoModel>? = null  // 玩法区域 派对用来不停变化的信息

    fun updateBanner(slideShowModelList: List<SlideShowModel>?) {
        if (this.slideShowModelList == null) {
            this.slideShowModelList = slideShowModelList
            notifyDataSetChanged()
        } else {
            this.slideShowModelList = slideShowModelList
            notifyItemChanged(getPositionByViewType(TYPE_BANNER_HOLDER), REFRESH_TYPE_BANNER)
        }
    }

    fun updateFunction(show: Boolean) {
        this.isTaskHasRed = show
        notifyItemChanged(getPositionByViewType(TYPE_FUNCTION_HOLDER), REFRESH_TYPE_RED)
    }

    fun updateRegionDiff(userRankModel: UserRankModel?) {
        this.regionDiff = userRankModel
        notifyItemChanged(getPositionByViewType(TYPE_GAMETYPE_HOLDER), REFRESH_TYPE_REGION)
    }

    fun updatePartyList(list: List<PartyRoomInfoModel>?) {
        this.partyList = list
        notifyItemChanged(getPositionByViewType(TYPE_GAMETYPE_HOLDER), REFRESH_TYPE_PARTY)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BANNER_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_banner_item_view, parent, false)
                BannerViewHolder(view)
            }
            TYPE_FUNCTION_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_funcation_item_view, parent, false)
                FuncationAreaViewHolder(view, listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_type_item_view, parent, false)
                GameTypeViewHolder(view, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            when (holder) {
                is BannerViewHolder -> slideShowModelList?.let { holder.bindData(it) }
                is FuncationAreaViewHolder -> holder.bindData(isTaskHasRed)
                is GameTypeViewHolder -> holder.bindRegionData(regionDiff)
            }
        } else {
            for (refreshType in payloads) {
                if (refreshType is Int) {
                    when (refreshType) {
                        REFRESH_TYPE_BANNER -> {
                            if (holder is BannerViewHolder) {
                                slideShowModelList?.let { holder.bindData(it) }
                            }
                        }
                        REFRESH_TYPE_RED -> {
                            if (holder is FuncationAreaViewHolder) {
                                holder.bindData(isTaskHasRed)
                            }
                        }
                        REFRESH_TYPE_REGION -> {
                            if (holder is GameTypeViewHolder) {
                                holder.bindRegionData(regionDiff)
                            }
                        }
                        REFRESH_TYPE_PARTY -> {
                            if (holder is GameTypeViewHolder) {
                                holder.bindPartyData(partyList)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (slideShowModelList.isNullOrEmpty()) {
            2
        } else {
            3
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            getPositionByViewType(TYPE_BANNER_HOLDER) -> TYPE_BANNER_HOLDER
            getPositionByViewType(TYPE_FUNCTION_HOLDER) -> TYPE_FUNCTION_HOLDER
            getPositionByViewType(TYPE_GAMETYPE_HOLDER) -> TYPE_GAMETYPE_HOLDER
            else -> 0
        }
    }

    private fun getPositionByViewType(viewType: Int): Int {
        return if (slideShowModelList.isNullOrEmpty()) {
            when (viewType) {
                TYPE_BANNER_HOLDER -> -1
                TYPE_FUNCTION_HOLDER -> 0
                TYPE_GAMETYPE_HOLDER -> 1
                else -> -1
            }
        } else {
            when (viewType) {
                TYPE_BANNER_HOLDER -> 0
                TYPE_FUNCTION_HOLDER -> 1
                TYPE_GAMETYPE_HOLDER -> 2
                else -> -1
            }
        }
    }
}

interface ClickGameListener {
    fun onClickTaskListener()
    fun onClickRankListener()
    fun onClickPracticeListener()
    fun onClickMallListner()

    fun onCreateRoomListener()
    fun onRaceRoomListener()
    fun onDoubleRoomListener()
    fun onRelayRoomListener()
    fun onBattleRoomListener()
    fun onGrabRoomListener()
    fun onMicRoomListener()
    fun onPartyRoomListener()

    fun onClickRankArea()
}
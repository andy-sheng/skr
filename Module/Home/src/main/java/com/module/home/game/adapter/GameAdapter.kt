package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.base.BaseFragment
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.model.*
import com.module.home.game.viewholder.*

import java.util.ArrayList

class GameAdapter(internal var mBaseFragment: BaseFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "GameAdapter"

    private val TYPE_BANNER_HOLDER = 0       // 广告
    private val TYPE_FUNCATION_HOLDER = 1    // 功能区域（做任务，排行榜，练歌房）
    private val TYPE_RECOMMEND_HOLDER = 2    // 推荐房
    private val TYPE_QUICK_ROOM_HOLDER = 3   // 快速房
    private val TYPE_GAMETYPE_HOLDER = 4     // 玩法区域

    private var mObjArr = arrayOfNulls<Any>(5)
    private var mDataList: MutableList<Any> = ArrayList()

    var onCreateRoomListener: (() -> Unit)? = null
    var onSelectSpecialListener: ((specialModel: SpecialModel?) -> Unit)? = null
    var onClickTaskListener: (() -> Unit)? = null
    var onClickRankListener: (() -> Unit)? = null
    var onClickPracticeListener: (() -> Unit)? = null
    var onMoreRoomListener: (() -> Unit)? = null

    var onEnterRoomListener: ((model: RecommendModel) -> Unit)? = null
    var onPkRoomListener: (() -> Unit)? = null
    var onDoubleRoomListener: (() -> Unit)? = null
    var onBattleRoomListener: (() -> Unit)? = null
    var onGrabRoomListener: (() -> Unit)? = null

    init {
        mObjArr[TYPE_GAMETYPE_HOLDER] = GameTypeModel()
        mObjArr[TYPE_FUNCATION_HOLDER] = FuncationModel(false)
    }

    fun updateBanner(bannerModel: BannerModel?) {
        mObjArr[TYPE_BANNER_HOLDER] = bannerModel
        setDataList()
    }

    fun updateFuncation(funcationModel: FuncationModel) {
        mObjArr[TYPE_FUNCATION_HOLDER] = funcationModel
        setDataList()
    }

    fun updateRecommendRoomInfo(recommendRoomModel: RecommendRoomModel?) {
        mObjArr[TYPE_RECOMMEND_HOLDER] = recommendRoomModel
        setDataList()
    }

    fun updateQuickJoinRoomInfo(quickJoinRoomModel: QuickJoinRoomModel?) {
        mObjArr[TYPE_QUICK_ROOM_HOLDER] = quickJoinRoomModel
        setDataList()
    }

    fun updateGrabGameInfo(model: SpecialModel) {
        var gameTypeModel = mObjArr[TYPE_GAMETYPE_HOLDER]
        if (gameTypeModel is GameTypeModel) {
            gameTypeModel.mSpecialModel = model
        }
        setDataList()
    }

    fun updateDoubleRemainTime(times: Int) {
        var gameTypeModel = mObjArr[TYPE_GAMETYPE_HOLDER]
        if (gameTypeModel is GameTypeModel) {
            gameTypeModel.mRemainTime = times
        }
        setDataList()
    }

    private fun setDataList() {
        mDataList.clear()
        for (`object` in mObjArr) {
            if (`object` != null) {
                mDataList.add(`object`)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_BANNER_HOLDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.game_banner_item_view, parent, false)
            return BannerViewHolder(view)
        } else if (viewType == TYPE_FUNCATION_HOLDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.game_funcation_item_view, parent, false)
            return FuncationAreaViewHolder(view, onClickTaskListener, onClickPracticeListener, onClickRankListener)
        } else if (viewType == TYPE_RECOMMEND_HOLDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.game_recommend_room_item_view, parent, false)
            return RecommendRoomViewHolder(view, mBaseFragment, onEnterRoomListener, onMoreRoomListener)
        } else if (viewType == TYPE_QUICK_ROOM_HOLDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.game_quick_room_item_view, parent, false)
            return QuickRoomViewHolder(view, mBaseFragment, onCreateRoomListener, onSelectSpecialListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.game_type_item_view, parent, false)
            return GameTypeViewHolder(view, onDoubleRoomListener, onPkRoomListener, onCreateRoomListener, onGrabRoomListener, onBattleRoomListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val obj = mDataList[position]) {
            is BannerModel -> (holder as BannerViewHolder).bindData(obj)
            is FuncationModel -> (holder as FuncationAreaViewHolder).bindData(obj)
            is RecommendRoomModel -> (holder as RecommendRoomViewHolder).bindData(obj)
            is QuickJoinRoomModel -> (holder as QuickRoomViewHolder).bindData(obj)
            is GameTypeModel -> (holder as GameTypeViewHolder).bindData(obj)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position]) {
            is BannerModel -> TYPE_BANNER_HOLDER
            is FuncationModel -> TYPE_FUNCATION_HOLDER
            is RecommendRoomModel -> TYPE_RECOMMEND_HOLDER
            is QuickJoinRoomModel -> TYPE_QUICK_ROOM_HOLDER
            is GameTypeModel -> TYPE_GAMETYPE_HOLDER
            else -> 0
        }
    }
}

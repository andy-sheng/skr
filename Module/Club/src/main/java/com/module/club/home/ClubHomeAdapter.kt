package com.module.club.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.core.userinfo.model.ClubInfo
import com.component.busilib.banner.BannerViewHolder
import com.component.busilib.banner.SlideShowModel
import com.module.club.R
import com.module.club.home.viewholder.ClubFunctionViewHolder
import com.module.club.home.viewholder.ClubListViewHolder

class ClubHomeAdapter(var listener: ClubHomeClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val mTag = "GameHomeAdapter"

    companion object {
        const val TYPE_BANNER_HOLDER = 0       // 广告
        const val TYPE_FUNCTION_HOLDER = 1     // 功能区域（搜索家族，家族榜单和创建家族）
        const val TYPE_CLUBLIST_HOLDER = 2     // 玩法区域

        // 局部更新
        const val REFRESH_TYPE_BANNER = 1      // 广告区域
        const val REFRESH_TYPE_CLUB = 2        // 功能区域我的家族
        const val REFRESH_TYPE_UPDATE_LIST = 3  // 更新家族列表
        const val REFRESH_TYPE_ADD_LIST = 4     // loadMore新增家族
    }

    var slideShowModelList: List<SlideShowModel>? = null  //广告的数据
    var clubInfo: ClubInfo? = null              // 我的家族信息
    var clubList = ArrayList<ClubInfo>()

    fun updateBanner(slideShowModelList: List<SlideShowModel>?) {
        if (this.slideShowModelList == null) {
            this.slideShowModelList = slideShowModelList
            notifyDataSetChanged()
        } else {
            this.slideShowModelList = slideShowModelList
            notifyItemChanged(getPositionByViewType(TYPE_BANNER_HOLDER), REFRESH_TYPE_BANNER)
        }
    }

    fun updateFunction(clubInfo: ClubInfo?) {
        this.clubInfo = clubInfo
        notifyItemChanged(getPositionByViewType(TYPE_FUNCTION_HOLDER), REFRESH_TYPE_CLUB)
    }

    fun addClubList(list: List<ClubInfo>?, isClean: Boolean) {
        if (isClean) {
            clubList.clear()
            if (!list.isNullOrEmpty()) {
                clubList.addAll(list)
            }
            notifyItemChanged(getPositionByViewType(TYPE_CLUBLIST_HOLDER), REFRESH_TYPE_UPDATE_LIST)
        } else {
            if (!list.isNullOrEmpty()) {
                clubList.addAll(list)
                notifyItemChanged(getPositionByViewType(TYPE_CLUBLIST_HOLDER), REFRESH_TYPE_ADD_LIST)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            getPositionByViewType(TYPE_BANNER_HOLDER) -> TYPE_BANNER_HOLDER
            getPositionByViewType(TYPE_FUNCTION_HOLDER) -> TYPE_FUNCTION_HOLDER
            getPositionByViewType(TYPE_CLUBLIST_HOLDER) -> TYPE_CLUBLIST_HOLDER
            else -> 0
        }
    }

    private fun getPositionByViewType(viewType: Int): Int {
        return if (slideShowModelList.isNullOrEmpty()) {
            when (viewType) {
                TYPE_BANNER_HOLDER -> -1
                TYPE_FUNCTION_HOLDER -> 0
                TYPE_CLUBLIST_HOLDER -> 1
                else -> -1
            }
        } else {
            when (viewType) {
                TYPE_BANNER_HOLDER -> 0
                TYPE_FUNCTION_HOLDER -> 1
                TYPE_CLUBLIST_HOLDER -> 2
                else -> -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BANNER_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_banner_item_view, parent, false)
                BannerViewHolder(view)
            }
            TYPE_FUNCTION_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.club_funcation_item_view, parent, false)
                ClubFunctionViewHolder(view, listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.club_list_item_view, parent, false)
                ClubListViewHolder(view, listener)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            when (holder) {
                is BannerViewHolder -> slideShowModelList?.let { holder.bindData(it) }
                is ClubFunctionViewHolder -> holder.bindData(clubInfo)
                is ClubListViewHolder -> holder.bindData(clubList, true)
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
                        REFRESH_TYPE_CLUB -> {
                            if (holder is ClubFunctionViewHolder) {
                                holder.bindData(clubInfo)
                            }
                        }
                        REFRESH_TYPE_UPDATE_LIST -> {
                            if (holder is ClubListViewHolder) {
                                holder.bindData(clubList, true)
                            }
                        }
                        REFRESH_TYPE_ADD_LIST -> {
                            if (holder is ClubListViewHolder) {
                                holder.bindData(clubList, false)
                            }
                        }
                    }
                }
            }
        }
    }
}

interface ClubHomeClickListener {
    fun onClickSearchClub()
    fun onClickRankClub()
    fun onClickCreatClub()

    fun onClickClubInfo(clubInfo: ClubInfo?)
}
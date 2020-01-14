package com.module.club.home.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.club.R
import com.module.club.home.ClubHomeClickListener

class ClubFunctionViewHolder(item: View, var listener: ClubHomeClickListener) : RecyclerView.ViewHolder(item) {

    private val clubSearchTv: ExTextView = item.findViewById(R.id.club_search_tv)
    private val clubRankTv: ExTextView = item.findViewById(R.id.club_rank_tv)
    private val clubCreateTv: ExTextView = item.findViewById(R.id.club_create_tv)

    private var clubInfo: ClubInfo? = null

    init {
        clubSearchTv.setAnimateDebounceViewClickListener {
            listener.onClickSearchClub()
        }

        clubRankTv.setAnimateDebounceViewClickListener {
            listener.onClickSearchClub()
        }

        clubCreateTv.setAnimateDebounceViewClickListener {
            if (clubInfo == null) {
                listener.onClickCreatClub()
            } else {
                listener.onClickClubInfo(clubInfo)
            }
        }
    }

    fun bindData(clubInfo: ClubInfo?) {
        this.clubInfo = clubInfo

        if (clubInfo == null) {
            clubCreateTv.text = "创建家族"
        } else {
            clubCreateTv.text = "我的家族"
        }
    }
}
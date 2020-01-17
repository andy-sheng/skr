package com.module.club.home.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.club.R
import com.module.club.home.ClubHomeClickListener

class ClubFunctionViewHolder(item: View, var listener: ClubHomeClickListener) : RecyclerView.ViewHolder(item) {

    private val clubSearchIv: ImageView = item.findViewById(R.id.club_search_iv)
    private val clubRankIv: ImageView = item.findViewById(R.id.club_rank_iv)
    private val clubCreateIv: ImageView = item.findViewById(R.id.club_create_iv)

    private var clubInfo: ClubInfo? = null

    init {
        clubSearchIv.setAnimateDebounceViewClickListener {
            listener.onClickSearchClub()
        }

        clubRankIv.setAnimateDebounceViewClickListener {
            listener.onClickRankClub()
        }

        clubCreateIv.setAnimateDebounceViewClickListener {
            if (clubInfo != null && (clubInfo?.clubID ?: 0) > 0) {
                listener.onClickClubInfo(clubInfo)
            } else {
                listener.onClickCreatClub()
            }
        }
    }

    fun bindData(clubInfo: ClubInfo?) {
        this.clubInfo = clubInfo

        if (this.clubInfo != null && (this.clubInfo?.clubID ?: 0) > 0) {
            clubCreateIv.background = U.getDrawable(R.drawable.club_home_myclub_icon)
        } else {
            clubCreateIv.background = U.getDrawable(R.drawable.club_home_create_icon)
        }
    }
}
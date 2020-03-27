package com.module.club.rank

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.component.person.utils.StringFromatUtils
import com.module.club.R
import com.component.club.ClubRoleUtils

class ClubRankAdapter(val listener: Listener) : RecyclerView.Adapter<ClubRankAdapter.ClubRankViewHolder>() {

    var mDataList = ArrayList<ClubRankModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_rank_list_item_layout, parent, false)
        return ClubRankViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ClubRankViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class ClubRankViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val seqTv: TextView = item.findViewById(R.id.seq_tv)
        private val avatarView: AvatarView = item.findViewById(R.id.avatar_view)
        private val hotTv: TextView = item.findViewById(R.id.hot_tv)
        private val nameTv: TextView = item.findViewById(R.id.name_tv)
        private val roleTagTv: TextView = item.findViewById(R.id.role_tag_tv)

        var mPos = -1
        var mModel: ClubRankModel? = null

        init {
            item.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: ClubRankModel) {
            this.mPos = position
            this.mModel = model

            seqTv.text = model.rankSeq.toString()
            avatarView.bindData(model.userInfoModel)
            hotTv.text = StringFromatUtils.formatTenThousand(model.value)
            nameTv.text = model.userInfoModel?.nicknameRemark

            if (ClubRoleUtils.getClubRoleBackground(model.userInfoModel?.clubInfo?.roleType
                            ?: 0) != null) {
                roleTagTv.visibility = View.VISIBLE
                roleTagTv.background = ClubRoleUtils.getClubRoleBackground(model.userInfoModel?.clubInfo?.roleType
                        ?: 0)
                roleTagTv.text = model.userInfoModel?.clubInfo?.roleDesc
                ClubRoleUtils.getClubRoleTextColor(model.userInfoModel?.clubInfo?.roleType
                        ?: 0)?.let {
                    roleTagTv.setTextColor(it)
                }
            } else {
                roleTagTv.visibility = View.INVISIBLE
            }
        }
    }

    interface Listener {
        fun onClickAvatar(position: Int, model: ClubRankModel?)
    }
}
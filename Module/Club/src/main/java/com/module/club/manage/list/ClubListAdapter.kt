package com.module.club.manage.list

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.club.R

class ClubListAdapter(var listener: Listener) : RecyclerView.Adapter<ClubListAdapter.ClubListItem>() {

    var mDataList = ArrayList<ClubInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubListItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_list_item_view_layout, parent, false)
        return ClubListItem(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ClubListItem, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class ClubListItem(item: View) : RecyclerView.ViewHolder(item) {

        private val clubLogoSdv: SimpleDraweeView = item.findViewById(R.id.club_logo_sdv)
        private val clubNameTv: TextView = item.findViewById(R.id.club_name_tv)
        private val popularTv: ExTextView = item.findViewById(R.id.popular_tv)
        private val clubDescTv: ExTextView = item.findViewById(R.id.club_desc_tv)
        private val divider: View = item.findViewById(R.id.divider)

        var mPos = -1
        var mModel: ClubInfo? = null

        init {
            item.setDebounceViewClickListener {
                listener.onClickItem(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: ClubInfo) {
            this.mPos = position
            this.mModel = model

            AvatarUtils.loadAvatarByUrl(clubLogoSdv,
                    AvatarUtils.newParamsBuilder(model.logo)
                            .setCircle(false)
                            .setCornerRadius(8.dp().toFloat())
                            .build())
            clubNameTv.text = model.name
            popularTv.text = "${model.hot}"
            clubDescTv.text = "家族成员/${model.memberCnt}人"
        }
    }

    interface Listener {
        fun onClickItem(position: Int, model: ClubInfo?)
    }
}
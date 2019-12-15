package com.module.playways.party.home

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class PartyClubViewHolder(item: View, var listener: PartyRoomAdapter.Listener) : RecyclerView.ViewHolder(item) {

    val imageBg: ExImageView = item.findViewById(R.id.image_bg)
    val recyclerView: RecyclerView = item.findViewById(R.id.recycler_view)
    val adapter: PartyClubAdapter = PartyClubAdapter()

    init {
        recyclerView.layoutManager = GridLayoutManager(item.context, 4)
        recyclerView.adapter = adapter
    }

    fun bindData(list: List<ClubInfo>?) {
        adapter.mDataList.clear()
        if (!list.isNullOrEmpty()) {
            adapter.mDataList.addAll(list)
        }
        adapter.notifyDataSetChanged()
    }

    inner class PartyClubAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val ITEM_TYPE_CLUB = 1
        val ITEM_TYPE_MORE = 2

        var mDataList = ArrayList<ClubInfo>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ITEM_TYPE_CLUB) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_club_item_view_layout, parent, false)
                PartyClubViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_club_more_item_view_layout, parent, false)
                MorePartyViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            if (mDataList.size > 3) {
                return 4
            }
            return mDataList.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            if (mDataList.size > 3) {
                if (position == 3) {
                    return ITEM_TYPE_MORE
                }
            } else {
                if (position == mDataList.size) {
                    return ITEM_TYPE_MORE
                }
            }
            return ITEM_TYPE_CLUB
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is PartyClubViewHolder) {
                holder.bindData(position, mDataList[position])
            }
        }

        inner class PartyClubViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            private val clubLogoSdv: SimpleDraweeView = item.findViewById(R.id.club_logo_sdv)
            private val clubNameTv: TextView = item.findViewById(R.id.club_name_tv)

            var mPos = -1
            var mModel: ClubInfo? = null

            init {
                item.setDebounceViewClickListener {
                    listener.onClickClub(mPos, mModel)
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
            }
        }

        inner class MorePartyViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            init {
                item.setDebounceViewClickListener {
                    listener.onClickClubMore()
                }
            }
        }
    }
}
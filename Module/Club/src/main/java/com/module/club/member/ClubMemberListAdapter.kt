package com.module.club.member

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.club.R
import com.module.club.homepage.utils.ClubRoleUtils
import com.zq.live.proto.Common.EClubMemberRoleType

class ClubMemberListAdapter(var myRoleType: Int, var listener: Listener) : RecyclerView.Adapter<ClubMemberListAdapter.ClubMemberViewHolder>() {

    var mDataList = ArrayList<UserInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_member_list_item_layout, parent, false)
        return ClubMemberViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ClubMemberViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class ClubMemberViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarView: AvatarView = item.findViewById(R.id.avatar_view)
        private val titleTv: ExTextView = item.findViewById(R.id.title_tv)
        private val removeTv: ExTextView = item.findViewById(R.id.remove_tv)
        private val nameTv: TextView = item.findViewById(R.id.name_tv)
        private val statusTv: TextView = item.findViewById(R.id.status_tv)
        private val roleTagTv: TextView = item.findViewById(R.id.role_tag_tv)

        var mPos = -1
        var mModel: UserInfoModel? = null

        init {
            avatarView.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
            nameTv.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
            statusTv.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
            roleTagTv.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }

            removeTv.setDebounceViewClickListener {
                listener.onClickRemove(mPos, mModel)
            }
            titleTv.setDebounceViewClickListener {
                listener.onClickTitle(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: UserInfoModel) {
            this.mPos = position
            this.mModel = model

            avatarView.bindData(model)
            nameTv.text = model.nicknameRemark
            statusTv.text = model.ranking?.rankingDesc
            if (ClubRoleUtils.getClubRoleBackground(model.clubInfo.roleType) != null) {
                roleTagTv.visibility = View.VISIBLE
                roleTagTv.background = ClubRoleUtils.getClubRoleBackground(model.clubInfo.roleType)
                roleTagTv.text = model.clubInfo.roleDesc
                ClubRoleUtils.getClubRoleTextColor(model.clubInfo.roleType)?.let {
                    roleTagTv.setTextColor(it)
                }
            } else {
                roleTagTv.visibility = View.INVISIBLE
            }

            if ((myRoleType == EClubMemberRoleType.ECMRT_Founder.value || myRoleType == EClubMemberRoleType.ECMRT_CoFounder.value)
                    && myRoleType < model.clubInfo.roleType) {
                // 族长或副族长，只能操作权限低的人
                removeTv.visibility = View.VISIBLE
                titleTv.visibility = View.VISIBLE
            } else {
                removeTv.visibility = View.GONE
                titleTv.visibility = View.GONE
            }

            if (model.userId == MyUserInfoManager.uid.toInt() || model.clubInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value) {
                removeTv.visibility = View.GONE
                titleTv.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun onClickAvatar(position: Int, model: UserInfoModel?)
        fun onClickRemove(position: Int, model: UserInfoModel?)
        fun onClickTitle(position: Int, model: UserInfoModel?)
    }
}
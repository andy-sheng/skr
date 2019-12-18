package com.module.club.apply

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.club.R

class ClubApplyListAdapter(var hasManager: Boolean, var listener: Listener) : RecyclerView.Adapter<ClubApplyListAdapter.ClubApplyViewHolder>() {

    var mDataList = ArrayList<ClubApplyInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubApplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_apply_list_item_layout, parent, false)
        return ClubApplyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ClubApplyViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }


    inner class ClubApplyViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarView: AvatarView = item.findViewById(R.id.avatar_view)
        private val agreeTv: ExTextView = item.findViewById(R.id.agree_tv)
        private val refuseTv: ExTextView = item.findViewById(R.id.refuse_tv)
        private val nameTv: TextView = item.findViewById(R.id.name_tv)
        private val statusTv: TextView = item.findViewById(R.id.status_tv)

        var mPos = -1
        var mModel: ClubApplyInfoModel? = null

        init {
            avatarView.setDebounceViewClickListener {
                listener.onCLickAvatar(mPos, mModel)
            }
            nameTv.setDebounceViewClickListener {
                listener.onCLickAvatar(mPos, mModel)
            }
            statusTv.setDebounceViewClickListener {
                listener.onCLickAvatar(mPos, mModel)
            }
            agreeTv.setDebounceViewClickListener {
                listener.onClickAgree(mPos, mModel)
            }
            refuseTv.setDebounceViewClickListener {
                listener.onClickRefuse(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: ClubApplyInfoModel) {
            this.mPos = position
            this.mModel = model

            avatarView.bindData(model.user)
            nameTv.text = model.user?.nicknameRemark
            statusTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.applyTimeMs, System.currentTimeMillis())
            if (hasManager) {
                agreeTv.visibility = View.VISIBLE
                refuseTv.visibility = View.VISIBLE
            } else {
                agreeTv.visibility = View.GONE
                refuseTv.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun onClickAgree(position: Int, model: ClubApplyInfoModel?)
        fun onClickRefuse(position: Int, model: ClubApplyInfoModel?)
        fun onCLickAvatar(position: Int, model: ClubApplyInfoModel?)
    }
}
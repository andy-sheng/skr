package com.component.person.guard

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView

class GuardListAdapter(val isMySelf: Boolean, val listener: Listener) : RecyclerView.Adapter<GuardListAdapter.GuardViewHolder>() {

    var mDataList = ArrayList<GuardInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guard_list_item_view_layout, parent, false)
        return GuardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: GuardViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }


    inner class GuardViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val content: ConstraintLayout = item.findViewById(R.id.content)
        private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
        private val nicknameTv: NickNameView = item.findViewById(R.id.nickname_tv)
        private val statusTv: ExTextView = item.findViewById(R.id.status_tv)

        var mPos = -1
        var mModel: GuardInfoModel? = null

        init {
            avatarIv.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
            nicknameTv.setDebounceViewClickListener {
                listener.onClickAvatar(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: GuardInfoModel) {
            this.mPos = position
            this.mModel = model

            avatarIv.bindData(model.userInfoModel)
            nicknameTv.setAllStateText(model.userInfoModel)
            if (isMySelf) {
                statusTv.visibility = View.VISIBLE
                statusTv.text = "${U.getDateTimeUtils().formateTimeString(model.expireTimeMs)}到期"
            } else {
                statusTv.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun onClickAvatar(position: Int, model: GuardInfoModel?)
    }
}
package com.module.msg.follow

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import io.rong.imkit.R

class SPFollowAdapter : RecyclerView.Adapter<SPFollowAdapter.SPFollowViewHolder>() {

    var mDataList = ArrayList<SPFollowRecordModel>()
    var onClickItemListener: ((model: SPFollowRecordModel?, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SPFollowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_follow_item_view_layout, parent, false)
        return SPFollowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: SPFollowViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class SPFollowViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarView: AvatarView = item.findViewById(R.id.avatar_view)
        private val timeTv: TextView = item.findViewById(R.id.time_tv)
        private val nameView: NickNameView = item.findViewById(R.id.name_view)
        private val actionDescTv: TextView = item.findViewById(R.id.action_desc_tv)
        private val contentTv: TextView = item.findViewById(R.id.content_tv)

        var mModel: SPFollowRecordModel? = null
        var mPos: Int = -1

        init {
            item.setDebounceViewClickListener {
                onClickItemListener?.invoke(mModel, mPos)
            }
        }

        fun bindData(position: Int, model: SPFollowRecordModel) {
            this.mModel = model
            this.mPos = position

            avatarView.bindData(model.userInfo)
            nameView.setAllStateText(model.userInfo)
            timeTv.text = "${U.getDateTimeUtils().getDateTimeString(model.spFollowInfo?.timeMs
                    ?: 0L, false, U.app())}"
            actionDescTv.text = model.spFollowInfo?.actionDesc
            contentTv.text = model.spFollowInfo?.content
        }
    }
}
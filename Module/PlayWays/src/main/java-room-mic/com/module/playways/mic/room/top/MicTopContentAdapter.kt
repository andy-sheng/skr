package com.module.playways.mic.room.top

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.account.UserAccountManager
import com.common.core.avatar.AvatarUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.model.RacePlayerInfoModel
import org.greenrobot.eventbus.EventBus
import java.net.UnknownServiceException

class MicTopContentAdapter : RecyclerView.Adapter<MicTopContentAdapter.RaceTopViewHolder>() {

    var mDataList = ArrayList<RacePlayerInfoModel>()

    override fun onBindViewHolder(holder: RaceTopViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceTopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_top_item_view_layout, parent, false)
        return RaceTopViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (mDataList.size >= 7) {
            7
        } else {
            mDataList.size
        }
    }

    inner class RaceTopViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)

        var mPostion = 0
        var mModel: RacePlayerInfoModel? = null

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel?.let {
                        if (it.userID != UserAccountManager.SYSTEM_ID && it.userID != UserAccountManager.SYSTEM_GRAB_ID && it.userID != UserAccountManager.SYSTEM_RANK_AI) {
                            EventBus.getDefault().post(ShowPersonCardEvent(it.userInfo.userId))
                        }
                    }
                }
            })
        }

        fun bindData(position: Int, model: RacePlayerInfoModel) {
            this.mPostion = position
            this.mModel = model

            avatarIv.bindData(model.userInfo)
        }
    }
}
package com.module.playways.race.room.view.actor

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RacePlayerInfoModel
import com.zq.live.proto.RaceRoom.ERUserRole
import org.greenrobot.eventbus.EventBus

class RaceActorAdapter(val mRoomDate: RaceRoomData) : RecyclerView.Adapter<RaceActorAdapter.RaceActorViewHolder>() {

    var mDataList = ArrayList<RacePlayerInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceActorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_actor_item_layout, parent, false)
        return RaceActorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RaceActorViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class RaceActorViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val statusTv: TextView = item.findViewById(R.id.status_tv)
        private val nameTv: TextView = item.findViewById(R.id.name_tv)
        private val descTv: TextView = item.findViewById(R.id.desc_tv)
        private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)

        var mPosition = 0
        var mModel: RacePlayerInfoModel? = null

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (mModel?.fakeUserInfo == null || mModel?.userID == MyUserInfoManager.uid.toInt()) {
                        mModel?.let {
                            EventBus.getDefault().post(ShowPersonCardEvent(it.userID))
                        }
                    }
                }
            })
        }

        fun bindData(pos: Int, model: RacePlayerInfoModel) {
            this.mPosition = pos
            this.mModel = model

            avatarIv.bindData(model.userInfo, model.fakeUserInfo?.nickName, model.fakeUserInfo?.avatarUrl)

            if (mRoomDate.realRoundInfo?.isSingerByUserId(model.userID) == true) {
                // 是当前轮次的演唱者
                statusTv.visibility = View.VISIBLE
                statusTv.setTextColor(Color.parseColor("#FFC15B"))
                statusTv.text = "演唱中"
            } else {
                when {
                    model.role == ERUserRole.ERUR_WAIT_USER.value -> {
                        statusTv.visibility = View.VISIBLE
                        statusTv.setTextColor(U.getColor(R.color.white_trans_50))
                        statusTv.text = "等待中"
                    }
                    else -> {
                        statusTv.visibility = View.GONE
                    }
                }
            }
            descTv.text = model.userInfo.ranking?.rankingDesc
            if (!TextUtils.isEmpty(model.fakeUserInfo?.nickName)) {
                nameTv.text = model.fakeUserInfo?.nickName
            } else {
                nameTv.text = UserInfoManager.getInstance().getRemarkName(model.userInfo?.userId
                        ?: 0, model.userInfo?.nickname)
            }
        }
    }
}

package com.module.playways.mic.room.seat

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.view.VoiceChartView
import com.module.playways.R
import com.module.playways.mic.room.model.MicSeatModel

class MicSeatRecyclerAdapter : RecyclerView.Adapter<MicSeatRecyclerAdapter.MicSeatHolder>() {
    val mDataList: ArrayList<MicSeatModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MicSeatHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_seat_state_item_layout, parent, false)
        return MicSeatHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: MicSeatHolder, position: Int) {
        val model = mDataList.get(position)
        holder.bindData(model, position)
    }

    inner class MicSeatHolder : RecyclerView.ViewHolder {
        var model: MicSeatModel? = null
        var position: Int? = null
        var avatarIv: BaseImageView
        var waitingTv: ExTextView
        var voiceChartView: VoiceChartView
        var userName: ExTextView
        var songNameList: ExTextView
        var stateTv: ExTextView

        constructor(itemView: View) : super(itemView) {
            avatarIv = itemView.findViewById(R.id.avatar_iv)
            waitingTv = itemView.findViewById(R.id.waiting_tv)
            voiceChartView = itemView.findViewById(R.id.voice_chart_view)
            userName = itemView.findViewById(R.id.user_name)
            songNameList = itemView.findViewById(R.id.song_name_list)
            stateTv = itemView.findViewById(R.id.state_tv)
        }

        fun bindData(model: MicSeatModel, position: Int) {
            this.model = model
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.user?.userInfo?.avatar)
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                    .setCircle(true)
                    .build())

            userName.text = UserInfoManager.getInstance().getRemarkName(model?.user?.userInfo?.userId!!, model.user?.userInfo?.nickname)
            songNameList.text = getSongNameList()

            model!!.user?.let {
                if (it.isNextSing) {
                    waitingTv.visibility = View.VISIBLE
                } else {
                    waitingTv.visibility = View.GONE
                }
            }

            model!!.user?.let {
                if (it.isCurSing) {
                    waitingTv.visibility = View.GONE
                    voiceChartView.visibility = View.VISIBLE
                    voiceChartView.start()
                } else {
                    voiceChartView.visibility = View.GONE
                    voiceChartView.stop()
                }
            }
        }

        fun getSongNameList(): SpannableStringBuilder {
            val spanUtils = SpanUtils()
            if (model?.music == null || model?.music?.size ?: 0 == 0) {
                spanUtils.append("暂无歌曲").setForegroundColor(U.getColor(R.color.white_trans_50))
                return spanUtils.create()
            }

            model?.music?.let {
                for (i in 0 until it.size) {
                    val index = i + 1
                    if (i == 0) {
                        if (model!!.user?.isCurSing == true) {
                            spanUtils.append("$index.《${it[i].itemName.toString()}》").setForegroundColor(Color.parseColor("#FFC15B"))
                        } else {
                            spanUtils.append("$index.《${it[i].itemName.toString()}》").setForegroundColor(U.getColor(R.color.white_trans_50))
                        }
                    } else {
                        spanUtils.append("$index.《${it[i].itemName.toString()}》").setForegroundColor(U.getColor(R.color.white_trans_50))
                    }
                }
            }

            return spanUtils.create()
        }
    }
}